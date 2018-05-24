package trafficcontroller;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class TrafficController {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TrafficController().start();
        });
    }

    private void start() {
        MainWindow m = new MainWindow();
    }
}

class MainWindow implements Observer {
    private JLabel statusLabel;
    private JTextField portText;
    private JSlider timeSlider;
    private JSlider delaySlider;
    private JButton startButton;
    private JButton stopButton;

    private Thread serverThread;

    @Override
    public void update(Observable o, Object data) {
        statusLabel.setText((String) data);
    }

    MainWindow() {
        JFrame mainFrame = new JFrame("Traffic Controller");
        mainFrame.getRootPane().setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400,400);
        mainFrame.setLocationRelativeTo(null);
        showPane(mainFrame.getContentPane());
        mainFrame.setVisible(true);
    }

    private void showPane(Container pane) {
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;
        Font defaultFont = new Font("Dialog", Font.PLAIN, 14);

        JLabel timeLabel = new JLabel("Cyclus time", JLabel.CENTER);
        timeLabel.setFont(defaultFont);
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(timeLabel, c);

        timeSlider = new JSlider(JSlider.HORIZONTAL, 10, 30, 15);
        timeSlider.setMajorTickSpacing(5);
        timeSlider.setSnapToTicks(true);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        c.gridx = 1;
        c.gridy = 0;
        pane.add(timeSlider, c);

        JLabel delayLabel = new JLabel("Delay", JLabel.CENTER);
        delayLabel.setFont(defaultFont);
        c.gridx = 0;
        c.gridy = 1;
        pane.add(delayLabel, c);

        delaySlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 3);
        delaySlider.setMajorTickSpacing(1);
        delaySlider.setPaintTicks(true);
        delaySlider.setPaintLabels(true);
        c.gridx = 1;
        c.gridy = 1;
        pane.add(delaySlider, c);

        JLabel portLabel = new JLabel("Port", JLabel.CENTER);
        portLabel.setFont(defaultFont);
        c.gridx = 0;
        c.gridy = 2;
        pane.add(portLabel, c);

        portText = new JTextField("8080", 5);
        c.gridx = 1;
        c.gridy = 2;
        pane.add(portText, c);

        startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent e) -> {
            startServer(this);
        });

        stopButton = new JButton("Stop");
        stopButton.addActionListener((ActionEvent e) -> {
            stopServer();
        });
        stopButton.setEnabled(false);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 3;
        pane.add(controlPanel, c);

        statusLabel = new JLabel("Waiting for action...", JLabel.CENTER);
        statusLabel.setFont(defaultFont);
        c.ipady = 40;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 4;
        pane.add(statusLabel, c);
    }

    private void startServer(MainWindow mainWindow) {
        try {
            int port = Integer.parseInt(portText.getText());
            int cyclusTime = timeSlider.getValue();
            int delay = delaySlider.getValue() * 1000;
            serverThread = new Thread(() -> {
                MessageObservable observable = new MessageObservable();
                observable.addObserver(mainWindow);
                KruisPuntServer server = new KruisPuntServer(port, cyclusTime, delay, observable);
            });
            serverThread.start();
            String serverStatus = "Server listening on localhost:" + port;
            statusLabel.setForeground(Color.GREEN);
            statusLabel.setText(serverStatus);
            startButton.setEnabled(false);
            timeSlider.setEnabled(false);
            delaySlider.setEnabled(false);
            portText.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (IllegalArgumentException ex) {
            statusLabel.setText("Invalid port number");
        }
    }

    private void stopServer() {
        serverThread.interrupt();
        statusLabel.setForeground(Color.RED);
        statusLabel.setText("Server stopped.");
        startButton.setEnabled(true);
        timeSlider.setEnabled(true);
        delaySlider.setEnabled(true);
        portText.setEnabled(true);
        stopButton.setEnabled(false);
    }
}

class MessageObservable extends Observable {

    MessageObservable() {
        super();
    }

    void sendMessage(Object data) {
        setChanged();
        notifyObservers(data);
    }
}