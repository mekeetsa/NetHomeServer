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
 * 2010-10-24 pela Added SELECT tag
 * 2010-10-27 pela Added support of IllegalFormat exception so that its error message is displayed
 * 2010-10-31 pela Changed ServletOutputStream to PrintWriter to support UTF-8 and encodings properly and 
 *                 the printActions method now emits a %20 where attribute names includes the space character.
 * 2010-11-09 pela Added support of 'command' attribute type when generating HTML
 * 2010-11-12 pela Added support of 'options' attribute type when generating HTML
 */
package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.python.google.common.collect.Lists;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SelectClassPage extends PortletPage {

    protected HomeService server;
    protected String pageName = "edit";
    private CreationEventCache creationEventCache;

    public SelectClassPage(String mLocalURL, HomeService server, String mediaDirectory, CreationEventCache creationEventCache) {
        super(mLocalURL);
        this.server = server;
        this.creationEventCache = creationEventCache;
    }

    @Override
    public List<String> getJavaScriptFileNames() {
        List<String> scripts = new ArrayList<String>();
        scripts.add("web/home/createitem.js");
        return scripts;
    }

    public String getPageName() {
        return "Select Class";
    }

    public String getPageNameURL() {
        return pageName;
    }

    /**
     * This is the main entrance point of the class. This is called when a http
     * request is routed to this servlet.
     */
    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        EditItemArguments pageArguments = new EditItemArguments(req);
        printSelectClassPage(res.getWriter(), pageArguments);
    }

    /**
     * Prints the class selection page. This is the first step when a user
     * seletcts to create a new instance.
     */
    protected void printSelectClassPage(PrintWriter p, EditItemArguments arguments)
            throws ServletException, IOException {
        p.println("<div class=\"itemcolumn log\">");
        if (arguments.hasEvent()) {
            ItemEvent itemEvent = creationEventCache.getItemEvent(arguments.getEventId());
            printClassesListPanel(p, arguments, creationEventCache.getItemsCreatableByEvent(itemEvent.getEvent()), "Create new item",
                    "Select Item type from this list. The Item will be initialized to handle the selected device");
        } else {
            printClassesListPanel(p, arguments, server.listClasses(), "Create new Item", "Select Item type from this list");
            p.println("<br>");
            printEventsPanel(p, arguments);
        }
        p.println("</div>");
    }

    private void printEventsPanel(PrintWriter p, EditItemArguments arguments) {
        HomeUrlBuilder createLink = new HomeUrlBuilder(localURL);
        createLink.preserveReturnPage(arguments).withPage(pageName).preserveRoom(arguments);
        p.printf("<script type=\"text/javascript\">homeManager.classUrl=%s;</script>", createLink.toQuotedString());
        p.println("<div class=\"panel thin\">");
        p.println(" <h1>Detected Devices</h1>");
        p.println(" <h2>Create Items based on detected devices</h2>");
        p.println(" <div class=\"panellist\">");
        p.println("<div id=\"eventsTableHolder\"></div>");
        printListPanelEnd(p);
    }

    private void printClassSelection(PrintWriter p, EditItemArguments arguments) {
        // Print page heading
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader\">");
        p.println("	<img src=\"web/home/item.png\" />");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>Create new Item</li>");
        p.println("   <li class=\"classname\">Step 1: Select item type or create from Event</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<br>");

        // Print class selection box
        p.println("<form name=\"create\" action=\"" + localURL
                + "\" method=\"get\" >");
        p.println("<input type=\"hidden\" name=\"a\" value=\"create\" />");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName
                + "\" />");
        if (arguments.getRoom() != null) {
            p.println("<input type=\"hidden\" name=\"room\" value=\"" + arguments.getRoom()
                    + "\" />");
        }
        if (arguments.hasReturnPage()) {
            p.println("<input type=\"hidden\" name=\"return\" value=\"" + arguments.getReturnPage()
                    + "\" />");
        }
        p.println("<table class=\"actions\">");
        p.println(" <tr>");
        p.println(" <td class=\"actioncolumn\"><input type=\"submit\" value=\"Create new item of selected type\"> </td>");
        p.println(" <td><select name=\"class_name\" >");
        p.println("	<option value=\"TCPCommandPort\">- Select Type -</option>");

        // Print all selectable classes
        List<HomeItemInfo> classNames = server.listClasses();
        for (HomeItemInfo classInfo : classNames) {
            p.println("	<option value=\"" + classInfo.getClassName() + "\">" + classInfo.getClassName()
                    + "</option>");
        }
        p.println("	</select>");
        p.println("  </td>");
        p.println(" </tr>");
        p.println("</table>");
        p.println("</div>");
        p.println("</form>");

        // End of the Item info section
        p.println("</div>");
    }

    private void printClassesListPanel(PrintWriter p, EditItemArguments arguments, List<HomeItemInfo> itemClasses, String h1, String h2) {
        p.println("<div class=\"panel thin\">");
        p.printf(" <h1>%s</h1>", h1);
        if (h2 != null) {
            p.printf(" <h2>%s</h2>", h2);
        }
        p.println(" <div class=\"creationlist\">");
        p.println(" <table>");
        Map<String, List<HomeItemInfo>> categorizedInfo = categorizeItemInfo(itemClasses);
        for (String category : HomeItemModel.HOME_ITEM_CATEGORIES) {
            List<HomeItemInfo> itemInfos = categorizedInfo.get(category);
            if (itemInfos != null) {
                printCategoryHeaderRow(p, category);
                for (HomeItemInfo itemInfo : itemInfos) {
                    printClassRow(p, itemInfo, arguments);
                }
            }
        }
        p.println(" </table>");
        p.println(" </div>");
        p.println(" <h5></h5>");
        p.println("</div>");
    }

    private void printCategoryHeaderRow(PrintWriter p, String category) {
        p.println("  <tr>");
        p.printf("   <td>&nbsp;</td>\n");
        p.printf("   <td>&nbsp;</td>\n");
        p.printf("   <td>&nbsp;</td>\n");
        p.println("  </tr>");
        p.println("  <tr class=\"categoryheader\">");
        p.printf("   <td><img src=\"web/home/%s\" />%s</td>\n", HomeGUI.itemIcon(category, false), category);
        p.printf("   <td>&nbsp;</td>\n");
        p.printf("   <td>&nbsp;</td>\n");
        p.println("  </tr>");
    }

    private Map<String, List<HomeItemInfo>> categorizeItemInfo(List<HomeItemInfo> itemClasses) {
        Map<String, List<HomeItemInfo>> categorizedInfo = new HashMap<>();
        for (HomeItemInfo itemClass : itemClasses) {
            List<HomeItemInfo> currentList = categorizedInfo.get(itemClass.getCategory());
            if (currentList == null) {
                currentList = Lists.newArrayList();
                categorizedInfo.put(itemClass.getCategory(), currentList);
            }
            currentList.add(itemClass);
        }
        return categorizedInfo;
    }

    private boolean itemCandBeCreatedFromEvent(ItemEvent itemEvent, HomeItemInfo itemInfo) {
        return itemInfo.getCreationEventTypes().length > 0 && itemInfo.getCreationEventTypes()[0].equals(itemEvent.getEvent().getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
    }

    private void printClassRow(PrintWriter p, HomeItemInfo event, EditItemArguments arguments) {
        HomeUrlBuilder createLink = new HomeUrlBuilder(localURL);
        createLink.preserveReturnPage(arguments).withPage(pageName).withAction("create")
                .addParameter("class_name", event.getClassName()).preserveRoom(arguments);
        if (arguments.hasEvent()) {
            createLink.addParameter("event", Long.toString(arguments.getEventId()));
        }
        p.println("  <tr>");
        p.printf("   <td>%s</td>\n", event.getClassName());
        p.printf("   <td><a href=%s>Create Item</a></td>\n",createLink.toQuotedString());
        p.printf("   <td><a href=\"http://wiki.nethome.nu/doku.php?id=%s\" target=\"new_window\" >View online documentation</a></td>\n", event.getClassName());
//        p.printf("   <td><img src=\"web/home/%s\" /></td>\n", HomeGUI.itemIcon(event.getCategory(), true));
        p.println("  </tr>");
    }

    protected void printItemEditColumnStart(PrintWriter p) throws ServletException,
            IOException {
        p.println("<div class=\"itemcolumn edit\">");
    }

    protected void printListPanelStart(PrintWriter p, String header) {
        p.println("<div class=\"panel thin\">");
        p.println(" <h1>" + header + "</h1>");
        p.println(" <div class=\"panellist\">");
    }

    private void printListPanelEnd(PrintWriter p) {
        p.println(" </div>");
        p.println(" <h5></h5>");
        p.println("</div>");
        p.println("<div id=\"includeState\"></div>");
    }
}
