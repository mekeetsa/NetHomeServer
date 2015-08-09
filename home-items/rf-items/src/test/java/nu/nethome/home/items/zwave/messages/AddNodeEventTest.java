package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.Hex;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class AddNodeEventTest {

    @Test
    public void canDecodeStartedEvent() throws Exception, DecoderException {
        AddNode.Event addNodeEvent = new AddNode.Event(new ByteArrayInputStream(Hex.hexStringToByteArray("004AFF010000")));
        assertThat(addNodeEvent.status, is(AddNode.Event.Status.LEARN_READY));
    }
}
