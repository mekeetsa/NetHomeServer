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

import nu.nethome.coders.RollerTrol;
import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

import static nu.nethome.coders.RollerTrol.*;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Hardware", creationInfo = RollerTrolBlind.RollerTrolCreationInfo.class)
public class RollerTrolBlind extends HomeItemAdapter implements HomeItem {

    public static final String HOUSE_CODE_ATTRIBUTE = "RollerTrol.HouseCode";
    public static final String DEVICE_CODE_ATTRIBUTE = "RollerTrol.DeviceCode";
    public static final String COMMAND_ATTRIBUTE = "RollerTrol.Command";

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
            + "<HomeItem Class=\"RollerTrolBlind\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"RemoteId\" Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"Channel\" Type=\"StringList\" Get=\"getDeviceCode\" Set=\"setDeviceCode\" >"
            + "     <item>1</item> <item>2</item> <item>3</item> <item>4</item> <item>5</item> <item>6</item> <item>7</item> <item>8</item> <item>All</item></Attribute>"
            + "  <Action Name=\"up\" 	Method=\"up\" />"
            + "  <Action Name=\"stop\" 	Method=\"stop\" />"
            + "  <Action Name=\"down\" 	Method=\"down\" Default=\"true\" />"
            + "  <Action Name=\"confirm\" 	Method=\"confirm\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(RollerTrolBlind.class.getName());

    // Public attributes
    private int houseCode = 1;
    private int deviceCode = 1;

    public boolean receiveEvent(Event event) {
        // Check if this is an inward event directed to this instance
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("RollerTrol_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt(HOUSE_CODE_ATTRIBUTE) == houseCode) &&
                event.getAttributeInt(DEVICE_CODE_ATTRIBUTE) == deviceCode) {
            // In that case, update our state accordingly
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


    public String getModel() {
        return MODEL;
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
    }

    public void up() {
        sendCommand(UP);
    }

    public void stop() {
        sendCommand(STOP);
    }

    public void down() {
        sendCommand(DOWN);
    }

    public void confirm() {
        sendCommand(CONFIRM);
    }
}
