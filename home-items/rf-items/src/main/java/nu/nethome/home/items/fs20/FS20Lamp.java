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

package nu.nethome.home.items.fs20;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.Lamp;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value="Lamps", creationEvents = FHZ1000PcPort.EVENT_TYPE_FS20_EVENT)
public class FS20Lamp extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"

            + "<HomeItem Class=\"FS20Lamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" 		Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"DeviceCode\" Type=\"String\" Get=\"getDeviceCode\" 	Set=\"setDeviceCode\" />"
            + "  <Attribute Name=\"FHZ1000PcPort\" 	Type=\"Item\" Get=\"getFHZ1000PcPort\" 		Set=\"setFHZ1000PcPort\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Attribute Name=\"OnDimLevel\" Type=\"String\" Get=\"getOnDimLevel\" 	Set=\"setOnDimLevel\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "</HomeItem> ");

    protected boolean m_IsAddressed = false;

    // Public attributes
    protected boolean isOn = false;
    protected String houseCode = "11111124";
    protected String deviceCode = "1111";
    protected String fhz1000PcPort = "FHZ1000PcPort";
    private int onDimLevel = 0;
    private int dimLevel1 = 25;
    private int dimLevel2 = 50;
    private int dimLevel3 = 75;
    private int dimLevel4 = 100;

    public FS20Lamp() {
    }

    public boolean receiveEvent(Event event) {
        // Check the events and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(FHZ1000PcPort.EVENT_TYPE_FS20_EVENT) &&
                event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE).equals(houseCode) &&
                event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE).equals(deviceCode)) {
                receiveCommand((byte) event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE));
                return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        houseCode = event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE);
        deviceCode = event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE);
        return true;
    }

    public void receiveCommand(byte command) {
        switch (command) {
            case FHZ1000PcPort.COMMAND_ON:
            case FHZ1000PcPort.COMMAND_DIM_UP:
            case FHZ1000PcPort.COMMAND_DIM_DOWN:
                isOn = true;
                break;
            case FHZ1000PcPort.COMMAND_OFF:
                isOn = false;
                break;
            case FHZ1000PcPort.COMMAND_TOGGLE:
            case FHZ1000PcPort.COMMAND_DIM_LOOP:
                isOn = !isOn;
        }
        if ((command >= FHZ1000PcPort.COMMAND_DIM1) && (command < FHZ1000PcPort.COMMAND_ON)) {
            isOn = true;
        }
    }

    public String getModel() {
        return MODEL;
    }

    public String getState() {
        if (isOn) {
            return "On";
        }
        return "Off";
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
    }

    /**
     * @return Returns the deviceCode.
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * @param deviceCode The deviceCode to set.
     */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    /**
     * @return Returns the houseCode.
     */
    public String getHouseCode() {
        return houseCode;
    }

    /**
     * @param houseCode The houseCode to set.
     */
    public void setHouseCode(String houseCode) {
        this.houseCode = houseCode;
    }

    /**
     * @return Returns the fhz1000PcPort.
     */
    public String getFHZ1000PcPort() {
        return fhz1000PcPort;
    }

    /**
     * @param port The fhz1000PcPort to set.
     */
    public void setFHZ1000PcPort(String port) {
        fhz1000PcPort = port;
    }

    public void sendCommand(byte command) {
        Event ev = server.createEvent(FHZ1000PcPort.EVENT_TYPE_FS20_COMMAND, "");
        ev.setAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE, houseCode);
        ev.setAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE, deviceCode);
        ev.setAttribute(Event.EVENT_VALUE_ATTRIBUTE, command);
        server.send(ev);
    }

    public void on() {
        if (onDimLevel > 0) {
            dimTo(onDimLevel);
        } else {
            sendCommand(FHZ1000PcPort.COMMAND_ON);
        }
        isOn = true;
    }

    public void off() {
        sendCommand(FHZ1000PcPort.COMMAND_OFF);
        isOn = false;
    }

    public void bright() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_UP);
        isOn = true;
    }

    public void dim() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_DOWN);
        isOn = true;
    }

    public void toggle() {
        sendCommand(FHZ1000PcPort.COMMAND_TOGGLE);
        isOn = !isOn;
    }

    public void dimLoop() {
        sendCommand(FHZ1000PcPort.COMMAND_DIM_LOOP);
        isOn = !isOn;
    }

    public void dim1() {
        dimTo(dimLevel1);
        isOn = true;
    }

    public void dim2() {
        dimTo(dimLevel2);
        isOn = true;
    }

    public void dim3() {
        dimTo(dimLevel3);
        isOn = true;
    }

    public void dim4() {
        dimTo(dimLevel4);
        isOn = true;
    }



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
        } else if (newDimLevel > 100) {
            return 100;
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
            if (isOn) {
                dimTo(onDimLevel);
            }
        }
    }

    public void dimTo(int percent) {
        if ((percent < 0) || (percent > 100)) return;
        byte commandValue = (byte) (percent * 16 / 100);
        sendCommand(commandValue);
        isOn = commandValue > 0 ? true : false;
    }
}
