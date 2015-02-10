package nu.nethome.home.items.net.wemo.soap;

import org.junit.Test;

/**
 *
 */
public class WemoInsightSwitchClientTest {
    @Test
    public void testSetOn() throws Exception, WemoInsightSwitchClient.WemoException {
        WemoInsightSwitchClient wemoSwitch = new WemoInsightSwitchClient("http://192.168.1.17:49153");
//        wemoSwitch.setOnState(true);
//        Thread.sleep(1000);
//        wemoSwitch.setOnState(false);
    }
}
