package nu.nethome.home.items.zwave;


import nu.nethome.home.item.HomeItemAdapter;

public class ZWaveNode extends HomeItemAdapter{

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveNode\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" />"
            + "</HomeItem> ");

    @Override
    public String getModel() {
        return MODEL;
    }
}
