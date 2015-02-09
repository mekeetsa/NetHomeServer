package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.net.wemo.soap.WemoInsightSwitchClient;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a switch (typically connected to a lamp) which is controlled by
 * the Zhejiang RF protocol. The ZhejiangLamp requires a port which can send Zhejiang
 * protocol messages as RF signals. This is typically done with the AudioProtocolTransmitter
 * <p/>
 * <br>
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "WemoInsightSwitch_Message")
public class WemoInsightSwitch extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoInsightSwitch.class.getName());
    private WemoInsightSwitchClient insightSwitch = new WemoInsightSwitchClient("");

    // Public attributes
    private boolean state = false;
    private String deviceURL = "http://192.168.1.16:49153";

    public String getModel() {
        return MODEL;
    }

    public String getState() {
        if (state) {
            return "On";
        }
        return "Off";
    }

    public String getDeviceURL() {
        return deviceURL;
    }

    public void setDeviceURL(String button) {
        deviceURL = button;
        insightSwitch = new WemoInsightSwitchClient(deviceURL);
    }

    public void on() {
        logger.fine("Switching on " + name);
        try {
            insightSwitch.on();
        } catch (WemoInsightSwitchClient.WemoException e) {
            logger.warning("Failed to contact Wemo device: " + e.getMessage());
        }
        state = true;
    }

    public void off() {
        logger.fine("Switching off " + name);
        try {
            insightSwitch.off();
        } catch (WemoInsightSwitchClient.WemoException e) {
            logger.warning("Failed to contact Wemo device: " + e.getMessage());
        }
        state = false;
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        state = !state;
        if (state) {
            on();
        } else {
            off();
        }
    }
}
