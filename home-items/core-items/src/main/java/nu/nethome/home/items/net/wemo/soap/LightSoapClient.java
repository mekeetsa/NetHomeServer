package nu.nethome.home.items.net.wemo.soap;

import javax.xml.soap.*;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class LightSoapClient {

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
        headers.addHeader("SOAPACTION", "\"" + nameSpace + "#"  + method + "\"");
        headers.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
        soapMessage.saveChanges();
        SOAPMessage result = sendRequest(serverURI, soapMessage);
        return null;
    }

    private SOAPMessage sendRequest(String url, SOAPMessage request) throws SOAPException, IOException {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // Send SOAP Message to SOAP Server
        SOAPMessage soapResponse = soapConnection.call(request, url);
        soapResponse.writeTo(System.out);

        soapConnection.close();
        return soapResponse;
    }
}
