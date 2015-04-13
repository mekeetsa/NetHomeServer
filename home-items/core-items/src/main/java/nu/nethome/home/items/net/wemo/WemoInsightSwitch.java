package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Belkin wemo insight switch
 *
 * @author Stefan
 *         Todo: Move out soap client
 *         Todo: Handle lost connection
 *         Todo: timePeriod
 *         Todo: todayMW
 *         Todo: powerThresholdMW
 *         Todo: lastChange (??)
 *         Todo: subscribe for state changes
 */

@Plugin
@HomeItemType(value = "Lamps", creationInfo = WemoInsightSwitch.WemoCreationInfo.class)
public class WemoInsightSwitch extends HomeItemAdapter implements HomeItem {

    public static final String UPN_P_CREATION_MESSAGE = "UPnP_Creation_Message";
    public static final int UPDATE_RETRY_ATTEMPTS = 4;
    public static final int READ_RETRY_ATTEMPTS = 2;

    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UPN_P_CREATION_MESSAGE};

        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("DeviceType").equals("urn:Belkin:device:insight:1");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Insight Switch: \"%s\", SerialNr: %s", e.getAttribute("FriendlyName"), e.getAttribute("SerialNumber"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"SerialNumber\" Type=\"String\" Get=\"getSerialNumber\" 	Init=\"setSerialNumber\" />"
            + "  <Attribute Name=\"CurrentConsumption\" Type=\"String\" Get=\"getCurrentPowerConsumption\" 	Unit=\"W\" />"
            + "  <Attribute Name=\"LastOnTime\" Type=\"Duration\" Get=\"getLastOnTime\" />"
            + "  <Attribute Name=\"OnTimeToday\" Type=\"Duration\" Get=\"getOnTimeToday\" />"
            + "  <Attribute Name=\"TotalOnTime\" Type=\"Duration\" Get=\"getTotalOnTime\" />"
            + "  <Attribute Name=\"TotalConsumption\" Type=\"String\" Get=\"getTotalPowerConsumption\" 	Unit=\"kWh\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");
    public static final int TIME_TO_CACHE_STATE = 300;

    private static Logger logger = Logger.getLogger(WemoInsightSwitch.class.getName());
    private WemoInsightSwitchClient insightSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");
    private InsightState currentState;
    private long lastStateUpdate = 0;
    private String wemoDescriptionUrl = "";

    // Public attributes
    private String serialNumber = "";

    public String getModel() {
        return MODEL;
    }

    WemoInsightSwitchClient getInsightSwitch() {
        return insightSwitch;
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(UPN_P_CREATION_MESSAGE) &&
                event.getAttribute("DeviceType").equals("urn:Belkin:device:insight:1") &&
                event.getAttribute("SerialNumber").equals(serialNumber)) {
            setDeviceURL(event.getAttribute("Location"));
            return true;
        }
        return handleInit(event);
    }

    @Override
    protected boolean initAttributes(Event event) {
        setDeviceURL(event.getAttribute("Location"));
        serialNumber = event.getAttribute("SerialNumber");
        return true;
    }

    private String extractBaseUrl(String url) {
        int pos = url.indexOf("/", 9);
        if (pos > 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    public String getState() {
        updateCurrentState();
        if (currentState == null) {
            return "";
        } else if (currentState.getState() == InsightState.State.On) {
            return "On " + getCurrentPowerConsumption();
        } else if (currentState.getState() == InsightState.State.Idle) {
            return "Idle";
        }
        return "Off";
    }

    public String getDeviceURL() {
        return wemoDescriptionUrl;
    }

    public void setDeviceURL(String url) {
        wemoDescriptionUrl = url;
        getInsightSwitch().setWemoURL(extractBaseUrl(url));
    }

    public void on() {
        logger.fine("Switching on " + name);
        setOnState(true);
    }

    public void off() {
        logger.fine("Switching off " + name);
        setOnState(false);
    }

    private void setOnState(boolean isOn) {
        for (int retry = 0; retry < UPDATE_RETRY_ATTEMPTS; retry++) {
            try {
                getInsightSwitch().setOnState(isOn);
                lastStateUpdate = 0;
                return;
            } catch (WemoException e) {
                logger.log(Level.FINE, "Failed to set on state in " + wemoDescriptionUrl, e);
            }
        }
        logger.log(Level.INFO, String.format("Failed to set on state in %s after %d retries", name, UPDATE_RETRY_ATTEMPTS));
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        updateCurrentState();
        if (currentState == null) {
            return;
        }
        if (currentState.getState() == InsightState.State.Off) {
            on();
        } else {
            off();
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCurrentPowerConsumption() {
        updateCurrentState();
        return currentState != null ? String.format("%.1f", currentState.getCurrentConsumption()) : "";
    }

    public String getTotalPowerConsumption() {
        updateCurrentState();
        return currentState != null ? String.format("%.3f", currentState.getTotalConsumption()) : "";
    }

    public String getTotalOnTime() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getTotalOnTime()) : "";
    }

    public String getLastOnTime() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getLastOnTime()) : "";
    }

    public String getOnTimeToday() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getOnTimeToday()) : "";
    }

    private void updateCurrentState() {
        if (System.currentTimeMillis() - lastStateUpdate > TIME_TO_CACHE_STATE) {
            for (int retry = 0; retry < READ_RETRY_ATTEMPTS; retry++) {
                try {
                    currentState = getInsightSwitch().getInsightParameters();
                    lastStateUpdate = System.currentTimeMillis();
                    return;
                } catch (WemoException e) {
                    logger.log(Level.FINE, "Failed to get from " + wemoDescriptionUrl, e);
                    currentState = null;
                }
            }
            logger.log(Level.INFO, String.format("Failed to get state in %s after %d retries", name, READ_RETRY_ATTEMPTS));
        }
    }
}
