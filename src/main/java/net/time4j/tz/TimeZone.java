/*
 * -----------------------------------------------------------------------
 * Copyright © 2013 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (TimeZone.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.tz;

import net.time4j.base.GregorianDate;
import net.time4j.base.UnixTime;
import net.time4j.base.WallTime;
import net.time4j.engine.ChronoEntity;
import net.time4j.engine.ChronoException;
import net.time4j.engine.ChronoFunction;

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * <p>L&auml;dt und h&auml;lt Zeitzonen mitsamt ihren Regeln. </p>
 *
 * @author      Meno Hochschild
 * @concurrency All static methods are thread-safe while this class is
 *              immutable as long as the underlying time zone data are.
 */
public abstract class TimeZone
    implements Serializable {

    //~ Statische Felder/Initialisierungen --------------------------------

    private static final boolean ALLOW_SYSTEM_TZ_OVERRIDE =
        Boolean.getBoolean("allow.system.tz.override");

    private static volatile NameData NAME_DATA = null;
    private static volatile TimeZone SYSTEM_TZ_CURRENT = null;
    private static volatile boolean ACTIVE = true;
    private static int SOFT_LIMIT = 11;

    private static final Map<String, TZID> PREDEFINED;
    private static final Provider PROVIDER;
    private static final ConcurrentMap<String, NamedReference> CACHE;
    private static final ReferenceQueue<TimeZone> QUEUE;
    private static final LinkedList<TimeZone> LAST_USED;

    private static final TimeZone SYSTEM_TZ_ORIGINAL;

    static {
        CACHE = new ConcurrentHashMap<String, NamedReference>();
        QUEUE = new ReferenceQueue<TimeZone>();
        LAST_USED = new LinkedList<TimeZone>(); // strong references

        List<Class<? extends TZID>> areas =
            new ArrayList<Class<? extends TZID>>();
        areas.add(TZID.AFRICA.class);
        areas.add(TZID.AMERICA.class);
        areas.add(TZID.AMERICA.ARGENTINA.class);
        areas.add(TZID.AMERICA.INDIANA.class);
        areas.add(TZID.AMERICA.KENTUCKY.class);
        areas.add(TZID.AMERICA.NORTH_DAKOTA.class);
        areas.add(TZID.ANTARCTICA.class);
        areas.add(TZID.ASIA.class);
        areas.add(TZID.ATLANTIC.class);
        areas.add(TZID.AUSTRALIA.class);
        areas.add(TZID.EUROPE.class);
        areas.add(TZID.INDIAN.class);
        areas.add(TZID.PACIFIC.class);

        Map<String, TZID> temp = new HashMap<String, TZID>();
        temp.put(ZonalOffset.UTC.canonical(), ZonalOffset.UTC);

        for (Class<? extends TZID> area : areas) {
            for (TZID tzid : area.getEnumConstants()) {
                temp.put(tzid.canonical(), tzid);
            }
        }

        PREDEFINED = Collections.unmodifiableMap(temp);

        ServiceLoader<Provider> sl = ServiceLoader.load(Provider.class);
        Provider loaded = null;

        for (Provider provider : sl) {
            if (
                (loaded == null)
                || (provider.getVersion().compareTo(loaded.getVersion()) > 0)
            ) {
                loaded = provider;
                break;
            }
        }

        PROVIDER = (
            (loaded == null)
            ? new OldStyleTZProvider()
            : loaded
        );

        TimeZone systemTZ = null;

        try {
            String zoneID = System.getProperty("user.timezone");

            if (
                "Z".equals(zoneID)
                || "UTC".equals(zoneID)
            ) {
                systemTZ = ZonalOffset.UTC.getModel();
            } else if (zoneID != null) {
                systemTZ = TimeZone.getTZ(new NamedID(zoneID), false);
            }
        } catch (SecurityException se) {
            // OK, dann Zugriff auf j.u.TimeZone.getDefault()
        }

        if (systemTZ == null) {
            SYSTEM_TZ_ORIGINAL = TimeZone.getDefaultTZ();
        } else {
            SYSTEM_TZ_ORIGINAL = systemTZ;
        }

        if (ALLOW_SYSTEM_TZ_OVERRIDE) {
            SYSTEM_TZ_CURRENT = SYSTEM_TZ_ORIGINAL;
        }

        // Aliases + Available-IDs
        NAME_DATA = new NameData(PROVIDER);
    }

    //~ Konstruktoren -----------------------------------------------------

    /**
     * <p>Nur zur paket-privaten Verwendung. </p>
     */
    TimeZone() {
        super();

    }

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Liefert alle verf&uuml;gbaren Zeitzonenkennungen. </p>
     *
     * @return  unmodifiable list of available time zone ids in ascending order
     */
    public static List<TZID> getAvailableIDs() {

        return NAME_DATA.availables;

    }

    /**
     * <p>Liefert die Standard-Zeitzone des Systems. </p>
     *
     * <p>Der verwendete Algorithmus basiert vorrangig auf der
     * System-Property &quot;user.timezone&quot;, dann erst auf der
     * Methode {@code java.util.TimeZone.getDefault()}. Ist zus&auml;tzlich
     * die Property &quot;allow.system.tz.override&quot; auf den Wert
     * &quot;true&quot; gesetzt, dann kann nach einer Kombination aus
     * {@code java.util.TimeZone.setDefault()} und der Methode
     * {@link Cache#refresh()} in dieser Klasse die Standard-Zeitzone
     * auch ge&auml;ndert werden, sonst wird sie einmalig beim Laden
     * dieser Klasse gesetzt. </p>
     *
     * <p>Zu beachten: Kann die Standard-Zeitzone zum Beispiel wegen eines
     * falschen Property-Werts in &quot;user.timezone&quot; nicht interpretiert
     * werden, f&auml;llt diese Methode auf die UTC-Zeitzone zur&uuml;ck. </p>
     *
     * @return  default time zone data of system
     * @see     java.util.TimeZone#getDefault()
     */
    public static TimeZone ofSystem() {

        if (ALLOW_SYSTEM_TZ_OVERRIDE) {
            return SYSTEM_TZ_CURRENT;
        } else {
            return SYSTEM_TZ_ORIGINAL;
        }

    }

    /**
     * <p>Liefert die Zeitzone mit der angegebenen ID. </p>
     *
     * @param   tzid    time zone id
     * @return  time zone data
     * @throws  ChronoException wenn die angegebene Zeitzone nicht geladen
     *          werden kann
     */
    public static TimeZone of(TZID tzid) {

        return TimeZone.getTZ(tzid, true);

    }

    /**
     * <p>Versucht bevorzugt, die Zeitzone mit der angegebenen ID zu laden,
     * sonst eine Alternative. </p>
     *
     * <p>Ist die Zeitzone zur ID nicht ladbar, dann wird als zweiter Versuch
     * die Zeitzone passend zum zweiten Argument geladen. Schl&auml;gt auch
     * das fehl, wird schlie&szlig;lich die Standard-Zeitzone der JVM geladen.
     * Im Gegensatz zu {@link #of(TZID)} wirft diese Methode niemals eine
     * Ausnahme. </p>
     *
     * @param   tzid        preferred time zone id
     * @param   fallback    alternative time zone id
     * @return  time zone data
     */
    public static TimeZone of(
        TZID tzid,
        TZID fallback
    ) {

        TimeZone ret = TimeZone.getTZ(tzid, false);

        if (ret == null) {
            ret = TimeZone.getTZ(fallback, false);

            if (ret == null) {
                ret = TimeZone.ofSystem();
            }
        }

        return ret;

    }

    /**
     * <p>Liefert die Zeitzonen-ID. </p>
     *
     * @return  time zone id
     * @see     java.util.TimeZone#getID()
     */
    public abstract TZID getID();

    /**
     * <p>Ermittelt die Zeitzonenverschiebung zum angegebenen Zeitpunkt auf
     * der UT-Weltzeitlinie in Sekunden. </p>
     *
     * @param   ut      unix time
     * @return  shift in seconds which yields local time if added to unix time
     * @see     java.util.TimeZone#getOffset(long)
     */
    public abstract ZonalOffset getOffset(UnixTime ut);

    /**
     * <p>Ermittelt die Zeitzonenverschiebung zum angegebenen lokalen
     * Zeitpunkt in Sekunden. </p>
     *
     * <p>Als Konfliktstrategie f&uuml;r L&uuml;cken oder &Uuml;berlappungen
     * auf dem lokalen Zeitstrahl wird {@code TransitionStrategy.PUSH_FORWARD}
     * verwendet. Dieses Verhalten ist zum JDK konform. </p>
     *
     * @param   localDate   local date in time zone
     * @param   localTime   local wall time in time zone
     * @return  shift in seconds which yields unix time if subtracted
     *          from local time choosing later offset at gaps or overlaps
     * @see     java.util.TimeZone#getOffset(int, int, int, int, int, int)
     * @see     TransitionStrategy#PUSH_FORWARD
     */
    public abstract ZonalOffset getOffset(
        GregorianDate localDate,
        WallTime localTime
    );

    /**
     * <p>Bestimmt, ob der angegebene lokale Zeitpunkt in eine L&uuml;cke
     * f&auml;llt. </p>
     *
     * <p>Das klassiche Beispiel liegt vor, wenn wegen eines &Uuml;bergangs
     * von der Winter- zur Sommerzeit eine bestimmte Uhrzeit nicht existiert,
     * weil die Uhr vorgestellt wurde. </p>
     *
     * @param   localDate   local date in time zone
     * @param   localTime   local wall time in time zone
     * @return  {@code true} if the local time is not defined due to
     *          transition gaps else {@code false}
     */
    public abstract boolean isInvalid(
        GregorianDate localDate,
        WallTime localTime
    );

    /**
     * <p>Herrscht zum angegebenen Zeitpunkt Sommerzeit in der Zeitzone? </p>
     *
     * <p>Die DST-Korrektur selbst kann als Differenz zwischen dem Gesamt-Offset
     * und dem Standard-Offset erhalten werden, wenn sich der Standard-Offset
     * historisch nicht ge&auml;ndert hat. Alternativ und genauer kann die
     * DST-Korrektur &uuml;ber die Offset-Historie ermittelt werden. </p>
     *
     * @param   ut      unix time
     * @return  {@code true} if the argument represents summer time
     *          else {@code false}
     * @see     java.util.TimeZone#inDaylightTime(java.util.Date)
     */
    public abstract boolean isDaylightSaving(UnixTime ut);

    /**
     * <p>Liefert die zugrundeliegenden &Uuml;berg&auml;nge und Regeln. </p>
     *
     * @return  {@code TransitionHistory} or {@code null} if there is no
     *          better {@code Provider} than {@code java.util.TimeZone}
     */
    public abstract TransitionHistory getHistory();

    /**
     * <p>Beschreibt die Zeitzonendatenbank mit Name, Ort und Version. </p>
     *
     * @return  String
     */
    public static String getProviderInfo() {

        StringBuilder sb = new StringBuilder(128);
        sb.append(TimeZone.class.getName());
        sb.append("[provider=");
        sb.append(PROVIDER.getName());

        if (!(PROVIDER instanceof OldStyleTZProvider)) {
            sb.append(",location=");
            sb.append(PROVIDER.getLocation());
            sb.append(",version=");
            sb.append(PROVIDER.getVersion());
        }

        sb.append(']');
        return sb.toString();

    }

    /**
     * <p>Definiert eine Abfrage nach der Zeitzonen-ID als Singleton. </p>
     *
     * <p>Die Abfrage liefert f&uuml;r alle Entit&auml;ten vom Typ
     * {@code UnixTime} immer die UTC-Zeitzone, sonst {@code null}, es
     * sei denn, eine Entit&auml;t definiert ein anderes Abfrageergebnis.
     * Beispiel: </p>
     *
     * <pre>
     *  net.time4j.Moment moment = ...;
     *  System.out.println(moment.get(TimeZone.identifier()));
     *  // Ausgabe: Z (ZonalOffset.UTC)
     * </pre>
     *
     * @return  singleton-query for time zone id
     */
    public static ChronoFunction<ChronoEntity<?>, TZID> identifier() {

        return Query.SINGLETON;

    }

    /**
     * <p>Liefert den anzuzeigenden Zeitzonennamen. </p>
     *
     * <p>Ist der Zeitzonenname nicht ermittelbar, wird die ID der Zeitzone
     * geliefert. </p>
     *
     * @param   daylightSaving      asking for summer time version
     * @param   abbreviated         asking for abbreviation
     * @param   locale              language setting
     * @return  localized time zone name for display purposes
     * @see     java.util.TimeZone#getDisplayName(boolean,int,Locale)
     * @see     Locale#getDefault()
     * @see     #getID()
     */
    public String getDisplayName(
        boolean daylightSaving,
        boolean abbreviated,
        Locale locale
    ) {

        if (locale == null) {
            throw new NullPointerException("Missing locale.");
        }
        
        String id = this.getID().canonical();
        java.util.TimeZone tz = OldStyleTimeZone.findZone(id);

        if (tz.getID().equals(id)) {
            return tz.getDisplayName(
                daylightSaving,
                abbreviated
                    ? java.util.TimeZone.SHORT
                    : java.util.TimeZone.LONG,
                locale
            );
        } else {
            return id;
        }

    }

    private static TimeZone getDefaultTZ() {

        return TimeZone.of(
            new NamedID(java.util.TimeZone.getDefault().getID()),
            ZonalOffset.UTC
        );

    }

    private static TimeZone getTZ(
        TZID tzid,
        boolean wantsException
    ) {

        // Liegt eine feste Verschiebung vor?
        if (tzid instanceof ZonalOffset) {
            return ((ZonalOffset) tzid).getModel();
        }

        String zoneID = tzid.canonical();

        // Suche im Cache
        TimeZone tz = null;
        NamedReference sref = CACHE.get(zoneID);

        if (sref != null) {
            tz = sref.get();
            if (tz == null) {
                CACHE.remove(sref.tzid);
            }
        }

        if (tz != null) {
            return tz;
        }

        // java.util.TimeZone hat keine öffentliche Historie
        if (PROVIDER instanceof OldStyleTZProvider) {
            TimeZone test = new OldStyleTimeZone(zoneID);

            if (
                test.getID().canonical().equals("GMT")
                && !zoneID.equals("GMT")
            ) {
                // JDK-Fallback => null
            } else {
                tz = test;
            }
        } else { // exakte Suche in Historie
            TransitionHistory history = PROVIDER.load(zoneID, false);

            if (history == null) {
                // Alias-Suche + Fallback-Option
                tz = TimeZone.getZoneByAlias(tzid, zoneID);
            } else {
                tz = new HistorizedTimeZone(tzid, history);
            }
        }

        // Ungültige ID?
        if (tz == null) {
            if (wantsException) {
                throw new ChronoException("Unknown time zone: " + zoneID);
            } else {
                return null;
            }
        }

        // bei Bedarf im Cache speichern
        if (ACTIVE) {
            NamedReference oldRef =
                CACHE.putIfAbsent(
                    zoneID,
                    new NamedReference(tz, QUEUE)
                );

            if (oldRef == null) {
                synchronized (TimeZone.class) {
                    LAST_USED.addFirst(tz);

                    while (LAST_USED.size() >= SOFT_LIMIT) {
                        LAST_USED.removeLast();
                    }
                }
            } else {
                TimeZone oldZone = oldRef.get();

                if (oldZone != null) {
                    tz = oldZone;
                }
            }
        }

        return tz;

    }

    private static TimeZone getZoneByAlias(
        TZID tzid,
        String zoneID
    ) {

        TransitionHistory history = null;
        String alias = zoneID;
        Map<String, String> aliases = NAME_DATA.aliases;

        while (
            (history == null)
            && ((alias = aliases.get(alias)) != null)
        ) {
            history = PROVIDER.load(alias, false);
        }

        if (
            (history == null)
            && PROVIDER.isFallbackEnabled()
        ) {
            history = PROVIDER.load(zoneID, true);
        }

        if (history == null) {
            return null;
        } else {
            return new HistorizedTimeZone(tzid, history);
        }

    }

    //~ Innere Interfaces -------------------------------------------------

    /**
     * <p>SPI-Interface, das eine Zeitzonendatenbank kapselt und passend zu
     * einer Zeitzonen-ID (hier als String statt als {@code TZID}) die
     * Zeitzonendaten liefert. </p>
     *
     * <p>Implementierungen sind in der Regel zustandslos und halten keinen
     * Cache. Letzterer sollte der Klasse {@code TimeZone} vorbehalten sein.
     * Weil dieses Interface per {@code java.util.ServiceLoader} genutzt wird,
     * mu&szlig; eine konkrete Implementierung einen &ouml;ffentlichen
     * Konstruktor ohne Argumente definieren. </p>
     *
     * @author  Meno Hochschild
     * @see     java.util.ServiceLoader
     */
    public interface Provider {

        //~ Methoden ------------------------------------------------------

        /**
         * <p>Liefert alle verf&uuml;gbaren Zeitzonenkennungen. </p>
         *
         * @return  unmodifiable set of time zone ids
         * @see     java.util.TimeZone#getAvailableIDs()
         */
        Set<String> getAvailableIDs();

        /**
         * <p>Liefert eine Alias-Tabelle, in der die Schl&uuml;ssel alternative
         * Zonen-IDs darstellen und in der die zugeordneten Werte wieder
         * Aliasnamen oder letztlich kanonische Zonen-IDs sind. </p>
         *
         * <p>Beispiel: &quot;PST&quot; => &quot;America/Los_Angeles&quot;. </p>
         *
         * @return  map from all time zone aliases to canoncial ids
         */
        Map<String, String> getAliases();

        /**
         * <p>L&auml;dt die Zeitzonendaten zur angegebenen Zonen-ID. </p>
         *
         * <p>Diese Methode wird von {@code TimeZone} aufgerufen. Das zweite
         * Argument ist normalerweise {@code false}, so da&szlig; es sich um
         * eine exakte Suchanforderung handelt. Nur wenn die Methode
         * {@code isFallbackEnabled()} den Wert {@code true} zur&uuml;ckgibt
         * und vorher weder die exakte Suche noch die Alias-Suche erfolgreich
         * waren, kann ein erneuter Aufruf mit dem zweiten Argument
         * {@code true} erfolgen. </p>
         *
         * @param   zoneID      time zone id (z.B. &quot;Europe/Berlin&quot;)
         * @param   fallback    fallback allowed if a time zone id cannot be
         *                      found, not even by alias?
         * @return  time zone history or {@code null} if there are no data
         * @throws  IllegalStateException if time zone database is broken
         * @see     #getAvailableIDs()
         * @see     #getAliases()
         * @see     #isFallbackEnabled()
         * @see     java.util.TimeZone#getTimeZone(String)
         */
        TransitionHistory load(
            String zoneID,
            boolean fallback
        );

        /**
         * <p>Soll eine alternative Zeitzone mit eventuell anderen Regeln
         * geliefert werden, wenn die Suche nach einer Zeitzone erfolglos
         * war? </p>
         *
         * @return  boolean
         * @see     #load(String, boolean)
         */
        boolean isFallbackEnabled();

        /**
         * <p>Gibt den Namen dieser Zeitzonendatenbank an. </p>
         *
         * <p>Die Olson/IANA-Zeitzonendatenbank hat den Namen
         * &quot;TZDB&quot;. </p>
         *
         * @return  String
         */
        String getName();

        /**
         * <p>Beschreibt die Quelle der Zeitzonendatenbank. </p>
         *
         * @return  String which refers to an URI or empty if unknown
         */
        String getLocation();

        /**
         * <p>Liefert die Version der Zeitzonendatenbank. </p>
         *
         * <p>Meist liegt die Version im Olson-Format vor. Dieses Format sieht
         * als Versionskennung eine 4-stellige Jahreszahl gefolgt von einem
         * Buchstaben im Bereich a-z vor. </p>
         *
         * @return  String (for example &quot;2011n&quot;) or empty if unknown
         */
        String getVersion();

    }

    //~ Innere Klassen ----------------------------------------------------

    /**
     * <p>Bietet statische Methoden zum Konfigurieren des
     * Zeitzonendatenpuffers. </p>
     */
    public static class Cache {

        //~ Konstruktoren -------------------------------------------------

        private Cache() {
            // no instantiation
        }

        //~ Methoden ------------------------------------------------------

        /**
         * <p>Erlaubt eine Aktualisierung, wenn sich die Zeitzonendatenbank
         * ge&auml;ndert hat (<i>dynamic update</i>). </p>
         *
         * <p>Der interne Cache wird entleert. Auch wird bei Bedarf die
         * Standard-Zeitzone neu ermittelt. </p>
         */
        public static void refresh() {

            synchronized (TimeZone.class) {
                while (QUEUE.poll() != null) {}
                LAST_USED.clear();
            }

            NAME_DATA = new NameData(PROVIDER);
            CACHE.clear();

            if (ALLOW_SYSTEM_TZ_OVERRIDE) {
                SYSTEM_TZ_CURRENT = TimeZone.getDefaultTZ();
            }

        }

        /**
         * <p>Aktiviert oder deaktiviert den internen Cache. </p>
         *
         * <p>Standardm&auml;&szlig;ig ist der Cache aktiv. Ein Abschalten des
         * Cache kann die Performance insbesondere dann verschlechtern, wenn der
         * zugrundeliegende {@code Provider} selbst keinen Cache hat. </p>
         *
         * @param   active  {@code true} if chache shall be active
         *                  else {@code false}
         */
        public static void setCacheActive(boolean active) {

            ACTIVE = active;

            if (!active) {
                CACHE.clear();
            }

        }

        /**
         * <p>Konfiguriert die Gr&ouml;&szlig;e des internen Cache neu. </p>
         *
         * @param   minimumCacheSize    new minimum size of cache
         * @throws  IllegalArgumentException if the argument is negative
         */
        public static void setMinimumCacheSize(int minimumCacheSize) {

            if (minimumCacheSize < 0) {
                throw new IllegalArgumentException(
                    "Negative time zone cache size: " + minimumCacheSize);
            }

            NamedReference ref;

            while ((ref = (NamedReference) QUEUE.poll()) != null) {
                CACHE.remove(ref.tzid);
            }

            synchronized (TimeZone.class) {
                SOFT_LIMIT = minimumCacheSize + 1;
                int n = LAST_USED.size() - minimumCacheSize;

                for (int i = 0; i < n; i++) {
                    LAST_USED.removeLast();
                }
            }

        }

    }

    private static class Query
        implements ChronoFunction<ChronoEntity<?>, TZID> {

        //~ Statische Felder/Initialisierungen ----------------------------

        static final Query SINGLETON = new Query();

        //~ Konstruktoren -------------------------------------------------

        private Query() {
            super();

        }

        //~ Methoden ------------------------------------------------------

        @Override
        public TZID apply(ChronoEntity<?> entity) {

            if (entity instanceof UnixTime) {
                return ZonalOffset.UTC;
            } else {
                return null;
            }

        }

    }

    private static class NamedReference
        extends SoftReference<TimeZone> {

        //~ Instanzvariablen ----------------------------------------------

        private final TZID tzid;

        //~ Konstruktoren -------------------------------------------------

        NamedReference(
            TimeZone tz,
            ReferenceQueue<TimeZone> queue
        ) {
            super(tz, queue);
            this.tzid = tz.getID();

        }

    }

    private static class NamedID
        implements TZID, Serializable {

        //~ Statische Felder/Initialisierungen ----------------------------

        private static final long serialVersionUID = -4889632013137688471L;

        //~ Instanzvariablen ----------------------------------------------

        /**
         * @serial  time zone id
         */
        private final String tzid;

        //~ Konstruktoren -------------------------------------------------

        NamedID(String tzid) {
            super();

            this.tzid = tzid;

        }

        //~ Methoden ------------------------------------------------------

        @Override
        public String canonical() {

            return this.tzid;

        }

        @Override
        public String toString() {

            return this.getClass().getName() + "@" + this.tzid;

        }

    }

    private static class NameData {

        //~ Instanzvariablen ----------------------------------------------

        private final Map<String, String> aliases;
        private final List<TZID> availables;

        //~ Konstruktoren -------------------------------------------------

        NameData(Provider provider) {
            super();

            Set<String> ids = provider.getAvailableIDs();
            List<TZID> list = new ArrayList<TZID>();
            boolean hasUTC = false;

            for (String id : ids) {
                TZID tzid = PREDEFINED.get(id);

                if (tzid == null) {
                    list.add(new NamedID(id));
                } else {
                    if (tzid == ZonalOffset.UTC) {
                        hasUTC = true;
                    }
                    list.add(tzid);
                }
            }

            if (!hasUTC) {
                list.add(ZonalOffset.UTC);
            }

            Collections.sort(
                list,
                new Comparator<TZID>() {
                    @Override
                    public int compare(
                        TZID o1,
                        TZID o2
                    ) {
                        return o1.canonical().compareTo(o2.canonical());
                    }
                }
            );

            this.availables =
                Collections.unmodifiableList(list);
            this.aliases =
                Collections.unmodifiableMap(
                    new HashMap<String, String>(provider.getAliases()));

        }

    }

    private static class OldStyleTZProvider
        implements Provider {

        //~ Methoden ------------------------------------------------------

        @Override
        public Set<String> getAvailableIDs() {

            Set<String> ret = new HashSet<String>();
            String[] temp = java.util.TimeZone.getAvailableIDs();

            for (String s : temp) {
                ret.add(s);
            }

            return ret;

        }

        @Override
        public Map<String, String> getAliases() {

            return Collections.emptyMap(); // JDK hat eingebaute Alias-Suche!

        }

        @Override
        public boolean isFallbackEnabled() {

            return false; // JDK hat eingebauten Fallback-Mechanismus!

        }

        @Override
        public String getName() {

            return "java.util.TimeZone";

        }

        @Override
        public String getLocation() {

            return "";

        }

        @Override
        public String getVersion() {

            return "";

        }

        @Override
        public TransitionHistory load(
            String zoneID,
            boolean fallback
        ) {

            return null; // keine öffentliche Historie

        }

    }

}
