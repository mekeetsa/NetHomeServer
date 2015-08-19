package nu.nethome.home.items.zwave;

import nu.nethome.home.items.zwave.messages.DecoderException;
import nu.nethome.home.items.zwave.messages.MemoryGetId;
import nu.nethome.home.items.zwave.messages.Message;
import nu.nethome.home.items.zwave.messages.MultiMessageProcessor;
import nu.nethome.home.items.zwave.messages.commands.MultiInstanceAssociation;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 */
public abstract class ZWaveExecutor {

    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";
    public static final String ZWAVE_EVENT_TYPE = "ZWave_Message";

    private HomeService server;
    private MultiMessageProcessor messageProcessor;

    public ZWaveExecutor(HomeService server, OutputStream outputStream) {
        //To change body of created methods use File | Settings | File Templates.
        this.server = server;
        messageProcessor = new MultiMessageProcessor();
        messageProcessor.addMessageProcessor(MemoryGetId.MEMORY_GET_ID, new MemoryGetId.Response.Processor());
    }

    public String executeCommandLine(String commandLine) {
        CommandLineParser parser = new CommandLineParser(commandLine);
        String command = parser.getString();
        if (command.equalsIgnoreCase("MemoryGetId")) {
            sendRequest(new MemoryGetId.Request());
        }
        print("Ok.\n\r");
        return "";
    }

    private void sendRequest(Message request) {
        byte[] message = request.encode();
        String data = Hex.asHexString(message);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int) message[1]) & 0xFF);
        event.setAttribute("Direction", "Out");
        server.send(event);
    }


    void processEvent(Event event) {
        if (event.isType(ZWAVE_EVENT_TYPE) &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE).length() > 0) {
            byte[] message = Hex.hexStringToByteArray(event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            processZWaveMessage(message);
        }
    }

    private void processZWaveMessage(byte[] message) {
        try {
            Message received = messageProcessor.process(message);
            print(received.toString() + "\n\r");
        } catch (DecoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    abstract void print(String string);

    static class CommandLineParser {
        Iterator<String> elements;

        CommandLineParser(String line) {
            this.elements = Arrays.asList(line.split(" ")).iterator();
        }

        public String getString() {
            return elements.next();
        }

        public int getInt() {
            return Integer.parseInt(elements.next());
        }
    }
}
