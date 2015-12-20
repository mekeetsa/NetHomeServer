package nu.nethome.home.items.timer.SunTimer;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SunTimerTest {

    private SunTimer sunTimer;
    private LocalHomeItemProxy proxy;
    private HomeService server;
    private Timer timer;
    private Calendar calendar;
    private DateFormat dateFormat;

    @Before
    public void setUp() throws Exception {
        sunTimer = spy(new SunTimer());
        proxy = new LocalHomeItemProxy(sunTimer);
        server = mock(HomeService.class);
        timer = mock(Timer.class);
        doReturn(timer).when(sunTimer).createTimer();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        doReturn(calendar).when(sunTimer).getTime();
        dateFormat = new SimpleDateFormat("HH:mm");
        doReturn(Calendar.TUESDAY).when(sunTimer).getToday();
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
        proxy.setAttributeValue("Tuesdays", "10:00-01:00->11:00,->13:00,15:00->");
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("09:00->11:00,->13:00,15:00->"));
    }

    @Test
    public void updateDayTimeExpressionDoesNotSetCurrentDayStringIfNotActivated() throws Exception {
        proxy.setAttributeValue("Tuesdays", "10:00->11:00");
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
    }

    @Test
    public void updateDayTimeExpressionSetsCurrentDayStringIfActivated() throws Exception {
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
        proxy.setAttributeValue("Tuesdays", "11:00->12:00");
        assertThat(proxy.getAttributeValue("Timer Today"), is("11:00->12:00"));
    }

    @Test
    public void updateDayTimeExpressionWithUnchangedValueDoesNotUpdateCurrentDayString() throws Exception {
        proxy.setAttributeValue("Tuesdays", "11:00->12:00");
        sunTimer.activate(server);
        proxy.setAttributeValue("Tuesdays", "11:00->12:00");
        proxy.setAttributeValue("Tuesdays", "11:00->12:00");
        proxy.setAttributeValue("Tuesdays", "11:00->12:00");
        verify(sunTimer, times(1)).applySwitchTimesForToday();
    }

    @Test
    public void activationCreatesTimerTasks() throws Exception {
        proxy.setAttributeValue("Tuesdays", "13:00->14:00");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer, times(2)).schedule(taskCaptor.capture(), dateCaptor.capture());
        assertThat(taskCaptor.getAllValues().size(), is(2));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("13:00"));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(1)), is("14:00"));
        assertThat(taskCaptor.getAllValues().get(0), instanceOf(SunTimer.SunTimerTask.class));
        assertThat(taskCaptor.getAllValues().get(1), instanceOf(SunTimer.SunTimerTask.class));
    }

    @Test
    public void activationDoesNotCreatesTimerTasksInThePast() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Tuesdays", "10:00->11:00,11:30->12:30");

        sunTimer.activate(server);

        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer).schedule(any(TimerTask.class), dateCaptor.capture());
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("12:30"));
    }

    @Test
    public void onTimerTaskExecutesOnMethod() throws Exception {
        proxy.setAttributeValue("Tuesdays", "13:00->");
        proxy.setAttributeValue("OnCommand", "call,foo,fie");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer).schedule(taskCaptor.capture(), any(Date.class));
        taskCaptor.getValue().run();
        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void offTimerTaskExecutesOffMethod() throws Exception {
        proxy.setAttributeValue("Tuesdays", "->13:00");
        proxy.setAttributeValue("OffCommand", "call,foo,fie");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer).schedule(taskCaptor.capture(), any(Date.class));
        taskCaptor.getValue().run();
        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void executesMostRecentOnCommandInThePastWhenActivated() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Tuesdays", "10:00->11:00,11:30->12:30");
        proxy.setAttributeValue("OnCommand", "call,foo,fie");

        sunTimer.activate(server);

        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void executesMostRecentOffCommandInThePastWhenActivated() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Tuesdays", "10:00->11:00,11:30->11:40");
        proxy.setAttributeValue("OffCommand", "call,foo,fie");

        sunTimer.activate(server);

        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void stopsTimerWhenItemIsStopped() throws Exception {
        sunTimer.activate(server);

        sunTimer.stop();

        verify(timer).cancel();
    }

    @Test
    public void updateDayTimeExpressionCreatesTimerTasksAndCancelsExistingTimer() throws Exception {
        sunTimer.activate(server);

        proxy.setAttributeValue("Tuesdays", "13:00->14:00");

        verify(timer).cancel();
        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer, times(2)).schedule(taskCaptor.capture(), dateCaptor.capture());
        assertThat(taskCaptor.getAllValues().size(), is(2));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("13:00"));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(1)), is("14:00"));
        assertThat(taskCaptor.getAllValues().get(0), instanceOf(SunTimer.SunTimerTask.class));
        assertThat(taskCaptor.getAllValues().get(1), instanceOf(SunTimer.SunTimerTask.class));
    }
}
