package io.github.rothes.hustauth.config;

import org.simpleyaml.configuration.file.YamlFile;

public class ConfigData {

    public final boolean debug;
    public final boolean showConsoleOnLaunch;
    public final int consoleMaxRecords;
    public final float guiFontSize;

    public final String authLink;
    public final String userId;
    public final String password;
    public final String service;
    public final boolean passwordEncrypted;

    public final boolean loginOnLaunch;
    public final int loginOnLaunchOnce;
    public final boolean dailyLogin;
    public final long loginInterval;
    public final long getUserInfoInterval;

    private final YamlFile yamlFile;

    ConfigData(ConfigManager configManager) {
        yamlFile = configManager.getYamlFile();

        debug = getBoolean(ConfigKey.DEBUG);
        showConsoleOnLaunch = getBoolean(ConfigKey.SHOW_CONSOLE_ON_LAUNCH);
        consoleMaxRecords = getInt(ConfigKey.CONSOLE_MAX_RECORDS);
        guiFontSize = getFloat(ConfigKey.GUI_FONT_SIZE);
        authLink = correctLink(getString(ConfigKey.AUTH_LINK));
        userId = getString(ConfigKey.USER_ID);
        password = getString(ConfigKey.PASSWORD);
        service = getString(ConfigKey.SERVICE);
        passwordEncrypted = getBoolean(ConfigKey.PASSWORD_ENCRYPTED);
        loginOnLaunch = getBoolean(ConfigKey.LOGIN_ON_LAUNCH);
        loginOnLaunchOnce = getInt(ConfigKey.LOGIN_ON_LAUNCH_ONCE);
        dailyLogin = getBoolean(ConfigKey.DAILY_LOGIN);
        loginInterval = getLong(ConfigKey.LOGIN_INTERVAL);
        getUserInfoInterval = getLong(ConfigKey.GET_USER_INFO_INTERVAL);
    }

    private String getString(ConfigKey key) {
        return yamlFile.getString(key.getPath());
    }

    private boolean getBoolean(ConfigKey key) {
        return yamlFile.getBoolean(key.getPath());
    }

    private int getInt(ConfigKey key) {
        return yamlFile.getInt(key.getPath());
    }

    private long getLong(ConfigKey key) {
        return yamlFile.getLong(key.getPath());
    }

    private float getFloat(ConfigKey key) {
        return (float) yamlFile.getDouble(key.getPath());
    }

    private static String correctLink(String link) {
        if (link.endsWith("/")) {
            return link;
        }
        return link + "/";
    }

}
