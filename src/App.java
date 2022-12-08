import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class is responsible for the physical representation of the program and handling the <code>ActionListener</code>s
 */
public class App extends JFrame implements Runnable, Observable{
    /**
     * <code>JList</code> containing and physically showing the registered users. One can be chosen and messages can be sent to this user.
     */
    private JList<String> contactJList;
    /**
     * Showing the received messages. Message that were received from the chosen user.
     */
    private JList<String> receivedList;
    /**
     * Showing the sent messages. The sent messages that were sent to the chosen user.
     */
    private JList<String> sentList;
    /**
     * When this button is clicked, the message will be sent to the server and appears on the sent list side.
     */
    private JButton sendButton;
    /**
     * In this text field must be written, that should be sent to the chosen user.
     */
    private JTextField messageTextField;
    /**
     * In this panel will appear the view. The view that was designed by a built in GUI designer in IntelliJ.
     */
    private JPanel mainPanel;
    /**
     * A label, on the top of the screen, where the time will be displayed
     */
    private JLabel timeLabel;
    /**
     * Every application has an own socket which is used to communicate with the server.
     * This should be unique and only one should exist, so on the server side it is easier to identify the client sockets.
     */
    private final DatagramSocket socket;
    /**
     * A username which is given, when the client logs in.
     */
    private final String username;

    /**
     * Instance of the <code>ServerCommunication</code> class which is responsible for the actual communication through the network, with the help of sockets.
     */
    private final ServerCommunication communicator;

    public App() {

        try {
            communicator = new ServerCommunication();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        try {
            /**
             * Setting that the lists should have any values selected on default.
             */
            receivedList.setSelectionInterval(-1, -1);
            sentList.setSelectionInterval(-1, -1);
            socket = new DatagramSocket();
            /**
             * Getting the username of the client when starting the program and saving it as a property which specifies one running application.
             */
            username = loginDialog();
            printContacts();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        /**
         * Setting the <code>ContentPane</code> to the panel, which was designed in the built in GUI designer.
         */
        setContentPane(mainPanel);
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSendButtonOnClick();
        /**
         * <code>ActionListener</code> which is triggered when a new user is selected in the contact's list.
         * In this case the proper messages must be get from the server which were exchanged between the actual client and the chosen user.
         */
        contactJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DefaultListModel<String> emptyListModel = new DefaultListModel<>();
                sentList.setModel(emptyListModel);
                receivedList.setModel(emptyListModel);
            }

        });
        /**
         * A new observer is registered for this observable (observed) class.
         */
        register(communicator);
    }

    /**
     * At the beggining of the program a login dialog pops up, where the username must be written.
     * If nothing is written or the "cancel" button or the "x" button is clicked, the program stops running.
     * The given username is saved and used for identification when asking for messages and sending messages.
     * @return returns the username, written in the input field.
     */
    private String loginDialog() {
        /**
         * Setting up the login dialog
         */
        JFrame loginFrame = new JFrame("Login");
            String username = JOptionPane.showInputDialog(loginFrame, "Username: ", null);
            if(username == null){
                System.exit(0);
            }if(username.equals("")){
                System.exit(0);
            }
        try {
            /**
             * Sending the login message if a valid name is written and "OK" button is pressed.
             */
            communicator.sendLoginToServer("login," + username, socket);
        } catch (IOException e) {
            System.out.println("Could not log in");
        }
        return username;
    }

    /**
     * Prints the list of the users into the contact list.
     * The client's name, who is using the application should not appear for themselves.
     */
    private void printContacts() {
        ArrayList<String> contactsList = communicator.getContactsFromServer(socket, username);
        DefaultListModel<String> contactListModel = new DefaultListModel<>();
        int pos = contactJList.getSelectedIndex();
        for (String s : contactsList) {
            if (!(s.equals(username))) {
                contactListModel.addElement(s);
            }
        }

        contactJList.setModel(contactListModel);
        contactJList.setSelectedIndex(pos);
    }

    /**
     * Setting the <code>ActionListener</code> for "Send!" button.
     * When clicking this button, the message must be sent to the server and the text should disappear from the text field.
     * Sending the message is implemented with the help of observer pattern.
     */
    private void setSendButtonOnClick() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if (contactJList.getSelectedValue() != null) {
                       // communicator.sendMessageToServer(username, contactJList.getSelectedValue(), messageTextField.getText());
                        reportToObservers();
                    }

                messageTextField.setText("");

            }
        });
    }

    /**
     * There are actions which should be repeated periodically.
     * For this, an <code>ActionListener</code> is needed which will be triggered by a timer every 100 ms.
     * Every 100 ms the contact list, the time label is refreshed and the messages that were sent between the client and the chosen user.
     */
    ActionListener forTimer = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            Date date = new Date();
            timeLabel.setText(date.toString());
            printContacts();
            if (contactJList.getSelectedValue() != null) {
                communicator.getMessagesFromServer(receivedList, sentList, username, contactJList.getSelectedValue());
            }
        }
    };

    /**
     * Separate thread for the timer, which triggeres <code>forTimer ActionListener</code> every 100 ms.
     */
    @Override
    public void run() {
        Timer timer = new Timer(100, forTimer);
        timer.start();
    }

    /**
     * implementation of the observer pattern. Observers can be registered to this observed (observable) class.
     * @param observer Observer who should observ the observable objects
     */
    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    /**
     * implementation of the observer pattern. Observer should be removed which belongs to this class.
     * @param observer Observer which should be removed from the observer list of the observable
     */
    @Override
    public void unregister(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Signaling the observers to take an action, because in this observed class the trigerring action already happened.
     */
    @Override
    public void reportToObservers() {
        for(Observer o: observers){
            o.report( username, contactJList.getSelectedValue(), messageTextField.getText());
        }
    }
    
}
