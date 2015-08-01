package nu.nethome.home.items.zwave;

import java.io.ByteArrayOutputStream;

public class Request {
    public static final byte Z_WAVE_REQUEST = 0;
    private final byte requestId;

    public Request(byte requestId) {
        this.requestId = requestId;
    }

    public byte[] encode() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        addRequestData(result);
        return result.toByteArray();
    }

    protected void addRequestData(ByteArrayOutputStream result) {
        result.write(Z_WAVE_REQUEST);
        result.write(requestId);
    }
}
