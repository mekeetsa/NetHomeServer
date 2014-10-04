package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import org.xmpp.Connection;
import org.xmpp.TcpConnection;
import org.xmpp.XmppSession;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.client.Presence;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;

@Plugin
@HomeItemType("Ports")
public class XmppClient extends HomeItemAdapter {

    private final String model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"XmppClient\" Category=\"Ports\" >"
            + "  <Attribute Name=\"ListenPort\" Type=\"String\" Get=\"getListenPort\" Init=\"setListenPort\" Default=\"true\" />"
            + "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
            + "</HomeItem> ");

    XmppSession session;

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void activate() {
        startSession();
    }

    @Override
    public void stop() {
        stopSession();
    }

    private void stopSession() {
        if (session != null) {

        }
    }

    private void startSession() {
        Connection tcpConnection = new TcpConnection("", 0);
        session = new XmppSession("jabber.se", tcpConnection);

        // Listen for presence changes
        session.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming()) {
                    handlePresenceEvent(e);
                }
            }
        });
        // Listen for messages
        session.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                handleMessageEvent(e);
            }
        });

        try {
            session.connect();
        } catch (IOException e) {
            session = null;
            return;
        }

        try {
            session.login("nethome", "ssgjabber", "resource");
        } catch (FailedLoginException e) {
            session = null;
            return;
        } catch (LoginException e) {
            session = null;
            return;
        }

        session.send(new Presence());
    }

    private void handleMessageEvent(MessageEvent event) {
        System.out.println("Received message" + event.getMessage().getBody());
    }

    private void handlePresenceEvent(PresenceEvent event) {
        System.out.println("Received presence" + event.getPresence().getStatus());
    }
}
