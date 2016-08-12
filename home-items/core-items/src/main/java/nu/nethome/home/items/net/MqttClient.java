/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNetHome is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HomeItem class which connects to a MQTT-Server, subscribes for a topic and sends the messages
 * as events in OpenNetHome.
 *
 * @author Jocke G and Patrik Gustavsson
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MqttClient extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MqttClient\" Category=\"Ports\" >"
            + "  <Attribute Name=\"Port\" Type=\"String\" Get=\"getPort\" Set=\"setPort\" Default=\"1883\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" Set=\"setAddress\" />"
            + "  <Attribute Name=\"BaseTopic\" Type=\"String\" Get=\"getBaseTopic\" Set=\"setBaseTopic\" />"
            + "</HomeItem> ");

    /*
	 * Externally visible attributes
     */
    protected int port = 1883;
    protected String address = "tcp://localhost";
    protected String baseTopic = "MyHome/#";

    /*
	 * Internal attributes
     */
    private static Logger logger = Logger.getLogger(MqttClient.class.getName());
    protected org.eclipse.paho.client.mqttv3.MqttClient client;

    public MqttClient() {
    }

    public String getModel() {
        return MODEL;
    }

    public String getPort() {
        return String.valueOf(port);
    }

    public void setPort(String listenPort) {
        this.port = Integer.parseInt(listenPort);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address.trim();
        if(!address.startsWith("tcp://")){
            address = "tcp://" + address;
        }
        this.address = address;
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public void setBaseTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        try {
            client = new org.eclipse.paho.client.mqttv3.MqttClient(address + ":" + port, "OpenNetHomeServer-Sub", null);
            client.setCallback(new SubscribeCallback());
            client.connect();
            client.subscribe(baseTopic);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to connect to MQTT Server", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException ex) {
                logger.log(Level.WARNING, "MQTT refused to disconnect", ex);
            }
            client = null;
        }
    }

    public class SubscribeCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.fine("Message arrived. Topic: " + topic + " Message: " + message.toString());
            Event mqtt_message = server.createEvent("Mqtt_Message", "");
            mqtt_message.setAttribute("Direction", "In");
            mqtt_message.setAttribute("Mqtt.Topic", topic);
            mqtt_message.setAttribute("Mqtt.Message", message.toString());
            server.send(mqtt_message);
        }
        
        private boolean hasAction(HomeItemProxy homeItemProxy, String action) {
            return false;
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
        }
    }
}
