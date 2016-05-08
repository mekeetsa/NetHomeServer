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

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MeterCommandClass;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationInfo = ZWaveMeter.CreationInfo.class)
public class ZWaveMeter extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String METER_COMMAND_CLASS_AS_HEX = "32";

    public static class CreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {ZWaveNode.ZWAVE_NODE_REPORT};

        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).contains(METER_COMMAND_CLASS_AS_HEX);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("ZWave Meter, node: %d", e.getAttributeInt("NodeId"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveMeter\" Category=\"Gauges\"  Morphing=\"%b\" >"
            + "  <Attribute Name=\"Value\" Type=\"String\" Get=\"getValue\" Default=\"true\" Unit=\"%s\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"Instance\" Type=\"String\" Get=\"getInstance\" 	Set=\"setInstance\" />"
//            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
//            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
//            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Action Name=\"update\" 	Method=\"sendGetCommand\" Default=\"true\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(ZWaveMeter.class.getName());
    private MultiMessageProcessor messageProcessor;

    private int nodeId;
    protected Integer instance = null;
    private double value;
    private Date latestUpdateOrCreation = new Date();
    private boolean hasBeenUpdated = false;
    private String unit = "";

    public ZWaveMeter() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addCommandProcessor(new MeterCommandClass.Report.Processor() {
            @Override
            protected MeterCommandClass.Report process(MeterCommandClass.Report command, CommandArgument node) throws DecoderException {
                meterReport(command, node);
                return command;
            }
        });
    }

    private void meterReport(MeterCommandClass.Report report, CommandArgument node) {
        value = report.value;
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
        if (unit.isEmpty()) {
            try {
                unit = report.getUnit().unit;
            } catch (DecoderException e) {
                // Ignore
            }
        }
    }

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (isMeterReport(event) && isForThisNode(event)) {
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

    private boolean isForThisNode(Event event) {
        return event.getAttributeInt(ZWaveController.ZWAVE_NODE) == nodeId;
    }

    private static boolean isMeterReport(Event e) {
        return e.isType(ZWaveController.ZWAVE_EVENT_TYPE) && e.getAttribute("Direction").equals("In") &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND_CLASS) == MeterCommandClass.COMMAND_CLASS &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND) == MeterCommandClass.REPORT;
    }

    @Override
    protected boolean initAttributes(Event event) {
        nodeId = event.getAttributeInt("NodeId");
        instance = null;
        return true;
    }

    @Override
    public String getModel() {
        return String.format(MODEL, unit.isEmpty(), unit);
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

    public String getValue() {
        return hasBeenUpdated ? String.format("%.1f", value) : "";
    }

    public void sendGetCommand() {
        Command command = new MeterCommandClass.Get();
        if (instance != null) {
            command = new MultiInstanceCommandClass.EncapsulationV2(instance, command);
        }
        final SendData.Request request = new SendData.Request((byte) nodeId, command);
        Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
        event.setAttribute("Direction", "Out");
        server.send(event);
    }
}
