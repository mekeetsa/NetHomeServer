package nu.nethome.home.items.net.wemo;

/**
 *
 */
public class BridgeDevice {
    private final int deviceIndex;
    private final String deviceID;
    private final String friendlyName;
    private final String iconVersion;
    private final String firmwareVersion;
    private final String capabilityIDs;
    private final String currentState;
    private final int onState;
    private final int brightness;

    public BridgeDevice(int deviceIndex, String deviceID, String friendlyName, String iconVersion, String firmwareVersion, String capabilityIDs, String currentState) {
        this.deviceIndex = deviceIndex;
        this.deviceID = deviceID;
        this.friendlyName = friendlyName;
        this.iconVersion = iconVersion;
        this.firmwareVersion = firmwareVersion;
        this.capabilityIDs = capabilityIDs;
        this.currentState = currentState;
        String stateParts[] = currentState.split(",");
        if (stateParts.length >= 2) {
            if (stateParts[0].equals("1")) {
                onState = 1;
            } else {
                onState = 0;
            }
            String brightParts[] = stateParts[1].split(":");
            brightness = Integer.parseInt(brightParts[0]);
        } else {
            onState = -1;
            brightness = -1;
        }

    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getIconVersion() {
        return iconVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getCapabilityIDs() {
        return capabilityIDs;
    }

    public String getCurrentRawState() {
        return currentState;
    }

    public int getOnState() {
        return onState;
    }

    public int getBrightness() {
        return brightness;
    }
}
