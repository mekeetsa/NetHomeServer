package nu.nethome.home.items.zwave;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import nu.nethome.home.items.jeelink.PortException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class ZWavePort {
    public static interface Receiver {
        void receiveMessage(byte[] message);

        void receiveFrameByte(byte frameByte);
    }

    static final byte SOF = 0x01;
    static final byte ACK = 0x06;
    static final byte NAK = 0x15;
    static final byte CAN = 0x18;

    private static Logger logger = Logger.getLogger(ZWavePort.class.getName());

    String portName = "/dev/ttyAMA0";
    private Receiver receiver;
    protected SerialPort serialPort;
    protected volatile boolean isOpen = false;
    private List<String> portList;

    public ZWavePort(String portName, Receiver receiver) throws PortException {
        this.portName = portName;
        this.receiver = receiver;
        serialPort = new SerialPort(this.portName);
        open();
    }

    /**
     * Create for test
     *
     * @param portName
     * @param receiver
     * @param port
     * @throws PortException
     */
    ZWavePort(String portName, Receiver receiver, SerialPort port) throws PortException {
        this.portName = portName;
        this.receiver = receiver;
        this.serialPort = port;
    }

    private void open() throws PortException {
        portList = Arrays.asList(SerialPortList.getPortNames());
        if (!portList.contains(portName)) {
            throw new PortException("Port " + portName + " not Found");
        }
        try {
            serialPort.openPort();
            if (!serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)) {
                throw new PortException("Could not set serial port parameters");
            }
        } catch (SerialPortException e) {
            throw new PortException("Could not open port " + portName, e);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveLoop();
            }
        }, "Port receive thread").start();
        isOpen = true;
    }

    public void close() {
        isOpen = false;
        if (serialPort != null) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                // Ignore
            }
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void sendMessage(byte[] message) throws SerialPortException {
        serialPort.writeByte(SOF);
        byte messageLength = (byte) (message.length + 1);
        serialPort.writeByte(messageLength);
        serialPort.writeBytes(message);
        serialPort.writeByte(calculateChecksum(message, messageLength));
    }

    private static byte calculateChecksum(byte[] buffer, byte messageLength) {
        byte checkSum = -1;
        checkSum ^= messageLength;
        for (byte messageByte : buffer) {
            checkSum ^= messageByte;
        }
        return checkSum;
    }

    private void receiveLoop() {
        SerialPort localPort = serialPort;
        try {
            synchronizeCommunication();
        } catch (SerialPortException e) {
            logger.warning("ZWave port Failed to write to serial port - exiting");
            return;
        }
        while (localPort.isOpened() && isOpen) {
            try {
                logger.fine("Starting read message");
                readMessage(localPort);
                logger.fine("Have read message");
            } catch (SerialPortException e) {
                logger.fine("Serial port exception");
                // Probably port is closed, ignore and will exit the while
            } catch (Exception e) {
                logger.fine("General exception");
            }
        }
    }

    void readMessage(SerialPort localPort) throws SerialPortException, SerialPortTimeoutException {
        int frameByte = readByte(localPort, 3000);
        switch (frameByte) {
            case SOF:
                int messageLength;
                messageLength = readByte(localPort, 20);
                byte[] message = localPort.readBytes(messageLength - 1, 1000);
                int checksum = readByte(localPort, 20);
                processMessage(message, checksum);
                break;
            case ACK:
            case NAK:
            case CAN:
                processMessage(frameByte);
                break;
            default:
                synchronizeCommunication();
                break;
        }
    }

    private void processMessage(byte[] message, int checksum) throws SerialPortException {
        // NYI Verify checksum
        sendResponse(ACK);
        receiver.receiveMessage(message);
    }

    private void processMessage(int frameByte) {
        receiver.receiveFrameByte((byte) frameByte);
    }

    private void synchronizeCommunication() throws SerialPortException {
        sendResponse(NAK);
    }

    private int readByte(SerialPort localPort, int timeout) throws SerialPortException, SerialPortTimeoutException {
        byte[] data = localPort.readBytes(1, timeout);
        return (int) data[0];
    }

    private boolean sendResponse(int message) throws SerialPortException {
        return serialPort.writeByte((byte) message);
    }
}
