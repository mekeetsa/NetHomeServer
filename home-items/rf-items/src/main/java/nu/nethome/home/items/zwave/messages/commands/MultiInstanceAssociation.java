package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Modifies Association Groups in a node. The MultiInstanceAssociation command class is an extension of the
 * Association command class, where MultiInstanceAssociation can handle nodes with multiple instances/endpoints
 */
public class MultiInstanceAssociation implements CommandClass {

    private static final int SET_ASSOCIATION = 0x01;
    private static final int GET_ASSOCIATION = 0x02;
    private static final int ASSOCIATION_REPORT = 0x03;
    private static final int REMOVE_ASSOCIATION = 0x04;

    public static final int COMMAND_CLASS = 0x8E;

    public static class Get extends CommandAdapter {
        public final int group;

        public Get(int group) {
            super(COMMAND_CLASS, GET_ASSOCIATION);
            this.group = group;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(group);
        }
    }

    public static class Set extends CommandAdapter {
        public final int group;
        public final List<AssociatedNode> nodes;

        public Set(int group, List<AssociatedNode> nodes) {
            this(group, nodes, SET_ASSOCIATION);
        }

        Set(int group, List<AssociatedNode> node, int command) {
            super(COMMAND_CLASS, command);
            this.group = group;
            this.nodes = node;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(group);
            for (AssociatedNode node : nodes) {
                if (!node.isMultiInstance()) {
                    result.write(node.nodeId);
                }
            }
            result.write(0);
            for (AssociatedNode node : nodes) {
                if (node.isMultiInstance()) {
                    result.write(node.nodeId);
                    result.write(node.instance);
                }
            }
        }
    }

    public static class Remove extends Set {
        public Remove(int group, List<AssociatedNode> nodes) {
            super(group, nodes, REMOVE_ASSOCIATION);
        }
    }

    public static class Report extends CommandAdapter {
        public final int group;
        public final int maxAssociations;
        public final int reportsToFollow;
        public final AssociatedNode[] nodes;

        public Report(byte[] data) throws DecoderException {
            super(data, COMMAND_CLASS, ASSOCIATION_REPORT);
            group = in.read();
            maxAssociations = in.read();
            reportsToFollow = in.read();
            int numberOfNodeBytes = (data.length - 5);
            ArrayList<AssociatedNode> associatedNodes = new ArrayList<>();
            int nextNode = in.read();
            int readBytes = 1;
            // First comes the nodes without instance as single bytes followed by a single 0-byte
            while (nextNode != 0 && readBytes < numberOfNodeBytes) {
                associatedNodes.add(new AssociatedNode(nextNode));
                nextNode = in.read();
                readBytes++;
            }
            // Then comes the nodes with instance id:s
            while (readBytes < numberOfNodeBytes) {
                int nodeId = in.read();
                int instanceId = in.read();
                associatedNodes.add(new AssociatedNode(nodeId, instanceId));
                readBytes += 2;
            }
            nodes = associatedNodes.toArray(new AssociatedNode[associatedNodes.size()]);
        }

        public static class Processor extends CommandProcessorAdapter<Report> {
            @Override
            public Report process(byte[] command) throws DecoderException {
                return process(new Report(command));
            }
        }
    }
}
