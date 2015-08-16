package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.Hex;
import nu.nethome.home.items.zwave.messages.DecoderException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class MultiInstanceAssociationTest {

    @Test
    public void decodeKnownTestData() throws Exception, DecoderException {
        MultiInstanceAssociation.Report report = new MultiInstanceAssociation.Report(Hex.hexStringToByteArray("8E0302050006000102"));
        assertThat(report.associationId, is(2));
        assertThat(report.maxAssociations, is(5));
        assertThat(report.nodes.length, is(2));
        assertThat(report.nodes[0], is(new AssociatedNode(6,0)));
        assertThat(report.nodes[1], is(new AssociatedNode(1,2)));
    }
}
