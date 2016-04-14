package nu.nethome.home.items.zwave;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.ApplicationUpdate;
import nu.nethome.zwave.messages.RequestNodeInfo;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.ApplicationSpecificCommandClass;
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
    private MultiMessageProcessor messageProcessor;

    public ZWaveCentralSceneRemapButton() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addMessageProcessor(ApplicationUpdate.REQUEST_ID, Message.Type.REQUEST, new ApplicationUpdate.Event.Processor() {
            @Override
            protected ApplicationUpdate.Event process(ApplicationUpdate.Event command) throws DecoderException {
                // NYI
                return command;
            }
        });
        messageProcessor.addCommandProcessor(new ApplicationSpecificCommandClass.Report.Processor() {
            @Override
            protected ApplicationSpecificCommandClass.Report process(ApplicationSpecificCommandClass.Report command, CommandArgument node) throws DecoderException {
                // NYI
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
}
