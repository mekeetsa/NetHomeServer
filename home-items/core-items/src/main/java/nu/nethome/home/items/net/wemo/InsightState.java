package nu.nethome.home.items.net.wemo;

/**
 *
 */
public class InsightState {
    int state;          // 0 off, 1 on, 8 on without load
    long lastChange;
    long onForSeconds;
    long onTodaySeconds;
    long onTotalSeconds;
    long timePeriod;    // Period over which averages are calculated
    long currentMW;
    long todayMW;
    double totalMW;
    long powerThresholdMW;

    public InsightState(String rawStateRepresentation) throws WemoException {
        String states[] = rawStateRepresentation.split("\\|");
        if (states.length < 11) {
            throw new WemoException("Wrong number of parameters in WemoInsight state");
        }
        state = Integer.parseInt(states[0]);
        lastChange = Long.parseLong(states[1]);
        onForSeconds = Long.parseLong(states[2]);
        onTodaySeconds = Long.parseLong(states[3]);
        onTotalSeconds = Long.parseLong(states[4]);
        timePeriod = Long.parseLong(states[5]);
        String unknown = states[6];
        currentMW = Long.parseLong(states[7]);
        todayMW = Long.parseLong(states[8]);
        totalMW = Double.parseDouble(states[9]);
        powerThresholdMW = Long.parseLong(states[10]);
    }

    public int getState() {
        return state;
    }

    public long getLastChange() {
        return lastChange;
    }

    public long getOnForSeconds() {
        return onForSeconds;
    }

    public long getOnTodaySeconds() {
        return onTodaySeconds;
    }

    public long getOnTotalSeconds() {
        return onTotalSeconds;
    }

    public long getTimePeriod() {
        return timePeriod;
    }

    public long getCurrentMW() {
        return currentMW;
    }

    public long getTodayMW() {
        return todayMW;
    }

    public double getTotalMW() {
        return totalMW;
    }

    public long getPowerThresholdMW() {
        return powerThresholdMW;
    }
}
