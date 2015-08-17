package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.Hex;
import nu.nethome.home.items.zwave.messages.DecoderException;
import nu.nethome.home.items.zwave.messages.SendData;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MultiInstanceAssociationTest {

    public static final int TRANSMIT_OPTIONS = SendData.TRANSMIT_OPTION_ACK |
            SendData.TRANSMIT_OPTION_AUTO_ROUTE | SendData.TRANSMIT_OPTION_EXPLORE;

    @Test
    public void decodeKnownTestData() throws Exception, DecoderException {
        MultiInstanceAssociation.Report report = new MultiInstanceAssociation.Report(Hex.hexStringToByteArray("8E0302050006000102"));
        assertThat(report.group, is(2));
        assertThat(report.maxAssociations, is(5));
        assertThat(report.nodes.length, is(2));
        assertThat(report.nodes[0], is(new AssociatedNode(6)));
        assertThat(report.nodes[1], is(new AssociatedNode(1,2)));
    }

    @Test
    public void decodeKnownTestData15() throws Exception, DecoderException {
        MultiInstanceAssociation.Report report = new MultiInstanceAssociation.Report(Hex.hexStringToByteArray("8E03030A00000105"));
        assertThat(report.group, is(3));
        assertThat(report.maxAssociations, is(10));
        assertThat(report.nodes.length, is(1));
        assertThat(report.nodes[0], is(new AssociatedNode(1,5)));
    }

    @Test
    public void setNode() throws Exception {
        MultiInstanceAssociation.Set set = new MultiInstanceAssociation.Set(2, Arrays.asList(new AssociatedNode(1, 9), new AssociatedNode(7)));
        SendData.Request request = new SendData.Request((byte) 2, set, TRANSMIT_OPTIONS);
        String result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
    }

    @Test
    public void removeNode() throws Exception {
        MultiInstanceAssociation.Remove remove = new MultiInstanceAssociation.Remove(2, Arrays.asList(new AssociatedNode(1, 9), new AssociatedNode(7)));
        SendData.Request request = new SendData.Request((byte) 2, remove, TRANSMIT_OPTIONS);
        String result = Hex.asHexString(request.encode());
        System.out.println("event,ZWave_Message,Direction,Out,Value," + result);
    }
}
