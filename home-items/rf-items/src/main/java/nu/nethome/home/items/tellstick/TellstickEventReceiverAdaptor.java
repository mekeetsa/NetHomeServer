package nu.nethome.home.items.tellstick;

/**
 *
 */
public abstract class TellstickEventReceiverAdaptor implements TellstickEventReceiver {
    private boolean active = true;
    @Override
    public void setActive(boolean status) {
        active = status;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void processEvent(TellstickEvent event) {
        if (active) {
            processActiveEvent(event);
        }
    }

    protected abstract void processActiveEvent(TellstickEvent event);
}
