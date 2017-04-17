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
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Represents a IKEA Trådfri Gateway and handles communications with it
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class IkeaGateway extends HomeItemAdapter {

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
    private Endpoint dtlsEndpoint;


    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        setupDtlsEndpoint();
    }

    private void setupDtlsEndpoint() {
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        InMemoryPskStore pskStore = new InMemoryPskStore();
        pskStore.addKnownPeer(new InetSocketAddress(getAddress(), DESTINATION_PORT),
                "",
                securityCode.getBytes());
        builder.setPskStore(pskStore);
        builder.setSupportedCipherSuites(new CipherSuite[]{CipherSuite.TLS_PSK_WITH_AES_128_CCM_8});
        DTLSConnector dtlsconnector = new DTLSConnector(builder.build(), null);
        try {
            dtlsEndpoint = new CoapEndpoint(dtlsconnector, NetworkConfig.getStandard());
            dtlsEndpoint.start();
            EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint);
        } catch (IOException e) {
            dtlsEndpoint = null;
        }
    }

    private void tearDownDtlsEndpoint() {
        if (dtlsEndpoint != null) {
            dtlsEndpoint.stop();
            dtlsEndpoint.destroy();
        }
    }

    @Override
    public void stop() {
        tearDownDtlsEndpoint();
        super.stop();
    }

    public void reconnect() {
        tearDownDtlsEndpoint();
        setupDtlsEndpoint();
    }

    public void findBridge() {

    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("XXXXX") &&
                event.getAttribute("Direction").equals("Out")) {
            //...
            return true;
        }
        return false;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
