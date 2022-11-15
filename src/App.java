import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import static java.lang.Thread.sleep;

public class App extends JFrame implements Runnable{
    private JList contactList;
    private JList receivedList;
    private JList sentList;
    private JButton sendButton;
    private JTextField messageTextField;
    private JPanel mainPanel;
    private JLabel timeLabel;

    public App(){
        setContentPane(mainPanel);
        setSize(600,600);
        setVisible(true);






        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                putMessageToSentList();
                receivedListGetsEmptyMessage();
            }
        });
    }

    private void putMessageToSentList() {
        int sentListSize = sentList.getModel().getSize();
        DefaultListModel sentListModel = new DefaultListModel();
        for (int i = 0; i < sentListSize; i++){
            sentListModel.addElement(sentList.getModel().getElementAt(i));
        }
        String s = messageTextField.getText() +'\n';
        sentListModel.addElement(s);

        sentList.setModel(sentListModel);
    }

    private void receivedListGetsEmptyMessage() {
        int receivedListSize = receivedList.getModel().getSize();
        DefaultListModel receivedListModel = new DefaultListModel();
        for (int i = 0; i < receivedListSize; i++){
            receivedListModel.addElement(receivedList.getModel().getElementAt(i));
        }

        receivedListModel.addElement("1");

        receivedList.setModel(receivedListModel);
    }


    @Override
    public void run() {
        ActionListener forTimer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date date = new Date();
                timeLabel.setText(date.toString());
            }
        };

        while (true) {
            Timer timer = new Timer(0, forTimer);
            timer.start();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        }
    }
    public JList getSentList(){
        return this.sentList;
    }
}
