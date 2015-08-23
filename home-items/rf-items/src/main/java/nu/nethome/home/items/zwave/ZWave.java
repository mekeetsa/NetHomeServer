package nu.nethome.home.items.zwave;

import jssc.SerialPortException;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.jeelink.PortException;
import nu.nethome.home.items.zwave.messages.*;
import nu.nethome.util.plugin.Plugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Plugin
@HomeItemType(value = "Hardware")
public class ZWave extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWave\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"PortName\" Type=\"StringList\" Get=\"getPortName\" Set=\"setPortName\" >"
            + "    %s </Attribute>"
            + "  <Attribute Name=\"HomeId\" Type=\"String\" Get=\"getHomeId\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" />"
            + "  <Attribute Name=\"Node\" Type=\"String\" Get=\"getNode\" Set=\"setNode\" />"
            + "  <Attribute Name=\"Association\" Type=\"String\" Get=\"getAssociation\" Set=\"setAssociation\" />"
            + "  <Action Name=\"requestIdentity\" 	Method=\"requestIdentity\" Default=\"true\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"StartInclusion\"		Method=\"startInclusion\" />"
            + "  <Action Name=\"EndInclusion\"		Method=\"endInclusion\" />"
            + "  <Action Name=\"reportAssociations\"		Method=\"reportAssociations\" />"
            + "  <Action Name=\"getAssociation\"		Method=\"fetchAssociation\" />"
            + "</HomeItem> ");
    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";
    public static final String ZWAVE_EVENT_TYPE = "ZWave_Message";

    private static Logger logger = Logger.getLogger(ZWave.class.getName());

    private ZWavePort port;
    private String portName = "/dev/ttyAMA0";
    private int homeId = 0;
    private int nodeId = 0;
    private int node = 0;
    private int association = 0;

    public boolean receiveEvent(nu.nethome.home.system.Event event) {
        if (event.isType(ZWAVE_EVENT_TYPE) &&
                event.getAttribute("Direction").equals("Out") &&
                event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE).length() > 0 &&
                port != null && port.isOpen()) {
            byte[] message = Hex.hexStringToByteArray(event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            try {
                port.sendMessage(message);
            } catch (SerialPortException e) {
                logger.warning("Failed to send ZWave message: " + event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            }
            return true;
        }
        return false;
    }

    public String getModel() {
        return String.format(MODEL, getPortNames());
    }

    private String getPortNames() {
        StringBuilder model = new StringBuilder();
        List<String> ports = ZWavePort.listAvailablePortNames();
        model.append("<item>");
        model.append(portName);
        model.append("</item>");
        for (String port : ports) {
            model.append("<item>");
            model.append(port);
            model.append("</item>");
        }
        return model.toString();
    }


    @Override
    public void activate() {
        openPort();
    }

    private void openPort() {
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
            requestIdentity();
        } catch (PortException e) {
            logger.log(Level.WARNING, "Could not open ZWave port", e);
        }
    }

    public String requestIdentity() {
        try {
            port.sendMessage(new MemoryGetId.Request().encode());
        } catch (SerialPortException e) {
            logger.log(Level.WARNING, "Could not send ZWave initial message", e);
        }
        return "";
    }

    private void closePort() {
        logger.fine("Closing port");
        nodeId = 0;
        homeId = 0;
        if (port != null) {
            port.close();
            port = null;
        }
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        closePort();
        openPort();
    }

    public String startInclusion() {
        return sendRequest(new AddNode.Request(AddNode.Request.InclusionMode.ANY));
    }

    public String endInclusion() {
        return sendRequest(new AddNode.Request(AddNode.Request.InclusionMode.STOP));
    }

    public String fetchAssociation() {
        //return sendRequest(new ApplicationCommandRequest((byte) node, new Association.Get(association)));
        return "";
    }

    public String reportAssociations() {
        // return sendRequest(new ApplicationCommandRequest((byte) node, new Association.Report()));
        return "";
    }

    private String sendRequest(MessageAdaptor request) {
        try {
            if (port != null && port.isOpen()) {
                port.sendMessage(request.encode());
            }
        } catch (SerialPortException e) {
            logger.log(Level.WARNING, "Could not send ZWave message", e);
        }
        return "";
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
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, "FrameByte");
        event.setAttribute("Direction", "In");
        server.send(event);
    }

    private void receiveMessage(byte[] message) {
        String data = Hex.asHexString(message);
        logger.info(data);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int) message[1]) & 0xFF);
        event.setAttribute("Direction", "In");
        server.send(event);
        if (MessageAdaptor.decodeMessageId(message) == MemoryGetId.MEMORY_GET_ID) {
            try {
                MemoryGetId.Response memoryGetIdResponse = new MemoryGetId.Response(message);
                homeId = memoryGetIdResponse.homeId;
                nodeId = memoryGetIdResponse.nodeId;
            } catch (DecoderException e) {
                logger.warning("Could not parse ZWave response:" + e.getMessage());
            }
        }
        if (MessageAdaptor.decodeMessageId(message) == AddNode.REQUEST_ID) {
            try {
                AddNode.Event addNodeResponse = new AddNode.Event(message);
                logger.info(addNodeResponse.toString());
            } catch (DecoderException e) {
                logger.warning("Could not parse ZWave response:" + e.getMessage());
            }
        }
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String serialPort) {
        if (!portName.equals(serialPort)) {
            portName = serialPort;
            closePort();
            if (isActivated()) {
                openPort();
            }
        }
    }

    public String getState() {
        return port != null ? "Connected" : "Disconnected";
    }

    public String getHomeId() {
        return homeId != 0 ? Integer.toHexString(homeId) : "";
    }

    public String getNodeId() {
        return nodeId != 0 ? Integer.toString(nodeId) : "";
    }

    public String getNode() {
        return Integer.toString(node);
    }

    public void setNode(String node) {
        this.node = Integer.parseInt(node);
    }

    public String getAssociation() {
        return Integer.toString(association);
    }

    public void setAssociation(String association) {
        this.association = Integer.parseInt(association);
    }
}
