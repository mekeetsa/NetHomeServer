/**
 * Copyright (C) 2005-2013, Stefan Strömberg stefangs@nethome.nu>
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

import nu.nethome.home.item.Action;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/**
 * A base class for writing HomeGUI-plugins which present a portlet interface with HomeItems.
 * Contains helper methods for generating the portlets.
 *
 * @author Stefan Stromberg
 */
public abstract class PortletPage implements HomePageInterface {

    public static final String LOG_FILE_ATTRIBUTE = "LogFile";
    protected String localURL; // NYI - how to handle this

    public PortletPage(String mLocalURL) {
        localURL = mLocalURL;
    }

    public static String fromURL(String aURLFragment) {
        if (aURLFragment == null) {
            return null;
        }
        String result;
        String sIn = aURLFragment.replaceAll("[+]", "%2B");
        try {
            result = URLDecoder.decode(sIn, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }

    public List<String> getCssFileNames() {
        return null;
    }

    public List<String> getJavaScriptFileNames() {
        return null;
    }

    public boolean supportsEdit() {
        return false;
    }

    public List<EditControl> getEditControls() {
        return null;
    }

    public String getIconUrl() {
        return null;
    }

    protected void printColumnStart(PrintWriter p, boolean isLeft) {
        if (isLeft) {
            p.println("<div id=\"leftCol" + getPageNameURL () + "\" class=\"itemcolumn left\">");
        } else {
            p.println("<div id=\"rightCol" + getPageNameURL () + "\" class=\"itemcolumn\">");
        }
    }

    protected void printColumnEnd(PrintWriter p) throws ServletException, IOException {
        p.println("</div>");
    }

    protected void printItemPortletStart(PrintWriter p, String name, String link, String linkName) throws ServletException, IOException {
        p.println("<div class=\"portlet mod-feat\">");
        p.print(" <div class=\"header\"><h3>");
        if (link != null && linkName != null) {
            p.print(HTMLEncode.encode(name) + " " +
                    "<a href=\"" + link + "\">" + HTMLEncode.encode(linkName) + "</a>");
        } else if (link != null) {
            p.print("<a href=\"" + link + "\">" + HTMLEncode.encode(name) + "</a>");
        } else {
            p.print(HTMLEncode.encode(name));
        }
        p.println("</h3></div>");
        p.println(" <div class=\"content\">");
    }

    protected void printItemPortletEnd(PrintWriter p, String itemId, String addLink, String subpage, Map<String, CategorizedItemList> categories) throws ServletException, IOException {
        if (addLink != null) {
            String subpageArgument = subpage != null ? ("&subpage=" + subpage) : "";
            p.println("  <form action=\"" + localURL + "?page=" + getPageNameURL() + subpageArgument + "&mode=edit\" method=\"POST\">");
            p.println("  <input type=\"hidden\" name=\"a\" value=\"move\">");
            p.println("  <input type=\"hidden\" name=\"to\" value=\"" + itemId + "\">");
            p.println("  <div class=\"bottomlink\"><a href=\"" + addLink + "\"><img src=\"web/home/item_new16.png\" />Add new Item...</a>");
            p.println("  <select   onchange=\"this.form.submit()\" name=\"name\">");
            p.println("  <option value=\"\">Add existing Item</option>");
            for (String category : HomeItemModel.HOME_ITEM_CATEGORIES) {
                if (categories.containsKey(category)) {
                    CategorizedItemList itemsInCategory = categories.get(category);
                    p.println("  <optgroup label=\"" + category + "\">");
                    for (HomeItemProxy item : itemsInCategory.getItems()) {
                        p.println("  <option value=\""
                                + item.getAttributeValue("ID")
                                + "\""
                                + ">" + item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE)
                                + "</option>");
                    }
                    p.println("  </optgroup>");
                }
            }
            p.println("  </select>");
            p.println("  </div>");
            p.println("  </form>");

        }
        p.println(" </div>");
        p.println("</div>");
    }

    protected void printRoom(PrintWriter p, String page, String subpage, String itemName, String headerLink, String addLink, String itemNames[], HomeService server, boolean includeActions, Map<String, CategorizedItemList> categories) throws ServletException, IOException {

        // View a combined graph of those subitems which has a log file
        String headerLinkName = null;
        if( headerLink == null ) {
            boolean hasGraph = false;
            StringBuilder url = new StringBuilder(256);
            url.append(localURL + "?page=graphs&subpage=");
            for (String name : itemNames) {
                HomeItemProxy item = server.openInstance(name);
                if (item != null && hasLogFile(item)) {
                    if( hasGraph ) {
                        url.append("-");
                    }
                    hasGraph = true;
                    url.append(item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
                }
            }
            if (hasGraph) {
                headerLinkName = "(view graph...)";
                headerLink = url.toString();
            }
        }

        // Start Room Portlet
        printItemPortletStart(p, itemName, headerLink, headerLinkName);

        // List all instances in the room
        for (String name : itemNames) {

            // Open the instance
            HomeItemProxy item = server.openInstance(name);
            if (item == null) continue;

            // Print instance
            printHomeItem(p, item, page, subpage, includeActions);
        }

        // End Portlet
        printItemPortletEnd(p, getItemId(itemName, server), addLink, subpage, categories);
    }

    private String getItemId(String itemName, HomeService server) {
        String itemId = "";
        if (itemName != null && !itemName.isEmpty()) {
            final HomeItemProxy roomItem = server.openInstance(itemName);
            itemId = roomItem != null ? roomItem.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) : "";
        }
        return itemId;
    }

    /**
     * Prints a HomeItem instance to the output stream.
     *
     * @param p              Output stream
     * @param item           HomeItem to print
     * @param page           Name of the current page
     * @param subpage        sub page identity
     * @param includeActions print actions of the home item
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void printHomeItem(PrintWriter p,
                               HomeItemProxy item, String page, String subpage, boolean includeActions) throws ServletException, IOException {

        HomeItemModel model = item.getModel();
        String defaultAttributeValue = "";
        String defaultAttributeUnit = "";
        boolean hasDefaultAttribute = model.getDefaultAttribute() != null;
        if (hasDefaultAttribute) {
            defaultAttributeValue = item.getAttributeValue(model.getDefaultAttribute().getName());
            if (defaultAttributeValue.length() > 0) {
                String unit = model.getDefaultAttribute().getUnit();
                defaultAttributeUnit  = " data-unit=\"" + unit + "\"";
                defaultAttributeValue += " " + unit;
            }
        }

        // Category
        String category = model.getCategory();
        String arrowIconAttributes = "";
        String arrowIconImageClass = arrowIcon(category);
        if (category.equals("Lamps")) {
            if (item.getAttributeValue("State").equals("Off")) {
                arrowIconImageClass = "lamp_off";
            } else {
                arrowIconImageClass = "lamp_on";
            }
            arrowIconAttributes = " data-item=\"" + item.getAttributeValue("ID") + "\" data-On=\"lamp_on\" data-Off=\"lamp_off\" data-lastclass=\"" + arrowIconImageClass + "\"";
        } else {
            arrowIconAttributes = " data-item=\"" + item.getAttributeValue("ID") + "\"";
        }

        if( model.getDefaultAction().length() > 0 ) {
            arrowIconImageClass += " has_href";
            arrowIconAttributes += " onclick=\"callItemAction('"
                + item.getAttributeValue("ID") + "','" + model.getDefaultAction() + "');\"";
        }
        else if (model.getClassName().equals("Plan")) {
            arrowIconImageClass += " has_href";
            arrowIconAttributes += " onclick=\"window.open('" + localURL + "?page=plan&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "','_self');\"";
        } else if (model.getCategory().equals("Infrastructure")) {
            arrowIconImageClass += " has_href";
            arrowIconAttributes += " onclick=\"window.open('" + localURL + "?page=rooms&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "','_self');\"";
        } else if (hasLogFile(item)) {
            arrowIconImageClass += " has_href";
            arrowIconAttributes += " onclick=\"window.open('" + localURL + "?page=graphs&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "','_self');\"";
        }

        HomeUrlBuilder url = new HomeUrlBuilder(localURL).addParameter("page", "edit")
                .addParameter("name", HomeGUI.toURL(item.getAttributeValue("ID")))
                .addParameter("return", page).addParameterIfNotNull("returnsp", subpage);

        p.println("   <div id=\"hi-" + item.getAttributeValue("ID")
            + "\" class=\"homeitem\">");
        p.println("    <img class=\"hi_divider\" src=\"web/home/item_divider.png\" />");
        p.println("    <div class=\"homeiteminfo\">");
        p.println("     <div class=\"homeitemli\" id=\"hili-" + item.getAttributeValue("ID") + "\">");
        p.println("       <a href=\"" + url.toString() + "\">" + item.getAttributeValue("Name") + "</a>");
        if( hasDefaultAttribute && !defaultAttributeValue.isEmpty() ) {
            p.println("       <div class=\"valuedivider\"></div>");
            p.println("       <div data-item=\"" + item.getAttributeValue("ID") + "\""
                + defaultAttributeUnit + " class=\"itemvalue\">" + defaultAttributeValue + "</div>");
        }
        p.println("     </div>");
        if (includeActions) {
            printItemActions(p, item, page, subpage, model);
        }
        p.println("    </div>");
        // the icon is on the right by default (should be float left on a large display)
        // (the reason is the z-order on a mobile device) 
        p.println("    <div id=\"icon-" + item.getAttributeValue("ID")
            + "\" class=\"icon " + arrowIconImageClass
            + "\"" + arrowIconAttributes + "></div>");
        p.println("    <div data-item=\"" + item.getAttributeValue("ID") + "\""
            + defaultAttributeUnit + " class=\"itemvalue\">" + defaultAttributeValue + "</div>");
        p.println("   </div>");
    }

    private static String arrowIcon(String itemType) {
        if (itemType.equals("Lamps")) {
            return "lamp_off";
        }
        if (itemType.equals("Timers")) {
            return "timer";
        }
        if (itemType.equals("Ports")) {
            return "port";
        }
        if (itemType.equals("GUI")) {
            return "gui";
        }
        if (itemType.equals("Hardware")) {
            return "hw";
        }
        if (itemType.equals("Controls")) {
            return "control";
        }
        if (itemType.equals("Gauges")) {
            return "gauge";
        }
        if (itemType.equals("Thermometers")) {
            return "temp";
        }
        if (itemType.equals("Infrastructure")) {
            return "house";
        }
        if (itemType.equals("Actuators")) {
            return "actuator";
        }
        return "item.png";
    }

    private void printItemActions(PrintWriter p, HomeItemProxy item, String page, String subpage, HomeItemModel model) {
        p.println("	   <div class=actions>");
        if (model.getClassName().equals("Plan")) {
            p.println("		  <div class=\"act_gotoLoc default\"><a href=\"" + localURL + "?page=plan&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">Go to location...</a></div>");
        } else if (model.getCategory().equals("Infrastructure")) {
            p.println("		  <div class=\"act_gotoLoc default\"><a href=\"" + localURL + "?page=rooms&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">Go to location...</a></div>");
        } else if (hasLogFile(item)) {
            p.println("		  <div class=\"act_viewGraph default\"><a href=\"" + localURL + "?page=graphs&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">View graph...</a></div>");
        }
        List<Action> actions = model.getActions();
        int size = 0;
        // First, print the default action (if any)
        if( model.getDefaultAction().length() > 0 ) {
            p.println("		  <div class=\"act_" + model.getDefaultAction() + " default\""
                + "><a href=\"javascript:void(0);\" onclick=\"callItemAction('"
                + item.getAttributeValue("ID") + "','" + model.getDefaultAction() + "');\">"
                + model.getDefaultAction() + "</a></div>");
        }
        // Print actions which are not default
        for (Action action : actions) {
            if (size > 60) break;
            if( action.getName().equals( model.getDefaultAction() ) ) {
                continue;
            }
            p.println("		  <div class=\"act_" + action.getName() + "\""
                + "><a href=\"javascript:void(0);\" onclick=\"callItemAction('"
                + item.getAttributeValue("ID") + "','" + action.getName() + "');\">"
                + action.getName() + "</a></div>");
            size += action.getName().length() + 2;
        }
        p.println("	   </div>");
    }

    private String getItemIconUrl(HomeItemModel model, String defaultAttributeValue) {
        String category = model.getCategory();
        String icon = HomeGUI.itemIcon(category, false);
        if (category.equals("Lamps")) {
            if (defaultAttributeValue != null && !defaultAttributeValue.isEmpty() && !defaultAttributeValue.trim().equalsIgnoreCase("Off")) {
                icon = "lamp_on.png";
            } else {
                icon = "lamp_off.png";
            }
        }
        return "web/home/" + icon;
    }

    private boolean hasLogFile(HomeItemProxy item) {
        return item.getAttributeValue(LOG_FILE_ATTRIBUTE).length() > 0;
    }

    protected void printRedirectionScript(PrintWriter p, String url) {
        p.println("<script type=\"text/javascript\">location.href=\"" + url + "\"</script>");
    }
}
