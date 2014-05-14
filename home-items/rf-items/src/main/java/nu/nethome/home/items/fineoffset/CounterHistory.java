package nu.nethome.home.items.fineoffset;

import org.apache.commons.lang3.Validate;

/**
 *
 */
public class CounterHistory {
    
    long history[];
    int nextValuePosition = 0;
    long totalValues;

    public CounterHistory(int size) {
        history = new long[size];
    }
    
    public void addValue(long value) {
        history[nextValuePosition] = value;
        nextValuePosition = (nextValuePosition + 1) % history.length;
        totalValues++;
    }
    
    public long differenceSince(long value, int timeSince) {
        Validate.inclusiveBetween(0, history.length, timeSince, "Time Since value not valid");
        long counterValueToCompareWith;
        if (totalValues == 0 || timeSince == 0) {
            counterValueToCompareWith = value;
        } else if (totalValues <= timeSince) {
            counterValueToCompareWith = history[0];
        } else {
            counterValueToCompareWith = getValueTimeAgo(timeSince);
        }
        return value - counterValueToCompareWith;
    }

    private long getValueTimeAgo(int timeSince) {
        return history[(nextValuePosition + history.length - timeSince) % history.length];
    }

    public String getHistoryAsString() {
        StringBuilder result = new StringBuilder();
        String divider = "";
        for (long i = Math.min(history.length, totalValues); i > 0; i-- ) {
            result.append(divider);
            result.append(getValueTimeAgo((int)i));
            divider = ",";
        }
        return result.toString();
    }

    public void setHistoryFromString(String historyString) {
        String parts[] = historyString.split(",");
        for (String value : parts) {
            addValue(Long.parseLong(value));
        }
    }
}
