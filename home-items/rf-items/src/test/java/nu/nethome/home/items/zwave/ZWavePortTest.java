package nu.nethome.home.items.zwave;

import jssc.SerialPort;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * 0120F9819C1C012F
 */
public class ZWavePortTest {

    private ZWavePort zWavePort;

    class Receiver implements ZWavePort.Receiver {

        public byte[] message = new byte[1];

        @Override
        public void receiveMessage(byte[] message) {
            this.message = message;
        }

        @Override
        public void receiveFrameByte(byte frameByte) {
            message[0] = frameByte;
        }
    }

    private SerialPort port;
    private Receiver receiver;

    @Before
    public void setUp() throws Exception {
        port = mock(SerialPort.class);
        receiver = new Receiver();
        zWavePort = new ZWavePort("Name", receiver, port);
    }

    @Test
    public void canReceiveNAK() throws Exception {
        byte[] nak = {ZWavePort.NAK};
        doReturn(nak).when(port).readBytes(1);

        zWavePort.readMessage(port);

        assertThat(receiver.message[0], is((byte)ZWavePort.NAK));
    }

    @Test
    public void canReceiveAndAcknowledge10ByteMessage() throws Exception {
        byte[] sof = {ZWavePort.SOF};
        byte[] length = {10};
        when(port.readBytes(1)).thenReturn(sof, length);
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        when(port.readBytes(10, 1000)).thenReturn(data);

        zWavePort.readMessage(port);

        assertThat(receiver.message, is(data));
        verify(port).writeByte((byte) ZWavePort.ACK);
    }
}
