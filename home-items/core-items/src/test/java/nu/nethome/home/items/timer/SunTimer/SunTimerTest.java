package nu.nethome.home.items.timer.SunTimer;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class SunTimerTest {

    private SunTimer sunTimer;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        sunTimer = spy(new SunTimer());
        proxy = new LocalHomeItemProxy(sunTimer);
    }

    @Test
    public void testGetModel() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(sunTimer.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }

    static final String[] weekdays = {"Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "Saturdays", "Sundays"};

    @Test
    public void setAndGetWeekDays() throws Exception {
        for (String weekday : weekdays) {
            proxy.setAttributeValue(weekday, weekday + "x");
        }
        for (String weekday : weekdays) {
            assertThat(proxy.getAttributeValue(weekday), is(weekday + "x"));
        }
    }

    @Test
    public void activationSetsCurrentDayString() throws Exception {
        doReturn(Calendar.TUESDAY).when(sunTimer).getToday();
        proxy.setAttributeValue("Tuesdays", "10:00-01:00->11:00,->13:00,15:00->");
        HomeService server = mock(HomeService.class);
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("09:00->11:00,->13:00,15:00->"));
    }
}
