/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MqttValueLogger extends HomeItemAdapter implements ValueItem, HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ValueLogger\" Category=\"Gauges\"  >"
            + "  <Attribute Name=\"Value\" Type=\"String\" Get=\"getLastValue\" Default=\"true\" />"
            + "  <Attribute Name=\"ValueAction\" Type=\"Value\" Get=\"getValueAction\" 	Set=\"setValueAction\" />"
            + "  <Attribute Name=\"LogInterval\" Type=\"String\" Get=\"getLogInterval\" 	Set=\"setLogInterval\" />"
            + "  <Attribute Name=\"Topic\" Type=\"String\" Get=\"getTopic\" 	Set=\"setTopic\" />"
            + "  <Attribute Name=\"QOS\" Type=\"StringList\" Get=\"getQos\" 	Set=\"setQos\" >"
            + "     <item>0</item>  <item>1</item> <item>2</item></Attribute>"
            + "  <Attribute Name=\"Retain\" Type=\"Boolean\" Get=\"getRetain\" 	Set=\"setRetain\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(MqttValueLogger.class.getName());
    protected CommandLineExecutor executor;
    protected Timer timer;
    protected String valueAction = "";
    protected int logInterval = 60;
    protected String topic = "";
    protected int qos = 0;
    protected boolean retain;
    protected String lastValue = "";

	public String getModel() {
		return m_Model;
	}

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        timer = new Timer("MQTTValueLogger", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logValue();
            }
        }, 0, logInterval * 1000);
    }

    private void logValue() {
        String value = getValue();
        if (!value.isEmpty() && !value.equals(lastValue)) {
            Event mqtt_message = server.createEvent("Mqtt_Message", "");
            mqtt_message.setAttribute("Direction", "Out");
            mqtt_message.setAttribute("Mqtt.Topic", topic);
            mqtt_message.setAttribute("Mqtt.Message", value);
            mqtt_message.setAttribute("Mqtt.QOS", qos);
            mqtt_message.setAttribute("Mqtt.Retain", retain ? "Yes" : "No");
            lastValue = value;
        }
    }

    @Override
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public String getValueAction() {
        return valueAction;
    }

    public void setValueAction(String valueAction) {
        this.valueAction = valueAction;
    }

    @Override
    public String getValue() {
        String result = executor.executeCommandLine(getValueAction());
        String results[] = result.split(",");
        if (results.length != 3 || !results[0].equalsIgnoreCase("ok") || results[2].length() == 0) {
            return "";
        }
        return results[2].replace("%2C", ".");
    }

    public String getLogInterval() {
        return getIntAttribute(logInterval);
    }

    public void setLogInterval(String logInterval) throws IllegalValueException {
        this.logInterval = setIntAttribute(logInterval, 5, 15 * 60);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getQos() {
        return getIntAttribute(qos);
    }

    public void setQos(String qos) throws IllegalValueException {
        this.qos = setIntAttribute(qos, 0, 3);
    }

    public String getRetain() {
        return retain ? "Yes" : "No";
    }

    public void setRetain(String retain) {
        this.retain = retain.equalsIgnoreCase("Yes") || retain.equalsIgnoreCase("True");;
    }

    public String getLastValue() {
        return lastValue;
    }
}

