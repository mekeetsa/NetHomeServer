package nu.nethome.home.items.timer.SunTimer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class TimeExpressionParserTest {

    private static final int HOUR = 60 * 60;
    private static final int MINUTE = 60;
    private TimeExpressionParser timeExpressionParser;

    @Before
    public void setUp() throws Exception {
        timeExpressionParser = new TimeExpressionParser();
    }

    @Test
    public void parseEmptyString() throws Exception {
        assertThat(timeExpressionParser.parseExpression("").size(), is(0));
    }

    @Test
    public void parseSimpleOnTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(MINUTE * 2 + HOUR));
    }

    @Test
    public void parseSimpleOffTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("->2:03");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(false));
        assertThat(switchTimes.get(0).value(), is(MINUTE * 3 + HOUR * 2));
    }

    @Test
    public void parseSimpleTimeSpan() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04");
        assertThat(switchTimes.size(), is(2));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 1 + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
    }

    @Test
    public void parseMultipleTimeSpans() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04,05:06->,->07:08");
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 1 + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).value(), is(HOUR * 5 + MINUTE * 6));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).value(), is(HOUR * 7 + MINUTE * 8));
    }

    @Test
    public void useVariables() throws Exception {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("A", "01:02");
        variables.put("B", "03:04");
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("A->B,05:00->A", variables);
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 1 + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).value(), is(HOUR * 5 + MINUTE * 0));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).value(), is(HOUR * 1 + MINUTE * 2));
    }

    @Ignore
    @Test
    public void addTimes() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("05:01+2:07+01:1");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 8 + MINUTE * 9));
    }
}
