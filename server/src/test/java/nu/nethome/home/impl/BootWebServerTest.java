package nu.nethome.home.impl;

import org.junit.Ignore;
import org.junit.Test;

public class BootWebServerTest {

    @Ignore
    @Test
    public void testWebServer() throws Exception {
        final BootWebServer bootWebServer = new BootWebServer("Starting OpenNetHomeServer");
        bootWebServer.start(8020);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            bootWebServer.beginSection("Iteration " + i);
        }
        bootWebServer.stop();
    }
}
