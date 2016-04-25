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

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.MultiLevelSwitchCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationInfo = ZWaveDimmer.CreationInfo.class)
public class ZWaveDimmer extends HomeItemAdapter implements HomeItem {

    private static final String SWITCH_MULTI_LEVEL_COMMAND_CLASS_AS_HEX = "26";
    private static final int FULL_ON_DIM_LEVEL = 99;

    public static class CreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {ZWaveNode.ZWAVE_NODE_REPORT};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).contains(SWITCH_MULTI_LEVEL_COMMAND_CLASS_AS_HEX);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("ZWave Dimmer, node: %d", e.getAttributeInt("NodeId"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveDimmer\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"Instance\" Type=\"String\" Get=\"getInstance\" 	Set=\"setInstance\" />"
            + "  <Attribute Name=\"OnDimLevel\" Type=\"String\" Get=\"getOnDimLevel\" 	Set=\"setOnDimLevel\" />"
            + "  <Attribute Name=\"Level\" Type=\"String\" Get=\"getCurrentDimLevel\"  />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Attribute Name=\"DimStep\" Type=\"String\" Get=\"getDimStep\" 	Set=\"setDimStep\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "  <Action Name=\"store\" 	Method=\"store\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(ZWaveDimmer.class.getName());
    private MultiMessageProcessor messageProcessor;
    private int nodeId;
    protected Integer instance = null;
    // Public attributes
    private int currentDimLevel = 0;
    private int onDimLevel = 0;
    private int dimLevel1 = 25;
    private int dimLevel2 = 50;
    private int dimLevel3 = 75;
    private int dimLevel4 = 100;
    private int dimStep = 10;

    public ZWaveDimmer() {
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addCommandProcessor(new MultiLevelSwitchCommandClass.Report.Processor() {
            @Override
            protected MultiLevelSwitchCommandClass.Report process(MultiLevelSwitchCommandClass.Report command, CommandArgument node) throws DecoderException {
                multiLevelReport(command, node);
                return command;
            }
        });
    }

    private void multiLevelReport(MultiLevelSwitchCommandClass.Report report, CommandArgument node) {
        if ((instance == null && node.targetInstance == null) ||
                (instance != null && instance.equals(node.targetInstance)))
        currentDimLevel = report.level;
    }

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (isMultiLevelSwithReport(event) && event.getAttributeInt(ZWaveController.ZWAVE_NODE) == nodeId) {
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

    private static boolean isMultiLevelSwithReport(Event e) {
        return e.isType(ZWaveController.ZWAVE_EVENT_TYPE) && e.getAttribute("Direction").equals("In") &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND_CLASS) == MultiLevelSwitchCommandClass.COMMAND_CLASS &&
                e.getAttributeInt(ZWaveController.ZWAVE_COMMAND) == MultiLevelSwitchCommandClass.SWITCH_MULTILEVEL_REPORT;
    }

    @Override
    protected boolean initAttributes(Event event) {
        nodeId = event.getAttributeInt("NodeId");
        instance = null;
        return true;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    public String getState() {
        return currentDimLevel == 0 ? "Off" : "On";
    }

    public String getInstance() {
        return instance != null ? Integer.toString(instance) : "";
    }

    public void setInstance(String instance) {
        if (instance.isEmpty()) {
            this.instance = null;
        } else {
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

    public void sendDimCommand(int newLevel) {
        Command command = new MultiLevelSwitchCommandClass.Set(newLevel);
        if (instance != null) {
            command = new MultiInstanceCommandClass.EncapsulationV2(instance, command);
        }
        final SendData.Request request = new SendData.Request((byte) nodeId, command);
        Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
        event.setAttribute("Direction", "Out");
        server.send(event);
        currentDimLevel = newLevel;
    }

    public void on() {
        logger.fine("Switching on " + name);
        if (onDimLevel > 0) {
            sendDimCommand(onDimLevel);
        } else {
            sendDimCommand(FULL_ON_DIM_LEVEL);;
        }
    }

    public void off() {
        logger.fine("Switching off " + name);
        sendDimCommand(0);
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        currentDimLevel = (currentDimLevel == 0) ? FULL_ON_DIM_LEVEL : 0;
        sendDimCommand(currentDimLevel);
    }

    /**
     * Dim to the pre set dim level 1
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim1() {
        sendDimCommand(dimLevel1);
    }

    /**
     * Dim to the pre set dim level 2
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim2() {
        sendDimCommand(dimLevel2);
    }

    /**
     * Dim to the pre set dim level 3
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim3() {
        sendDimCommand(dimLevel3);
    }

    /**
     * Dim to the pre set dim level 4
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim4() {
        sendDimCommand(dimLevel4);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel1() {
        return Integer.toString(dimLevel1);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel1 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel1(String mDimLevel1) {
        dimLevel1 = stringToDimLevel(mDimLevel1);
    }

    private int stringToDimLevel(String level) {
        int newDimLevel = Integer.parseInt(level);
        return toDimLevel(newDimLevel);
    }

    private int toDimLevel(int newDimLevel) {
        if (newDimLevel < 0) {
            return 0;
        } else if (newDimLevel > FULL_ON_DIM_LEVEL) {
            return FULL_ON_DIM_LEVEL;
        }
        return newDimLevel;
    }


    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel2() {
        return Integer.toString(dimLevel2);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel2 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel2(String mDimLevel2) {
        dimLevel2 = stringToDimLevel(mDimLevel2);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel3() {
        return Integer.toString(dimLevel3);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel3 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel3(String mDimLevel3) {
        dimLevel3 = stringToDimLevel(mDimLevel3);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel4() {
        return Integer.toString(dimLevel4);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel4 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel4(String mDimLevel4) {
        dimLevel4 = stringToDimLevel(mDimLevel4);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getOnDimLevel() {
        if (onDimLevel == 0) {
            return "";
        }
        return Integer.toString(onDimLevel);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param level dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setOnDimLevel(String level) {
        if (level.length() == 0) {
            onDimLevel = 0;
        } else {
            onDimLevel = stringToDimLevel(level);
            if (currentDimLevel > 0) {
                sendDimCommand(onDimLevel);
            }
        }
    }

    public void dim() {
        sendDimCommand(toDimLevel(currentDimLevel - dimStep));
    }

    public void bright() {
        sendDimCommand(toDimLevel(currentDimLevel + dimStep));
    }

    public void store() {
        onDimLevel = currentDimLevel;
    }

    public String getDimStep() {
        return Integer.toString(dimStep);
    }

    public void setDimStep(String dimStep) {
        this.dimStep = stringToDimLevel(dimStep);
    }

    public String getCurrentDimLevel() {
        return Integer.toString(currentDimLevel);
    }

}
