package nu.nethome.home.items.web.servergui;

import nu.nethome.home.system.HomeService;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class MediaPageTest {
    @Test
    public void canIdentifyMediaFile() throws Exception {
        final MediaPage mediaPage = new MediaPage("", mock(HomeService.class), "");

        assertThat(mediaPage.isImageFile(new File("foo.bar")), is(false));
        assertThat(mediaPage.isImageFile(new File("foo")), is(false));
        assertThat(mediaPage.isImageFile(new File("foo.jpg.txt")), is(false));

        assertThat(mediaPage.isImageFile(new File("foo.jpg")), is(true));
        assertThat(mediaPage.isImageFile(new File("foo.png")), is(true));
        assertThat(mediaPage.isImageFile(new File("foo.gif")), is(true));
        assertThat(mediaPage.isImageFile(new File("foo.bmp")), is(true));
        assertThat(mediaPage.isImageFile(new File("foo.JPEG")), is(true));
        assertThat(mediaPage.isImageFile(new File("foo.fie.PNG")), is(true));
    }
}
