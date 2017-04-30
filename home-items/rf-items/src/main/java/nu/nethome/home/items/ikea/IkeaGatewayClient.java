package nu.nethome.home.items.ikea;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 */
public class IkeaGatewayClient {

    private InMemoryPskStore pskStore;
    private CoapEndpoint dtlsEndpoint;

    public void start() {
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        pskStore = new InMemoryPskStore();
        builder.setPskStore(pskStore);
        builder.setSupportedCipherSuites(new CipherSuite[]{CipherSuite.TLS_PSK_WITH_AES_128_CCM_8});
        DTLSConnector dtlsconnector = new DTLSConnector(builder.build(), null);
        try {
            dtlsEndpoint = new CoapEndpoint(dtlsconnector, NetworkConfig.getStandard());
            dtlsEndpoint.start();
            EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint);
        } catch (IOException e) {
            dtlsEndpoint = null;
        }
    }

    public void setRouterKey(InetSocketAddress peerAddress, String identity, byte[] key) {
        pskStore.addKnownPeer(peerAddress, identity, key);
    }

    public void stop() {
        if (dtlsEndpoint != null) {
            dtlsEndpoint.stop();
            dtlsEndpoint.destroy();
            dtlsEndpoint = null;
        }
    }

    public Response sendCoapsMessage(String uri, String method, String body) {
        Request request = requestFromType(method);
        request.setURI(uri);
        request.setPayload(body);
        request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);

        request.send();

        Response response;
        try {
            response = request.waitForResponse();
            return response;
        } catch (InterruptedException e) {
            return null;
        }
    }

    private static Request requestFromType(String method) {
        if (method.equalsIgnoreCase("GET")) {
            return Request.newGet();
        } else if (method.equalsIgnoreCase("POST")) {
            return Request.newPost();
        } else if (method.equalsIgnoreCase("PUT")) {
            return Request.newPut();
        } else if (method.equalsIgnoreCase("DELETE")) {
            return Request.newDelete();
        } else {
            return Request.newGet();
        }
    }

}
