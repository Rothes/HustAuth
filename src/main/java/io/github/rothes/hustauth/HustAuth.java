package io.github.rothes.hustauth;

import io.github.rothes.hustauth.auth.AuthHandler;
import io.github.rothes.hustauth.auth.AuthTask;
import io.github.rothes.hustauth.auth.AuthTaskScheduler;
import io.github.rothes.hustauth.config.ConfigManager;
import io.github.rothes.hustauth.gui.ConsoleGui;
import io.github.rothes.hustauth.gui.GuiManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Scanner;

public class HustAuth {

    private static final Logger LOGGER = LogManager.getRootLogger();
    public static final HustAuth INS = new HustAuth();
    private final ConfigManager configManager = ConfigManager.INS;
    private boolean stopping = false;

    private HustAuth() {
        // private
    }

    public void reload() {
        getConfigManager().reload();
        HustAuth.log("已完成配置加载.");
    }

    public void start() {
        reload();
        HustAuth.log("正在初始化界面.");
        GuiManager.initGuis();
        HustAuth.log("正在启动应用...");
        if (getConfigManager().getConfigData().dailyLogin) {
            AuthTaskScheduler.schedule();
        }

        if (getConfigManager().getConfigData().loginOnLaunch) {
            HustAuth.log("开始首次执行登入任务.");
            LocalTime now = LocalTime.now();
            AuthTask.runNew(now.getHour() <= getConfigManager().getConfigData().loginOnLaunchOnce / 60
                    && now.getMinute() < getConfigManager().getConfigData().loginOnLaunchOnce % 60);
        }

        if (System.console() != null) {
            commandLineInterface();
        }
    }

    public void commandLineInterface() {
        while (!stopping) {
            inputCommand();
        }
    }

    public void inputCommand() {
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextLine()) {
            return;
        }
        String cmd = scanner.nextLine();
        executeCommand(cmd);
    }

    public void executeCommand(String cmd) {
        switch (cmd.toUpperCase(Locale.ROOT).replaceFirst("^/", "")) {
            case "CANCEL":
                if (AuthTask.stop()) {
                    log("已停止目前的登入任务.");
                } else {
                    log("目前并没有登入任务正在执行.");
                }
                return;
            case "LOGOUT":
                AuthHandler.Result result = AuthHandler.logOut();
                log("下线" + (result.isSuccess() ? "成功." : "失败. " + result.getMessage()));
                return;
            case "LOGINO":
                log("启动登入任务.");
                AuthTask.runNew(true);
                return;
            case "LOGIN":
                log("启动登入任务.");
                AuthTask.runNew(false);
                return;
            case "CLEAR":
                ConsoleGui.clear();
                return;
            case "GC":
                System.gc();
                log("已运行 gc .");
                return;
            case "STOP":
            case "QUIT":
            case "END":
                stop();
                return;
        }
        log("可用指令:\n" +
                " * /cancel       - 停止登入任务\n" +
                " * /logout       - 尝试下线\n" +
                " * /logino       - 根据配置尝试登入, 并要求至少成功登入一次\n" +
                " * /login        - 根据配置尝试登入\n" +
                " * /clear        - 清空控制台日志\n" +
                " * /gc           - 运行 JVM GC\n" +
                " * /stop         - 结束程序");
    }

    public void stop() {
        stopping = true;
        log("正在结束程序.");
        AuthTask.shutdown();
        AuthTaskScheduler.shutdown();
        GuiManager.closeGuis();
        System.exit(0);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void log(String message) {
        getLogger().info(message);
    }

    public static void warn(String message) {
        getLogger().warn(message);
    }

    public static void error(String message) {
        getLogger().error(message);
    }

    public static void error(String message, Throwable throwable) {
        getLogger().error(message, throwable);
    }

    public static void debug(String message) {
        if (INS.getConfigManager().getConfigData().debug) {
            LogManager.getLogger("debug").debug(message);
        }
    }

}
