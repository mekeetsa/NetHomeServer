package nu.nethome.home.items.net.wemo.soap;

import org.w3c.dom.*;
import org.w3c.dom.Node;

import javax.xml.soap.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class LightSoapClient {
    public static final int CONNECT_TIMEOUT = 100;
    public static final int READ_TIMEOUT = 100;

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
        SOAPMessage response = sendRequest(serverURI, soapMessage);
        Map<String, String> result = new HashMap<>();
        SOAPBody soapBody1 = response.getSOAPBody();
        Iterator childElements = soapBody1.getChildElements();
        while (childElements.hasNext()) {
            Object element = childElements.next();
            if (element instanceof SOAPElement) {
                SOAPElement methodResponseElement = (SOAPElement) element;
                for (int i = 0; i < methodResponseElement.getChildNodes().getLength(); i++) {
                    Node parameterNode = methodResponseElement.getChildNodes().item(i);
                    if (parameterNode.getNodeType() == Node.ELEMENT_NODE && parameterNode.getChildNodes().getLength() == 1) {
                        result.put(parameterNode.getLocalName(), parameterNode.getChildNodes().item(0).getTextContent());
                    }
                }
            }
        }
        return result;
    }

    private SOAPMessage sendRequest(String url, SOAPMessage request) throws SOAPException, IOException {
        request.writeTo(System.out);
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
