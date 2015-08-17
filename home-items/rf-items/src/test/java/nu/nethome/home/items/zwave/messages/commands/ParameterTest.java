package nu.nethome.home.items.zwave.messages.commands;

import nu.nethome.home.items.zwave.Hex;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class ParameterTest {

    @Test
    public void canPackAndUnpack() throws Exception {
        verifyPackAndUnpack(new Parameter(5, 1));
        verifyPackAndUnpack(new Parameter(-5, 1));
        verifyPackAndUnpack(new Parameter(500, 2));
        verifyPackAndUnpack(new Parameter(-500, 2));
        verifyPackAndUnpack(new Parameter(100000, 3));
        verifyPackAndUnpack(new Parameter(-100000, 3));
    }

    @Test
    public void testFormat() throws Exception {
        Parameter parameter = new Parameter(0x0567, 2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        parameter.write(out);
        assertThat(Hex.asHexString(out.toByteArray()), is("020567"));
    }

    private void verifyPackAndUnpack(Parameter parameter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        parameter.write(out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Parameter readParameter = new Parameter(in);
        assertThat(parameter, is(readParameter));
    }
}
