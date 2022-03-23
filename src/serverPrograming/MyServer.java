package serverPrograming;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {

    private final ServerSocket serverSocket;

    public MyServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Listen for connections (clients to connect) on port 6666.
     * Will be closed in the Broadcaster.
     * The operating system schedules the threads.
     */
    public void startServer() {
        try {

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                Broadcaster broadcaster = new Broadcaster(socket);
                Thread thread = new Thread(broadcaster);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        MyServer server = new MyServer(serverSocket);
        server.startServer();
    }
}
