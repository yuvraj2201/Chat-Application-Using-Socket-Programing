package serverPrograming;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Broadcaster implements Runnable{

    public static ArrayList<Broadcaster> clients = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    /**
     * @param socket
     * When a client connects their username is sent.
     * Add the new client handler to the array so they can receive messages from others.
     */
    public Broadcaster(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            clients.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Everything in this method is run on a separate thread. We want to listen for messages on a separate thread
     * because listening (bufferedReader.readLine()) is a blocking operation.
     * A blocking operation means the caller waits for the callee to finish its operation.
     * Continue to listen for messages while a connection with the client is still established.
     * Read what the client sent and then send it to every other client.
     */
    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    /**
     *@param messageToSend
     * Send a message through each client handler thread so that everyone gets the message.
     * Basically each client handler is a connection to a client. So for any message that is received,
     * loop through each connection and send it down it.
     */
    public void broadcastMessage(String messageToSend) {
        for (Broadcaster broadcaster : clients) {
            try {
                if (!broadcaster.clientUsername.equals(clientUsername)) {
                    broadcaster.bufferedWriter.write(messageToSend);
                    broadcaster.bufferedWriter.newLine();
                    broadcaster.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    /**
     * If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
     */
    public void removeClientHandler() {
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
        clients.remove(this);

    }

    /**
     * @param socket
     * @param bufferedReader
     * @param bufferedWriter
     * The client disconnected or an error occurred so remove them from the list so no message is broadcasted.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
