package by.bsuir.configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server extends Thread {

    private final int port;

    private final int channels;

    private final String ip;

    private final String password;

    private final List<ServerConnection> connections;

    private ServerSocket serverSocket;

    public Server(int port, int channels, String ip, String password) {
        this.port = port;
        this.channels = channels;
        this.ip = ip;
        this.password = password;
        connections = new LinkedList<>();
    }

    List<ServerConnection> getConnections() {
        return connections;
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ServerConnection connection = new ServerConnection(password, this, clientSocket);
                connections.add(connection);
                connection.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() throws IOException {
        String[] numbers = ip.trim().split("\\.");
        byte[] address = new byte[] { (byte) Short.parseShort(numbers[0]), (byte) Short.parseShort(numbers[1]),
                (byte) Short.parseShort(numbers[2]), (byte) Short.parseShort(numbers[3]) };
        serverSocket = new ServerSocket(port, channels, InetAddress.getByAddress(address));
        start();
    }

    public void stopServer() {
        try {
            for (ServerConnection connection : connections) {
                connection.getSocket().shutdownOutput();
                connection.getSocket().shutdownInput();
                connection.getSocket().close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}