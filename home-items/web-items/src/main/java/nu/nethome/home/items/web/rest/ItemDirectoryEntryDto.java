package nu.nethome.home.items.web.rest;

import nu.nethome.home.system.DirectoryEntry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemDirectoryEntryDto {

    private String name;
    private String id;
    private String category;

    public ItemDirectoryEntryDto() {
    }

    public ItemDirectoryEntryDto(DirectoryEntry entry) {
        name = entry.getInstanceName();
        id = Long.toString(entry.getInstanceId());
        category = entry.getCategory();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public static List<ItemDirectoryEntryDto> toDtos(Collection<DirectoryEntry> entries) {
        List<ItemDirectoryEntryDto> items = new ArrayList<ItemDirectoryEntryDto>(entries.size());
        for (DirectoryEntry entry : entries) {
            items.add(new ItemDirectoryEntryDto(entry));
        }
        return items;
    }
}
