package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.Hex;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class AddNodeEventTest {

    @Test
    public void canDecodeStartedEvent() throws Exception, Event.DecoderException {
        AddNodeEvent addNodeEvent = new AddNodeEvent(Hex.hexStringToByteArray("004AFF010000"));
        assertThat(addNodeEvent.status, is(AddNodeEvent.Status.LEARN_READY));
    }
}
