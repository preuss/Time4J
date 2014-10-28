package net.time4j.tz.olson;

import net.time4j.Iso8601Format;
import net.time4j.Moment;
import net.time4j.PatternType;
import net.time4j.format.ChronoFormatter;
import net.time4j.tz.Timezone;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(Parameterized.class)
public class ZoneNameParsingTest {

 	@Parameterized.Parameters
        (name= "{index}: "
            + "(pattern={0},locale={1},timezone={2},value={3},text={4})")
 	public static Iterable<Object[]> data() {
 		return Arrays.asList(
            new Object[][] {
                {"MMMM/dd/uuuu HH:mm:ss z",
                        "us",
                        "America/Los_Angeles",
                        "2012-06-30T23:59:60Z",
                        "June/30/2012 16:59:60 PDT"},
                {"MMMM/dd/uuuu HH:mm:ss zzzz",
                        "us",
                        "America/Los_Angeles",
                        "2012-06-30T23:59:60Z",
                        "June/30/2012 16:59:60 Pacific Daylight Time"},
                {"d. MMM uuuu HH:mm z",
                        "de",
                        "Europe/Berlin",
                        "2012-03-31T23:59Z",
                        "1. Apr 2012 01:59 MESZ"},
                {"d. MMM uuuu HH:mm zzzz",
                        "de",
                        "Europe/Berlin",
                        "2012-03-31T23:59Z",
                        "1. Apr 2012 01:59 Mitteleuropäische Sommerzeit"},
                {"d. MMMM uuuu HH:mm z",
                        "de",
                        "Europe/Berlin",
                        "2012-03-01T23:59Z",
                        "2. März 2012 00:59 MEZ"},
                {"d. MMMM uuuu HH:mm zzzz",
                        "de",
                        "Europe/Berlin",
                        "2012-03-01T23:59Z",
                        "2. März 2012 00:59 Mitteleuropäische Zeit"},
                {"d. MMM uuuu HH:mm z",
                        "fr",
                        "Europe/Paris",
                        "2012-03-31T23:59Z",
                        "1. avr. 2012 01:59 CEST"},
                {"d. MMM uuuu HH:mm zzzz",
                        "fr",
                        "Europe/Paris",
                        "2012-03-31T23:59Z",
                        "1. avr. 2012 01:59 Heure d'été d'Europe centrale"},
                {"d. MMMM uuuu HH:mm z",
                        "fr",
                        "Europe/Paris",
                        "2012-03-01T23:59Z",
                        "2. mars 2012 00:59 CET"},
                {"d. MMMM uuuu HH:mm zzzz",
                        "fr",
                        "Europe/Paris",
                        "2012-03-01T23:59Z",
                        "2. mars 2012 00:59 Heure d'Europe centrale"},
                {"MMMM/dd/uuuu hh:mm a z",
                        "us",
                        "America/Los_Angeles",
                        "2012-02-21T14:30Z",
                        "February/21/2012 06:30 AM PST"},
                {"MMMM/dd/uuuu hh:mm a zzzz",
                        "us",
                        "America/Los_Angeles",
                        "2012-02-21T14:30Z",
                        "February/21/2012 06:30 AM Pacific Standard Time"},
                {"d. MMMM uuuu HH:mm z",
                        "en",
                        "Europe/London",
                        "2012-03-01T23:59Z",
                        "1. March 2012 23:59 GMT"},
                {"d. MMMM uuuu HH:mm z",
                        "en",
                        "Europe/London",
                        "2012-03-31T23:59Z",
                        "1. April 2012 00:59 BST"},
                {"d. MMMM uuuu HH:mm zzzz",
                        "en",
                        "Europe/London",
                        "2012-03-01T23:59Z",
                        "1. March 2012 23:59 Greenwich Mean Time"},
                {"d. MMMM uuuu HH:mm zzzz",
                        "en",
                        "Europe/London",
                        "2012-03-31T23:59Z",
                        "1. April 2012 00:59 British Summer Time"},
                 {"uuuu-MM-dd'T'HH:mm:ss.SSS z",
                        "in",
                        "Asia/Kolkata",
                        "2012-06-30T23:59:60,123000000Z",
                        "2012-07-01T05:29:60.123 IST"},
                 {"uuuu-MM-dd'T'HH:mm:ss.SSS zzzz",
                        "in",
                        "Asia/Kolkata",
                        "2012-06-30T23:59:60,123000000Z",
                        "2012-07-01T05:29:60.123 India Standard Time"}
           }
        );
    }

    private ChronoFormatter<Moment> formatter;
    private Moment value;
    private String text;

    public ZoneNameParsingTest(
        String pattern,
        String locale,
        String tzid,
        String value,
        String text
    ) throws ParseException {
        super();

        this.formatter =
            Moment.formatter(
                pattern,
                PatternType.CLDR,
                toLocale(locale),
                Timezone.of(tzid).getID());
        this.value = Iso8601Format.EXTENDED_DATE_TIME_OFFSET.parse(value);
        this.text = text;
    }

    @Test
    public void print() {
        assertThat(
            this.formatter.format(this.value),
            is(this.text));
    }

    @Test
    public void parse() throws ParseException {
        assertThat(
            this.formatter.parse(this.text),
            is(this.value));
    }

    private static Locale toLocale(String locale) {
        if (locale.equals("en")) {
            return Locale.UK;
        } else if (locale.equals("us")) {
            return Locale.US;
        } else if (locale.equals("in")) {
            return new Locale("en", "IN");
        }
        return new Locale(locale, locale.toUpperCase());
    }

}