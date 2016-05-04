package nu.nethome.home.items.zwave;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.ApplicationCommand;
import nu.nethome.zwave.messages.commandclasses.BasicCommandClass;
import nu.nethome.zwave.messages.commandclasses.CentralSceneCommandClass;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MultiLevelSwitchCommandClass;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 */
@Plugin
@HomeItemType(value = "Controls", creationInfo = ZWaveRemapButton.CreationInfo.class)
public class ZWaveRemapButton extends HomeItemAdapter implements HomeItem {

    public static class CreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {"ZWave_Message"};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return isBasicSet(e);
        }

        @Override
        public String getCreationIdentification(Event e) {
            final int node = e.getAttributeInt(ZWaveController.ZWAVE_NODE);
            final String instance = e.getAttribute(ZWaveController.ZWAVE_ENDPOINT);
            return String.format("ZWave Button, node: %d%s%s ", node, instance.isEmpty() ? "" : ", instance: ", instance);
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" 	Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"InstanceId\" 	Type=\"String\" Get=\"getInstanceId\" 	Set=\"setInstanceId\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(ZWaveRemapButton.class.getName());

    private boolean isEnabled = true;
    private int nodeId;
    private Integer instanceId;
    private String onCommand = "";
    private String offCommand = "";
    CommandLineExecutor commandExecutor;
    private MultiMessageProcessor messageProcessor;

    public ZWaveRemapButton() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addCommandProcessor(new BasicCommandClass.Set.Processor() {
            @Override
            protected BasicCommandClass.Set process(BasicCommandClass.Set command, CommandArgument node) throws DecoderException {
                processCommand(command.isOn);
                return command;
            }
        });
    }

    private void processCommand(boolean on) {
        commandExecutor.executeCommandLine(on ? onCommand : offCommand);
    }

    @Override
    public void activate() {
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isEnabled && isBasicSet(event) && isForThisInstance(event)) {
            try {
                messageProcessor.process(Hex.hexStringToByteArray(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)));
                return true;
            } catch (DecoderException e) {
                // Ignore
            }
        }
        return handleInit(event);
    }

    private boolean isForThisInstance(Event event) {
        return (event.getAttributeInt(ZWaveController.ZWAVE_NODE) == nodeId) &&
                ((instanceId == null && event.getAttribute(ZWaveController.ZWAVE_ENDPOINT).isEmpty()) ||
                        (instanceId != null && instanceId.equals(event.getAttributeInt(ZWaveController.ZWAVE_ENDPOINT))));
    }

    private static boolean isBasicSet(Event e) {
        return e.isType("ZWave_Message") && e.getAttribute("Direction").equals("In") &&
                e.getAttributeInt("ZWave.CommandClass") == BasicCommandClass.COMMAND_CLASS &&
                e.getAttributeInt("ZWave.Command") == BasicCommandClass.SET;
    }

    @Override
    protected boolean initAttributes(Event event) {
        nodeId = event.getAttributeInt(ZWaveController.ZWAVE_NODE);
        final String endPoint = event.getAttribute(ZWaveController.ZWAVE_ENDPOINT);
        if (!endPoint.isEmpty()) {
            instanceId = Integer.parseInt(endPoint);
        }
        return true;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    public String getInstanceId() {
        return instanceId != null ? Integer.toString(instanceId) : "";
    }

    public void setInstanceId(String instance) {
        if (instance.isEmpty()) {
            this.instanceId = null;
        } else {
            int instanceNum = Integer.parseInt(instance);
            this.instanceId = ((instanceNum > 0) && (instanceNum < 256)) ? instanceNum : this.instanceId;
        }
    }

    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(String nodeId) {
        this.nodeId = Integer.parseInt(nodeId);
    }

    public String getOnCommand() {
        return onCommand;
    }

    public void setOnCommand(String onCommand) {
        this.onCommand = onCommand;
    }

    public String getOffCommand() {
        return offCommand;
    }

    public void setOffCommand(String offCommand) {
        this.offCommand = offCommand;
    }

    public String getState() {
        return isEnabled ? "Enabled" : "Disabled";
    }

    public void setState(String state) {
        isEnabled = state.equalsIgnoreCase("Enabled");
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

}


