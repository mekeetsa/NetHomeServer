package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.xmpp.Jid;
import org.xmpp.TcpConnection;
import org.xmpp.XmppSession;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashSet;

import static nu.nethome.home.items.net.Message.*;

@Plugin
@HomeItemType("Ports")
public class XmppClient extends HomeItemAdapter {

    public static final String XMPP_PREFIX = "xmpp:";
    private final String model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"XmppClient\" Category=\"Ports\" >"
            + "  <Attribute Name=\"Status\" Type=\"String\" Get=\"getStatus\" Default=\"true\" />"
            + "  <Attribute Name=\"Domain\" Type=\"String\" Get=\"getDomain\" Set=\"setDomain\" />"
            + "  <Attribute Name=\"UserName\" Type=\"String\" Get=\"getUserName\" Set=\"setUserName\" />"
            + "  <Attribute Name=\"Password\" Type=\"Password\" Get=\"getPassword\" Set=\"setPassword\" />"
            + "  <Attribute Name=\"Resource\" Type=\"String\" Get=\"getResource\" Set=\"setResource\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" />"
            + "  <Action Name=\"SayHi\"		Method=\"sayHi\" />"
            + "</HomeItem> ");

    private XmppSession session;
    private String domain = "jabber.se";
    private String userName = "";
    private String password = "";
    private String resource = "";
    private String status = "Not Connected";

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void activate() {
        startSession();
    }

    public void reconnect() {
        stopSession();
        startSession();
    }

    @Override
    public void stop() {
        stopSession();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isOutgoingMessage(event) && (session != null)) {
            return processMessage(event.getAttribute(TO), event.getAttribute(BODY));
        }
        return false;
    }

    private boolean isOutgoingMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(EVENT_TYPE) &&
                event.getAttribute(DIRECTION).equals(OUT_BOUND);
    }

    private boolean processMessage(String to, String body) {
        boolean hasSent = false;
        for (String recipient : to.split(",")) {
            if (recipient.toLowerCase().startsWith(XMPP_PREFIX)) {
                session.send(new Message(Jid.valueOf(recipient.substring(XMPP_PREFIX.length())), Message.Type.CHAT, body));
                hasSent = true;
            }
        }
        return hasSent;
    }

    private void startSession() {
        try {
            session = createSession();
            status = "Connected";
        } catch (IOException e) {
            status = "Failed to connect: " + e.getMessage();
        } catch (LoginException e) {
            status = "Failed to login: " + e.getMessage();
        }
    }

    private void stopSession() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                // Failed to close
            } finally {
                session = null;
                status = "Not Connected";
            }
        }
    }

    public void sayHi() {
        Jid stefan = Jid.valueOf("stefangs@" + domain);
        session.send(new Message(stefan, Message.Type.CHAT, "Hi Stefan!"));
    }

    private XmppSession createSession() throws IOException, LoginException {
        XmppSession newSession = new XmppSession(domain, new TcpConnection(domain, 5222));
        limitAuthenticationMechanisms(newSession);
        trustAnyCertificate(newSession);
        listenForPresenceChanges(newSession);
        listenForMessages(newSession);
        newSession.connect();
        newSession.login(userName, password, resource);
        newSession.send(new Presence());
        return newSession;
    }

    private void listenForMessages(XmppSession session) {
        session.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                handleMessageEvent(e);
            }
        });
    }

    private void listenForPresenceChanges(XmppSession session) {
        session.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming()) {
                    handlePresenceEvent(e);
                }
            }
        });
    }

    private void limitAuthenticationMechanisms(XmppSession session) {
        session.getAuthenticationManager().setPreferredMechanisms(new LinkedHashSet<>(Arrays.asList("DIGEST-MD5", "PLAIN")));
    }

    private void trustAnyCertificate(XmppSession session) throws IOException {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Could not configure trust", e);
        }
        try {
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        session.getSecurityManager().setSSLContext(sslContext);
    }

    private void handleMessageEvent(MessageEvent event) {
        System.out.println("Received message: " + event.getMessage().getBody());
    }

    private void handlePresenceEvent(PresenceEvent event) {
        System.out.println("Received presence: " + event.getPresence().getStatus());
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getStatus() {
        if (session != null) {
            return session.getStatus().name().toLowerCase();
        }
        return status;
    }
}
