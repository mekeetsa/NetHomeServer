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

package nu.nethome.home.items.zwave;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.SwitchBinaryCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps")
public class ZWaveLamp extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"Instance\" Type=\"String\" Get=\"getInstance\" 	Set=\"setInstance\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(ZWaveLamp.class.getName());
    private MultiMessageProcessor messageProcessor;
    // Public attributes
    private boolean state = false;
    private int nodeId;
    protected Integer instance = null;

    public ZWaveLamp() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addCommandProcessor(new SwitchBinaryCommandClass.Report.Processor() {
            @Override
            protected SwitchBinaryCommandClass.Report process(SwitchBinaryCommandClass.Report command, CommandArgument node) throws DecoderException {
                switchBinaryReport(command, node);
                return command;
            }
        });
    }

    private void switchBinaryReport(SwitchBinaryCommandClass.Report report, CommandArgument node) {
        if ((instance == null && node.targetInstance == null) ||
                (instance != null && instance.equals(node.targetInstance)))
        state = report.isOn;
    }

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (isSwithBinaryReport(event) && event.getAttributeInt(ZWaveController.ZWAVE_NODE) == nodeId) {
            try {
                messageProcessor.process(Hex.hexStringToByteArray(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE)));
            } catch (DecoderException e) {
                // Ignore
            }
            return true;
        } else {
            return handleInit(event);
        }
    }

    private static boolean isSwithBinaryReport(Event e) {
        return e.isType(ZWaveController.ZWAVE_EVENT_TYPE) && e.getAttribute("Direction").equals("In") &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND_CLASS) == SwitchBinaryCommandClass.COMMAND_CLASS &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND) == SwitchBinaryCommandClass.SWITCH_BINARY_REPORT;
    }

    @Override
    protected boolean initAttributes(Event event) {
        nodeId = 0; // TODO: NYI
        instance = null; //
        return true;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    public String getState() {
        return state ? "On" : "Off";
    }

    public String getInstance() {
        return instance != null ? Integer.toString(instance) : "";
    }

    public void setInstance(String instance) {
        if (instance.isEmpty()) {
            this.instance = null;
        } else {
            // TODO: handle in base class
            int instanceNum = Integer.parseInt(instance);
            this.instance = ((instanceNum > 0) && (instanceNum < 256)) ? instanceNum : this.instance;
        }
    }

    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(String nodeId) {
        this.nodeId = Integer.parseInt(nodeId);
    }

    public void sendCommand(int stateCommand) {
        Command command = new SwitchBinaryCommandClass.Set(stateCommand != 0);
        if (instance != null) {
            command = new MultiInstanceCommandClass.EncapsulationV2(instance, command);
        }
        final SendData.Request request = new SendData.Request((byte) nodeId, command);
        Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
        event.setAttribute("Direction", "Out");
        server.send(event);
    }

    public void on() {
        logger.fine("Switching on " + name);
        sendCommand(1);
        state = true;
    }

    public void off() {
        logger.fine("Switching off " + name);
        sendCommand(0);
        state = false;
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        state = !state;
        sendCommand(state ? 1 : 0);
    }
}
