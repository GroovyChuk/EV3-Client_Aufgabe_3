package src.Client;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JConsolePanel extends JPanel{
    public static JTextArea consoleTextArea;
    public static JTextField textFieldDriveFetch;
    public static JButton buttonDriveFetch;

    public JConsolePanel() {
        setPreferredSize(new Dimension(JConstants.PANEL_CONSOLE_SIZE_X,JConstants.WINDOW_SIZE_Y));
        setLayout(new BorderLayout());

        consoleTextArea = new JTextArea(32,1);
        consoleTextArea.setSize(new Dimension(JConstants.PANEL_CONSOLE_SIZE_X,JConstants.WINDOW_SIZE_Y));
        consoleTextArea.setEditable(false);
        consoleTextArea.setLineWrap(true);

        DefaultCaret caret = (DefaultCaret)consoleTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);

        buttonDriveFetch = new JButton("Drive and fetch");
        buttonDriveFetch.setMargin(new Insets(0,0,0,0));
        textFieldDriveFetch = new JTextField(12);

        JPanel jPanelDriveFetch = new JPanel();
        jPanelDriveFetch.setLayout(new BorderLayout());

        jPanelDriveFetch.add(textFieldDriveFetch);
        jPanelDriveFetch.add(buttonDriveFetch,BorderLayout.EAST);

        add(scrollPane);
        add(jPanelDriveFetch,BorderLayout.SOUTH);

        initListeners();
        writeToConsole("EV3-Client started ...");
    }

    public static void writeToConsole (String message){
        if (consoleTextArea.getText().length() > 0)
            consoleTextArea.setText(consoleTextArea.getText() + "\n-> " + message);
        else
            consoleTextArea.setText("-> " + message);
    }

    public void initListeners () {
        buttonDriveFetch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = textFieldDriveFetch.getText();
                JRobotPanel.virtualRobotStep = Integer.parseInt(command.split("#")[1]);
                command += "#" + JConstants.PARTICLE_SENSOR_AMOUNT;
                MQTTClient.publish(command, MQTTClient.TOPIC_DRIVE);
            }
        });
    }

}
