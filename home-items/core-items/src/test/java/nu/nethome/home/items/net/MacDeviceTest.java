package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.misc.ArpScanner;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MacDeviceTest {
    public static final String CORRECT_MAC = "e4:f4:c6:09:2c:26";
    public static final String WRONG_MAC = "dd:f4:c6:09:2c:26";
    private HomeService server;
    private InternalEvent sentEvent;
    private Event receivedEvent;
    private MacDevice macDevice;
    private LocalHomeItemProxy itemProxy;
    private HomeItemProxy fooItem;

    @Before
    public void setUp() throws Exception {
        macDevice = new MacDevice();
        itemProxy = new LocalHomeItemProxy(macDevice);
        itemProxy.setAttributeValue("Reply", "Reply message");
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
        receivedEvent = mock(Event.class);
        doReturn(ArpScanner.ARP_SCAN_MESSAGE).when(receivedEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        macDevice.activate(server);

        fooItem = mock(HomeItemProxy.class);
        doReturn(fooItem).when(server).openInstance("foo");
    }

    @Test
    public void canSetMacAddress() throws Exception {
        itemProxy.setAttributeValue("MacAddress", CORRECT_MAC);
        assertThat(itemProxy.getAttributeValue("MacAddress"), is(CORRECT_MAC));
    }

    @Test
    public void performsActionWhileNotPresentWhenEmpty() throws Exception {
        itemProxy.setAttributeValue("ActionWhileAbsent", "call,foo,fie");
        doReturn("").when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDevice.receiveEvent(receivedEvent);

        verify(fooItem).callAction("fie");
    }

    @Test
    public void performsActionWhileNotPresentWhenWrongMacs() throws Exception {
        itemProxy.setAttributeValue("ActionWhileAbsent", "call,foo,fie");
        doReturn(WRONG_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDevice.receiveEvent(receivedEvent);

        verify(fooItem).callAction("fie");
    }
}
