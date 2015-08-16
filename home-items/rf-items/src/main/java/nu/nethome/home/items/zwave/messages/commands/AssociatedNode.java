package nu.nethome.home.items.zwave.messages.commands;

/**
 *
 */
public class AssociatedNode {
    public final int nodeId;
    public final Integer instance;

    public boolean isMultiInstance() {
        return instance != null;
    }

    public AssociatedNode(int nodeId, int instance) {
        this.nodeId = nodeId;
        this.instance = instance;
    }

    public AssociatedNode(int nodeId) {
        this.nodeId = nodeId;
        this.instance = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociatedNode that = (AssociatedNode) o;

        if (nodeId != that.nodeId) return false;
        if (instance != null ? !instance.equals(that.instance) : that.instance != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeId;
        result = 31 * result + (instance != null ? instance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "" + nodeId + (isMultiInstance() ? ("." + instance) : "");
    }
}
