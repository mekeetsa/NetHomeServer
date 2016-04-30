/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
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
import nu.nethome.home.items.blinds.BlindState;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.MultiLevelSwitchCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MultiMessageProcessor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Actuators", creationInfo = ZWaveBlind.CreationInfo.class)
public class ZWaveBlind extends HomeItemAdapter implements HomeItem {

    public static final int MINIMAL_MOVEMENT_TIME = 1000;
    private static final String SWITCH_MULTI_LEVEL_COMMAND_CLASS_AS_HEX = "26";

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
            return String.format("ZWave Multi Level Switch, node: %d", e.getAttributeInt("NodeId"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"RollerTrolBlind\" Category=\"Actuators\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" 	Set=\"setNodeId\" />"
            + "  <Attribute Name=\"TravelTime\" Type=\"String\" Get=\"getTravelTime\" 	Set=\"setTravelTime\" />"
            + "  <Attribute Name=\"Position1\" Type=\"String\" Get=\"getPosition1\" 	Set=\"setPosition1\" />"
            + "  <Attribute Name=\"Position2\" Type=\"String\" Get=\"getPosition2\" 	Set=\"setPosition2\" />"
            + "  <Action Name=\"up\" 	Method=\"blindUp\" />"
            + "  <Action Name=\"stop\" 	Method=\"blindStop\" />"
            + "  <Action Name=\"down\" 	Method=\"blindDown\" />"
            + "  <Action Name=\"toggle\" 	Method=\"blindToggle\" Default=\"true\" />"
            + "  <Action Name=\"Position1\" 	Method=\"position1\" />"
            + "  <Action Name=\"Position2\" 	Method=\"position2\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(ZWaveBlind.class.getName());

    private MultiMessageProcessor messageProcessor;
    private Timer stopTimer = new Timer("ZWaveBlind", true);
    private int nodeId = 1;
    private BlindState state = new BlindState();
    private int position1;
    private int position2;

    public ZWaveBlind(int nodeId) {
        this.nodeId = nodeId;
        messageProcessor = new MultiMessageProcessor();
    }

    public ZWaveBlind() {
        this(0);
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
        return true;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void stop() {
        super.stop();
        deactivateStopTimer();
    }

    private void deactivateStopTimer() {
        stopTimer.cancel();
    }

    public String getNodeId() {
        return Integer.toString(nodeId);
    }

    public void setNodeId(String nodeId) {
        this.nodeId = Integer.parseInt(nodeId);
    }

    public void sendCommand(int newLevel) {
        Command command;
        if (newLevel == 0) {
            command = new MultiLevelSwitchCommandClass.StopLevelChange();
        } else if (newLevel > 0) {
            command = new MultiLevelSwitchCommandClass.StartLevelChange(MultiLevelSwitchCommandClass.StartLevelChange.Direction.UP, 10);
        } else {
            command = new MultiLevelSwitchCommandClass.StartLevelChange(MultiLevelSwitchCommandClass.StartLevelChange.Direction.DOWN, 10);
        }
        final SendData.Request request = new SendData.Request((byte) nodeId, command);
        Event event = server.createEvent(ZWaveController.ZWAVE_EVENT_TYPE, Hex.asHexString(request.encode()));
        event.setAttribute("Direction", "Out");
        server.send(event);
    }

    public String getState() {
        return state.getStateString();
    }

    public void blindUp() {
        sendCommand(1);
        state.up();
    }

    public void blindStop() {
        sendCommand(0);
        state.stop();
    }

    public void blindDown() {
        sendCommand(-1);
        state.down();
    }

    public void blindToggle() {
        if (state.isTravelling()) {
            blindStop();
        } else if (state.getStateString().equals(BlindState.UP_STRING)) {
            blindDown();
        } else {
            blindUp();
        }
    }

    public String getTravelTime() {
        return state.getTravelTime() > 0 ? Long.toString(state.getTravelTime() / 1000) : "";
    }

    public void setTravelTime(String travelTime) {
        if (travelTime.length() == 0) {
            state.setTravelTime(0);
        } else {
            state.setTravelTime(Long.parseLong(travelTime) * 1000);
        }
    }

    public String getPosition1() {
        return position1 > 0 ? Integer.toString(position1) : "";
    }

    public void setPosition1(String position) {
        this.position1 = calculatePosition(position, this.position1);
    }

    public String position1() {
        return goToPosition(position1);
    }

    private String goToPosition(int pos) {
        long wantedPosition = state.getTravelTime() * pos / 100;
        long currentPosition = state.getCurrentPosition();
        long distance = Math.abs(wantedPosition - currentPosition);
        if (distance < MINIMAL_MOVEMENT_TIME) {
            return "";
        }
        if (wantedPosition > currentPosition) {
            blindDown();
        } else {
            blindUp();
        }
        activateStopTimer(distance);
        return "";
    }

    public String getPosition2() {
        return position2 > 0 ? Integer.toString(position2) : "";
    }

    public void setPosition2(String position) {
        this.position2 = calculatePosition(position, this.position2);
    }

    private int calculatePosition(String position, int result) {
        if (position.length() == 0) {
            result = 0;
        } else {
            int pos = Integer.parseInt(position);
            if (pos > 1 && pos < 100) {
                result = pos;
            }
        }
        return result;
    }

    public String position2() {
        return goToPosition(position2);
    }

    private void activateStopTimer(long time) {
        stopTimer = new Timer("RollerTrolBlind", true);
        stopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                blindStop();
            }
        }, time);
    }
}
