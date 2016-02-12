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

/*
 * History:
 * 2010-10-31 pela Changed ServletOutputStream to PrintWriter to support UTF-8 and encodings properly
 */
package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RoomsPage extends PortletPage {

    DefaultPageIdentity defaultLocation;

    public RoomsPage(String mLocalURL, DefaultPageIdentity defaultPageIdentity) {
        super(mLocalURL);
        defaultLocation = defaultPageIdentity;
    }

    public List<String> getCssFileNames() {
        return null;
    }

    @Override
    public boolean supportsEdit() {
        return true;
    }

    @Override
    public List<EditControl> getEditControls() {
        return Arrays.<EditControl>asList(
                new EditControlAdapter("<a href=\"javascript:gotoRoomEditPage();\">" +
                        "<img src=\"web/home/door_new16.png\" />&nbsp;</a></td><td><a href=\"" +
                        "javascript:gotoRoomEditPage();\">Add new Room...</a>"),
                new EditControlAdapter("<a href=\"" + localURL + "?page=edit&a=create&mode=edit&class_name=Location\">" +
                        "<img src=\"web/home/door_new16.png\" />&nbspAdd new Location...</a>"));
    }

    public List<String> getJavaScriptFileNames() {
        return Arrays.asList("web/home/rooms.js");
    }

    public String getPageName() {
        return "Rooms";
    }

    public String getPageNameURL() {
        return "rooms";
    }

    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        PrintWriter p = res.getWriter();
        HomeGUIArguments arguments = new HomeGUIArguments(req);
        if (arguments.isAction("move")) {
            moveItemToRoom(server, arguments.getName(), req.getParameter("to"));
        }
        HomeItemProxy viewedLocation = findLocation(server, arguments, defaultLocation);
        String viewedLocationId = viewedLocation.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE);
        printPageData(p, viewedLocationId);
        List<HomeItemProxy> leftColumnRooms = new LinkedList<>();
        List<HomeItemProxy> rightColumnRooms = new LinkedList<>();
        if (viewedLocation.getModel().getClassName().equals("Location")) {
            distributeRoomsOverColumns(server, viewedLocation, leftColumnRooms, rightColumnRooms);
        } else {
            leftColumnRooms.add(viewedLocation);
        }
        Map<String, CategorizedItemList> categories = null;
        if (arguments.isEditMode()) {
            categories = CategorizedItemList.categorizeItems(server);
        }
        printRoomColumn(server, p, leftColumnRooms, viewedLocationId, arguments.isEditMode(), categories, true);
        printRoomColumn(server, p, rightColumnRooms, viewedLocationId, arguments.isEditMode(), categories, false);
    }

    private void moveItemToRoom(HomeService server, String item, String room) {
        final HomeItemProxy itemProxy = server.openInstance(item);
        final HomeItemProxy roomProxy = server.openInstance(room);
        if (itemProxy != null && roomProxy != null) {
            final String[] roomItems = roomProxy.getAttributeValue("Items").split(",");
            String result = "";
            String separator = "";
            for (String roomItem : roomItems) {
                if (!roomItem.equals(item)) {
                    result += separator;
                    result += roomItem;
                    separator = ",";
                }
            }
            result += separator + item;
            try {
                roomProxy.setAttributeValue("Items", result);
            } catch (IllegalValueException e) {
                // Ignore
            }
        }
    }

    private void distributeRoomsOverColumns(HomeService server, HomeItemProxy viewedLocation, List<HomeItemProxy> leftColumnRooms, List<HomeItemProxy> rightColumnRooms) {
        for (String roomIdentity : viewedLocation.getAttributeValue("Items").split(",")) {
            HomeItemProxy item = server.openInstance(roomIdentity);
            if (item != null) {
                if (item.getAttributeValue("Position").equals("1") ||
                        item.getAttributeValue("Position").equals("Left")) {
                    leftColumnRooms.add(item);
                } else {
                    rightColumnRooms.add(item);
                }
            }
        }
    }

    private void printPageData(PrintWriter p, String subPage) {
        p.println("<script> ");
        p.println("homeManager.location=\"" + subPage + "\";");
        p.println("</script>");
    }


    private void printRoomColumn(HomeService server, PrintWriter p, List<HomeItemProxy> rooms, String returnSubPage, boolean editMode, Map<String, CategorizedItemList> categories, boolean isLeftColumn) throws ServletException, IOException {
        printColumnStart(p, isLeftColumn);

        for (HomeItemProxy room : rooms) {
            String itemNames[] = room.getAttributeValue("Items").split(",");
            String headerLink = localURL + "?page=edit&name=" + room.getAttributeValue("ID") + "&return=" + this.getPageNameURL();
            String addLink = localURL + "?page=edit&room=" + room.getAttributeValue("ID") + "&return=" + this.getPageNameURL();
            if (returnSubPage != null) {
                addLink += "&returnsp=" + returnSubPage;
                headerLink += "&returnsp=" + returnSubPage;
            }
            if (editMode) {
                addLink += "&mode=edit";
                headerLink += "&mode=edit";
            }
            printRoom(p, "rooms", returnSubPage, room.getAttributeValue("Name"), editMode ? headerLink : null,
                    editMode ? addLink : null, itemNames, server, true, categories);
        }

        printColumnEnd(p);
    }

    private HomeItemProxy findLocation(HomeService server, HomeGUIArguments arguments, DefaultPageIdentity defaultLocationIdentity) {
        HomeItemProxy foundLocationItem = null;
        if (arguments.hasSubpage()) {
            foundLocationItem = server.openInstance(arguments.getSubpage());
        }
        if (foundLocationItem == null) {
            foundLocationItem = server.openInstance(defaultLocationIdentity.getDefaultPage());
        }
        if (foundLocationItem == null) {
            foundLocationItem = server.createInstance("Location", "DefaultLocation");
            int counter = 0;
            while(foundLocationItem == null && counter < 100) {
                foundLocationItem = server.createInstance("Location", "DefaultLocation" + counter++);
            }
            if (foundLocationItem != null) {
                defaultLocationIdentity.setDefaultPage(foundLocationItem.getAttributeValue("ID"));
                addAllRooms(foundLocationItem, server);
            }
        }
        return foundLocationItem;
    }

    private void addAllRooms(HomeItemProxy foundLocationItem, HomeService server) {

        List<DirectoryEntry> directoryEntries = server.listInstances("");
        StringBuilder result = new StringBuilder();
        String separator = "";
        for (DirectoryEntry directoryEntry : directoryEntries) {
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            if (model.getClassName().equals("Room")) {
                result.append(separator);
                result.append(item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
                separator = ",";
            }
        }
        try {
            foundLocationItem.setAttributeValue("Items", result.toString());
        } catch (IllegalValueException e) {
            // Nothing to do
        }
    }
}
