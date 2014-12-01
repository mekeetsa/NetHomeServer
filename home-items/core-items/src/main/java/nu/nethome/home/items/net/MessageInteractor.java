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
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MessageInteractor extends Message implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"MessageReader\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"Subject\" Type=\"String\" Get=\"getSubject\" 	Set=\"setSubject\" />"
            + "  <Attribute Name=\"Reply\" Type=\"Text\" Get=\"getMessage\" 	Set=\"setMessage\" />"
            + "  <Attribute Name=\"TriggerText\" Type=\"String\" Get=\"getTriggerText\" 	Set=\"setTriggerText\" />"
            + "  <Attribute Name=\"Command\" Type=\"Command\" Get=\"getCommand\" 	Set=\"setCommand\" />"
            + "</HomeItem> ");

	private static Logger logger = Logger.getLogger(MessageInteractor.class.getName());
    private String command = "";
    private String triggerText = "";
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
        if (isInboundMessage(event)) {
            return processMessage(event.getAttribute(FROM), event.getAttribute(BODY));
        }
        return false;
    }

    private boolean isInboundMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(MESSAGE_TYPE) &&
                event.getAttribute(DIRECTION).equals(IN_BOUND);
    }

    private boolean processMessage(String from, String body) {
        if (body.contains(triggerText)) {
            commandLineExecutor.executeCommandLine(command);
            if (!getMessage().isEmpty()) {
                sentTo(from);
            }
            return true;
        } else {
            return false;
        }
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTriggerText() {
        return triggerText;
    }

    public void setTriggerText(String triggerText) {
        this.triggerText = triggerText;
    }
}
