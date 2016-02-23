package nu.nethome.home.impl;

import org.junit.Test;

public class BootWebServerTest {

    @Test
    public void testWebServer() throws Exception {
        final BootWebServer bootWebServer = new BootWebServer();
        bootWebServer.start(8020);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            bootWebServer.setMessage("Iteration " + i);
        }
        bootWebServer.stop();
    }
}
