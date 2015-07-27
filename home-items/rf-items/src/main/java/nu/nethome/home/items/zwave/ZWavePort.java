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

    static final int SOF = 0x01;
    static final int ACK = 0x06;
    static final int NAK = 0x15;
    static final int CAN = 0x18;

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
            logger.info("Created port");
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

    private void receiveLoop() {
        String result = "";
        SerialPort localPort = serialPort;
        try {
            synchronizeCommunication();
        } catch (SerialPortException e) {
            logger.warning("Failed to write to serial port - exiting");
            return;
        }
        logger.info("Entering receive loop");
        while (localPort.isOpened() && isOpen) {
            try {
                logger.info("Starting read message");
                readMessage(localPort);
                logger.info("Have read message");
            } catch (SerialPortException e) {
                logger.info("Serial port exception");
                // Probably port is closed, ignore and will exit the while
            } catch (Exception e) {
                logger.info("General exception");
                // Problem in the decoders.
            }
        }
    }

    void readMessage(SerialPort localPort) throws SerialPortException, SerialPortTimeoutException {
        int frameByte = readByte(localPort);
        switch (frameByte) {
            case SOF:
                int messageLength;
                messageLength = readByte(localPort);
                byte[] message = localPort.readBytes(messageLength, 1000);
                processMessage(message);
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

    private void processMessage(byte[] message) throws SerialPortException {
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

    private int readByte(SerialPort localPort) throws SerialPortException {
        byte[] data = localPort.readBytes(1);
        return (int) data[0];
    }

    private boolean sendResponse(int message) throws SerialPortException {
        return serialPort.writeByte((byte) message);
    }
}
