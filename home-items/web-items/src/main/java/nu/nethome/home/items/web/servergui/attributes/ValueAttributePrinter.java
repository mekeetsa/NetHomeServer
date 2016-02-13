/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.web.servergui.attributes;

import nu.nethome.home.item.*;
import nu.nethome.home.items.web.servergui.AttributeTypePrinterInterface;
import nu.nethome.home.items.web.servergui.CategorizedItemList;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.system.HomeService;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ValueAttributePrinter implements AttributeTypePrinterInterface {

    protected HomeService server;

    public ValueAttributePrinter(HomeService serverConnection) {
        server = serverConnection;
    }

    public String getTypeName() {
        return "Value";
    }

    @Override
    public String attributeToPrintValue(String value) {
        return value;
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        // Create an identity for the parameter
        String identity = getIdFromNumber(attributeNumber);

        String[] callCmdAction = attribute.getValue().split(",");
        HomeItemProxy targetItem = null;

        if (attribute.getValue().length() > 0 && (callCmdAction.length != 3 || !callCmdAction[0].equalsIgnoreCase("get"))) {
            return printCustomCommand(p, attribute, identity);
        }
        final boolean isValidReference = (callCmdAction.length == 3);
        final String targetItemIdentity = isValidReference ? callCmdAction[1] : "";
        final String itemAttributeName = isValidReference ? callCmdAction[2] : "";
        if (isValidReference) {
            targetItem = server.openInstance(targetItemIdentity);
        }
        String targetItemName = (targetItem == null) ? "" : targetItem.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE);
        printItemSelection(p, identity, targetItemName);
        printItemAttributeSelection(p, identity, itemAttributeName, targetItem);

        return true;
    }

    private void printItemSelection(PrintWriter p, String identity, String targetItemName) {
        p.println("  <td>");
        p.println("  <select class=\"attributevalue-item\" name=\""
                + identity + "\">");
        p.println("  <option class=\"attributecmd-itemdim\" value=\"\">[No Item Selected]</option>");

        Map<String, CategorizedItemList> categories = CategorizedItemList.categorizeItems(server);
        for (String category : HomeItemModel.HOME_ITEM_CATEGORIES) {
            if (categories.containsKey(category)) {
                CategorizedItemList itemsInCategory = categories.get(category);
                p.println("  <optgroup label=\"" + category + "\">");
                for (HomeItemProxy item : itemsInCategory.getItems()) {
                    p.println("  <option value=\""
                            + item.getAttributeValue("ID")
                            + "\""
                            + (item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE).equals(targetItemName) ? " selected='selected'" : "")
                            + ">" + item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE)
                            + "</option>");
                }
                p.println("  </optgroup>");
            }
        }
        p.println("  <optgroup label=\"Custom values\">");
        p.println("  <option value=\"custom,command\">Custom</option>");
        p.println("  </optgroup>");
        p.println("  </select>");
    }

    private void printItemAttributeSelection(PrintWriter p, String identity, String attribute, HomeItemProxy targetItem) {
        p.println("  <select class=\"attributecmd-action\" name=\"" + identity + "_a\">");
        p.println("  <optgroup label=\"Select an Attribute\">");
        if (targetItem != null) {
            List<Attribute> attributes = targetItem.getAttributeValues();
            for (Attribute anAttribute : attributes) {
                p.println("  <option"
                        + (attribute.equalsIgnoreCase(anAttribute.getName()) ? " selected='selected'"
                        : "") + ">" + anAttribute.getName() + "</option>");
            }
        }
        p.println("  </optgroup></select>");
    }

    private boolean printCustomCommand(PrintWriter p, Attribute attribute, String identity) {
        p.println("  <td>");
        p.println("  <input class=\"iteminput\" type=\"string\" name=\""
                + identity + "\" value=\"" + attribute.getValue() + "\">");
        return true;
    }

    public void updateAttributeValue(HomeItemProxy item,
                                     Attribute attribute, HttpServletRequest req, boolean isInitiation,
                                     int attributeNumber) throws IllegalValueException {
        // Get the identity to look for
        String identity = getIdFromNumber(attributeNumber);

        // Get the corresponding parameter value from the request
        String itemIdentity = req.getParameter(identity);
        String itemAttribute = req.getParameter(identity + "_a");
        String newAttributeValue;

        if (itemIdentity == null || !isAttributeWritable(attribute, isInitiation)) {
            return;
        }

        // Check and update the HomeItem
        itemIdentity = PortletPage.fromURL(itemIdentity);
        HomeItemProxy actionTargetItem = server.openInstance(itemIdentity);
        if (itemAttribute != null && itemAttribute.length() > 0 && actionTargetItem != null) {
            itemAttribute = PortletPage.fromURL(itemAttribute);
            newAttributeValue = "get," + actionTargetItem.getAttributeValue("ID");
            newAttributeValue = newAttributeValue + "," + itemAttribute;
        } else {
            newAttributeValue = itemIdentity;
        }
        item.setAttributeValue(attribute.getName(), newAttributeValue);
    }

    private boolean isAttributeWritable(Attribute attribute, boolean isInitiation) {
        return (!attribute.isReadOnly() || (attribute.isCanInit() && isInitiation));
    }

    /**
     * Create a unique parameter name for this attribute
     *
     * @param number an attribute number that is unique
     * @return an identity string
     */
    protected String getIdFromNumber(int number) {
        return "a" + Integer.toString(number);
    }
}
