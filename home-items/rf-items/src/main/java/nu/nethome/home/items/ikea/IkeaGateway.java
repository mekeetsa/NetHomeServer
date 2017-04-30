/*
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

package nu.nethome.home.items.ikea;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Represents a IKEA Trådfri Gateway and handles communications with it
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class IkeaGateway extends HomeItemAdapter {

    public static final String IKEA_MESSAGE = "IKEA_Message";
    public static final String IKEA_RESOURCE = "IKEA.Resource";
    public static final String IKEA_METHOD = "IKEA.Method";
    public static final String IKEA_BODY = "IKEA.Body";
    public static final String IKEA_ID = "IKEA.Id";

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"IkeaGateway\"  Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" Set=\"setAddress\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getBridgeIdentity\" Init=\"setBridgeIdentity\" />"
            + "  <Attribute Name=\"DeviceName\" Type=\"String\" Get=\"getDeviceName\"  />"
            + "  <Attribute Name=\"SecurityCode\" Type=\"String\" Get=\"getSecurityCode\" Init=\"setSecurityCode\" />"
            + "  <Attribute Name=\"RefreshInterval\" Type=\"String\" Get=\"getRefreshInterval\" Set=\"setRefreshInterval\" />"
            + "  <Action Name=\"findBridge\" Method=\"findBridge\" />"
            + "  <Action Name=\"reconnect\" Method=\"reconnect\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(IkeaGateway.class.getName());
    private static final int DESTINATION_PORT = 5684;

    private String securityCode = "";
    private String address = "";
    private String bridgeIdentity = "";
    private int refreshInterval = 5;
    private int refreshCounter = 0;
    private String state = "Disconnected";
    private IkeaGatewayClient client = new IkeaGatewayClient();

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        client.start();
        setPresharedKeyIfAvaliable();
    }

    private void setPresharedKeyIfAvaliable() {
        if (!securityCode.isEmpty() && !address.isEmpty()) {
            client.setRouterKey(new InetSocketAddress(getAddress(), DESTINATION_PORT),"", securityCode.getBytes());
        }
    }

    @Override
    public void stop() {
        client.stop();
        super.stop();
    }

    public void reconnect() {
    }

    public void findBridge() {

    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType(IKEA_MESSAGE) && event.getAttribute("Direction").equals("Out")) {
            sendCoapsMessage(event.getAttribute(IKEA_RESOURCE),
                    event.getAttribute(IKEA_METHOD),
                    event.getAttribute(IKEA_BODY),
                    event.getAttribute(IKEA_ID));
            client.getNodes(address);
            return true;
        }
        return false;
    }

    private void sendCoapsMessage(String resource, String method, String body, String id) {
        String uri = String.format("coaps://%s%s", address, resource);
        client.sendCoapMessage(uri, method, body);
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        String oldCode = this.securityCode;
        this.securityCode = securityCode;
        if (oldCode != securityCode && isActivated()) {
            setPresharedKeyIfAvaliable();
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        String oldAddress = this.address;
        this.address = address;
        if (oldAddress != address && isActivated()) {
            setPresharedKeyIfAvaliable();
        }
    }

    public String getBridgeIdentity() {
        return bridgeIdentity;
    }

    public void setBridgeIdentity(String bridgeIdentity) {
        this.bridgeIdentity = bridgeIdentity;
    }

    public String getRefreshInterval() {
        return Integer.toString(refreshInterval);

    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = Integer.parseInt(refreshInterval);
        refreshCounter = this.refreshInterval + 1;
    }

    public String getState() {
        return state;
    }

}
