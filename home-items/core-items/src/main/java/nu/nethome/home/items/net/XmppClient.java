package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import org.xmpp.Connection;
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
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.cert.CertificateException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Plugin
@HomeItemType("Ports")
public class XmppClient extends HomeItemAdapter {

    private final String model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"XmppClient\" Category=\"Ports\" >"
            + "  <Attribute Name=\"Domain\" Type=\"String\" Get=\"getDomain\" Set=\"setDomain\" Default=\"true\" />"
            + "  <Attribute Name=\"UserName\" Type=\"String\" Get=\"getUserName\" Set=\"setUserName\" />"
            + "  <Attribute Name=\"Password\" Type=\"String\" Get=\"getPassword\" Set=\"setPassword\" />"
            + "  <Attribute Name=\"Resource\" Type=\"String\" Get=\"getResource\" Set=\"setResource\" />"
            + "  <Action Name=\"SayHi\"		Method=\"sayHi\" />"
            + "</HomeItem> ");

    XmppSession session;

    String domain = "jabber.se";
    String userName = "";
    String password = "";
    String resource = "";

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
            try {
                session.close();
            } catch (IOException e) {
                // Failed to close
            }
        }
    }

    public void sayHi() {
        Jid stefan = Jid.valueOf("stefangs@" + domain + "/Monal");
        session.send(new Message(stefan, Message.Type.CHAT, "Hi Stefan!"));
    }

    private void startSession() {
        Connection tcpConnection = new TcpConnection(domain, 5222);
        session = new XmppSession(domain, tcpConnection);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
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
            session.login(userName, password, resource);
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
}
