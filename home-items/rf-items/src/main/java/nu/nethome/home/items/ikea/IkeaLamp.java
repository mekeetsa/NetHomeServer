/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.ikea;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import static nu.nethome.home.items.ikea.IkeaGateway.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationInfo = IkeaLamp.IkeaCreationInfo.class)
public class IkeaLamp extends HomeItemAdapter implements HomeItem {

    public static class IkeaCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {IkeaGateway.IKEA_NODE_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.isType(IkeaGateway.IKEA_NODE_MESSAGE) &&
                    (e.getAttributeInt(IkeaGateway.IKEA_NODE_TYPE) == 2) &&
                    !hasColor(e);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Ikea lamp %d: \"%s\"",e.getAttributeInt(IKEA_NODE_ID), e.getAttribute(IkeaGateway.IKEA_NODE_NAME));
        }
    }

    protected static boolean hasColor(Event event) {
        JSONObject node = new JSONObject(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
        JSONArray lights = node.getJSONArray("3311");
        JSONObject light = lights.getJSONObject(0);
        return light.has("5706") && !light.getString("5706").isEmpty();
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"IkeaLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getLampId\" 	Set=\"setLampId\" />"
            + "  <Attribute Name=\"LampModel\" Type=\"String\" Get=\"getLampModel\" 	Init=\"setLampModel\" />"
            + "  <Attribute Name=\"Version\" Type=\"String\" Get=\"getLampVersion\" 	Init=\"setLampVersion\" />"
            + "  <Attribute Name=\"Brightness\" Type=\"String\" Get=\"getCurrentBrightness\"  />"
            + "  <Attribute Name=\"OnBrightness\" Type=\"String\" Get=\"getBrightness\" 	Set=\"setBrightness\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "  <Attribute Name=\"DimStep\" Type=\"String\" Get=\"getDimStep\" 	Set=\"setDimStep\" />"
            + "</HomeItem> ");

    private static int X_MIN = 24930;
    private static int X_MAX = 33135;
    private static int Y_MIN = 24694;
    private static int Y_MAX = 27211;

    private String lampId = "";
    private int onBrightness = -1;
    private int currentBrightness = 100;
    protected int colorTemperature = -1;
    protected boolean isOn;
    private String dimLevel1 = "0";
    private String dimLevel2 = "33";
    private String dimLevel3 = "66";
    private String dimLevel4 = "100";
    private int dimStep = 10;
    private String lampModel = "";
    private String lampVersion = "";

    public IkeaLamp() {
    }

    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType(IkeaGateway.IKEA_NODE_MESSAGE) &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute(IkeaGateway.IKEA_NODE_ID).equals(lampId)) {
            updateAttributes(event);
            return true;
        }
        return handleInit(event);
    }

    private void updateAttributes(Event event) {
        JSONObject node = new JSONObject(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
        JSONObject info = node.getJSONObject("3");
        this.lampModel = info.getString("1");
        this.lampVersion = info.getString("3");
        JSONArray lights = node.getJSONArray("3311");
        JSONObject light = lights.getJSONObject(0);
        this.isOn = light.has("5850") && light.getInt("5850") != 0;
        if (light.has("5851")) {
            this.currentBrightness = ikeaTopercent(light.getInt("5851"));
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        lampId = event.getAttribute(IkeaGateway.IKEA_NODE_ID);
        updateAttributes(event);
        return true;
    }

    protected void sendOnCommand(int brightness, int temperature) {
        Event ev = createEvent();
        ev.setAttribute(IkeaGateway.IKEA_METHOD, "PUT");
        JSONObject light = new JSONObject();
        light.put("5850", 1);
        if (brightness >= 0) {
            light.put("5851", percentToIkea(brightness));
            currentBrightness = brightness;
        }
        if (temperature >= 0) {
            light.put("5709", percentToX(temperature));
            light.put("5710", percentToY(temperature));
        }
        ev.setAttribute(IkeaGateway.IKEA_BODY, String.format("{\"3311\":[%s]}", light.toString()));
        server.send(ev);
        isOn = true;
    }

    private int percentToIkea(int brightness) {
        return (brightness * 254) / 100;
    }
    private int ikeaTopercent(int brightness) {
        return (brightness * 100) / 254;
    }
    private int percentToX(int temperature) {
        return X_MIN + (temperature * (X_MAX - X_MIN)) / 100;
    }
    private int percentToY(int temperature) {
        return Y_MIN + (temperature * (Y_MAX - Y_MIN)) / 100;
    }

    protected void sendOffCommand() {
        Event ev = createEvent();
        ev.setAttribute(IkeaGateway.IKEA_METHOD, "PUT");
        ev.setAttribute(IkeaGateway.IKEA_BODY, "{\"3311\":[{\"5850\":0}]}");
        server.send(ev);
        isOn = false;
    }

    private Event createEvent() {
        Event ev = server.createEvent(IkeaGateway.IKEA_MESSAGE, "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute(IkeaGateway.IKEA_RESOURCE, "/15001/" + lampId);
        return ev;
    }

    public void on() {
        sendOnCommand(onBrightness, colorTemperature);
        isOn = true;
    }

    public void off() {
        sendOffCommand();
        isOn = false;
    }

    public void toggle() {
        if (isOn) {
            off();
        } else {
            on();
        }
    }

    public void setBrightness(String level) {
        if (level.length() == 0) {
            onBrightness = -1;
        } else {
            int newDimLevel = Integer.parseInt(level);
            if ((newDimLevel >= 0) && (newDimLevel <= 100) && (newDimLevel != onBrightness)) {
                onBrightness = newDimLevel;
                if (isOn) {
                    on();
                }
            }
        }
    }

    public String getBrightness() {
        return onBrightness < 0 ? "" : Integer.toString(onBrightness);
    }

    public String getLampId() {
        return lampId;
    }

    public void setLampId(String lampId) {
        this.lampId = lampId;
    }

    public String getState() {
        return isOn ? "On" : "Off";
    }

    public boolean isOn() {
        return isOn;
    }

    public String getLampModel() {
        return lampModel;
    }

    public void setLampModel(String lampModel) {
        this.lampModel = lampModel;
    }

    public String getLampVersion() {
        return lampVersion;
    }

    public void setLampVersion(String lampVersion) {
        this.lampVersion = lampVersion;
    }

    public void dim1() {
        sendOnCommand(dimLevel1);
    }

    protected void sendOnCommand(String dimAndColor) {
        int dimLevel;
        if (!dimAndColor.isEmpty()) {
            dimLevel = Integer.parseInt(dimAndColor.trim());
        } else  {
            dimLevel = -1;
        }
        sendOnCommand(dimLevel, colorTemperature);
    }

    public void dim2() {
        sendOnCommand(dimLevel2);
    }

    public void dim3() {
        sendOnCommand(dimLevel3);
    }

    public void dim4() {
        sendOnCommand(dimLevel4);
    }

    public String getDimLevel1() {
        return dimLevel1;
    }

    public void setDimLevel1(String dimLevel1) {
        this.dimLevel1 = dimLevel1;
    }

    public String getDimLevel2() {
        return dimLevel2;
    }

    public void setDimLevel2(String dimLevel2) {
        this.dimLevel2 = dimLevel2;
    }

    public String getDimLevel3() {
        return dimLevel3;
    }

    public void setDimLevel3(String dimLevel3) {
        this.dimLevel3 = dimLevel3;
    }

    public String getDimLevel4() {
        return dimLevel4;
    }

    public void setDimLevel4(String dimLevel4) {
        this.dimLevel4 = dimLevel4;
    }

    public void bright() {
        currentBrightness += dimStep;
        if (currentBrightness > 100) {
            currentBrightness = 100;
        }
        sendOnCommand(currentBrightness, colorTemperature);
    }

    public void dim() {
        currentBrightness -= dimStep;
        if (currentBrightness < 1) {
            currentBrightness = 1;
        }
        sendOnCommand(currentBrightness, colorTemperature);
    }

    public String getCurrentBrightness() {
        return Integer.toString(currentBrightness);
    }

    public String getDimStep() {
        return Integer.toString(dimStep);
    }

    public void setDimStep(String dimStep) {
        this.dimStep = Integer.parseInt(dimStep);
    }

}

