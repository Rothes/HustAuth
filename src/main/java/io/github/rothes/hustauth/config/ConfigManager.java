package io.github.rothes.hustauth.config;

import io.github.rothes.hustauth.HustAuth;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;

public class ConfigManager {

    public static final ConfigManager INS = new ConfigManager();
    private final YamlFile yamlFile = new YamlFile("config.yml");
    private ConfigData configData;

    private ConfigManager() {
        // private
    }

    public YamlFile getYamlFile() {
        return yamlFile;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void reload() {
        HustAuth.log("正在加载配置.");
        try {
            yamlFile.createOrLoadWithComments();
            if (setDefaults()) {
                save();
            }
            configData = new ConfigData(this);
        } catch (IOException e) {
            HustAuth.error("读取配置文件时出错", e);
        }
    }

    public void save() {
        HustAuth.log("保存配置文件 config.yml .");
        try {
            yamlFile.save();
        } catch (IOException e) {
            HustAuth.error("保存配置文件时出错", e);
        }
    }

    public boolean setDefaults() {
        boolean found = false;
        for (ConfigKey key : ConfigKey.values()) {
            if (!yamlFile.contains(key.getPath())) {
                found = true;
                yamlFile.set(key.getPath(), key.getDefault());
                yamlFile.setComment(key.getPath(), key.getComment());
                HustAuth.log("已补充缺失配置项 " + key.getPath());
            }
        }
        return found;
    }

}
