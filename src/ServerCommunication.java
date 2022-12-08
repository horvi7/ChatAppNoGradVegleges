import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class manages the whole communication between the server application and the client application.
 */
public class ServerCommunication implements Observer{

    /**
     * IPv4 Address of the server. With this server communicates the client program.
     */
    private InetAddress serverIP;
    /**
     * Socket used on the client side to communicate with the server.
     */
    private final DatagramSocket socket;

    /**
     * When this class is instantiated at the start of the program, the constructor waits for the IPv4 Address of the server,
     * which is broadcasted by the server.
     * This address is saved and used later to be able to start communicating with the server.
     * @throws IOException
     */
    public ServerCommunication() throws IOException {
        socket = new DatagramSocket();
        DatagramSocket broadcastReceiver = new DatagramSocket(50001);
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

        broadcastReceiver.receive(packet);
        serverIP = packet.getAddress();
    }

    /**
     * When clicking on the username in the contactlist, the user, who has logged in should see the previously sent messages with this user
     * After the request is sent, a response comes from the server. This response is interpreted and the messages are put on the screen.
     * @param receivedList <code>JList</code> where the messages should appear, which were sent by the chosen user
     * @param sentList  <code>JList</code> where the messages should appear, which were sent by the user itself
     * @param username Username, which was given at login. It's used for getting the appropriate messages from the server
     * @param other With this user communicates the user who logged in, and asking for the messages - that were previously sent between them - from the server
     */
    public void getMessagesFromServer(JList<String> receivedList, JList<String> sentList, String username, String other) {
        try {
            /**
             * Creating and sending the request message to the server.
             */
            String packetMessage = "get," + username + "," + other;
            DatagramPacket msg = new DatagramPacket(packetMessage.getBytes(), packetMessage.getBytes().length, serverIP, 50000);
            socket.send(msg);

            DatagramPacket packet;
            //itt egy deadlock, ezt meg kéne oldani. az utolsó üzenet feldolgozása után is vár a socket
            socket.setSoTimeout(100);
            int numOfReceivedMessages =0;
            /**
             * Receiving the data from the server
             */
            do {
                packet = new DatagramPacket(new byte[1024], 1024);
                try {
                    socket.receive(packet);
                }catch (SocketTimeoutException e){
                    break;
                }
                numOfReceivedMessages++;
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                String[] messageElements = receivedMessage.split(",");
                if (messageElements.length == 3) {
                    String from = messageElements[0];
                    String message = messageElements[2];
                    /**
                     * The messages appear physically on the screen
                     */
                    placeMessages(sentList, receivedList, from, username, message, numOfReceivedMessages);
                }

            } while (true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The messages that are sent from the client itself appear on the right side of the window and the received messages appear
     * in the middle of the screen.
     * @param sentList here must be placed the sent messages
     * @param receivedList here must be placed the received messages
     * @param from the sender part of a received message
     * @param username the username of the client
     * @param message the sent message's inhalt
     * @param numOfReceivedMessages number of received messages to be able to manipulate the received and sent messages lists
     */
    private void placeMessages(JList<String> sentList, JList<String> receivedList, String from, String username, String message, int numOfReceivedMessages) {
        DefaultListModel<String> receivedListModel = new DefaultListModel<>();
        DefaultListModel<String> sentListModel = new DefaultListModel<>();

        /**
         * Getting the previous elements that were already in the lists.
         */
        int sentListSize = sentList.getModel().getSize();
        int receivedListSize= receivedList.getModel().getSize();
        for (int i = 0; i < sentListSize; i++) {
            sentListModel.addElement(sentList.getModel().getElementAt(i));
        }
        for (int i = 0; i < receivedListSize; i++) {
            receivedListModel.addElement(receivedList.getModel().getElementAt(i));
        }
        /**
         * if the message that the client got and the username are equal then the message should appear on the sender side
         * and on the other side should not appear a message ("")
         * vica versa
         */
        if(numOfReceivedMessages > sentListSize) {
            if (username.equals(from)) {
                sentListModel.addElement(message);
                receivedListModel.addElement(" ");

            } else {
                receivedListModel.addElement(message);
                sentListModel.addElement(" ");
            }
        }
        sentList.setModel(sentListModel);
        receivedList.setModel(receivedListModel);


    }

    /**
     * When logging in, the client must send a login message to the server, because it trigger actions on the server side.
     * e.g. the client will receive the list of the registered users after sending the login message.
     * This method can be repeatedly used, to get a refreshment on the contact list, making it up to date.
     * @param message The well-constructed message that the server can interpret
     * @param socket socket of the client, which sends the data to the server
     * @throws IOException
     */
    public void sendLoginToServer(String message, DatagramSocket socket) throws IOException {
        try {

            DatagramPacket msg = new DatagramPacket(message.getBytes(), message.getBytes().length, serverIP,50000);
            socket.send(msg);

        } catch (IOException e) {
            throw new IOException();

        }
    }

    /**
     * When clicking the "Send!" button the message must be sent to the server.
     * This method cuntructs a message, based on the sender, recipient and the message, which can be interpreted and saved by the server
     * @param from Sender of the message used to construct a <code>DatagramPacket</code> that can be sent to the server
     * @param to Recipient of the message used to construct a <code>DatagramPacket</code> that can be sent to the server
     * @param message The inhalt of the message used to construct a <code>DatagramPacket</code> that can be sent to the server
     * @throws IOException
     */
    public void sendMessageToServer(String from, String to, String message) throws IOException {
        try {
            if (!message.equals("")){
                String packetMessage = "msg," + from + "," + to + "," + message;
            DatagramPacket msg = new DatagramPacket(packetMessage.getBytes(), packetMessage.getBytes().length, serverIP, 50000);
            socket.send(msg);
        }
        } catch (IOException e) {
            throw new IOException();

        }
    }

    /**
     * The server can ask for the contact (users) explicitly. A login message is sent to the server, which responds with the list
     * of the registered users. An <code>ArrayList</code> is made and returned by this function.
     * @param socket Socket of the application used to send the login type message to the server
     * @param username Username of the client
     * @return
     */
    public ArrayList<String> getContactsFromServer(DatagramSocket socket, String username) {
        try {
            /**
             * login message sent to server
             */
            sendLoginToServer("login," + username, socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<String> contactList = new ArrayList<>();

        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        try {
            /**
             * Makin <code>ArrayList</code> which will be returned
             */
            socket.receive(packet);
            String contacts = new String(packet.getData(), 0, packet.getLength());
            String[] contactsArr = contacts.split(",");
            Collections.addAll(contactList, contactsArr);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contactList;
    }

    /**
     *This action will be fired after observer is triggered by an observed object
     * @param username username of the client
     * @param contact chosen contact in the list
     * @param msg message that must be sent
     */
    @Override
    public void report(String username, String contact, String msg) {
        try {
            sendMessageToServer(username, contact, msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
