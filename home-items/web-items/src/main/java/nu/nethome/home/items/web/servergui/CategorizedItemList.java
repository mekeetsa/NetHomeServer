package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import java.util.*;

/**
*
*/
public class CategorizedItemList {
    private String category;
    private List<HomeItemProxy> items;

    public CategorizedItemList(String category) {
        this.category = category;
        items = new ArrayList<>();
    }

    public void addItem(HomeItemProxy item) {
        items.add(item);
    }

    public String getCategory() {
        return category;
    }

    public List<HomeItemProxy> getItems() {
        return Collections.unmodifiableList(items);
    }

    public static Map<String, CategorizedItemList> categorizeItems(HomeService server) {
        Map<String, CategorizedItemList> itemCategories = new HashMap<>();
        for (DirectoryEntry directoryEntry : server.listInstances("")) {
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            CategorizedItemList category = itemCategories.get(model.getCategory());
            if (category == null) {
                category = new CategorizedItemList(model.getCategory());
                itemCategories.put(model.getCategory(), category);
            }
            category.addItem(item);
        }
        return itemCategories;
    }
}
