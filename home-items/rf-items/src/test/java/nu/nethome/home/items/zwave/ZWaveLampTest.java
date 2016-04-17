package nu.nethome.home.items.zwave;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.HomeServer;
import nu.nethome.home.items.pronto.ProntoDevice;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ZWaveLampTest {
    private ZWaveLamp zWaveLamp;

    @Mock private HomeService server;
    @Mock private HomeServer realServer;
    @Mock private Event sentEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(server.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        when(realServer.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        zWaveLamp = new ZWaveLamp();
        zWaveLamp.activate(server);
    }

    @Test
    public void hasValidModel() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(zWaveLamp.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }
}
