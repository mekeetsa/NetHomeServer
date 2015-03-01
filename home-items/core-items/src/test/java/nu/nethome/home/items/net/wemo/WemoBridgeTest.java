package nu.nethome.home.items.net.wemo;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 */
public class WemoBridgeTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void modelIsValidXML() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(new WemoBridge().getModel().getBytes());
        InputSource source = new InputSource(byteStream);

        parser.parse(source);
    }
}
