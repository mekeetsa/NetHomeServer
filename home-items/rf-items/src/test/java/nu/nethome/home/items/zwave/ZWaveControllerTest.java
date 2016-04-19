package nu.nethome.home.items.zwave;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZWaveControllerTest {

    private ZWaveController zWaveController;
    private HomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        zWaveController = new ZWaveController();
        proxy = new LocalHomeItemProxy(zWaveController);
    }

    @Test
    public void getEmptyNodes() throws Exception {
        assertThat(proxy.getAttributeValue("Nodes"), is(""));
    }

    @Test
    public void getSetNodes() throws Exception {
        proxy.setAttributeValue("Nodes", "2:17,5:32");
        assertThat(proxy.getAttributeValue("Nodes"), is("2:17,5:32"));
    }
}
