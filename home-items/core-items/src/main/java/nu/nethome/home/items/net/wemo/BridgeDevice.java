package nu.nethome.home.items.net.wemo;

/**
 *
 */
public class BridgeDevice {
    private final int deviceIndex;
    private final String deviceID;
    private final String FriendlyName;
    private final String iconVersion;
    private final String firmwareVersion;
    private final String capabilityIDs;
    private final String currentState;

    public BridgeDevice(int deviceIndex, String deviceID, String friendlyName, String iconVersion, String firmwareVersion, String capabilityIDs, String currentState) {
        this.deviceIndex = deviceIndex;
        this.deviceID = deviceID;
        FriendlyName = friendlyName;
        this.iconVersion = iconVersion;
        this.firmwareVersion = firmwareVersion;
        this.capabilityIDs = capabilityIDs;
        this.currentState = currentState;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getFriendlyName() {
        return FriendlyName;
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

    public String getCurrentState() {
        return currentState;
    }
}
