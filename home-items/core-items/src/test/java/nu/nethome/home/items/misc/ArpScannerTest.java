package nu.nethome.home.items.misc;

import org.junit.Before;

/**
 *
 */
public class ArpScannerTest {

    private static final String SCAN_RESPONSE1 = "Interface: eth0, datalink type: EN10MB (Ethernet)\n" +
            "Starting arp-scan 1.8.1 with 256 hosts (http://www.nta-monitor.com/tools/arp-scan/)\n" +
            "192.168.1.1     e4:f4:c6:09:2c:26\n" +
            "192.168.1.4     20:d5:bf:02:bd:ab\n" +
            "192.168.1.2     9c:20:7b:e0:08:70\n" +
            "192.168.1.9     24:0a:64:10:4a:2d\n" +
            "192.168.1.100   00:1f:d0:58:3c:e9\n" +
            "192.168.1.120   00:17:88:18:ce:ee\n" +
            "192.168.1.7     e0:c9:7a:38:fc:79\n" +
            "192.168.1.5     80:ea:96:1f:03:b5\n" +
            "\n" +
            "10 packets received by filter, 0 packets dropped by kernel\n" +
            "Ending arp-scan 1.8.1: 256 hosts scanned in 2.112 seconds (121.21 hosts/sec). 8 responded";

    private static final String SCAN_RESPONSE2 = "Interface: eth0, datalink type: EN10MB (Ethernet)\n" +
            "Starting arp-scan 1.8.1 with 256 hosts (http://www.nta-monitor.com/tools/arp-scan/)\n" +
            "192.168.1.1     e4:f4:c6:09:2c:26       (Unknown)\n" +
            "192.168.1.4     20:d5:bf:02:bd:ab       (Unknown)\n" +
            "192.168.1.7     e0:c9:7a:38:fc:79       (Unknown)\n" +
            "192.168.1.6     74:e1:b6:bd:3f:18       (Unknown)\n" +
            "192.168.1.9     24:0a:64:10:4a:2d       (Unknown)\n" +
            "192.168.1.100   00:1f:d0:58:3c:e9       GIGA-BYTE TECHNOLOGY CO.,LTD.\n" +
            "192.168.1.120   00:17:88:18:ce:ee       Philips Lighting BV\n" +
            "192.168.1.5     80:ea:96:1f:03:b5       (Unknown)\n" +
            "\n" +
            "10 packets received by filter, 0 packets dropped by kernel\n" +
            "Ending arp-scan 1.8.1: 256 hosts scanned in 4.033 seconds (63.48 hosts/sec). 8 responded";


    @Before
    public void setUp() throws Exception {

    }
}
