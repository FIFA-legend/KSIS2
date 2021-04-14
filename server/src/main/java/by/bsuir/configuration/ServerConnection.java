package by.bsuir.configuration;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServerConnection extends Thread {

    private String login;

    private final String password;

    private BufferedWriter outputStream;

    private final Socket socket;

    private final Server server;

    public ServerConnection(String password, Server server, Socket socket) {
        this.password = password;
        this.socket = socket;
        this.server = server;
    }

    public String getLogin() {
        return login;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while (!socket.isClosed() && (line = reader.readLine()) != null) {
            String command;
            if (line.contains(" ")) {
                command = line.substring(0, line.indexOf(" "));
            } else {
                command = line;
            }
            switch (command) {
                case "logout":
                    handleLogout();
                    break;
                case "login":
                    handleLogin(line);
                    break;
                case "msg":
                    handleMessage(line);
                    break;
                default:
                    String message = "Unknown command: " + command + "\n\r";
                    outputStream.write(message);
                    outputStream.flush();
                    break;
            }
        }
    }

    private void handleLogin(String line) throws IOException {
        String[] tokens = line.split("\\s+");
        if (tokens.length == 3) {
            String name = tokens[1];
            String pass = tokens[2];
            if (!isLogin(name) && pass.equals(password)) {
                String acceptMessage = "accept\n\r";
                outputStream.write(acceptMessage);
                outputStream.flush();
                login = name;
                String online = "online " + login + "\n\r";
                sendMessageToAll(online);
                getOnline(name);
            } else {
                String declineMessage = "decline\n\r";
                outputStream.write(declineMessage);
                outputStream.flush();
                server.getConnections().remove(this);
            }
        }
    }

    private boolean isLogin(String login) {
        for (ServerConnection connection : server.getConnections()) {
            if (connection.getLogin() != null && connection.getLogin().equals(login)) return true;
        }
        return false;
    }

    private void sendMessageToAll(String message) throws IOException {
        for (ServerConnection connection : server.getConnections()) {
            if (!login.equals(connection.getLogin())) {
                connection.send(message);
            }
        }
    }

    private void getOnline(String login) throws IOException {
        for (ServerConnection connection : server.getConnections()) {
            if (connection.getLogin() != null && !login.equals(connection.getLogin())) {
                String msg2 = "online " + connection.getLogin() + "\n\r";
                this.send(msg2);
            }
        }
    }

    private void send(String message) throws IOException {
        outputStream.write(message);
        outputStream.flush();
    }

    private void handleMessage(String line) throws IOException {
        String text = line.substring(line.indexOf(" ") + 1);
        String messageCommand = "msg " + login + " " + text + "\n\r";
        sendMessageToAll(messageCommand);
    }

    private void handleLogout() throws IOException {
        String leaveMessage = "logout " + login + "\n\r";
        sendMessageToAll(leaveMessage);
        send("quit");
        server.getConnections().remove(this);
        socket.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConnection that = (ServerConnection) o;
        return Objects.equals(login, that.login) && Objects.equals(password, that.password) && Objects.equals(outputStream, that.outputStream) && Objects.equals(socket, that.socket) && Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, outputStream, socket, server);
    }

    /*private void handleClientSocket() throws IOException, InterruptedException {
        outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens.length > 0) {
                String command = tokens[0];
                if (command.equalsIgnoreCase("logout")) {
                    handleLogout();
                    break;
                } else if (command.equalsIgnoreCase("login")) {
                    handleLogin(tokens);
                } else if (command.equalsIgnoreCase("msg")) {
                    handleMessage(tokens);
                } *//*else if (command.equalsIgnoreCase("join")) {
                    handleJoin(tokens);
                } else if (command.equalsIgnoreCase("leave")) {
                    handleLeave(tokens);
                }*//* else {
                    String message = "Unknown command: " + command + "\n\r";
                    outputStream.write(message.getBytes());
                }
            }
        }
    }*/

    /*private void handleLogin(String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String pass = tokens[2];
            if (!hasSuchUser(login) && pass.equals(password)) {
                String acceptMessage = "Logged In\n\r";
                outputStream.write(acceptMessage.getBytes());
                this.login = login;

                for (ServerConnection connection : server.getConnections()) {
                    if (connection.getLogin() != null && !login.equals(connection.getLogin())) {
                        String msg2 = "Online " + connection.getLogin() + "\n\r";
                        this.send(msg2);
                    }
                }

                String online = "Online " + login + "\n\r";
                for (ServerConnection connection : server.getConnections()) {
                    if (!login.equals(connection.getLogin())) {
                        connection.send(online);
                    }
                }
            } else {
                String declineMessage = "Wrong user or password\n\r";
                outputStream.write(declineMessage.getBytes());
            }
        }
    }

    private boolean hasSuchUser(String login) {
        for (ServerConnection connection : server.getConnections()) {
            if (connection.getLogin() != null && connection.getLogin().equals(login)) return true;
        }
        return false;
    }

    private void send(String message) throws IOException {
        outputStream.write(message.getBytes());
    }

    private void handleMessage(String[] tokens) throws IOException {
        String receiver = tokens[1];
        StringBuilder text = new StringBuilder();
        for (int i = 2; i < tokens.length; i++) {
            text.append(tokens[i]).append(" ");
        }

        boolean isTopic = (receiver.startsWith("#"));

        for (ServerConnection connection : server.getConnections()) {
            if (connection.getLogin().equals(login)) continue;
            if (isTopic) {
                if (connection.isInTopic(receiver) && this.isInTopic(receiver)) {
                    String result = "msg " + receiver + " " + login + ": " + text.toString() + "\n\r";
                    connection.send(result);
                }
            } else {
                if (connection.getLogin().equals(receiver)) {
                    String result = "msg " + login + ": " + text.toString() + "\n\r";
                    connection.send(result);
                }
            }
        }
    }

    private void handleLogout() throws IOException {
        String leaveMessage = "User \"" + login + "\" left the group.\n\r";
        for (ServerConnection connection : server.getConnections()) {
            if (!login.equals(connection.getLogin())) {
                connection.send(leaveMessage);
            }
        }
        socket.close();
        server.getConnections().remove(this);
    }*/

    /*private boolean isInTopic(String topic) {
        return topics.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            topics.add(tokens[1]);
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            topics.remove(tokens[1]);
        }
    }*/
}
