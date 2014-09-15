package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.Attribute;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AttributeDto {
    private String unit;
    private String type;
    private String name;
    private String value;
    private List<String> valueList;
    private Boolean readOnly;
    private Boolean canInit;

    public AttributeDto(Attribute attribute) {
        unit = attribute.getUnit().isEmpty() ? null : attribute.getUnit();
        type = attribute.getType().equals("String") ? null : attribute.getType();
        name = attribute.getName();
        value = attribute.getValue();
        valueList = attribute.getValueList().isEmpty() ? null : attribute.getValueList();
        readOnly = attribute.isReadOnly() ? true : null;
        canInit = attribute.isCanInit() && attribute.isReadOnly() ? true : null;
    }

    public AttributeDto() {
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getCanInit() {
        return canInit;
    }

    public void setCanInit(Boolean canInit) {
        this.canInit = canInit;
    }
}
