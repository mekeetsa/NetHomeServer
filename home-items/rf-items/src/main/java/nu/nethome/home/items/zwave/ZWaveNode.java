package nu.nethome.home.items.zwave;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.*;
import nu.nethome.zwave.messages.commandclasses.ApplicationSpecificCommandClass;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.Message;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class ZWaveNode extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveNode\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" Init=\"setNodeId\" />"
            + "  <Attribute Name=\"Manufacturer\" Type=\"String\" Get=\"getManufacturer\" Init=\"setManufacturer\" />"
            + "  <Attribute Name=\"DeviceType\" Type=\"String\" Get=\"getDeviceType\" Init=\"setDeviceType\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" Init=\"setDeviceId\" />"
            + "  <Attribute Name=\"SupportedClasses\" Type=\"String\" Get=\"getSupportedCommandClasses\" Init=\"setSupportedCommandClasses\" />"
            + "  <Attribute Name=\"ControlledClasses\" Type=\"String\" Get=\"getControlledCommandClasses\" Init=\"setControlledCommandClasses\" />"
            + "</HomeItem> ");

    public static final String ZWAVE_NODE_REPORT = "ZWave_Node_Report";

    private int nodeId;
    private DiscoveryState state;
    private List<Byte> supportedCommandClasses = Collections.emptyList();
    private List<Byte> controlledCommandClasses = Collections.emptyList();
    private int manufacturer;
    private int deviceType;
    private int deviceId;
    private MultiMessageProcessor messageProcessor;
    private Timer timer;

    public ZWaveNode() {
        this.state = new Initial();
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addMessageProcessor(ApplicationUpdate.REQUEST_ID, Message.Type.REQUEST, new ApplicationUpdate.Event.Processor() {
            @Override
            protected ApplicationUpdate.Event process(ApplicationUpdate.Event command) throws DecoderException {
                state.applicationUpdate(command);
                return command;
            }
        });
        messageProcessor.addCommandProcessor(new ApplicationSpecificCommandClass.Report.Processor() {
            @Override
            protected ApplicationSpecificCommandClass.Report process(ApplicationSpecificCommandClass.Report command, CommandArgument node) throws DecoderException {
                state.applicationSpecificReport(command, node);
                return command;
            }
        });
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        state.activate();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("ZWave_Message") &&
                event.getAttribute("Direction").equals("In")) {
            try {
                messageProcessor.process(Hex.hexStringToByteArray(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)));
            } catch (DecoderException e) {
                // Ignore
            }
        } else if (event.isType("ReportItems") && state.getClass().equals(ActiveState.class)) {
            sendNodeReport();
            return true;
        }
        return false;
    }

    private void sendNodeReport() {
        Event nodeReport = server.createEvent(ZWAVE_NODE_REPORT, asStringList(supportedCommandClasses, true));
        nodeReport.setAttribute("NodeId", nodeId);
        nodeReport.setAttribute("Direction", "In");
        server.send(nodeReport);
    }

    public String getState() {
        return state.getStateString();
    }

    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(String nodeId) {
        this.nodeId = Integer.parseInt(nodeId);
    }

    private void setTimeout(long i) {
        timer = new Timer("ZWave node timeout, true");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                state.timeout();
            }
        }, i);
    }

    private void cancelTimeout() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setSupportedCommandClasses(String value) {
        supportedCommandClasses = fromStringList(value, 16);
    }

    public String getSupportedCommandClasses() {
        return asStringList(supportedCommandClasses, true);
    }

    private static String asStringList(List<Byte> commandClasses, boolean asHex) {
        String commandClassesString = "";
        String separator = "";
        for (byte commandClass : commandClasses) {
            int cc = ((int) commandClass) & 0xFF;
            commandClassesString += String.format(asHex ? "%s%02X" : "%s%d", separator, cc);
            separator = ",";
        }
        return commandClassesString;
    }

    private List<Byte> fromStringList(String list, int radix) {
        String separated[] = list.split(",");
        List<Byte> result = new ArrayList<>(separated.length);
        for (String aSeparated : separated) {
            if (!aSeparated.isEmpty()) {
                result.add((byte) Integer.parseInt(aSeparated, radix));
            }
        }
        return result;
    }

    public void setControlledCommandClasses(String value) {
        controlledCommandClasses = fromStringList(value, 16);
    }

    public String getControlledCommandClasses() {
        return asStringList(controlledCommandClasses, true);
    }

    public String getManufacturer() {
        return Integer.toHexString(manufacturer);
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = Integer.parseInt(manufacturer, 16);
    }

    public String getDeviceType() {
        return Integer.toHexString(deviceType);
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = Integer.parseInt(deviceType, 16);
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = Integer.parseInt(deviceId, 16);
    }

    public String getDeviceId() {
        return Integer.toHexString(deviceId);
    }

    /*
        Discovery state machine:

        Things to check:
         * Routing info (GetRoutingInfoMessageClass())
         * is the node failed? (IsFailedNodeMessageClass)
         * Request node info (RequestNodeInfoMessageClass)
         * If node supports Manufacturer specific command class, request it to get manufacturer info (manufacturerSpecific.getManufacturerSpecificMessage())
         * If node supports version command class, loop through all command classes and request version
         * Check version of interface (versionCommandClass.getVersionMessage())
         * If node supports MultiInstance, get all endpoints? (multiInstance.initEndpoints(stageAdvanced))

     */
    abstract class DiscoveryState {
        abstract public String getStateString();

        void activate() {
        }

        void timeout() {
        }

        public void applicationUpdate(ApplicationUpdate.Event event) {
        }

        public void applicationSpecificReport(ApplicationSpecificCommandClass.Report report, CommandArgument node) {
        }
    }

    private class Initial extends DiscoveryState {
        @Override
        public String getStateString() {
            return "Initializing";
        }

        @Override
        public void activate() {
            if (supportedCommandClasses.size() == 0) {
                state = new RequestNodeInfoState();
                state.activate();
            } else {
                state = new ActiveState();
            }
        }
    }

    private class RequestNodeInfoState extends DiscoveryState {
        private int retries = 0;

        @Override
        public String getStateString() {
            return "FindingNodeInfo " + retries;
        }

        @Override
        public void activate() {
            retries = 0;
            requestNodeInfo();
        }

        private void requestNodeInfo() {
            final RequestNodeInfo.Request request = new RequestNodeInfo.Request(nodeId);
            final Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
            event.setAttribute("Direction", "Out");
            server.send(event);
            setTimeout(5000);
        }

        @Override
        void timeout() {
            synchronized (this) {
                if (state == this) {
                    if (retries < 3) {
                        retries++;
                        requestNodeInfo();
                    }
                }
            }
        }

        @Override
        public void applicationUpdate(ApplicationUpdate.Event event) {
            synchronized (this) {
                if (event.nodeId == nodeId && event.updateState == ApplicationUpdate.NODE_INFO_RECEIVED) {
                    cancelTimeout();
                    supportedCommandClasses = asByteList(event.supportedCommandClasses);
                    controlledCommandClasses = asByteList(event.controlledCommandClasses);
                    if (supportedCommandClasses.contains(new Byte((byte) ApplicationSpecificCommandClass.COMMAND_CLASS))) {
                        state = new RequestApplicationSpecificInfoState();
                        state.activate();
                    } else {
                        state = new ActiveState();
                    }
                }
            }
        }
    }

    private List<Byte> asByteList(byte[] byteList) {
        final ArrayList<Byte> bytes = new ArrayList<>(byteList.length);
        for (byte b : byteList) {
            bytes.add(b);
        }
        return bytes;
    }

    private class RequestApplicationSpecificInfoState extends DiscoveryState {
        private int retries = 0;

        @Override
        public String getStateString() {
            return "FindingApplicationInfo " + retries;
        }

        @Override
        public void activate() {
            retries = 0;
            requestApplicationInfo();
        }

        private void requestApplicationInfo() {
            final ApplicationSpecificCommandClass.Get get = new ApplicationSpecificCommandClass.Get();
            final SendData.Request request = new SendData.Request((byte) nodeId, get);
            Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
            event.setAttribute("Direction", "Out");
            server.send(event);
            setTimeout(4000);
        }

        @Override
        void timeout() {
            synchronized (this) {
                if (state == this) {
                    if (retries >= 3) {
                        state = new IncompleteState();
                    } else {
                        retries++;
                        requestApplicationInfo();
                    }
                }
            }
        }

        @Override
        public void applicationSpecificReport(ApplicationSpecificCommandClass.Report report, CommandArgument node) {
            if (node.sourceNode == nodeId) {
                manufacturer = report.manufacturer;
                deviceType = report.deviceType;
                deviceId = report.deviceId;
                state = new ActiveState();
            }
        }
    }

    private class ActiveState extends DiscoveryState {
        @Override
        public String getStateString() {
            return "Active";
        }
    }

    private class IncompleteState extends DiscoveryState {
        @Override
        public String getStateString() {
            return "Incomplete";
        }
    }
}
