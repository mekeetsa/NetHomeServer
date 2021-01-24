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


package nu.nethome.home.items.web.servergui;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * 
 * ActionButton
 * 
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("GUI")
public class ActionButton extends HomeItemAdapter implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ActionButton\" Category=\"GUI\" >"
			+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\"/>"
			+ "  <Attribute Name=\"PushAction\" Type=\"Command\" Get=\"getPushAction\" 	Set=\"setPushAction\" />"
			+ "  <Attribute Name=\"PushAction2\" Type=\"Command\" Get=\"getPushAction2\" 	Set=\"setPushAction2\" />"
			+ "  <Attribute Name=\"Icon\" Type=\"MediaFile\" Get=\"getIcon\" 	Set=\"setIcon\" />"
			+ "  <Attribute Name=\"ClickIcon\" Type=\"MediaFile\" Get=\"getClickIcon\" 	Set=\"setClickIcon\" />"
			+ "  <Attribute Name=\"ClickSound\" Type=\"MediaFile\" Get=\"getClickSound\" 	Set=\"setClickSound\" />"
			+ "  <Attribute Name=\"Text\" Type=\"String\" Get=\"getText\" 	Set=\"setText\" />"
			+ "  <Attribute Name=\"Title\" Type=\"String\" Get=\"getTitle\" 	Set=\"setTitle\" />"
            + "  <Action Name=\"pushToggle\" 	Method=\"performPushToggle\" Default=\"true\" />"
            + "  <Action Name=\"pushAction\" 	Method=\"performPushAction\" />"
            + "  <Action Name=\"pushAction2\" 	Method=\"performPushAction2\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(ActionButton.class.getName());
    private CommandLineExecutor executor;

	// Public attributes
        private boolean state = false;
	private String pushAction = "";
	private String pushAction2 = "";
	private String icon = "media/button_icon.png";
	private String clickIcon = "media/button_icon_down.png";
	private String clickSound = "";
	private String text = "";
	private String title = "";

	public String getModel() {
		return MODEL;
	}

    public void activate(HomeService server) {
           super.activate(server);
   		executor = new CommandLineExecutor(server, true);
   	}

    public String getState() {
        return state ? "On" : "Off";
    }

    public void performPushToggle() {
        executor.executeCommandLine( state ? pushAction2 : pushAction );
        state = ! state;
    }

    public void performPushAction() {
        executor.executeCommandLine(pushAction);
        state = true;
    }

    public void performPushAction2() {
        executor.executeCommandLine(pushAction2);
        state = false;
    }

    public String getPushAction() {
        return pushAction;
    }

    public void setPushAction(String pushAction) {
        this.pushAction = pushAction;
    }

    public String getPushAction2() {
        return pushAction2;
    }

    public void setPushAction2(String pushAction2) {
        this.pushAction2 = pushAction2;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClickIcon() {
        return clickIcon;
    }

    public void setClickIcon(String clickIcon) {
        this.clickIcon = clickIcon;
    }

    public String getClickSound() {
        return clickSound;
    }

    public void setClickSound(String clickSound) {
        this.clickSound = clickSound;
    }
}
