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

import java.util.Timer;
import java.util.TimerTask;

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
            + "  <Attribute Name=\"SupportedCommandClasses\" Type=\"String\" Get=\"getSupportedCommandClasses\" Init=\"setSupportedCommandClasses\" />"
            + "  <Attribute Name=\"ControlledCommandClasses\" Type=\"String\" Get=\"getControlledCommandClasses\" Init=\"setControlledCommandClasses\" />"
            + "</HomeItem> ");

    private int nodeId;
    private NodeState state;
    private byte[] supportedCommandClasses = new byte[0];
    private byte[] controlledCommandClasses = new byte[0];
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
        messageProcessor.addCommandProcessor(new ApplicationSpecificCommandClass.Report.Processor(){
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
        }
        return false;
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

    // TODO: Set supported classes
    public String getSupportedCommandClasses() {
        return asStringList(supportedCommandClasses);
    }

    private static String asStringList(byte[] supportedCommandClasses1) {
        String commandClassesString = "";
        String separator = "";
        for (byte commandClass : supportedCommandClasses1) {
            int cc = ((int) commandClass) & 0xFF;
            commandClassesString += String.format("%s%d", separator, cc);
            separator = ",";
        }
        return commandClassesString;
    }

    public String getControlledCommandClasses() {
        return asStringList(controlledCommandClasses);
    }

    public String getManufacturer() {
        return Integer.toHexString(manufacturer);
    }

    public String getDeviceType() {
        return Integer.toHexString(deviceType);
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

    abstract class NodeState {
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

    private class Initial extends NodeState {
        @Override
        public String getStateString() {
            return "Initializing";
        }

        @Override
        public void activate() {
            if (supportedCommandClasses.length == 0) {
                state = new RequestNodeInfoState();
                state.activate();
            }
        }
    }

    private class RequestNodeInfoState extends NodeState {
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
                    supportedCommandClasses = event.supportedCommandClasses;
                    controlledCommandClasses = event.controlledCommandClasses;
                    state = new RequestApplicationSpecificInfoState();
                    state.activate();
                }
            }
        }
    }

    private class RequestApplicationSpecificInfoState extends NodeState {
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

    private class ActiveState extends NodeState {
        @Override
        public String getStateString() {
            return "Active";
        }
    }

    private class IncompleteState extends NodeState {
        @Override
        public String getStateString() {
            return "Incomplete";
        }
    }
}
