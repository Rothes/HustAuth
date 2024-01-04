package io.github.rothes.hustauth.gui;

import com.google.gson.JsonObject;
import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.auth.AuthHandler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AuthGui {

    public static void show() {
        JFrame frame = new JFrame("HustAuth");
        frame.setFont(GuiManager.getUiFont());
        fresh(frame);
        frame.setVisible(true);
        frame.setBounds(0,0,240,260);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static void fresh(JFrame frame) {
        switch (AuthHandler.check()) {
            case NOT_AUTHENTICATED:
                auth(frame);
                break;
            case LOGGED_IN:
                loggedIn(frame);
                break;
            default:
                throw new AssertionError();
        }
    }

    private static void auth(JFrame frame) {
        JPanel panel = commonPanel();
        JLabel title = new JLabel("登入", SwingConstants.CENTER);
        title.setFont(GuiManager.getUiFont().deriveFont(18f));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        BoxLayout boxLayout = new BoxLayout(form, BoxLayout.Y_AXIS);
        form.setLayout(boxLayout);
        TitledBorder border = new TitledBorder("账户信息");
        border.setTitleFont(GuiManager.getUiFont());
        form.setBorder(border);

        JComboBox<String> service = new JComboBox<>(new String[]{"系统默认服务"});
        service.setFont(GuiManager.getUiFont());
        service.setMaximumSize(new Dimension(Integer.MAX_VALUE, service.getPreferredSize().height));
        service.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        JTextField userId = new JTextField();
        userId.setFont(GuiManager.getUiFont());
        userId.setMaximumSize(new Dimension(Integer.MAX_VALUE, userId.getPreferredSize().height));
        JPasswordField password = new JPasswordField();
        password.setFont(GuiManager.getUiFont());
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, password.getPreferredSize().height));
        userId.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateServices();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateServices();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateServices();
            }

            public void updateServices() {
                String[] services = AuthHandler.getServices(userId.getText());
                if (services.length == 1 && services[0].isEmpty()) {
                    services = new String[]{"系统默认服务"};
                }
                service.removeAllItems();
                for (String s : services) {
                    service.addItem(s);
                }
            }
        });
        form.add(label("用户名"));
        form.add(userId);
        form.add(label("密码"));
        form.add(password);
        form.add(label("服务"));
        form.add(service);
        panel.add(form, BorderLayout.CENTER);

        JButton button = new JButton("登入");
        button.setFont(GuiManager.getUiFont());
        button.addActionListener(e -> {
            AuthHandler.Result result = AuthHandler.login(userId.getText(), new String(password.getPassword()), (String) service.getSelectedItem(), false);
            if (result.isSuccess()) {
                HustAuth.log("已通过 GUI 手动登入");
                fresh(frame);
                SwingUtilities.updateComponentTreeUI(frame);
            } else {
                HustAuth.log("通过 GUI 手动登入失败, " + result.getMessage());
                JOptionPane.showMessageDialog(frame, "登入失败,\n" + result.getMessage(), "HustAuth 手动认证", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        frame.setContentPane(panel);
    }

    private static void loggedIn(JFrame frame) {
        JPanel panel = commonPanel();
        JLabel title = new JLabel("已登入", SwingConstants.CENTER);
        title.setFont(GuiManager.getUiFont().deriveFont(18f));
        panel.add(title, BorderLayout.NORTH);

        JsonObject userInfo = AuthHandler.getUserInfo();
        JLabel info = new JLabel("<html><table>" +
                "<tr><td>用户ID:</td><td>" + userInfo.getAsJsonPrimitive("userId").getAsString() + "</td>" +
                "<tr><td>服务名:</td><td>" + userInfo.getAsJsonPrimitive("service").getAsString() + "</td>" +
                "</table></html>",
                SwingConstants.CENTER);
        info.setFont(GuiManager.getUiFont());
        panel.add(info, BorderLayout.CENTER);

        JButton button = new JButton("下线");
        button.setFont(GuiManager.getUiFont());
        button.addActionListener(e -> {
            AuthHandler.Result result = AuthHandler.logOut();
            if (result.isSuccess()) {
                HustAuth.log("已通过 GUI 手动下线");
                fresh(frame);
                SwingUtilities.updateComponentTreeUI(frame);
            } else {
                HustAuth.log("通过 GUI 手动下线失败, " + result.getMessage());
                JOptionPane.showMessageDialog(frame, "下线失败,\n" + result.getMessage(), "HustAuth 手动认证", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        frame.setContentPane(panel);
    }

    private static JPanel commonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = new TitledBorder("手动认证");
        border.setTitleFont(GuiManager.getUiFont());
        panel.setBorder(border);
        return panel;
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        label.setFont(GuiManager.getUiFont());
        return label;
    }

}
