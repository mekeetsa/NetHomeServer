package nu.nethome.home.impl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BootWebServer {
    private static final String END_OF_HEADERS_STRING = "";
    private boolean isRunning;
    private ServerSocket serverSocket = null;
    private int listenPort;
    private String message;

    public BootWebServer() {
        message = "Starting";
    }

    public void start(int listenPort) {
        this.listenPort = listenPort;
        new Thread(new Runnable() {
            @Override
            public void run() {
                runLoop();
            }
        }, "BootWebServer").start();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                isRunning = false;
                serverSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public void runLoop() {
        try {
            isRunning = true;
            serverSocket = new ServerSocket(listenPort);
            while (isRunning) {
                try (Socket inSocket = serverSocket.accept()){
                    BufferedReader in = new BufferedReader(new InputStreamReader(inSocket.
                            getInputStream()));
                    PrintWriter out = new PrintWriter(inSocket.getOutputStream());
                    processRequest(inSocket);
                }
                catch (Exception e) {
                    // Ignore
                }
            }
        }
        catch (Exception e) {
            // Ignore
        }
        System.out.println("Exiting listener");
    }

    private void processRequest(Socket inSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inSocket.
                getInputStream()));
        PrintWriter out = new PrintWriter(inSocket.getOutputStream());
        consumeRequestData(in);
        printHttpHeaders(out);
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta http-equiv=\"REFRESH\" content=\"4;url=http://" + inSocket.getLocalAddress().getHostAddress() + ":" + listenPort + "/home\">\n" +
                "  <meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">\n" +
                "  <title>NetHome</title>\n" +
                "</head>\n" +
                "<body>\n" +
                " <p>" + message + "</p>\n" +
                "</body>\n");
        out.flush();
    }

    private void consumeRequestData(BufferedReader in) throws IOException {
        String s;
        do {
            s = in.readLine();
        } while (!END_OF_HEADERS_STRING.equals(s));
    }

    private void printHttpHeaders(PrintWriter out) {
        out.println("HTTP/1.0 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: OpenNetHome");
        out.println("");
    }
}
