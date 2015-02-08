package nu.nethome.home.items.net.wemo.soap;

import org.junit.Test;

/**
 *
 */
public class WemoSwitchTest {
    @Test
    public void testSetOn() throws Exception {

        WemoSwitch wemoSwitch = new WemoSwitch();
        wemoSwitch.setOn(true);
        Thread.sleep(1000);
        wemoSwitch.setOn(false);
    }
}
