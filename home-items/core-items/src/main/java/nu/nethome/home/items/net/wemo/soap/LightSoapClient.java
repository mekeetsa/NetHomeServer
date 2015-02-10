package nu.nethome.home.items.net.wemo.soap;

import javax.xml.soap.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

/**
 *
 */
public class LightSoapClient {
    public static final int CONNECT_TIMEOUT = 1000;
    public static final int READ_TIMEOUT = 1000;

    protected Map<String, String> sendRequest(String nameSpace, String serverURI, String method, Map<String, String> arguments) throws SOAPException, IOException {
        final String ns = "u";
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(ns, nameSpace);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(method, ns);
        for (String argument : arguments.keySet()) {
            SOAPElement soapBodyElem1 = soapBodyElem.addChildElement(argument);
            soapBodyElem1.addTextNode(arguments.get(argument));
        }
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPACTION", "\"" + nameSpace + "#" + method + "\"");
        headers.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
        soapMessage.saveChanges();
        SOAPMessage result = sendRequest(serverURI, soapMessage);
        return null;
    }

    private SOAPMessage sendRequest(String url, SOAPMessage request) throws SOAPException, IOException {
        SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
        URL endpoint = new URL(new URL(url), "", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                URL target = new URL(url.toString());
                URLConnection connection = target.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                return (connection);
            }
        });
        SOAPMessage soapResponse = soapConnection.call(request, endpoint);
        soapResponse.writeTo(System.out);
        soapConnection.close();
        return soapResponse;
    }
}
