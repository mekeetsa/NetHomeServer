package nu.nethome.home.items.zwave.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Request {
    public static final byte Z_WAVE_REQUEST = 0;
    private final byte requestId;

    public Request(byte requestId) {
        this.requestId = requestId;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            addRequestData(result);
            return result.toByteArray();
        } catch (IOException e) {
            // This should not happen
            return new byte[0];
        }
    }

    protected void addRequestData(ByteArrayOutputStream result) throws IOException {
        result.write(Z_WAVE_REQUEST);
        result.write(requestId);
    }
}
