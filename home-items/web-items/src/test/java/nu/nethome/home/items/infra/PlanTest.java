package nu.nethome.home.items.infra;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class PlanTest {

    private Plan plan;

    @Before
    public void setUp() throws Exception {
        plan = new Plan();
    }

    @Test
    public void canRemoveNonExistingItem() throws Exception {
        plan.setItems("");
        plan.removeItem("1");
        assertThat(plan.getItems(), is(""));
    }

    @Test
    public void canRemoveItem() throws Exception {
        plan.setItems("1,2,3");
        plan.removeItem("1");
        assertThat(plan.getItems(), is("2,3"));
    }

    @Test
    public void canAddFirstItem() throws Exception {
        plan.setItems("");
        plan.addItem("1");
        assertThat(plan.getItems(), is("1"));
    }

    @Test
    public void canAddItem() throws Exception {
        plan.setItems("1");
        plan.addItem("2");
        assertThat(plan.getItems(), is("1,2"));
    }

    @Test
    public void doesNotAddDuplicate() throws Exception {
        plan.setItems("1");
        plan.addItem("1");
        assertThat(plan.getItems(), is("1"));
    }

}
