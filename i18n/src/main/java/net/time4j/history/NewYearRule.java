/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2016 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (NewYearRule.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.history;


/**
 * <p>Defines a listing of common historic new year rules. </p>
 *
 * <p>Note that some rules were simultaneously used in any region by different people. </p>
 *
 * <p>Literature: </p>
 *
 * <ul>
 *     <li><a href="http://www.ortelius.de/kalender/jul_en.php">http://www.ortelius.de/kalender/jul_en.php</a></li>
 *     <li><a href="http://www.newadvent.org/cathen/03738a.htm#beginning">http://www.newadvent.org/cathen/03738a.htm#beginning</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/New_Year#Historical_European_new_year_dates">https://en.wikipedia.org/wiki/New_Year#Historical_European_new_year_dates</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/Anno_Domini#Change_of_year">https://en.wikipedia.org/wiki/Anno_Domini#Change_of_year</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/New_Year's_Day">https://en.wikipedia.org/wiki/New_Year's_Day</a></li>
 *     <li><a href="https://groups.google.com/forum/#!topic/soc.roots/SYETvljQPYc">https://groups.google.com/forum/#!topic/soc.roots/SYETvljQPYc</a></li>
 * </ul>
 *
 * @author  Meno Hochschild
 * @since   3.14/4.11
 */
/*[deutsch]
 * <p>Definiert eine Aufz&auml;hlung von in der Geschichte gebr&auml;uchlichen Neujahrsregeln. </p>
 *
 * <p>Zu beachten: Einige Regeln wurden in einer gegebenen Region manchmal gleichzeitig von verschiedenen
 * Menschen angewandt. </p>
 *
 * <p>Literatur: </p>
 *
 * <ul>
 *     <li><a href="http://www.ortelius.de/kalender/jul_en.php">http://www.ortelius.de/kalender/jul_en.php</a></li>
 *     <li><a href="http://www.newadvent.org/cathen/03738a.htm#beginning">http://www.newadvent.org/cathen/03738a.htm#beginning</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/New_Year#Historical_European_new_year_dates">https://en.wikipedia.org/wiki/New_Year#Historical_European_new_year_dates</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/Anno_Domini#Change_of_year">https://en.wikipedia.org/wiki/Anno_Domini#Change_of_year</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/New_Year's_Day">https://en.wikipedia.org/wiki/New_Year's_Day</a></li>
 *     <li><a href="https://groups.google.com/forum/#!topic/soc.roots/SYETvljQPYc">https://groups.google.com/forum/#!topic/soc.roots/SYETvljQPYc</a></li>
 * </ul>
 *
 * @author  Meno Hochschild
 * @since   3.14/4.11
 */
public enum NewYearRule {

    //~ Statische Felder/Initialisierungen --------------------------------

    /**
     * <p>The new year starts on January the first. </p>
     *
     * <p>This rule was and has been widely used until up to now. Some people call it the circumcision style
     * according to some efforts of the church to connect that day to any religious events. However, the Romans
     * had originally invented this rule celebrating the introduction of new consules in their office. </p>
     *
     * <p>Although sometimes in history the authorities like the church officially used other styles,
     * many people still used to inofficially celebrate New Year on first of January. </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr beginnt am ersten Tag des Januar. </p>
     *
     * <p>Diese Regel wurde und wird bis heute am meisten verwendet. Das Beschneidungsfest der Kirche
     * erinnert ebenfalls an diese Regel, die jedoch urspr&uuml;nglich von den R&ouml;mern stammt, die
     * damit die Amtseinführung neuer Konsuln feierten. </p>
     *
     * <p>Auch wenn manchmal in der Geschichte Autorit&auml;ten wie die Kirche offiziell andere Regeln
     * anwandten, haben viele Menschen nicht aufgeh&ouml;rt, inoffiziell Neujahr am 1. Januar zu feiern. </p>
     */
    BEGIN_OF_JANUARY() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            return HistoricDate.of(era, yearOfEra, 1, 1);
        }
        @Override
        int displayedYear(
            NewYearStrategy strategy,
            HistoricDate date
        ) {
            return date.getYearOfEra(); // overridden for performance
        }
    },

    /**
     * <p>The new year starts on 1st of March. </p>
     *
     * <p>This rule was used in the Republic of Venice until 1797. </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr f&auml;ngt am ersten Tag des M&auml;rz an. </p>
     *
     * <p>Diese Regel war in der Republik von Venedig bis 1797 gebr&auml;chlich. </p>
     */
    BEGIN_OF_MARCH() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            return HistoricDate.of(era, yearOfEra, 3, 1);
        }
    },

    /**
     * <p>The new year starts on 1st of September. </p>
     *
     * <p>This rule was used in Russia during midage (byzantine calendar). </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr f&auml;ngt am ersten Tag des September an. </p>
     *
     * <p>Diese Regel war in Ru&szlig;land im Mittelalter gebr&auml;chlich (byzantinischer Kalender). </p>
     */
    BEGIN_OF_SEPTEMBER() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            return HistoricDate.of(era, yearOfEra, 9, 1);
        }
    },

    /**
     * <p>The new year starts on 25th of December (yyyy-12-25). </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr beginnt Weihnachten zum postulierten Datum von Jesu Geburt (yyyy-12-25). </p>
     */
    CHRISTMAS_STYLE() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            if (era == HistoricEra.BC) {
                return HistoricDate.of(era, yearOfEra + 1, 12, 25);
            } else if (yearOfEra == 1) {
                return HistoricDate.of(HistoricEra.BC, HistoricEra.BC.yearOfEra(era, 0), 12, 25);
            } else {
                return HistoricDate.of(era, yearOfEra - 1, 12, 25);
            }
        }
        @Override
        int displayedYear(
            NewYearStrategy strategy,
            HistoricDate date
        ) { // does not work for era BC
            int yearOfEra = date.getYearOfEra();
            HistoricDate newYear = strategy.newYear(date.getEra(), yearOfEra + 1); // use strategy of next year!!!
            int yearOfDisplay = yearOfEra;
            if (date.compareTo(newYear) >= 0) {
                yearOfDisplay++;
            }
            return yearOfDisplay;
        }
    },

    /**
     * <p>The new year starts on Holy Saturday (one day before Easter Sunday). </p>
     *
     * <p>Mainly used in France. Due to the possibility to have two same dates per year, both dates were
     * distinguished by the addition &quot;after Easter/before Easter&quot;. </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr startet Samstag vor Ostern. </p>
     *
     * <p>Haupts&auml;chlich in Frankreich verwendet. Wegen der M&ouml;glichkeit, das gleiche Datum zweimal
     * im Jahr zu haben, wurden die Datumsangaben mit dem Zusatz &quot;nach Ostern/vor Ostern&quot; versehen. </p>
     */
    //EASTER_STYLE,

    /**
     * <p>The new year starts on Good Friday (two days before Easter Sunday). </p>
     *
     * <p>Due to the possibility to have two same dates per year, both dates were
     * distinguished by the addition &quot;after Easter/before Easter&quot;. </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr startet am Karfreitag. </p>
     *
     * <p>Wegen der M&ouml;glichkeit, das gleiche Datum zweimal im Jahr zu haben, wurden die Datumsangaben
     * mit dem Zusatz &quot;nach Ostern/vor Ostern&quot; versehen. </p>
     */
    //GOOD_FRIDAY,

    /**
     * <p>The new year starts on 25th of March (yyyy-03-25), also called Lady Day or Calculus Florentinus. </p>
     *
     * <p>This rule was also called the annunciation style and applied in parts of Europe during midage
     * (was officially in effect in England until 1752). A great disadvantage of this reckoning system is
     * Easter happening not at all, once or twice per year. </p>
     */
    /*[deutsch]
     * <p>Das neue Jahr beginnt im M&auml;rz zu Mari&auml; Verkündigung (yyyy-03-25), auch
     * Calculus Florentinus genannt. </p>
     *
     * <p>Diese Regel wurde in Teilen Europas im Mittelalter angewandt (war in England bis 1752 in Kraft).
     * Ein gro&szlig;er Nachteil dieser Z&auml;hlweise ist, da&azlig; Ostern gar nicht, einmal oder zweimal
     * im Jahr vorkommen kann. </p>
     */
    MARIA_ANUNCIATA() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            return HistoricDate.of(era, yearOfEra, 3, 25);
        }
    },

    /**
     * <p>The new year starts on 25th of March (yyyy-03-25), but one year earlier than the calculus florentinus. </p>
     *
     * <p>This rule was used in Pisa/Italy based on the statement that the date of incarnation of Jesus must
     * happen before the birth. </p>
     *
     * @see     #MARIA_ANUNCIATA
     */
    /*[deutsch]
     * <p>Das neue Jahr beginnt im M&auml;rz zu Mari&auml; Verkündigung (yyyy-03-25), aber ein Jahr
     * fr&uuml;her als nach dem Calculus Florentinus. </p>
     *
     * <p>Diese Regel wurde in Pisa/Italien angewandt und fu&szlig;t darauf, da&szlig; das Datum der
     * Fleischwerdung Jesu vor seiner Geburt liegen mu&szlig;. </p>
     *
     * @see     #MARIA_ANUNCIATA
     */
    CALCULUS_PISANUS() {
        @Override
        HistoricDate newYear(HistoricEra era, int yearOfEra) {
            return MARIA_ANUNCIATA.newYear(era, yearOfEra);
        }
        @Override
        int displayedYear(
            NewYearStrategy strategy,
            HistoricDate date
        ) { // does not make sense for era BC or yearOfEra <= 2
            int yearOfEra = date.getYearOfEra();
            HistoricDate newYear = this.newYear(date.getEra(), yearOfEra);
            int yearOfDisplay = yearOfEra - 1;
            if (date.compareTo(newYear) < 0) {
                yearOfDisplay--;
            }
            return yearOfDisplay;
        }
    };

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Creates a new-year-strategy based on this rule which is valid until given year of era. </p>
     *
     * @param   annoDomini  end year of validity range related to era AD (exclusive)
     * @return  NewYearStrategy
     * @see     HistoricEra#annoDomini(int)
     * @since   3.14/4.11
     */
    /*[deutsch]
     * <p>Erzeugt eine Neujahrsstrategie, die auf dieser Regel fu&szlig;t und bis zum angegebenen Jahr
     * der &Auml;ra g&uuml;ltig ist. </p>
     *
     * @param   annoDomini  end year of validity range related to era AD (exclusive)
     * @return  NewYearStrategy
     * @see     HistoricEra#annoDomini(int)
     * @since   3.14/4.11
     */
    public NewYearStrategy until(int annoDomini) {

        return new NewYearStrategy(this, annoDomini);

    }

    // calculates the new year
    abstract HistoricDate newYear(
        HistoricEra era,
        int yearOfEra
    );

    // also suitable for EASTER_STYLE and GOOD_FRIDAY
    int displayedYear(
        NewYearStrategy strategy,
        HistoricDate date
    ) { // does not make sense for era BC or yearOfEra <= 1

        int yearOfEra = date.getYearOfEra();
        HistoricDate newYear = this.newYear(date.getEra(), yearOfEra);
        int yearOfDisplay = yearOfEra;

        if (date.compareTo(newYear) < 0) {
            yearOfDisplay--;
        }

        return yearOfDisplay;

    }

}