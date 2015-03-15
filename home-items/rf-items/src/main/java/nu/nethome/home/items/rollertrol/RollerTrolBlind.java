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

package nu.nethome.home.items.rollertrol;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import static nu.nethome.coders.RollerTrol.*;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Actuators", creationInfo = RollerTrolBlind.RollerTrolCreationInfo.class)
public class RollerTrolBlind extends HomeItemAdapter implements HomeItem {

    public static final String HOUSE_CODE_ATTRIBUTE = "RollerTrol.HouseCode";
    public static final String DEVICE_CODE_ATTRIBUTE = "RollerTrol.DeviceCode";
    public static final String COMMAND_ATTRIBUTE = "RollerTrol.Command";
    public static final int MINIMAL_MOVEMENT_TIME = 1000;

    public static class RollerTrolCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {"RollerTrol_Message"};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return true;
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Rollertrol Blind Motor, Ch: %s, Remote: %s",
                    e.getAttribute(DEVICE_CODE_ATTRIBUTE), e.getAttribute(HOUSE_CODE_ATTRIBUTE));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"RollerTrolBlind\" Category=\"Actuators\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"RemoteId\" Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"Channel\" Type=\"StringList\" Get=\"getDeviceCode\" Set=\"setDeviceCode\" >"
            + "     <item>1</item> <item>2</item> <item>3</item> <item>4</item> <item>5</item> <item>6</item> <item>7</item> <item>8</item> <item>All</item></Attribute>"
            + "  <Attribute Name=\"TravelTime\" Type=\"String\" Get=\"getTravelTime\" 	Set=\"setTravelTime\" />"
            + "  <Attribute Name=\"Position1\" Type=\"String\" Get=\"getPosition1\" 	Set=\"setPosition1\" />"
            + "  <Attribute Name=\"Position2\" Type=\"String\" Get=\"getPosition2\" 	Set=\"setPosition2\" />"
            + "  <Action Name=\"up\" 	Method=\"blindUp\" />"
            + "  <Action Name=\"stop\" 	Method=\"blindStop\" />"
            + "  <Action Name=\"down\" 	Method=\"blindDown\" />"
            + "  <Action Name=\"toggle\" 	Method=\"blindToggle\" Default=\"true\" />"
            + "  <Action Name=\"Position1\" 	Method=\"position1\" />"
            + "  <Action Name=\"Position2\" 	Method=\"position2\" />"
            + "  <Action Name=\"setConfirm\" 	Method=\"blindConfirm\" />"
            + "  <Action Name=\"setLimit\" 	Method=\"blindLimit\" />"
            + "  <Action Name=\"setReverse\" 	Method=\"blindReverse\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(RollerTrolBlind.class.getName());

    private Timer stopTimer = new Timer("RollerTrolBlind", true);
    private int houseCode = 1;
    private int deviceCode = 1;
    private BlindState state = new BlindState();
    private int position1;
    private int position2;

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("RollerTrol_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt(HOUSE_CODE_ATTRIBUTE) == houseCode) &&
                event.getAttributeInt(DEVICE_CODE_ATTRIBUTE) == deviceCode) {
            // In that case, update our state accordingly
            int command = event.getAttributeInt(COMMAND_ATTRIBUTE);
            if (command == UP) {
                state.up();
            } else if (command == DOWN) {
                state.down();
            } else if (command == STOP) {
                state.stop();
            }
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        houseCode = event.getAttributeInt(HOUSE_CODE_ATTRIBUTE);
        deviceCode = event.getAttributeInt(DEVICE_CODE_ATTRIBUTE);
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

    public String getHouseCode() {
        return Integer.toString(houseCode);
    }

    public void setHouseCode(String houseCode) {
        this.houseCode = Integer.parseInt(houseCode);
    }

    public String getDeviceCode() {
        return deviceCode == 15 ? "All" : Integer.toString(deviceCode);
    }

    public void setDeviceCode(String deviceCode) {
        if (deviceCode.equalsIgnoreCase("all")) {
            this.deviceCode = 15;
        } else {
            this.deviceCode = Integer.parseInt(deviceCode);
        }
    }

    public void sendCommand(int command) {
        Event ev = server.createEvent("RollerTrol_Message", "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute(HOUSE_CODE_ATTRIBUTE, houseCode);
        ev.setAttribute(DEVICE_CODE_ATTRIBUTE, deviceCode);
        ev.setAttribute(COMMAND_ATTRIBUTE, command);
        ev.setAttribute("Repeat", 15);
        server.send(ev);
        deactivateStopTimer();
    }

    public String getState() {
        return state.getStateString();
    }

    public void blindUp() {
        sendCommand(UP);
        state.up();
    }

    public void blindStop() {
        sendCommand(STOP);
        state.stop();
    }

    public void blindDown() {
        sendCommand(DOWN);
        state.down();
    }

    public void blindToggle() {
        if (state.getStateString().equals(BlindState.UP_STRING)) {
            blindDown();
        } else {
            blindUp();
        }
    }

    public void blindConfirm() {
        sendCommand(CONFIRM);
    }

    public void blindLimit() {
        sendCommand(LIMIT);
    }

    public void blindReverse() {
        sendCommand(REVERSE);
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
