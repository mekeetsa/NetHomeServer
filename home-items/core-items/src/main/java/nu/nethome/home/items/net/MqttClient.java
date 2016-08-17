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
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\"  Default=\"true\" />"
            + "  <Attribute Name=\"Port\" Type=\"String\" Get=\"getPort\" Set=\"setPort\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" Set=\"setAddress\" />"
            + "  <Attribute Name=\"BaseTopic\" Type=\"String\" Get=\"getBaseTopic\" Set=\"setBaseTopic\" />"
            + "</HomeItem> ");
    public static final String MQTT_MESSAGE_TYPE = "Mqtt_Message";
    public static final String MQTT_MESSAGE = "Mqtt.Message";
    public static final String MQTT_TOPIC = "Mqtt.Topic";
    public static final String MQTT_QOS = "Mqtt.QOS";
    public static final String MQTT_RETAIN = "Mqtt.Retain";

    /*
	 * Externally visible attributes
     */
    protected int port = 1883;
    protected String address = "tcp://test.mosquitto.org";
    protected String baseTopic = "MyHome/#";

    /*
	 * Internal attributes
     */
    private static Logger logger = Logger.getLogger(MqttClient.class.getName());
    protected org.eclipse.paho.client.mqttv3.MqttClient client;
    private boolean connected = false;

    public MqttClient() {
    }

    public String getModel() {
        return MODEL;
    }

    public String getPort() {
        return String.valueOf(port);
    }

    public void setPort(String listenPort) {
        final int newPort = Integer.parseInt(listenPort);
        if (this.port != newPort && isActivated()) {
            disconnect();
            this.port = newPort;
            connect(true);
        }
        this.port = newPort;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address.trim();
        if(!address.startsWith("tcp://")){
            address = "tcp://" + address;
        }
        if (!address.equals(this.address) && isActivated()) {
            disconnect();
            this.address = address;
            connect(true);
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
        connect(true);
    }

    private void connect(boolean doLog) {
        try {
            client = new org.eclipse.paho.client.mqttv3.MqttClient(address + ":" + port, "OpenNetHomeServer-Sub", null);
            client.setCallback(new SubscribeCallback());
            client.connect();
            client.subscribe(baseTopic);
            connected = true;
        } catch (Exception e) {
            if (doLog) {
                logger.log(Level.WARNING, "Failed to connect to MQTT Server", e);
            }
            connected = false;
        }
    }

    @Override
    public void stop() {
        super.stop();
        disconnect();
    }

    private void disconnect() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException ex) {
                logger.log(Level.WARNING, "MQTT refused to disconnect", ex);
            }
            client = null;
        }
    }

    public boolean receiveEvent(Event event) {
        if (event.isType("MinuteEvent") && !connected) {
            disconnect();
            connect(false);
            return true;
        } else if (event.isType(MQTT_MESSAGE_TYPE) && event.getAttribute("Direction").equals("Out") && connected) {
            final MqttMessage mqttMessage = new MqttMessage(event.getAttribute(MQTT_MESSAGE).getBytes());
            if (event.hasAttribute(MQTT_QOS)) {
                mqttMessage.setQos(event.getAttributeInt(MQTT_QOS));
            }
            if (event.hasAttribute(MQTT_RETAIN)) {
                mqttMessage.setRetained(event.getAttribute(MQTT_RETAIN).equalsIgnoreCase("yes"));
            }
            try {
                client.publish(event.getAttribute(MQTT_TOPIC), mqttMessage);
            } catch (MqttException e) {
                connected = false;
                logger.log(Level.WARNING, "Failed to send MQTT-message", e);
            };
        }
        return false;
    }

    public String getState() {
        return connected ? "Connected" : "Disconnected";
    }

    public class SubscribeCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            connected = false;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            logger.fine("Message arrived. Topic: " + topic + " Message: " + message.toString());
            Event mqtt_message = server.createEvent(MQTT_MESSAGE_TYPE, "");
            mqtt_message.setAttribute("Direction", "In");
            mqtt_message.setAttribute(MQTT_TOPIC, topic);
            mqtt_message.setAttribute(MQTT_MESSAGE, message.toString());
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
