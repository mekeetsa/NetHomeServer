package nu.nethome.home.items.net;

import nu.nethome.home.items.net.Message;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.XmppSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class XmppClientTest {

    XmppClient client;
    private HomeService server;
    private Event event;
    private XmppSession session;

    @Before
    public void setUp() throws Exception {
        client = spy(new XmppClient());
        session = mock(XmppSession.class);
        doReturn(session).when(client).createSession();
        server = mock(HomeService.class);
        event = mock(Event.class);
        doReturn(Message.MESSAGE_TYPE).when(event).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        doReturn("xmpp:a@b").when(event).getAttribute(Message.TO);
        doReturn("subject").when(event).getAttribute(Message.SUBJECT);
        doReturn("text").when(event).getAttribute(Message.BODY);
        doReturn(Message.OUT_BOUND).when(event).getAttribute(Message.DIRECTION);
        doReturn(true).when(session).isConnected();

    }

    @Test
    public void sendsXmppMessage() throws Exception {
        client.activate(server);
        client.receiveEvent(event);

        ArgumentCaptor<Jid> jidArgumentCaptor = ArgumentCaptor.forClass(Jid.class);
        ArgumentCaptor<rocks.xmpp.core.stanza.model.client.Message> messageArgumentCaptor = ArgumentCaptor.forClass(rocks.xmpp.core.stanza.model.client.Message.class);
        verify(session).send(messageArgumentCaptor.capture());

        assertThat(messageArgumentCaptor.getValue().getTo().getDomain(), is("b"));
        assertThat(messageArgumentCaptor.getValue().getTo().getLocal(), is("a"));

    }

    @Test
    public void ignoresXmppMessageBeforeActivated() throws Exception {
        client.receiveEvent(event);
        verify(client, times(0)).createSession();
    }

    @Test
    public void ignoresXmppMessageWhenNotConnected() throws Exception {
        client.activate(server);
        doReturn(false).when(session).isConnected();
        client.receiveEvent(event);
        verify(session, times(0)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }
}
