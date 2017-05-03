package nu.nethome.home.items.ikea;

import org.eclipse.californium.core.coap.CoAP;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class IkeaGatewayClient {

    private final String NODES = "/15001";
    private static Logger logger = Logger.getLogger(IkeaGatewayClient.class.getName());

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

    public Response sendCoapMessage(String uri, String method, String body) {
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

    public JSONData getJsonMessage(String uri) {
        Request request = Request.newGet();
        request.setURI(uri);
        request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);

        request.send();

        Response response;
        try {
            response = request.waitForResponse();
            if (response != null &&
                    response.getPayloadSize() > 0 &&
                    CoAP.ResponseCode.isSuccess(response.getCode())) {
                return new JSONData(response.getPayloadString());
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
    }

    public List<JSONObject> getNodes(String address) {
        ArrayList<JSONObject> result = new ArrayList<>();
        JSONData nodelist = getJsonMessage(String.format("coaps://%s%s", address, NODES));
        if (nodelist != null && !nodelist.isObject()) {
            JSONArray jsonArray = nodelist.getArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                int nodeId = jsonArray.getInt(i);
                JSONData nodeInfo = getJsonMessage(String.format("coaps://%s%s/%d", address, NODES, nodeId));
                if (nodeInfo != null && nodeInfo.isObject()) {
                    result.add(nodeInfo.getObject());
                    System.out.printf("%s\n", nodeInfo.getObject().toString(3));
                }
            }
        }
        return result;
    }
    public String findGateway() {
        return "";
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
