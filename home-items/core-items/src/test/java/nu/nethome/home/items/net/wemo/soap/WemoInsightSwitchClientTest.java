package nu.nethome.home.items.net.wemo.soap;

import org.junit.Test;

/**
 *
 */
public class WemoInsightSwitchClientTest {
    @Test
    public void testSetOn() throws Exception, WemoInsightSwitchClient.WemoException {

        WemoInsightSwitchClient wemoSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");
        wemoSwitch.on();
        Thread.sleep(1000);
        wemoSwitch.off();
    }
}
