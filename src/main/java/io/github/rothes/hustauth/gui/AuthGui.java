package io.github.rothes.hustauth.gui;

import com.google.gson.JsonObject;
import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.auth.AuthHandler;
import io.github.rothes.hustauth.config.ConfigData;
import io.github.rothes.hustauth.storage.AccountRecord;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AuthGui {

    public static void show() {
        JFrame frame = new JFrame("HustAuth");
        frame.setFont(GuiManager.getUiFont());
        initByStatus(frame);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static void initByStatus(JFrame frame) {
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
        frame.setBounds(0,0,240,300);
        frame.setLocationRelativeTo(null);
        JPanel panel = commonPanel();
        JLabel title = new JLabel("登入", SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        TitledBorder border = new TitledBorder("账户信息");
        form.setBorder(border);

        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();
        List<AccountRecord> list = new ArrayList<>();
        list.add(new AccountRecord(configData.userId, configData.password, configData.service, configData.passwordEncrypted, configData.userId + " (配置文件)"));
        list.addAll(HustAuth.INS.getDbSource().getRecords());
        JComboBox<AccountRecord> userId = new JComboBox<>(list.toArray(new AccountRecord[0]));
        JTextComponent userText = (JTextComponent) userId.getEditor().getEditorComponent();
        userId.setEditable(true);
        JPasswordField password = new JPasswordField();
        ServiceComboBox service = new ServiceComboBox(userText);
        JCheckBox encrypted = new CustomCheckBox("密码已加密", "<html>填写的密码是已加密的密码时勾选此项.<br>获取加密密码的方式请参阅 config.yml<br>若您不清楚该内容, 请勿勾选.</html>");
        JCheckBox remember = new CustomCheckBox("记住账户", "保存账户信息到本地数据库, 下次登入时可自动填写.");
        ToolTipManager.sharedInstance().setDismissDelay(30000);

        border.setTitleFont(GuiManager.getUiFont());
        title.setFont(GuiManager.getUiFont().deriveFont(18f));
        service.setFont(GuiManager.getUiFont());
        userId.setFont(GuiManager.getUiFont());
        password.setFont(GuiManager.getUiFont());
        encrypted.setFont(GuiManager.getUiFont());
        remember.setFont(GuiManager.getUiFont());
        service.setMaximumSize(new Dimension(Integer.MAX_VALUE, service.getPreferredSize().height));
        service.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        userId.setMaximumSize(new Dimension(Integer.MAX_VALUE, userId.getPreferredSize().height));
        userId.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, password.getPreferredSize().height));
        userId.addActionListener(event -> {
            if (userId.getSelectedItem() instanceof AccountRecord) {
                AccountRecord record = (AccountRecord) userId.getSelectedItem();
                userId.setSelectedItem(record.getUserId());
                password.setText(record.getPassword());
                service.update();
                service.setSelectedItem(record.getService());
                encrypted.setSelected(record.isEncrypted());
                remember.setSelected(record.toString().equals(record.getUserId()));
            }
        });
        userId.setSelectedIndex(0);
        userText.getDocument().addDocumentListener(new DocumentListener() {

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
                service.update();
            }
        });
        form.add(label("用户名"));
        form.add(userId);
        form.add(label("密码"));
        form.add(password);
        form.add(label("服务"));
        form.add(service);
        Box checkBox = Box.createHorizontalBox();
        checkBox.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        checkBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, password.getPreferredSize().height));
        checkBox.add(encrypted);
        checkBox.add(remember);
        form.add(checkBox);
        panel.add(form, BorderLayout.CENTER);

        JButton button = new JButton("登入");
        button.setFont(GuiManager.getUiFont());
        button.addActionListener(e -> {
            AuthHandler.Result result = AuthHandler.login(userText.getText(), new String(password.getPassword()), (String) service.getSelectedItem(), encrypted.isSelected());
            if (result.isSuccess()) {
                HustAuth.log("已通过 GUI 手动登入");
                if (remember.isSelected()) {
                    HustAuth.INS.getDbSource().addRecord(new AccountRecord(userText.getText(), new String(password.getPassword()), (String) service.getSelectedItem(), encrypted.isSelected()));
                }
                frame.dispose();
                show();
            } else {
                HustAuth.log("通过 GUI 手动登入失败, " + result.getMessage());
                JOptionPane.showMessageDialog(frame, "登入失败,\n" + result.getMessage(), "HustAuth 快捷认证", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        frame.setContentPane(panel);
    }

    private static void loggedIn(JFrame frame) {
        frame.setBounds(0,0,240,260);
        frame.setLocationRelativeTo(null);
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
                frame.dispose();
                show();
            } else {
                HustAuth.log("通过 GUI 手动下线失败, " + result.getMessage());
                JOptionPane.showMessageDialog(frame, "下线失败,\n" + result.getMessage(), "HustAuth 快捷认证", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(button, BorderLayout.SOUTH);
        frame.setContentPane(panel);
    }

    private static JPanel commonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = new TitledBorder("快捷认证");
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

    private static class CustomCheckBox extends JCheckBox {

        public CustomCheckBox(String text, String toolTip) {
            super(text);
            setToolTipText(toolTip);
        }

        @Override
        public JToolTip createToolTip() {
            return new JToolTip() {
                @Override
                public Font getFont() {
                    return GuiManager.getUiFont();
                }
            };
        }

        @Override
        public Font getFont() {
            return GuiManager.getUiFont();
        }

    }

    private static class ServiceComboBox extends JComboBox<String> {

        private final JTextComponent textComponent;

        public ServiceComboBox(JTextComponent textComponent) {
            super(new String[]{"系统默认服务"});
            this.textComponent = textComponent;
        }

        public void update() {
            String[] services = AuthHandler.getServices(textComponent.getText());
            if (services.length == 1 && services[0].isEmpty()) {
                services = new String[]{"系统默认服务"};
            }
            removeAllItems();
            for (String s : services) {
                addItem(s);
            }
        }

    }

}
