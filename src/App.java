import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;

import java.util.ArrayList;
import java.util.Date;


public class App extends JFrame implements Runnable, Observable{
    private JList<String> contactJList;
    private JList<String> receivedList;
    private JList<String> sentList;
    private JButton sendButton;
    private JTextField messageTextField;
    private JPanel mainPanel;
    private JLabel timeLabel;
    private final DatagramSocket socket;
    private final String username;

    private final ServerCommunication communicator;

    public App() {

        try {
            communicator = new ServerCommunication();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        try {
            receivedList.setSelectionInterval(-1, -1);
            sentList.setSelectionInterval(-1, -1);
            socket = new DatagramSocket();
            username = loginDialog();
            printContacts();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        setContentPane(mainPanel);
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSendButtonOnClick();
        contactJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DefaultListModel<String> emptyListModel = new DefaultListModel<>();
                sentList.setModel(emptyListModel);
                receivedList.setModel(emptyListModel);
            }

        });
        register(communicator);
    }


    private String loginDialog() {
        JFrame loginFrame = new JFrame("Login");
            String username = JOptionPane.showInputDialog(loginFrame, "Username: ", null);
            if(username == null){
                System.exit(0);
            }if(username.equals("")){
                System.exit(0);
            }
        try {
            communicator.sendLoginToServer("login," + username, socket);
        } catch (IOException e) {
            System.out.println("Could not log in");
        }
        return username;
    }

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

    @Override
    public void run() {
        Timer timer = new Timer(100, forTimer);
        timer.start();
    }


    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void reportToObservers() {
        for(Observer o: observers){
            o.report( username, contactJList.getSelectedValue(), messageTextField.getText());
        }
    }
    
}
