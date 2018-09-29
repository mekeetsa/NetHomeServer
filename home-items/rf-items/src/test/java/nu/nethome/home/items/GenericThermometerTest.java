package nu.nethome.home.items;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class GenericThermometerTest {

    private static final double VALUE = 10.0;
    private static final double K = 0.01;
    private static final double M = 1.0;
    private static final String ADDRESS = "Foo";
    private GenericThermometer thermometer;
    private LocalHomeItemProxy proxy;
    @Mock private Event valueEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        thermometer = new GenericThermometer();
        proxy = new LocalHomeItemProxy(thermometer);
        proxy.setAttributeValue("K", Double.toString(K));
        proxy.setAttributeValue("M", "1.0");
        proxy.setAttributeValue("Address", ADDRESS);
    }

    @Test
    public void eventUpdatesValueWithKAndM() throws Exception {
        mockValueEvent(valueEvent, ADDRESS, (int)((VALUE - M)/K), 0, false);
        thermometer.receiveEvent(valueEvent);
        assertThat(proxy.getAttributeValue("Temperature"), is(String.format("%.1f", VALUE)));
    }

    @Test
    public void eventSetsBattLevelto10IfLow() throws Exception {
        mockValueEvent(valueEvent, ADDRESS, 0, 1, true);
        thermometer.receiveEvent(valueEvent);
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("10"));
    }

    @Test
    public void eventSetsBattLevelto100IfNotLow() throws Exception {
        mockValueEvent(valueEvent, ADDRESS, 0, 0, true);
        thermometer.receiveEvent(valueEvent);
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("100"));
    }

    @Test
    public void eventResetsBattLevelto100IfNotLow() throws Exception {
        mockValueEvent(valueEvent, ADDRESS, 0, 1, true);
        thermometer.receiveEvent(valueEvent);
        Event newEvent =  mock(Event.class);
        mockValueEvent(newEvent, ADDRESS, 0, 0, true);
        thermometer.receiveEvent(newEvent);
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("100"));
    }

    @Test
    public void eventResetsBattLevelto100IfNotIncluded() throws Exception {
        mockValueEvent(valueEvent, ADDRESS, 0, 1, true);
        thermometer.receiveEvent(valueEvent);
        Event newEvent =  mock(Event.class);
        mockValueEvent(newEvent, ADDRESS, 0, 0, false);
        thermometer.receiveEvent(newEvent);
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("100"));
    }

    @Test
    public void rawValueAttributeUpdatesValueWithKAndM() throws Exception {
        proxy.setAttributeValue("RawValue", Double.toString((VALUE - M)/K));
        assertThat(proxy.getAttributeValue("Temperature"), is(String.format("%.1f", VALUE)));
    }

    @Test
    public void emptyRawValueAttributeDoesNotUpdateValue() throws Exception {
        proxy.setAttributeValue("RawValue", Double.toString((VALUE - M)/K));
        proxy.setAttributeValue("RawValue", "");
        assertThat(proxy.getAttributeValue("Temperature"), is(String.format("%.1f", VALUE)));
    }

    private void mockValueEvent(Event event, String address, double value, int batteryLow, boolean includeBatt) {
        when(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)).thenReturn(Double.toString(value));
        when(event.isType("Temperature_Message")).thenReturn(true);
        when(event.getAttribute("Address")).thenReturn(address);
        when(event.getAttributeInt("LowBattery")).thenReturn(batteryLow);
        when(event.getAttribute("Direction")).thenReturn("In");
    }
}