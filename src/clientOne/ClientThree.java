package clientOne;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientThree {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientThree(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * This method is to send a message to server
     * Sending a message isn't blocking and can be done without spawning a thread,unlike waiting for a message.
     * Initially send the username of the client.
     * Create a scanner for user input.
     * While there is still a connection with the server, continue to scan the terminal and then send the message.
     */
    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Listening for a message is blocking so need a seperate thread to listen
     * While there is still a connection with the server, continue to scan the terminal and then read the message.
     */
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        // Close everything.
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    /**
     * @param socket
     * @param bufferedReader
     * @param bufferedWriter
     *
     * Helper method to close everything so you don't have to repeat yourself.
     * closing a socket will also close the socket's InputStream and OutputStream.
     * Closing the input stream closes the socket.
     * You need to use shutdownInput() on socket to just close the input stream.
     * Close the socket after closing the streams.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 6666);
        ClientThree client = new ClientThree(socket, username);
        client.listenForMessage(); //infinite loop to read messages
        client.sendMessage();   //infinite loop to send messages
    }
}
