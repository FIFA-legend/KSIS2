package by.bsuir.client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class Client {

    private final int port;

    private final String serverName;

    private final String login;

    private final String password;

    private Socket socket;

    private BufferedReader inputStream;

    private BufferedWriter outputStream;

    private final List<UserOnlineListener> onlineListeners;

    private final List<UserOfflineListener> offlineListeners;

    private final List<MessageListener> messageListeners;

    public Client(int port, String serverName, String login, String password) {
        this.port = port;
        this.serverName = serverName;
        this.login = login;
        this.password = password;
        onlineListeners = new LinkedList<>();
        offlineListeners = new LinkedList<>();
        messageListeners = new LinkedList<>();
    }

    public boolean connect() {
        try {
            String[] numbers = serverName.trim().split("\\.");
            byte[] address = new byte[] { (byte) Short.parseShort(numbers[0]), (byte) Short.parseShort(numbers[1]),
                    (byte) Short.parseShort(numbers[2]), (byte) Short.parseShort(numbers[3]) };
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByAddress(address), port);
            socket.connect(socketAddress);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login() {
        String command = "login " + login + " " + password + "\n\r";
        try {
            outputStream.write(command);
            outputStream.flush();
            String response = inputStream.readLine();
            if (response.equals("accept")) {
                startMessageReader();
                return true;
            } else if (response.equals("decline")) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean message(String message) {
        String cmd = "msg " + message + "\n\r";
        try {
            outputStream.write(cmd);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean logoff() {
        String command = "logout\n\r";
        try {
            outputStream.write(command);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void startMessageReader() {
        Thread thread = new Thread(this::readMessageLoop);
        thread.start();
    }

    private void readMessageLoop() {
        try {
            boolean flag = true;
            String line;
            while (flag && (line = inputStream.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String command = tokens[0];
                switch (command) {
                    case "online":
                        handleOnline(tokens);
                        break;
                    case "logout":
                        handleOffline(tokens);
                        break;
                    case "msg":
                        handleMessage(tokens);
                        break;
                    case "quit":
                        flag = false;
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserOnlineListener listener : onlineListeners) {
            listener.online(login);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserOfflineListener listener : offlineListeners) {
            listener.offline(login);
        }
    }

    private void handleMessage(String[] tokens) {
        String login = tokens[1];
        StringBuilder text = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            text.append(" ").append(tokens[i]);
        }

        for (MessageListener listener : messageListeners) {
            listener.message(login, text.toString());
        }
    }

    public void addUserOnlineListener(UserOnlineListener listener) {
        onlineListeners.add(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void addUserOfflineListener(UserOfflineListener listener) {
        offlineListeners.add(listener);
    }
}
