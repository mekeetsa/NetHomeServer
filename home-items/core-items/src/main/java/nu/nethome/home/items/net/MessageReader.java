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
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

import static nu.nethome.home.items.net.Message.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MessageReader extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"MessageReader\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"Message\" Type=\"Text\" Get=\"getMessage\" 	Set=\"setMessage\" />"
            + "  <Attribute Name=\"Command\" Type=\"Command\" Get=\"getCommand\" 	Set=\"setCommand\" />"
            + "</HomeItem> ");

	private static Logger logger = Logger.getLogger(MessageReader.class.getName());
    private String command;
    private String message;
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
        if (isOutgoingMessage(event)) {
            return processMessage(event.getAttribute(TO), event.getAttribute(BODY));
        }
        return false;
    }

    private boolean isOutgoingMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(MESSAGE_TYPE) &&
                event.getAttribute(DIRECTION).equals(IN_BOUND);
    }

    private boolean processMessage(String to, String body) {
        boolean hasSent = false;
            // Do Stuff
        return hasSent;
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
