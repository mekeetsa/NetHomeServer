package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.system.Event;

import java.util.*;

public class CreationEventCache {

    private List<ItemEvent> itemEvents = new ArrayList<ItemEvent>();
    private Map<String, List<HomeItemInfo>> itemsFromEvents = new HashMap<String, List<HomeItemInfo>>();
    private long collectTimeout = 1000 * 60 * 20;
    private long clearTimeout = 1000 * 60 * 60 * 2;
    private Date latestCollectionTime = new Date(0);
    private volatile boolean isAddingNodes = false;
    private volatile boolean isRemovingNodes = false;
    private volatile ChangedNodeInfo lastAddedNode;
    private volatile ChangedNodeInfo lastRemovedNode;

    public void addItemInfo(List<HomeItemInfo> itemInfos) {
        for (HomeItemInfo info : itemInfos) {
            for (String eventType : info.getCreationEventTypes()) {
                List<HomeItemInfo> mappedInfo = itemsFromEvents.get(eventType);
                if (mappedInfo == null) {
                    mappedInfo = new ArrayList<HomeItemInfo>();
                    itemsFromEvents.put(eventType, mappedInfo);
                }
                mappedInfo.add(info);
            }
        }
    }

    public synchronized void newEvent(Event event, boolean wasHandled) {
        if (event.isType("NodeInclusionEvent")) {
            updateNodeInclusionState(event);
        } else if (isCollecting() && isInbound(event)) {
            List<HomeItemInfo> itemsCreatableByEvent = getItemsCreatableByEvent(event);
            if (itemsCreatableByEvent.size() > 0) {
                cacheCreationEvent(event, wasHandled, itemsCreatableByEvent);
            }
        } else {
            clearOldCacheEntriesIfNeeded();
        }
    }

    private void updateNodeInclusionState(Event event) {
        final String value = event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        if (value.equals("InclusionStarted")) {
            this.isAddingNodes = true;
            lastAddedNode = null;
            lastRemovedNode = null;
        } else if (value.equals("AddedNode")) {
            this.isAddingNodes = true;
            lastAddedNode = new ChangedNodeInfo(event.getAttribute("Protocol"), event.getAttribute("Node"));
        }  else if (value.equals("InclusionEnded")) {
            this.isAddingNodes = false;
        } else if (value.equals("ExclusionStarted")) {
            this.isRemovingNodes = true;
            lastRemovedNode = null;
            lastAddedNode = null;
        } else if (value.equals("ExcludedNode")) {
            this.isRemovingNodes = true;
            lastRemovedNode = new ChangedNodeInfo(event.getAttribute("Protocol"), event.getAttribute("Node"));
        }  else if (value.equals("ExclusionEnded")) {
            this.isRemovingNodes = false;
        }
    }

    private void cacheCreationEvent(Event event, boolean wasHandled, List<HomeItemInfo> itemsCreatableByEvent) {
        String eventIdentity = itemsCreatableByEvent.get(0).getCreationIdentification(event);
        boolean updated = false;
        for (ItemEvent itemEvent : itemEvents) {
            if (itemEvent.getContent().equals(eventIdentity)) {
                itemEvent.updateEvent(event, wasHandled);
                updated = true;
                break;
            }
        }
        if (!updated) {
            itemEvents.add(new ItemEvent(event, eventIdentity, wasHandled));
        }
    }

    private synchronized void clearOldCacheEntriesIfNeeded() {
        if ((System.currentTimeMillis() > latestCollectionTime.getTime() + clearTimeout) &&
                itemEvents.size() > 0) {
            itemEvents.clear();
        }
    }

    public boolean isCollecting() {
        return latestCollectionTime.getTime() + collectTimeout > System.currentTimeMillis();
    }

    public synchronized List<ItemEvent> getItemEvents() {
        latestCollectionTime = new Date();
        return Collections.unmodifiableList(itemEvents);
    }

    public synchronized ItemEvent getItemEvent(long id) {
        for (ItemEvent event : itemEvents) {
            if (event.getId() == id) {
                return event;
            }
        }
        return null;
    }

    public List<HomeItemInfo> getItemsCreatableByEvent(Event event) {
        List<HomeItemInfo> itemInfos = this.itemsFromEvents.get(event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
        if (itemInfos != null) {
            List<HomeItemInfo> result = new ArrayList<>();
            for (HomeItemInfo info : itemInfos) {
                if (info.canBeCreatedBy(event)) {
                    result.add(info);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private boolean isInbound(Event event) {
        return event.getAttribute("Direction").equals("In");
    }

    public void setCollectionTimeout(long timeoutMs) {
        this.collectTimeout = timeoutMs;
    }

    public void setClearTimeout(long clearTimeout) {
        this.clearTimeout = clearTimeout;
    }

    public boolean isAddingNodes() {
        return isAddingNodes;
    }

    public ChangedNodeInfo getLastAddedNode() {
        return lastAddedNode;
    }

    public boolean isRemovingNodes() {
        return isRemovingNodes;
    }

    public ChangedNodeInfo getLastRemovedNode() {
        return lastRemovedNode;
    }

    public static class ChangedNodeInfo {
        public final String protocol;
        public final String identity;

        public ChangedNodeInfo(String protocol, String identity) {
            this.protocol = protocol;
            this.identity = identity;
        }
    }
}
