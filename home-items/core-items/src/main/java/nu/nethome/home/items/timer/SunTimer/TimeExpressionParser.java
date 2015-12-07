package nu.nethome.home.items.timer.SunTimer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TimeExpressionParser {

    public static final String TIME_PERIOD_SEPARATOR = "->";
    public static final String TIME_SEPARATOR = ":";

    public List<SwitchTime> parseExpression(String expression) {
        ArrayList<SwitchTime> switchTimes = new ArrayList<>();
        String[] switchTimeExpressions = expression.split(",");
        for (String switchTimeExpression : switchTimeExpressions) {
            switchTimes.addAll(SwitchTime.parseSwitchTimes(switchTimeExpression));
        }
        return switchTimes;
    }

    public static class SwitchTime {
        private boolean on;
        private int value = 0;

        public static List<SwitchTime> parseSwitchTimes(String expression) {
            ArrayList<SwitchTime> switchTimes = new ArrayList<>();
            String[] switchTimeExpressions = expression.split(TIME_PERIOD_SEPARATOR);
            if (switchTimeExpressions.length > 0 && !switchTimeExpressions[0].isEmpty()) {
                switchTimes.add(new SwitchTime(switchTimeExpressions[0], true));
            }
            if (switchTimeExpressions.length > 1 && !switchTimeExpressions[1].isEmpty()) {
                switchTimes.add(new SwitchTime(switchTimeExpressions[1], false));
            }
            return switchTimes;
        }

        public SwitchTime(String switchTimeExpression, boolean isOn) {
            this.on = isOn;
            String[] strings = switchTimeExpression.split(TIME_SEPARATOR);
            if (strings.length > 1) {
                value = Integer.parseInt(strings[1]) * 60 + Integer.parseInt(strings[0]) * 60 * 60;
            } else if (strings.length == 1) {
                value = Integer.parseInt(strings[0]) * 60;
            }
        }

        public int value() {
            return value;
        }

        public boolean isOn() {
            return on;
        }
    }
}
