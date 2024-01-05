package io.github.rothes.hustauth.gui;

import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.config.ConfigData;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class GuiManager {

    private static final Font uiFont;
    private static final Font monoFont;

    static {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Font tempFont;
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();
        float size = configData.guiFontSize;

        tempFont = getFont(fonts, "Sarasa UI SC", "Microsoft YaHei");
        if (tempFont != null) {
            tempFont = tempFont.deriveFont(size);
        }
        uiFont = tempFont;

        tempFont = getFont(fonts, "Sarasa Mono SC", "Cascadia Mono");
        if (tempFont == null) {
//            if (!System.getProperty("java.version").startsWith("1.") || configData.consoleForceMonoFont) {
//                // Java 1.8 has no fallback font... We can't use these.
//                tempFont = getFont(fonts, "Courier New", "Consolas");
//            }
            tempFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        }
        if (tempFont != null) {
            tempFont = tempFont.deriveFont(size);
        }
        monoFont = tempFont;
    }

    public static Font getUiFont() {
        return uiFont;
    }

    public static Font getMonoFont() {
        return monoFont;
    }

    public static void initGuis() {
        ConsoleGui.init();
        if (HustAuth.INS.getConfigManager().getConfigData().showConsoleOnLaunch) {
            ConsoleGui.show();
        }
        TrayIconGui.init();
    }

    public static void closeGuis() {
        TrayIconGui.close();
        ConsoleGui.close();
    }

    private static Font getFont(Font[] fonts, String... name) {
        for (String s : name) {
            for (Font font : fonts) {
                if (font.getFontName(Locale.ENGLISH).equals(s)) {
                    return font;
                }
            }
        }
        return null;
    }

}
