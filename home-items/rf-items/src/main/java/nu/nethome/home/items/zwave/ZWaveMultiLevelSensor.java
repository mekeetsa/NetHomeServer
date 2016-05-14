/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
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
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MeterCommandClass;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.MultiLevelSensorCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationInfo = ZWaveMultiLevelSensor.CreationInfo.class)
public class ZWaveMultiLevelSensor extends ZWaveMeter implements HomeItem {

    private static final String MULTI_LEVEL_SENSOR_COMMAND_CLASS_AS_HEX = "31";

    public static class CreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {ZWaveNode.ZWAVE_NODE_REPORT};

        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).contains(MULTI_LEVEL_SENSOR_COMMAND_CLASS_AS_HEX);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("ZWave Multi Level Sensor, node: %d", e.getAttributeInt("NodeId"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveMultiLevelSensor\" Category=\"Gauges\"  Morphing=\"%b\" >"
            + "  <Attribute Name=\"Value\" Type=\"String\" Get=\"getValue\" Default=\"true\" Unit=\"%s\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"Instance\" Type=\"String\" Get=\"getInstance\" 	Set=\"setInstance\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Action Name=\"update\" 	Method=\"sendGetCommand\" Default=\"true\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(ZWaveMultiLevelSensor.class.getName());

    @Override
    protected boolean isCommandForUs(Event e) {
        return e.isType(ZWaveController.ZWAVE_EVENT_TYPE) && e.getAttribute("Direction").equals("In") &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND_CLASS) == MultiLevelSensorCommandClass.COMMAND_CLASS &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND) == MultiLevelSensorCommandClass.REPORT;
    }

    @Override
    public String getModel() {
        return String.format(MODEL, unit.isEmpty(), unit);
    }

    @Override
    protected void addMessageProcessors() {
        messageProcessor.addCommandProcessor(new MultiLevelSensorCommandClass.Report.Processor() {
            @Override
            protected MultiLevelSensorCommandClass.Report process(MultiLevelSensorCommandClass.Report command, CommandArgument node) throws DecoderException {
                meterReport(command, node);
                return command;
            }
        });
    }

    private void meterReport(MultiLevelSensorCommandClass.Report report, CommandArgument node) {
        value = report.value;
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
        if (unit.isEmpty()) {
            try {
                unit = report.getUnit().unit;
                precision = report.precision;
            } catch (DecoderException e) {
                // Ignore
            }
        }
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

    @Override
    public void sendGetCommand() {
        Command command = new MultiLevelSensorCommandClass.Get();
        if (instance != null) {
            command = new MultiInstanceCommandClass.EncapsulationV2(instance, command);
        }
        final SendData.Request request = new SendData.Request((byte) nodeId, command);
        Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
        event.setAttribute("Direction", "Out");
        server.send(event);
    }
}
