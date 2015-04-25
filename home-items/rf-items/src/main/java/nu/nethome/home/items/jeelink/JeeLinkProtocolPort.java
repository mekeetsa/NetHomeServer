/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.jeelink;

import gnu.io.*;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.impl.PulseProtocolPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * The JeeLinkProtocolPort interfaces with an USB radio transceiver and sends and receives data
 * in form of pulse trains to/from the transceiver. The JeeLink transceiver can be found at:
 * {@see http://jeelink.com}. Special firmware from NetHome is needed on the JeeLink.
 *
 * @author Stefan
 */
public class JeeLinkProtocolPort implements SerialPortEventListener, Runnable, PulseProtocolPort {

    private static final int READ_BUFFER_SIZE = 40;
    protected int m_ReadBufferPointer = 0;
    String m_ComPort = "COM4";
    static Enumeration<CommPortIdentifier> portList;
    protected InputStream inputStream;

    protected OutputStream outStream;
    protected SerialPort m_SerialPort;
    protected CommPortIdentifier portId = null;
    protected boolean m_IsOpen = false;
    protected byte[] m_ReadBuffer = new byte[READ_BUFFER_SIZE];
    private ProtocolDecoder m_Decoder;
    private char m_LastCommand = 'x';
    private double m_AddForward = 0;
    private int m_Dupicates;
    private double m_PulseLengthCompensation = 0;
    private int m_Spikes = 0;
    private int m_Mode = 0;
    private boolean asynchronousRead = true;
    private String reportedVersion = "";

    private int m_ModulationOnPeriod = 0;
    private int m_ModulationOffPeriod = 0;

    public JeeLinkProtocolPort(ProtocolDecoder decoder) {
        m_Decoder = decoder;
        // In order for RxTx to recognize a serial port on Linux, we have
        // to add this system property. We make it possible to override by checking if the
        // property has already been set.
        if ((System.getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) &&
                (System.getProperty("gnu.io.rxtx.SerialPorts") == null)) {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:" + "" +
                    "/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyUSB2:" +
                    "/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2");
        }
    }

    public int open() {
        portList = CommPortIdentifier.getPortIdentifiers();

        boolean foundPort = false;
        
        /* Find the configured serial port */
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(m_ComPort)) {
                    // Ok, found
                    foundPort = true;
                    break;
                }
            }
        }
        if (!foundPort) {
            System.out.print("Failed to find serial Port: " + m_ComPort);
            return 1;
        }
        
        /* Try to open the serial port */
        try {
            m_SerialPort = (SerialPort) portId.open("SNAPPort", 2000);
        } catch (PortInUseException e) {
            System.out.print("COM Port " + m_ComPort + " is already in use");
            return 2;
        }
        try {
            inputStream = m_SerialPort.getInputStream();
            outStream = m_SerialPort.getOutputStream();
        } catch (IOException e) {
            System.out.print("COM Port " + m_ComPort + " could not be read " + e);
            return 3;
        }
        try {
            m_SerialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            System.out.print("COM Port " + m_ComPort + " has too many listeners" + e);
            return 4;
        }
        m_SerialPort.notifyOnDataAvailable(true);
        m_IsOpen = true;
        return 0;
    }

    public void close() {
        m_IsOpen = false;
        if (m_SerialPort != null) {
            m_SerialPort.close();
            m_SerialPort = null;
        }
    }

    public boolean isOpen() {
        return m_IsOpen;
    }


    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            return;
        }
    }

    public synchronized void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                if (asynchronousRead) {
                    try {
                        byte[] readBuffer = new byte[400];

                        while (inputStream.available() > 0) {
                            int numBytes = inputStream.read(readBuffer);
                            for (int i = 0; i < numBytes; i++) {
                                m_ReadBuffer[m_ReadBufferPointer++] = readBuffer[i];
                                if ((readBuffer[i] == 0x0A) || (m_ReadBufferPointer == READ_BUFFER_SIZE - 1)) {
                                    m_ReadBuffer[m_ReadBufferPointer] = 0;
                                    String result = new String(m_ReadBuffer, 0, m_ReadBufferPointer - 2);
                                    //System.out.println(result);
                                    if (m_ReadBufferPointer == READ_BUFFER_SIZE - 1) {
                                        System.out.println("Overflow!");
                                    }
                                    m_ReadBufferPointer = 0;
                                    try {
                                        analyzeReceivedCommand(result);
                                    } catch (Exception o) {
                                        // Problem down in the decoders!
                                        o.printStackTrace();
                                    }
                                }
                                // NYI - Process received bytes
                            }
                        }
                    } catch (IOException e) {
                        System.out.print("Error reading data from serial port " + e);
                    }
                    break;
                }
        }
    }

    /**
     * Analyze a command/event received from the device
     *
     * @param commandString Command string to analyze
     */
    private void analyzeReceivedCommand(String commandString) {
        if (commandString.length() < 1) return; // Make sure string is not empty
        char command = commandString.charAt(0);

        // Check if it is a version report
        if (command == 'V' && commandString.length() > 2) {
            reportedVersion = commandString.substring(2);
            return;
        }

        // Check if it is acknowledgment of command
        if (command == 'o') {
            acknowledgeCommand(commandString);
            return;
        }

        // Check if this is valid pulse data
        if (((command != 'm') && (command != 's')) || (commandString.length() != 5)) {
            System.out.println("Error - unknown command: " + commandString);
            sendQueryVersionCommand();
            return;
        }

        double pulseLength = Integer.parseInt(commandString.substring(1), 16);
        // System.out.println(Character.toString(command) + Double.toString(pulseLength) + " us");

        // Temporary - currently device signals overflow with this specific pulse value
        if (pulseLength == 32767) {
            System.out.println("Error - overflow in device");
            return;
        }

        // Detect two pulses of same type in a row. This is probably due to a very short "ringing" spike
        // after a transition, so the duplicate vale is added to the next pulse instead.
        if (command == m_LastCommand) {
            m_Dupicates++;
            System.out.println("Error - duplicate command " + Integer.toString(m_Dupicates));
            m_AddForward = pulseLength;
            return;
        }

        pulseLength += m_AddForward;
        m_AddForward = 0.0;
        if (pulseLength > 33000) {
            System.out.println("Error - Too long pulse");
        }

        if (pulseLength < 100.0) {
            m_Spikes++;
            System.out.println("Error - Spike" + Double.toString(pulseLength) + "(" + Integer.toHexString(m_Spikes) + ")");
        }

        parsePulse(pulseLength, command == 'm');
        m_LastCommand = command;
    }

    /**
     * Received ack on a command. Currently does nothing
     *
     * @param commandString
     */
    private void acknowledgeCommand(String commandString) {

    }

    /**
     * Send a processed pulse to the decoders
     *
     * @param pulseLength
     * @param isMark
     */
    private void parsePulse(double pulseLength, boolean isMark) {

        pulseLength += isMark ? m_PulseLengthCompensation : -m_PulseLengthCompensation;

        // Give the pulse to the decoder
        m_Decoder.parse(pulseLength, isMark);
    }

    public String[] getPortNames() {
        ArrayList<String> result = new ArrayList<String>();
        /* Find the serial ports */
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                result.add(portId.getName());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public String getSerialPort() {
        return m_ComPort;
    }

    public void setSerialPort(String serialPort) {
        m_ComPort = serialPort;
    }

    /**
     * Transmit the list of pulses (in microseconds) via the CUL device
     *
     * @param message      List of pulse lengths in microseconds, beginning with a mark pulse
     * @param repeat       Number of times to repeat message
     * @param repeatOffset Number pulses into the message the repeat sequence should begin
     * @return True if successful
     */
    public boolean playMessage(int message[], int repeat, int repeatOffset) {

        sendResetTransmitBufferCommand();
        // Loop through the flanks in the message
        for (int i = 0; i < message.length; i++) {
            int mark = message[i++];
            // Fill with 0 if uneven number of flanks
            int space = i < message.length ? message[i] : 0;
            sendAddFlankCommand(mark, space);
        }
        // Transmit the message, check if it should be modulated
        if ((m_ModulationOnPeriod > 0) || (repeatOffset > 0)) {
            // Yes, also write modulation parameters
            sendTransmitWithModulationCommand(repeat, repeatOffset, m_ModulationOnPeriod, m_ModulationOffPeriod);
        } else {
            sendTransmitCommand(repeat);

        }

        // NYI - Wait for confirmation

        return true;
    }

    private void sendTransmitWithModulationCommand(int repeat, int repeatOffset, int modulationOnPeriod, int modulationOffPeriod) {
        writeLine(String.format("S%02X%02X%02X%02X", repeat, modulationOnPeriod, modulationOffPeriod, repeatOffset));
    }

    private void sendTransmitCommand(int repeat) {
        writeLine(String.format("S%02X", repeat));
    }

    private void sendAddFlankCommand(int mark, int space) {
        String command = String.format("A%04X%04X", mark, space);
        writeLine(command);
    }

    private void sendResetTransmitBufferCommand() {
        writeLine("E");
    }

    private void sendQueryVersionCommand() {
        writeLine("V");
    }


    /**
     * Write a text line to the serial port. EOL-characters are added to the string
     *
     * @param line Line to be written
     */
    protected void writeLine(String line) {
        String totalLine = line + "\r\n";
        try {
            outStream.write(totalLine.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readArduinoVersion() throws IOException {
        int data = inputStream.read();
        while (inputStream.available() > 0) {
            data = inputStream.read();
        }

        System.out.println("syncing");
        for (int i = 0; i < 5; i++) {
            outStream.write(0x30);
            outStream.write(0x20);
            try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
        }

        System.out.println("waiting for response");
        int insync = inputStream.read();
        int ok = inputStream.read();
        if (insync == 0x14 && ok == 0x10) {
            System.out.println("insync");
        }

        System.out.println("reading major version");
        outStream.write(0x41);
        outStream.write(0x81);
        outStream.write(0x20);
        try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}

        System.out.println("waiting for response");
        insync = inputStream.read();
        int major = inputStream.read();
        ok = inputStream.read();
        if (insync == 0x14 && ok == 0x10) {
            System.out.println("insync");
        }

        System.out.println("reading minor version");
        outStream.write(0x41);
        outStream.write(0x82);
        outStream.write(0x20);
        try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}

        System.out.println("waiting for response");
        insync = inputStream.read();
        int minor = inputStream.read();
        ok = inputStream.read();
        if (insync == 0x14 && ok == 0x10) {
            System.out.println("insync");
        }

        System.out.println("version: " + major + "." + minor);

    }

    /**
     * See setModulationOnPeriod
     *
     * @return ModulationOnPeriod
     */
    public int getModulationOnPeriod() {
        return m_ModulationOnPeriod;
    }

    /**
     * The mark pulses may be modulated. This parameter specifies the on period
     * of this modulation. The time is specified in increments of 375nS. If on
     * and off periods are set to the same value the resulting modulation frequency
     * will be 10E9/(OnPeriod * 375 * 2).
     * Setting the period to 0 turns off the mark modulation.
     *
     * @param modulationOnPeriod 0 - 255.
     */
    public void setModulationOnPeriod(int modulationOnPeriod) {
        m_ModulationOnPeriod = modulationOnPeriod;
    }

    /**
     * See setModulationOffPeriod
     *
     * @return ModulationOffPeriod
     */
    public int getModulationOffPeriod() {
        return m_ModulationOffPeriod;
    }

    /**
     * Set the off period of mark pulse modulation. The time is specified in
     * increments of 375nS. See setModulationOnPeriod for details.
     *
     * @param modulationOffPeriod
     */
    public void setModulationOffPeriod(int modulationOffPeriod) {
        m_ModulationOffPeriod = modulationOffPeriod;
    }


    public int getMode() {
        return m_Mode;
    }

    /**
     * Temporary fix. I have to have different settings in the CUL-stick depending on
     * if I receive or transmit. 0 = Reception mode, 1 = transmission mode
     *
     * @param mode
     */
    public void setMode(int mode) {
        m_Mode = mode;
    }

    public String getReportedVersion() {
        return reportedVersion;
    }
}
