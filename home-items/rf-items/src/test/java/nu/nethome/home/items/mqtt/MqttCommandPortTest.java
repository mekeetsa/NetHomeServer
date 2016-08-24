package nu.nethome.home.items.mqtt;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MqttCommandPortTest {

    private static final String TEST_ITEM_NAME = "testItem";
    private MqttCommandPort mqttCommandPort;
    private HomeService service;
    private HomeItemProxy testItem;

    @Before
    public void setUp() throws Exception {
        mqttCommandPort = new MqttCommandPort();
        service = mock(HomeService.class);
        mqttCommandPort.activate(service);
        testItem = mock(HomeItemProxy.class);
        doReturn(testItem).when(service).openInstance(TEST_ITEM_NAME);

    }

    @Test
    public void findsCorrectHomeItemForActionWithOneLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME);

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void findsCorrectHomeItemForActionWithThreeLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test/more/levels");
        receiveMqttMessage("On", "test/more/levels/" + TEST_ITEM_NAME);

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void findsCorrectHomeItemForAttributeWithThreeLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test/more/levels");
        receiveMqttMessage("On", "test/more/levels/" + TEST_ITEM_NAME + ".attribute");

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void callsCorrectAction() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME);

        verify(testItem).callAction("On");
    }

    @Test
    public void setsAttributeValue() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME + ".attribute");

        verify(testItem).setAttributeValue("attribute", "On");
    }

    private void receiveMqttMessage(String value, String topic) {
        Event event = new InternalEvent("Mqtt_Message");
        event.setAttribute("Mqtt.Topic", topic);
        event.setAttribute("Mqtt.Message", value);
        event.setAttribute("Direction", "In");
        mqttCommandPort.receiveEvent(event);
    }
}