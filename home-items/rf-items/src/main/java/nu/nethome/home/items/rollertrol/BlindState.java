package nu.nethome.home.items.rollertrol;

/**
 *
 */
public class BlindState {
    public static final String UP_STRING = "Up";
    public static final String DOWN_STRING = "Down";
    private String state;
    private long fullTravelTime = 0;
    private long travelStartTime = 0;
    private long travelStartPosition = 0;
    private int travelDirection = 0;

    public BlindState() {
        state = UP_STRING;
    }

    public String getStateString() {
        String result = getCurrentPosition() == 0 && travelDirection != 1 ? UP_STRING : DOWN_STRING;
        if (!isInEndPosition()) {
            result += String.format(" %d%%", currentPositionInPercent());
        }
        return result;
    }

    private long currentPositionInPercent() {
        long percent = getCurrentPosition() * 100 / fullTravelTime;
        percent = ((percent) / 5) * 5;
        return percent;
    }

    private boolean isInEndPosition() {
        return (getCurrentPosition() == 0 && travelDirection != 1) ||
                getCurrentPosition() >= fullTravelTime + 1 && travelDirection != -1;
    }

    public void down() {
        travelStartPosition = getCurrentPosition();
        travelStartTime = System.currentTimeMillis();
        travelDirection = 1;
    }

    private long getCurrentPosition() {
        long rawPosition;
        if (fullTravelTime != 0) {
            rawPosition = travelStartPosition + travelDirection * timePassedSince(travelStartTime);
        } else {
            rawPosition = travelStartPosition + travelDirection;
        }
        rawPosition = Math.min(rawPosition, fullTravelTime + 1);
        rawPosition = Math.max(rawPosition, 0);
        return rawPosition;
    }

    public void up() {
        travelStartPosition = getCurrentPosition();
        travelStartTime = System.currentTimeMillis();
        travelDirection = -1;
    }

    /**
     * Time to travel the full length in ms
     * @param travelTime time in ms
     */
    public void setTravelTime(long travelTime) {
        this.fullTravelTime = travelTime;
    }

    public long getTravelTime() {
        return fullTravelTime;
    }

    public long timePassedSince(long time) {
        return System.currentTimeMillis() - time;
    }

    public void stop() {
        travelStartPosition = getCurrentPosition();
        travelStartTime = System.currentTimeMillis();
        travelDirection = 0;
    }
}
