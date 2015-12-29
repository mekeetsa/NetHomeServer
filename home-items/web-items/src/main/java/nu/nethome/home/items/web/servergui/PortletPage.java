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

    public List<String> getEditControls() {
        return null;
    }

    public String getIconUrl() {
        return null;
    }

    protected void printColumnStart(PrintWriter p, boolean isLeft) {
        if (isLeft) {
            p.println("<div class=\"itemcolumn left\">");
        } else {
            p.println("<div class=\"itemcolumn\">");
        }
    }

    protected void printColumnEnd(PrintWriter p) throws ServletException, IOException {
        p.println("</div>");
    }

    protected void printItemPortletStart(PrintWriter p, String name, String link) throws ServletException, IOException {
        p.println("<div class=\"portlet mod-feat\">");
        if (link != null) {
            p.println(" <div class=\"header\"><h3>" + "<a href=\"" + link + "\">" + HTMLEncode.encode(name) + "</a>" + "</h3></div>");
        } else {
            p.println(" <div class=\"header\"><h3>" + HTMLEncode.encode(name) + "</h3></div>");
        }
        p.println(" <div class=\"content\">");
        p.println("  <ul>");
    }

    protected void printItemPortletEnd(PrintWriter p, String addLink) throws ServletException, IOException {
        if (addLink != null) {
            p.println("  </ul><table class=\"bottomlink\"><tr><td><a href=\"" + addLink + "\"><img src=\"web/home/item_new16.png\" /></a></td><td><a href=\"" +
                    addLink + "\">Add Item...</a></td></tr></table>");
        } else {
            p.println("  </ul>");
        }
        p.println(" </div>");
        p.println("</div>");
    }

    protected void printRoom(PrintWriter p, String page, String subpage, String itemName, String headerLink, String addLink, String itemNames[], HomeService server, boolean includeActions) throws ServletException, IOException {

        // Start Room Portlet
        printItemPortletStart(p, itemName, headerLink);

        // List all instances in the room
        for (String name : itemNames) {

            // Open the instance
            HomeItemProxy item = server.openInstance(name);
            if (item == null) continue;

            // Print instance
            printHomeItem(p, item, page, subpage, includeActions);
        }

        // End Portlet
        printItemPortletEnd(p, addLink);
    }

    /**
     * Prints a HomeItem instance to the output stream.
     *
     *
     *
     * @param p    Output stream
     * @param item HomeItem to print
     * @param page Name of the current page
     * @param subpage sub page identity
     * @param includeActions print actions of the home item
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void printHomeItem(PrintWriter p,
                               HomeItemProxy item, String page, String subpage, boolean includeActions) throws ServletException, IOException {

        HomeItemModel model = item.getModel();
        String defaultAttributeValue = "";
        boolean hasDefaultAttribute = model.getDefaultAttribute() != null;
        if (hasDefaultAttribute) {
            defaultAttributeValue = item.getAttributeValue(model.getDefaultAttribute().getName());
            if (defaultAttributeValue.length() > 0) {
                defaultAttributeValue += " " + model.getDefaultAttribute().getUnit();
            }
        }


        p.println("   <li class=\"homeitem\">");
        p.println("	 <img src=\"" + getItemIconUrl(model, defaultAttributeValue) + "\" />");
        p.println("	 <img src=\"web/home/item_divider.png\" />");
        p.println("	 <span class=\"homeiteminfo\">");
        p.println("	  <ul>");
        HomeUrlBuilder url = new HomeUrlBuilder(localURL).addParameter("page", "edit")
                .addParameter("name", HomeGUI.toURL(item.getAttributeValue("ID")))
                .addParameter("return", page).addParameterIfNotNull("returnsp", subpage);
        p.println("	   <li><a href=\"" + url.toString() + "\">" + item.getAttributeValue("Name") + "</a>" + (hasDefaultAttribute ? (": " + defaultAttributeValue) : "") + "</li>");

        if (includeActions) {
            printItemActions(p, item, page, subpage, model);
        }
        p.println("	  </ul>");
        p.println("	 </span>");
        p.println("	</li>");
    }

    private void printItemActions(PrintWriter p, HomeItemProxy item, String page, String subpage, HomeItemModel model) {
        p.println("	   <li><span class=actions><ul>");
        if (model.getClassName().equals("Plan")) {
            p.println("		  <li><a href=\"" + localURL + "?page=plan&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">Go to location...</a></li>");
        } else if (model.getCategory().equals("Infrastructure")) {
            p.println("		  <li><a href=\"" + localURL + "?page=rooms&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">Go to location...</a></li>");
        } else if (hasLogFile(item)) {
            p.println("		  <li><a href=\"" + localURL + "?page=graphs&subpage=" +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\">View graph...</a></li>");
        }
        List<Action> actions = model.getActions();
        int size = 0;
        for (Action action : actions) {
            if (size > 60) break;
            HomeUrlBuilder actionUrl = new HomeUrlBuilder(localURL).addParameter("page", page)
                    .addParameterIfNotNull("subpage", subpage)
                    .addParameter("a", "perform_action")
                    .addParameter("name", item.getAttributeValue("ID"))
                    .addParameter("action", HomeGUI.toURL(action.getName()));
            p.println("		  <li><a href=\"" + actionUrl + "\">" + action.getName() + "</a></li>");
            size += action.getName().length() + 2;
        }
        p.println("	   </ul></span></li>");
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
        p.println("<script>location.href=\"" + url + "\"</script>");
    }
}
