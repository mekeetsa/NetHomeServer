package nu.nethome.home.items.zwave.messages.commands;

/**
 *
 */
public class AssociatedNode {
    public final int nodeId;
    public final int instance;

    public AssociatedNode(int nodeId, int instance) {
        this.nodeId = nodeId;
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociatedNode that = (AssociatedNode) o;

        if (instance != that.instance) return false;
        if (nodeId != that.nodeId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeId;
        result = 31 * result + instance;
        return result;
    }
}
