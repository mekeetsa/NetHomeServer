/**
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

package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.misc.ArpScanner;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MacDevice extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MacDevice\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"MacAddress\" Type=\"String\" Get=\"getMacAddress\" 	Set=\"setMacAddress\" />"
            + "  <Attribute Name=\"Command\" Type=\"Command\" Get=\"getCommand\" 	Set=\"setCommand\" />"
            + "  <Attribute Name=\"ActionWhileAbsent\" Type=\"Command\" Get=\"getActionWhileAbsent\" 	Set=\"setActionWhileAbsent\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MacDevice.class.getName());
    private String command = "";
    private String macAddress = "";
    private String actionWhileAbsent = "";
    private List<String> triggerTexts = new ArrayList<>();
    private CommandLineExecutor commandLineExecutor;

    public String getModel() {
        return MODEL;
    }

    public void activate(HomeService service) {
        super.activate(service);
        commandLineExecutor = new CommandLineExecutor(service, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isMacMessage(event)) {
            return processEvent(event);
        }
        return false;
    }

    private boolean processEvent(Event event) {
        boolean ourMacIsPresent = false;
        for (String mac : event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).split(",")) {
            if (!mac.isEmpty() && mac.equals(this.macAddress)) {
                ourMacIsPresent = true;
                break;
            }
        }
        if (!ourMacIsPresent) {
            commandLineExecutor.executeCommandLine(actionWhileAbsent);
        }

        return false;
    }

    private boolean isMacMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(ArpScanner.ARP_SCAN_MESSAGE);
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getActionWhileAbsent() {
        return actionWhileAbsent;
    }

    public void setActionWhileAbsent(String actionWhileAbsent) {
        this.actionWhileAbsent = actionWhileAbsent;
    }
}
