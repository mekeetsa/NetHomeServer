package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemProxy;

import java.util.ArrayList;
import java.util.List;

public class ItemDto {
    private String name;
    private String id;
    private String className;
    private String category;
    private List<String> actions;
    private List<AttributeDto> attributes;

    public ItemDto(String name, String id, String className, String category) {
        this.name = name;
        this.id = id;
        this.className = className;
        this.category = category;
    }

    public ItemDto(HomeItemProxy item) {
        name = item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE);
        id = item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE);
        className = item.getModel().getClassName();
        category = item.getModel().getCategory();
        actions = extractActionNames(item.getModel().getActions());
        attributes = extractAttributes(item.getAttributeValues());
    }

    private List<String> extractActionNames(List<Action> actions) {
        List<String> result = new ArrayList<String>(actions.size());
        for (Action action : actions) {
            result.add(action.getName());
        }
        return result;
    }

    private List<AttributeDto> extractAttributes(List<Attribute> attributeValues) {
        List<AttributeDto> result = new ArrayList<AttributeDto>(attributeValues.size());
        for (Attribute attribute : attributeValues) {
            result.add(new AttributeDto(attribute));
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<AttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDto> attributes) {
        this.attributes = attributes;
    }
}
