package nu.nethome.home.items.zwave.messages;

import nu.nethome.home.items.zwave.Hex;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class MemoryGetIdTest {

    @Test
    public void canDecodeKnownData() throws Exception, DecoderException {

        MemoryGetId.Response response = new MemoryGetId.Response(new ByteArrayInputStream(Hex.hexStringToByteArray("0120F9819C1C01")));

        assertThat(response.nodeId, is(1));
        assertThat(response.homeId, is(0xF9819C1C));
    }
}
