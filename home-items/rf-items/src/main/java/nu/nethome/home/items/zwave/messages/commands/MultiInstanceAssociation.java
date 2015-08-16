package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.messages.DecoderException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MultiInstanceAssociation implements CommandClass {

    private static final int SET_ASSOCIATION = 0x01;
    private static final int GET_ASSOCIATION = 0x02;
    private static final int ASSOCIATION_REPORT = 0x03;
    private static final int REMOVE_ASSOCIATION = 0x04;

    public static final int COMMAND_CLASS = 0x8E;

    public static class Get extends CommandAdapter {
        public final int associationId;

        public Get(int associationId) {
            super(COMMAND_CLASS, GET_ASSOCIATION);
            this.associationId = associationId;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(associationId);
        }
    }

    public static class Report extends CommandAdapter {
        public final int associationId;
        public final int maxAssociations;
        public final int reportsToFollow;
        public final AssociatedNode[] nodes;

        public Report(byte[] data) throws DecoderException {
            super(data, COMMAND_CLASS, ASSOCIATION_REPORT);
            associationId = in.read();
            maxAssociations = in.read();
            reportsToFollow = in.read();
            int numberOfNodeBytes = (data.length - 5);
            ArrayList<AssociatedNode> associatedNodes = new ArrayList<>();
            int nextNode = in.read();
            int readBytes = 1;
            // First comes the nodes without instance as single bytes followed by a 0-byte
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
    }
}
