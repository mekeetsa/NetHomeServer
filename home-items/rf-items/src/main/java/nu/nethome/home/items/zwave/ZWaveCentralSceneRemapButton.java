package nu.nethome.home.items.zwave;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.ApplicationUpdate;
import nu.nethome.zwave.messages.RequestNodeInfo;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.ApplicationSpecificCommandClass;
import nu.nethome.zwave.messages.commandclasses.CentralSceneCommandClass;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.Message;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class ZWaveCentralSceneRemapButton extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveCentralSceneRemapButton\" Category=\"Control\" >"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" Init=\"setNodeId\" />"
            + "  <Attribute Name=\"Scene\" Type=\"String\" Get=\"getScene\" Init=\"setScene\" />"
            + "  <Attribute Name=\"PressCommand\" Type=\"Command\" Get=\"getPressCommand\" Set=\"setPressCommand\" />"
            + "  <Attribute Name=\"HoldCommand\" Type=\"Command\" Get=\"getHoldCommand\" Set=\"setHoldCommand\" />"
            + "  <Attribute Name=\"ReleaseCommand\" Type=\"Command\" Get=\"getReleaseCommand\" Set=\"setReleaseCommand\" />"
            + "</HomeItem> ");

    private int nodeId = 0;
    private int scene = 1;
    String pressCommand = "";
    String holdCommand = "";
    String releaseCommand = "";
    private MultiMessageProcessor messageProcessor;
    CommandLineExecutor commandExecutor;

    public ZWaveCentralSceneRemapButton() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addCommandProcessor(new CentralSceneCommandClass.Set.Processor() {
            @Override
            protected CentralSceneCommandClass.Set process(CentralSceneCommandClass.Set command, CommandArgument node) throws DecoderException {
                centralSceneCommand(command, node);
                return command;
            }
        });
    }

    private void centralSceneCommand(CentralSceneCommandClass.Set command, CommandArgument node) {
        if (node.sourceNode == nodeId && command.scene == scene) {
            switch (command.press) {
                case 0:
                    commandExecutor.executeCommandLine(pressCommand);
                    break;
                case 1:
                    commandExecutor.executeCommandLine(holdCommand);
                    break;
                case 2:
                    commandExecutor.executeCommandLine(releaseCommand);
                    break;
            }
        }
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("ZWave_Message") &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttributeInt("Node") == nodeId) {
            try {
                messageProcessor.process(Hex.hexStringToByteArray(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)));
            } catch (DecoderException e) {
                // Ignore
            }
        }
        return false;
    }


    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(String nodeId) {
        this.nodeId = Integer.parseInt(nodeId);
    }

    public String getScene() {
        return Integer.toString(scene);
    }

    public void setScene(String scene) {
        this.scene = Integer.parseInt(scene);
    }

    public String getPressCommand() {
        return pressCommand;
    }

    public void setPressCommand(String pressCommand) {
        this.pressCommand = pressCommand;
    }

    public String getHoldCommand() {
        return holdCommand;
    }

    public void setHoldCommand(String holdCommand) {
        this.holdCommand = holdCommand;
    }

    public String getReleaseCommand() {
        return releaseCommand;
    }

    public void setReleaseCommand(String releaseCommand) {
        this.releaseCommand = releaseCommand;
    }
}
