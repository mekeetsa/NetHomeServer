package nu.nethome.home.items.net.wemo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class InsightStateTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void canDecodeState() throws Exception, WemoException {
        InsightState insightState = new InsightState("8|1423995823|0|0|0|1185615|0|0|0|0.000000|8000");
        assertThat(insightState.getState(), is(8));
    }
}
