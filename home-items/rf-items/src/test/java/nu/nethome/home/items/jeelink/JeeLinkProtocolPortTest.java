package nu.nethome.home.items.jeelink;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class JeeLinkProtocolPortTest {

    @Ignore
    @Test
    public void testReadingVersion() throws Exception {

        JeeLinkProtocolPort port = new JeeLinkProtocolPort("COM7", null);

        Thread.sleep(30000);

        port.readArduinoVersion();

        //port.getReportedVersion();

        port.close();

        Thread.sleep(5000);
    }

    @Ignore
    @Test
    public void testReadingVersion2() throws Exception {

        JeeLinkProtocolPort port = new JeeLinkProtocolPort("COM7", null);

        port.readArduinoVersion();

        Thread.sleep(5000);
    }
}
