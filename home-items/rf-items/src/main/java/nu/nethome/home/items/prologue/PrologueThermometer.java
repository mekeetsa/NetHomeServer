package nu.nethome.home.items.prologue;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.GenericThermometer;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@Plugin
@HomeItemType(value = "Thermometers", creationInfo = PrologueThermometer.CreationInfo.class)
public class PrologueThermometer extends GenericThermometer implements HomeItem {

    public static class CreationInfo implements AutoCreationInfo {
        @Override
        public String[] getCreationEvents() {
            return new String[]{"Prologue_Message"};
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return true;
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Prologue Thermometer, Ch: %d", e.getAttributeInt("Prologue.Channel") + 1);
        }
    }

    private static final String ADDRESS = (
            "  <Attribute Name=\"Channel\" Type=\"StringList\" Get=\"getAddress\" 	Set=\"setAddress\" >" +
            "    <item>1</item> <item>2</item> <item>3</item></Attribute>");

    public String getModel() {
        return String.format(MODEL, "PrologueThermometer", "Thermometers", ADDRESS);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("Prologue_Message") &&
                event.getAttributeInt("Prologue.Channel") + 1 == Integer.parseInt(address)) {
            update(event.getAttributeInt("Prologue.Temp"), event.getAttributeInt("Prologue.Battery") != 0);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        address = Integer.toString(event.getAttributeInt("Prologue.Channel") + 1);
        return true;
    }
}
