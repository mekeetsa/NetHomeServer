/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
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

import nu.nethome.coders.decoders.*;
import nu.nethome.coders.encoders.Encoders;
import nu.nethome.coders.encoders.ShortBeepEncoder;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;
import nu.nethome.util.ps.impl.ProtocolDecoderGroup;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * CULTransceiver ...
 *
 * @author Stefan
 */
@Plugin
@HomeItemType("Hardware")
public class JeeLink extends HomeItemAdapter implements HomeItem, ProtocolDecoderSink {

    private final String MODEL1 = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"JeeLink\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getConnected\" Default=\"true\" />" );

    private final String MODEL2 = ("  <Attribute Name=\"FirmwareVersion\" Type=\"String\" Get=\"getFirmwareVersion\"  />"
            + "  <Attribute Name=\"SendCount\" Type=\"String\" Get=\"getSendCount\"  />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"PlayTestBeep\"		Method=\"playTestBeep\" />"
            + "  <Attribute Name=\"TestBeepFrequency\" Type=\"String\" Get=\"getTestBeepFrequency\" 	Set=\"setTestBeepFrequency\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(JeeLink.class.getName());
    private ProtocolDecoderGroup protocolDecoders = new ProtocolDecoderGroup();

    // Public attributes
    private int testBeepFrequency = 2000;
    JeeLinkProtocolPort port;
    private long sendCount = 0;
    private float modulationFrequency = 0;
    private EncoderFactory factory;


    public JeeLink() {
        // Create the Protocol-Decoders and add them to the decoder group
        Collection<Class<? extends ProtocolDecoder>> allTypes = Decoders.getAllTypes();
        for (Class<? extends ProtocolDecoder> decoder : allTypes) {
            try {
                protocolDecoders.add(decoder.newInstance());
            } catch (InstantiationException|IllegalAccessException e) {
                // Ignore
            }
        }

        for (ProtocolDecoder decoder : protocolDecoders.getAllDecoders()) {
            decoder.setTarget(this);
        }

        // Create our CUL-Port and attach the decoders directly to it.
        port = new JeeLinkProtocolPort(protocolDecoders);
        port.setMode(1);

        // TODO This is a temporary fix...
        port.setSerialPort("COM12");
        factory = new EncoderFactory(Encoders.getAllTypes());
    }

    public boolean receiveEvent(Event event) {
        if (!event.getAttribute("Direction").equals("Out") || !port.isOpen()) {
            return false;
        }
        ProtocolEncoder foundEncoder = factory.getEncoder(event);
        if (foundEncoder != null) {
            try {
                Message parameters = factory.extractMessage(event);
                int repeat = calculateRepeat(event, foundEncoder);
                int modulationFrequency = calculateModulationFrequency(event, foundEncoder, parameters);
                setModulationFrequencyOnPort(modulationFrequency);
                port.playMessage(foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED), repeat, 0);
                setModulationFrequency(getModulationFrequency());
                sendCount++;
                return true;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            }
        }
        return false;
    }

    private int calculateModulationFrequency(Event event, ProtocolEncoder encoder, Message message) {
        if (event.getAttribute("Modulation").equals("On")) {
            return encoder.modulationFrequency(message);
        }
        return 0;
    }

    private int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return MODEL1 + getPortNameAttribute() + MODEL2;
    }

    private String getPortNameAttribute() {
        StringBuilder model = new StringBuilder();
        model.append("  <Attribute Name=\"SerialPort\" Type=\"StringList\" Get=\"getSerialPort\" Set=\"setSerialPort\" >");
        List<String> ports = port.listAvailablePortNames();
        model.append("<item>");
        model.append(port.getSerialPort());
        model.append("</item>");
        for (String port : ports) {
            model.append("<item>");
            model.append(port);
            model.append("</item>");
        }
        model.append("</Attribute>");
        return model.toString();
    }



    public void activate(HomeService server) {
        super.activate(server);
        factory.addEncoderTypes(server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class));
        port.open();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        if (port.isOpen()) {
            port.close();
        }
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        if (port.isOpen()) {
            port.close();
        }
        port.open();
    }

    /**
     * Play a short test beep. This is for test purposes
     */
    public void playTestBeep() {
        ShortBeepEncoder beep = new ShortBeepEncoder();
        beep.setFrequency(testBeepFrequency);
        beep.setDuration(0.05F);
        port.playMessage(beep.encode(), 10, 0);
        sendCount++;
    }

    /**
     * @return Returns the SerialPort.
     */
    public String getSerialPort() {
        return port.getSerialPort();
    }

    /**
     * @param SerialPort The SerialPort to set.
     */
    public void setSerialPort(String SerialPort) {
        port.setSerialPort(SerialPort);
        if (port.isOpen()) {
            port.close();
            port.open();
        }
    }

    /**
     * @return Returns the m_TestBeepFrequency.
     */
    public String getTestBeepFrequency() {
        return Integer.toString(testBeepFrequency);
    }

    /**
     * @param TestBeepFrequency The m_TestBeepFrequency to set.
     */
    public void setTestBeepFrequency(String TestBeepFrequency) {
        testBeepFrequency = Integer.parseInt(TestBeepFrequency);
    }

    public String getConnected() {
        return port.isOpen() ? "Connected" : "Not Connected";
    }

    public void parsedMessage(ProtocolMessage message) {
        // TODO Auto-generated method stub

    }

    public void partiallyParsedMessage(String protocol, int bits) {
        // TODO Auto-generated method stub

    }

    public void reportLevel(int level) {
        // TODO Auto-generated method stub

    }

    public String getModulationFrequency() {
        return Float.toString(modulationFrequency);
    }

    public void setModulationFrequency(String newModulationFrequency) {
        float frequency = Float.parseFloat(newModulationFrequency);
        if ((frequency >= 10000) && (frequency <= 100000)) {
            modulationFrequency = frequency;
        } else {
            modulationFrequency = 0;
        }
        setModulationFrequencyOnPort(modulationFrequency);
    }

    private void setModulationFrequencyOnPort(float frequency) {
        int modulationPeriod;
        if ((frequency > 0)) {
            modulationPeriod = (int) (1 / (frequency * 375E-9 * 2.0));
        } else {
            modulationPeriod = 0;
        }
        port.setModulationOnPeriod(modulationPeriod);
        port.setModulationOffPeriod(modulationPeriod);
    }

    public String getSendCount() {
        return Long.toString(sendCount);
    }

    public String getFirmwareVersion() {
        return port.getReportedVersion();
    }
}
