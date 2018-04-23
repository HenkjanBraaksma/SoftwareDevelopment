package trafficcontroller;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TrafficController {

    private JFrame mainFrame;
    private JSlider timeSlider;
    private JSlider delaySlider;
    private JTextField portText;
    private JButton startButton;
    private JButton stopButton;
    private JPanel controlPanel;
    private JLabel statusLabel;
    private Thread serverThread;

    public TrafficController() {
        prepareGUI();
    }

    public static void main(String[] args) {
        TrafficController trafficController = new TrafficController();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Traffic Controller");
        mainFrame.setSize(400, 400);
        mainFrame.setLocationRelativeTo(null);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        showPane(mainFrame.getContentPane());
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void showPane(Container pane) {
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel timeLabel = new JLabel("Cyclus time", JLabel.CENTER);
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(timeLabel, c);

        timeSlider = new JSlider(JSlider.HORIZONTAL, 15, 120, 30);
        timeSlider.setMajorTickSpacing(15);
        timeSlider.setSnapToTicks(true);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        c.gridx = 1;
        c.gridy = 0;
        pane.add(timeSlider, c);

        JLabel delayLabel = new JLabel("Delay", JLabel.CENTER);
        c.gridx = 0;
        c.gridy = 1;
        pane.add(delayLabel, c);

        delaySlider = new JSlider(JSlider.HORIZONTAL, 3, 10, 5);
        delaySlider.setMajorTickSpacing(1);
        delaySlider.setPaintTicks(true);
        delaySlider.setPaintLabels(true);
        c.gridx = 1;
        c.gridy = 1;
        pane.add(delaySlider, c);

        JLabel portLabel = new JLabel("Port:", JLabel.CENTER);
        c.gridx = 0;
        c.gridy = 2;
        pane.add(portLabel, c);

        portText = new JTextField("8080", 5);
        c.gridx = 1;
        c.gridy = 2;
        pane.add(portText, c);

        startButton = new JButton("Start");
        startButton.addActionListener(new ButtonClickListener());
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ButtonClickListener());
        stopButton.setEnabled(false);

        controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 3;
        pane.add(controlPanel, c);

        statusLabel = new JLabel("bla bla bla", JLabel.CENTER);
        c.ipady = 40;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 4;
        pane.add(statusLabel, c);
    }

    private class ButtonClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Start")) {
                String text = portText.getText();
                int cyclusTime = timeSlider.getValue();
                int delay = delaySlider.getValue() * 1000;
                try {
                    int port = Integer.parseInt(text);
                    serverThread = new Thread(new KruisPuntServer(port, cyclusTime, delay));
                    serverThread.start();
                    String newString = "Server listening on localhost:" + port;
                    statusLabel.setText(newString);
                    startButton.setEnabled(false);
                    timeSlider.setEnabled(false);
                    delaySlider.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid port number");
                } catch (IllegalArgumentException iae) {
                    statusLabel.setText("Invalid port number");
                }
            } else if (command.equals("Stop")) {
                serverThread.interrupt();
                startButton.setEnabled(true);
                timeSlider.setEnabled(true);
                delaySlider.setEnabled(true);
                stopButton.setEnabled(false);
                statusLabel.setText("Server stopped.");
            }
        }
    }
}
