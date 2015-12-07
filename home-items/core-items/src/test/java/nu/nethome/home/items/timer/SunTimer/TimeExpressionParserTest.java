package nu.nethome.home.items.timer.SunTimer;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class TimeExpressionParserTest {

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
        assertThat(switchTimes.get(0).value(), is(60 * 2 + 60 * 60));
    }

    @Test
    public void parseSimpleOffTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("->2:03");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(false));
        assertThat(switchTimes.get(0).value(), is(60 * 3 + 60 * 60 * 2));
    }

    @Test
    public void parseSimpleTimeSpan() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04");
        assertThat(switchTimes.size(), is(2));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(60 * 60 * 1 + 60 * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(60 * 60 * 3 + 60 * 4));
    }

    @Test
    public void parseMultipleTimeSpans() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04,05:06->,->07:08");
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(60 * 60 * 1 + 60 * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(60 * 60 * 3 + 60 * 4));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).value(), is(60 * 60 * 5 + 60 * 6));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).value(), is(60 * 60 * 7 + 60 * 8));
    }

}
