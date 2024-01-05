package io.github.rothes.hustauth.gui;

import io.github.rothes.hustauth.HustAuth;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public class GuiManager {

    private static final Font uiFont;
    private static final Font monoFont;

    static {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Font tempFont;
        float size = HustAuth.INS.getConfigManager().getConfigData().guiFontSize;

        tempFont = Arrays.stream(fonts).filter(it -> it.getFontName(Locale.ENGLISH).equals("Sarasa UI SC")).findAny()
                .orElse(Arrays.stream(fonts).filter(it -> it.getFontName(Locale.ENGLISH).equals("Microsoft YaHei")).findAny().orElse(null));
        if (tempFont != null) {
            tempFont = tempFont.deriveFont(size);
        }
        uiFont = tempFont;

        tempFont = Arrays.stream(fonts).filter(it -> it.getFontName(Locale.ENGLISH).equals("Sarasa Mono SC")).findAny()
            .orElse(Arrays.stream(fonts).filter(it -> it.getFontName(Locale.ENGLISH).equals("Cascadia Mono")).findAny()
                    .orElse(Arrays.stream(fonts).filter(it -> it.getFontName(Locale.ENGLISH).equals("Consolas")).findAny().orElse(null)));
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

}
