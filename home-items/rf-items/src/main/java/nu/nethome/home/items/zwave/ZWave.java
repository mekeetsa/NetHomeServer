package nu.nethome.home.items.zwave;

import jssc.SerialPortException;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.jeelink.PortException;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Plugin
@HomeItemType(value = "Hardware")
public class ZWave extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWave\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"PortName\" Type=\"String\" Get=\"getPortName\" Set=\"setPortName\" />"
            + "  <Attribute Name=\"HomeId\" Type=\"String\" Get=\"getHomeId\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" />"
            + "  <Action Name=\"requestIdentity\" 	Method=\"requestIdentity\" Default=\"true\" />"
            + "</HomeItem> ");
    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";

    private static Logger logger = Logger.getLogger(ZWave.class.getName());

    private ZWavePort port;
    private String portName = "/dev/ttyAMA0";
    private int homeId = 0;
    private int nodeId = 0;

    public boolean receiveEvent(Event event) {
        return false;
    }

    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        try {
            port = new ZWavePort(portName, new ZWavePort.Receiver() {
                @Override
                public void receiveMessage(byte[] message) {
                    logger.fine("Receiving Message");
                    ZWave.this.receiveMessage(message);
                }

                @Override
                public void receiveFrameByte(byte frameByte) {
                    logger.fine("Receiving byte Message");
                    ZWave.this.receiveFrameByte(frameByte);
                }
            });
            logger.fine("Created port");
        } catch (PortException e) {
            logger.log(Level.WARNING, "Could not open ZWave port", e);
        }
    }

    public String requestIdentity()  {
        try {
            port.sendMessage(new MemoryGetIdRequest().encode());
        } catch (SerialPortException e) {
            logger.log(Level.WARNING, "Could not send ZWave initial message", e);
        }
        return "";
    }

    private void closePort() {
        logger.fine("Closing port");
        if (port != null) {
            port.close();
            port = null;
        }
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        closePort();
    }

    private void receiveFrameByte(byte frameByte) {
        String data = String.format("%02X", frameByte);
        logger.info(data);
        Event event = server.createEvent("ZWave_Message", data);
        event.setAttribute(ZWAVE_TYPE, "FrameByte");
        event.setAttribute("Direction", "In");
        server.send(event);
    }

    private void receiveMessage(byte[] message) {
        String data = Hex.asHexString(message);
        logger.info(data);
        Event event = server.createEvent("ZWave_Message", data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int)message[1]) & 0xFF);
        event.setAttribute("Direction", "In");
        server.send(event);
        if (Response.decodeRequestId(message) == MemoryGetIdRequest.MemoryGetId) {
            try {
                MemoryGetIdResponse memoryGetIdResponse = new MemoryGetIdResponse(message);
                homeId = memoryGetIdResponse.homeId;
                nodeId = memoryGetIdResponse.nodeId;
            } catch (Response.DecoderException e) {
                logger.warning("Could not parse ZWave response:" + e.getMessage());
            }
        }
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getState() {
        return port != null ? "Connected" : "Disconnected";
    }

    public String getHomeId() {
        return Integer.toString(homeId);
    }

    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
}
