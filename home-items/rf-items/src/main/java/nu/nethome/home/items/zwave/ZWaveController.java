package nu.nethome.home.items.zwave;

import jssc.SerialPortException;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.*;
import nu.nethome.zwave.messages.AddNode;
import nu.nethome.zwave.messages.ApplicationCommand;
import nu.nethome.zwave.messages.GetInitData;
import nu.nethome.zwave.messages.MemoryGetId;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MessageAdaptor;
import nu.nethome.zwave.messages.framework.UndecodedMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class ZWaveController extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveController\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"PortName\" Type=\"StringList\" Get=\"getPortName\" Set=\"setPortName\" >"
            + "    %s </Attribute>"
            + "  <Attribute Name=\"PortAddress\" Type=\"String\" Get=\"getPortAddress\" Set=\"setPortAddress\" />"
            + "  <Attribute Name=\"HomeId\" Type=\"String\" Get=\"getHomeId\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" />"
            + "  <Attribute Name=\"Nodes\" Type=\"String\" Get=\"getNodes\" />"
            + "  <Action Name=\"RequestIdentity\" 	Method=\"requestIdentity\" Default=\"true\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"StartInclusion\"		Method=\"startInclusion\" />"
            + "  <Action Name=\"EndInclusion\"		Method=\"endInclusion\" />"
            + "</HomeItem> ");
    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";
    public static final String ZWAVE_EVENT_TYPE = "ZWave_Message";
    public static final String ZWAVE_COMMAND_CLASS = "ZWave.CommandClass";
    public static final String ZWAVE_COMMAND = "ZWave.Command";
    public static final String ZWAVE_NODE = "ZWave.Node";

    private static Logger logger = Logger.getLogger(ZWaveController.class.getName());

    private ZWavePort port;
    private String portName = "/dev/ttyAMA0";
    private String portAddress = "";
    private long homeId = 0;
    private int nodeId = 0;
    private List<Integer> nodes = Collections.emptyList();

    public boolean receiveEvent(nu.nethome.home.system.Event event) {
        if (event.isType(ZWAVE_EVENT_TYPE) &&
                event.getAttribute("Direction").equals("Out") &&
                event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE).length() > 0 &&
                port != null && port.isOpen()) {
            byte[] message = Hex.hexStringToByteArray(event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            try {
                port.sendMessage(message);
            } catch (PortException e) {
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
        List<String> ports = ZWaveRawSerialPort.listAvailablePortNames();
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
            if (portAddress.isEmpty()) {
            port = new ZWaveSerialPort(portName);
            } else {
                final String[] addressParts = portAddress.split(":");
                port = new ZWaveNetHomePort(addressParts[0], Integer.parseInt(addressParts[1]));
            }
            port.setReceiver(new MessageProcessor() {
                @Override
                public UndecodedMessage.Message process(byte[] message) {
                    logger.fine("Receiving Message");
                    ZWaveController.this.receiveMessage(message);
                    return null;
                }
            });
            logger.fine("Created port");
            requestIdentity();
            requestNodeInfo();
        } catch (PortException|IOException e) {
            logger.log(Level.WARNING, "Could not open ZWave port", e);
        }
    }

    public String requestIdentity() {
        try {
            port.sendMessage(new MemoryGetId.Request());
        } catch (PortException e) {
            logger.log(Level.WARNING, "Could not send ZWave initial message", e);
        }
        return "";
    }

    public String requestNodeInfo() {
        try {
            port.sendMessage(new GetInitData.Request());
        } catch (PortException e) {
            logger.log(Level.WARNING, "Could not send ZWave initial message", e);
        }
        return "";
    }

    private void closePort() {
        logger.fine("Closing port");
        nodeId = 0;
        homeId = 0;
        if (port != null) {
            try {
                port.close();
            } catch (PortException e) {
                // Just ignore...
            }
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

    private String sendRequest(MessageAdaptor request) {
        try {
            if (port != null && port.isOpen()) {
                port.sendMessage(request.encode());
            }
        } catch (PortException e) {
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
        try {
            sendMessageAsEvent(message);
            processMessage(message);
        } catch (DecoderException | IOException e) {
            logger.warning("Could not parse ZWave response:" + e.getMessage());
        }
    }

    private void sendMessageAsEvent(byte[] message) throws IOException, DecoderException {
        String data = Hex.asHexString(message);
        logger.info(data);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int) message[1]) & 0xFF);
        event.setAttribute("Direction", "In");
        if (MessageAdaptor.decodeMessageId(message).messageId == ApplicationCommand.REQUEST_ID) {
            ApplicationCommand.Request request = new ApplicationCommand.Request(message);
            event.setAttribute(ZWAVE_NODE, request.node);
            Command command = request.command;
            if (command.getCommandClass() == MultiInstanceCommandClass.COMMAND_CLASS && command.getCommand() == MultiInstanceCommandClass.ENCAP_V2) {
                MultiInstanceCommandClass.EncapsulationV2 encap = new MultiInstanceCommandClass.EncapsulationV2(command.encode());
                event.setAttribute("ZWave.Endpoint", encap.instance);
                command = encap.command;
            }
            event.setAttribute(ZWAVE_COMMAND_CLASS, command.getCommandClass());
            event.setAttribute(ZWAVE_COMMAND, command.getCommand());
        }
        server.send(event);
    }

    private void processMessage(byte[] message) throws DecoderException {
        final int messageId = MessageAdaptor.decodeMessageId(message).messageId;
        if (messageId == MemoryGetId.MEMORY_GET_ID) {
            MemoryGetId.Response memoryGetIdResponse = new MemoryGetId.Response(message);
            homeId = memoryGetIdResponse.homeId;
            nodeId = memoryGetIdResponse.nodeId;
        } else if (messageId == GetInitData.REQUEST_ID) {
            GetInitData.Response response = new GetInitData.Response(message);
            nodes = new ArrayList<>(response.nodes);
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
        if (port == null) {
            return "Disconnected";
        } else if (homeId == 0) {
            return "Connecting";
        } else {
            return "Connected";
        }
    }

    public String getHomeId() {
        return homeId != 0 ? Long.toHexString(homeId) : "";
    }

    public String getNodeId() {
        return homeId != 0 ? Integer.toString(nodeId) : "";
    }

    public String getNodes() {
        String result = "";
        String separator = "";
        for (Integer node : nodes) {
            if (!node.equals(1)) {
                result += separator;
                result += Integer.toString(node);
                separator = ",";
            }
        }
        return result;
    }

    public String getPortAddress() {
        return portAddress;
    }

    public void setPortAddress(String portAddress) {
        this.portAddress = portAddress;
    }
}
