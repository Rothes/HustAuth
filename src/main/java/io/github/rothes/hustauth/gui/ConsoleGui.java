package io.github.rothes.hustauth.gui;

import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.appenders.GuiConsoleAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ConsoleGui {

    private static final JFrame frame = new JFrame("HustAuth");
    private static JTextArea log;

    public static void init() {
        TitledBorder border = new TitledBorder("控制台");
        border.setTitleFont(GuiManager.getUiFont());
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(border);
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField input = new JTextField();

        GuiConsoleAppender appender = (GuiConsoleAppender) ((Logger) LogManager.getLogger("consoleGui")).getAppenders().get("LogToGuiConsole");
        appender.setMaxLines(HustAuth.INS.getConfigManager().getConfigData().consoleMaxRecords);
        log = appender.getTextArea();
        log.setFont(GuiManager.getMonoFont());
        inputPanel.add(input, BorderLayout.CENTER);
        JButton enter = new JButton("Go");
        enter.setPreferredSize(new Dimension(55,5));
        enter.setFont(GuiManager.getUiFont());
        ActionListener listener = e -> {
            HustAuth.INS.executeCommand(input.getText());
            input.setText("");
        };
        input.addActionListener(listener);
        enter.addActionListener(listener);
        inputPanel.add(enter, BorderLayout.EAST);

        panel.add(appender.getScrollPane(), BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(panel);
        frame.setBounds(0,0,440,360);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public static void clear() {
        log.setText("");
    }

    public static void show() {
        frame.setVisible(true);
        frame.toFront();
    }

    public static void close() {
        frame.dispose();
    }

}
