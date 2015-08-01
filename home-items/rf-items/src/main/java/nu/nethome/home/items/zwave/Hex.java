package nu.nethome.home.items.zwave;

/**
 *
 */
public class Hex {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    static String asHexString(byte[] message) {
        String data = new String();
        for (byte b : message) {
            data += String.format("%02X", b);
        }
        return data;
    }
}
