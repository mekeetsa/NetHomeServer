package nu.nethome.home.items.zwave;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.HomeServer;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.ApplicationCommand;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.SwitchBinaryCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ZWaveLampTest {
    private static final int NODE_ID = 17;
    private ZWaveLamp zWaveLamp;

    @Mock private HomeService server;
    @Mock private HomeServer realServer;
    @Mock private Event sentEvent;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(server.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        when(realServer.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        zWaveLamp = new ZWaveLamp();
        zWaveLamp.activate(server);
        proxy = new LocalHomeItemProxy(zWaveLamp);
        proxy.setAttributeValue("NodeId", Integer.toString(NODE_ID));
        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }

    @Test
    public void hasValidModel() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(zWaveLamp.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }

    @Test
    public void reportSetsState() throws Exception {
        final SwitchBinaryCommandClass.Report report = new SwitchBinaryCommandClass.Report(true);
        mockZWaveCommandEvent(sentEvent, NODE_ID, SwitchBinaryCommandClass.COMMAND_CLASS, SwitchBinaryCommandClass.SWITCH_BINARY_REPORT, report);
        zWaveLamp.receiveEvent(sentEvent);
        assertThat(proxy.getAttributeValue("State"), is("On"));
    }

    @Test
    public void reportSetsStateForSpecificInstance() throws Exception {
        proxy.setAttributeValue("Instance", "7");
        final SwitchBinaryCommandClass.Report report = new SwitchBinaryCommandClass.Report(true);
        final MultiInstanceCommandClass.EncapsulationV2 encapsulation = new MultiInstanceCommandClass.EncapsulationV2(7, report);
        mockZWaveCommandEvent(sentEvent, NODE_ID, SwitchBinaryCommandClass.COMMAND_CLASS, SwitchBinaryCommandClass.SWITCH_BINARY_REPORT, encapsulation);
        zWaveLamp.receiveEvent(sentEvent);
        assertThat(proxy.getAttributeValue("State"), is("On"));
    }

    @Test
    public void reportSetsNoStateForWrongSpecificInstance() throws Exception {
        final SwitchBinaryCommandClass.Report report = new SwitchBinaryCommandClass.Report(true);
        final MultiInstanceCommandClass.EncapsulationV2 encapsulation = new MultiInstanceCommandClass.EncapsulationV2(7, report);
        mockZWaveCommandEvent(sentEvent, NODE_ID, SwitchBinaryCommandClass.COMMAND_CLASS, SwitchBinaryCommandClass.SWITCH_BINARY_REPORT, encapsulation);
        zWaveLamp.receiveEvent(sentEvent);
        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }

    @Test
    public void reportSetsNoStateForWrongSpecificInstance2() throws Exception {
        proxy.setAttributeValue("Instance", "7");
        final SwitchBinaryCommandClass.Report report = new SwitchBinaryCommandClass.Report(true);
        mockZWaveCommandEvent(sentEvent, NODE_ID, SwitchBinaryCommandClass.COMMAND_CLASS, SwitchBinaryCommandClass.SWITCH_BINARY_REPORT, report);
        zWaveLamp.receiveEvent(sentEvent);
        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }

    private void mockZWaveCommandEvent(Event event, int nodeId, int commandClass, int commandId, Command command) {
        final ApplicationCommand.Request request = new ApplicationCommand.Request((byte) 17, command);
        when(event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE)).thenReturn(ZWaveController.ZWAVE_EVENT_TYPE);
        when(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)).thenReturn(Hex.asHexString(request.encode()));
        when(event.isType(ZWaveController.ZWAVE_EVENT_TYPE)).thenReturn(true);
        when(event.getAttribute(ZWaveController.ZWAVE_TYPE)).thenReturn("Request");
        when(event.getAttributeInt(ZWaveController.ZWAVE_MESSAGE_TYPE)).thenReturn(request.getRequestId());
        when(event.getAttribute("Direction")).thenReturn("In");
        when(event.getAttributeInt(ZWaveController.ZWAVE_NODE)).thenReturn(nodeId);
        when(event.getAttributeInt(ZWaveController.ZWAVE_COMMAND_CLASS)).thenReturn(commandClass);
        when(event.getAttributeInt(ZWaveController.ZWAVE_COMMAND)).thenReturn(commandId);
    }
}
