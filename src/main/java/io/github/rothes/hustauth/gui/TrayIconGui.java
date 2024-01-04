package io.github.rothes.hustauth.gui;

import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class TrayIconGui {

    private static TrayIcon trayIcon;

    public static void init() {
        if (SystemTray.isSupported()) {
            URL url = Main.class.getClassLoader().getResource("tray_icon.png");
            PopupMenu popupMenu = new PopupMenu("HustAuthMenu");
            popupMenu.add(menuItem("控制台", e -> ConsoleGui.show()));
            popupMenu.add(menuItem("手动认证", e -> AuthGui.show()));
            popupMenu.add(menuItem("重载配置", e -> HustAuth.INS.reload()));
            popupMenu.addSeparator();
            popupMenu.add(menuItem("退出", e -> HustAuth.INS.stop()));
            popupMenu.setFont(GuiManager.getUiFont());

            trayIcon = new TrayIcon(new ImageIcon(url).getImage(), "HustAuth", popupMenu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        ConsoleGui.show();
                    }
                }
            });
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (Exception e) {
                HustAuth.error("Failed to add trayIcon", e);
            }
        }
    }

    public static void close() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    private static MenuItem menuItem(String label, ActionListener listener) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.setFont(GuiManager.getUiFont());
        menuItem.addActionListener(listener);
        return menuItem;
    }

}
