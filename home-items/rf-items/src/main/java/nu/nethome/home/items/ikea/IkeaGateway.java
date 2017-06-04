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

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.MDNSScanner;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

import static nu.nethome.home.items.MDNSScanner.MDNS_CREATION_MESSAGE;
import static nu.nethome.home.items.MDNSScanner.MDNS_SERVICE_NAME;
import static nu.nethome.home.items.MDNSScanner.MDNS_SERVICE_TYPE;

/**
 * Represents a IKEA Trådfri Gateway and handles communications with it
 * TODO: Warmer Dim
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware", creationInfo = IkeaGateway.IkeaCreationInfo.class)
public class IkeaGateway extends HomeItemAdapter {

    public static class IkeaCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {MDNS_CREATION_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return isIkeaGatewaymDNSEvent(e);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("IKEA Trådfri gateway: \"%s\"",e.getAttribute(MDNS_SERVICE_NAME));
        }
    }

    private static boolean isIkeaGatewaymDNSEvent(Event e) {
        return e.getAttribute(MDNS_SERVICE_TYPE).equals("_coap._udp.local.") &&
                e.getAttribute(MDNS_SERVICE_NAME).startsWith("gw");
    }


    public static final String IKEA_MESSAGE = "IKEA_Message";
    public static final String IKEA_RESOURCE = "IKEA.Resource";
    public static final String IKEA_METHOD = "IKEA.Method";
    public static final String IKEA_BODY = "IKEA.Body";

    public static final String IKEA_NODE_MESSAGE = "IKEA_NodeMessage";
    public static final String IKEA_NODE_TYPE = "IKEA.NodeType";
    public static final String IKEA_NODE_ID = "IKEA.NodeId";
    public static final String IKEA_NODE_NAME = "IKEA.NodeName";

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"IkeaGateway\"  Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" Set=\"setAddress\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getBridgeIdentity\" Init=\"setBridgeIdentity\" />"
            + "  <Attribute Name=\"SecurityCode\" Type=\"Password\" Get=\"getSecurityCode\" Init=\"setSecurityCode\" />"
            + "  <Attribute Name=\"NodeCount\" Type=\"String\" Get=\"getNodeCount\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(IkeaGateway.class.getName());
    private static final int DESTINATION_PORT = 5684;

    private String securityCode = "";
    private String address = "";
    private String bridgeIdentity = "";
    private int refreshInterval = 5;
    private int refreshCounter = 0;
    private String state = "";
    private IkeaGatewayClient client = new IkeaGatewayClient();
    private int nodeCount = 0;

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
            nodeCount = client.getNodeIds(address).size();
        }
    }

    @Override
    public void stop() {
        client.stop();
        super.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (!isActivated()) {
            return handleInit(event);
        }
        if (event.isType(IKEA_MESSAGE) && event.getAttribute("Direction").equals("Out")) {
            sendCoapsMessage(event.getAttribute(IKEA_RESOURCE),
                    event.getAttribute(IKEA_METHOD),
                    event.getAttribute(IKEA_BODY),
                    event.getAttribute(IKEA_NODE_ID));
            return true;
        } else if (event.isType("ReportItems")) {
            reportNodes();
        } else if (event.isType(MDNS_CREATION_MESSAGE) && event.getAttribute(MDNSScanner.MDNS_LOCATION).equals(address)) {
            setAddress(event.getAttribute(MDNSScanner.MDNS_LOCATION));
            return true;
        }
        return false;
    }

    @Override
    protected boolean initAttributes(Event event) {
        setAddress(event.getAttribute(MDNSScanner.MDNS_LOCATION));
        setBridgeIdentity(event.getAttribute(MDNSScanner.MDNS_SERVICE_NAME));
        return true;
    }


    private void reportNodes() {
        List<JSONObject> nodes = client.getNodes(address);
        nodeCount = nodes.size();
        for (JSONObject node : nodes) {
            Event event = server.createEvent(IKEA_NODE_MESSAGE, node.toString());
            event.setAttribute("Direction", "In");
            event.setAttribute(IKEA_NODE_TYPE, node.getInt("5750"));
            event.setAttribute(IKEA_NODE_ID, node.getInt("9003"));
            event.setAttribute(IKEA_NODE_NAME, node.getString("9001"));
            server.send(event);
        }
    }

    private void sendCoapsMessage(String resource, String method, String body, String id) {
        String uri = String.format("coaps://%s%s", address, resource);
        JSONData jsonResponse = client.sendCoapMessage(uri, method, body);
        if (!id.isEmpty() && jsonResponse != null) {
            Event event = server.createEvent(IKEA_MESSAGE, jsonResponse.toString());
            event.setAttribute("Direction", "In");
            event.setAttribute(IKEA_NODE_ID, id);
            server.send(event);
        }
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
        return client.isConnected() ? "Connected" : "Not Connected";
    }

    public String getNodeCount() {
        return Integer.toString(nodeCount);
    }
}
