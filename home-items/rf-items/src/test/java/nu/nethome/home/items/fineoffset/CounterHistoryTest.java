package nu.nethome.home.items.fineoffset;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class CounterHistoryTest {

    public static final int HISTORY_LENGTH = 3;
    CounterHistory history;

    @Before
    public void setUp() throws Exception {
        history = new CounterHistory(HISTORY_LENGTH);
    }

    @Test
    public void allDiffsAreZeroForEmpty() throws Exception {
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            assertThat(history.differenceSince(17, i), is(0L));
        }
    }

    @Test
    public void differenceSinceNoTimeIsZero() throws Exception {
        history.addValue(17);
        assertThat(history.differenceSince(18, 0), is(0L));
    }

    @Test
    public void canCountDifference() throws Exception {
        history.addValue(17);
        history.addValue(18);
        history.addValue(19);
        assertThat(history.differenceSince(20, 1), is(1L));
        assertThat(history.differenceSince(20, 2), is(2L));
        assertThat(history.differenceSince(20, 3), is(3L));
    }

    @Test
    public void canCountDifferenceAround() throws Exception {
        history.addValue(1);
        history.addValue(2);
        history.addValue(17);
        history.addValue(18);
        history.addValue(19);
        assertThat(history.differenceSince(20, 1), is(1L));
        assertThat(history.differenceSince(20, 2), is(2L));
        assertThat(history.differenceSince(20, 3), is(3L));
    }

    @Test
    public void canGetFullHistoryAsString() throws Exception {
        history.addValue(1);
        history.addValue(2);
        history.addValue(17);
        history.addValue(18);
        history.addValue(19);
        assertThat(history.getHistoryAsString(), is("17,18,19"));
    }

    @Test
    public void canGetPartialHistoryAsString() throws Exception {
        history.addValue(18);
        history.addValue(19);
        assertThat(history.getHistoryAsString(), is("18,19"));
    }

    @Test
    public void canSetFullHistoryFromString() throws Exception {
        history.setHistoryFromString("10,20,30");
        assertThat(history.differenceSince(40, 1), is(10L));
        assertThat(history.differenceSince(40, 2), is(20L));
        assertThat(history.differenceSince(40, 3), is(30L));
        assertThat(history.getHistoryAsString(), is("10,20,30"));
    }
    @Test
    public void canSetPartialHistoryFromString() throws Exception {
        history.setHistoryFromString("20,30");
        assertThat(history.differenceSince(40, 1), is(10L));
        assertThat(history.differenceSince(40, 2), is(20L));
        assertThat(history.differenceSince(40, 3), is(20L));
        assertThat(history.getHistoryAsString(), is("20,30"));
    }
}
