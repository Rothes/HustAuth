package io.github.rothes.hustauth.auth;

import io.github.rothes.hustauth.HustAuth;

import java.util.Timer;
import java.util.TimerTask;

public class AuthTask {

    public static final Timer timer = new Timer("Timer-Auth");
    private static TimerTask task;

    public static void runNew(boolean once) {
        HustAuth.log("正在执行登入任务.");
        if (once) {
            HustAuth.log("强制本次登入任务必须登入一次, 等待认证系统下线.");
        }
        TimerTask newTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (AuthHandler.check() == AuthHandler.AuthStatus.LOGGED_IN) {
                        if (!once) {
                            HustAuth.log("目前已登入校园网, 终止登入任务.");
                            stop();
                        } else if (HustAuth.INS.getConfigManager().getConfigData().verbose) {
                            HustAuth.log("目前已登入校园网, 等待下次执行...");
                        }
                        return;
                    }

                    HustAuth.log("正在尝试登入校园网...");
                    AuthHandler.Result login = AuthHandler.login();
                    if (login.isSuccess()) {
                        HustAuth.log("成功登录校园网, 登入任务完成.");
                    } else {
                        HustAuth.log("登入校园网失败, " + login.getMessage() + "; 登入任务终止.");
                    }
                    stop();
                } catch (Throwable throwable) {
                    HustAuth.error("致命错误: ", throwable);
                    stop();
                }
            }
        };

        synchronized (timer) {
            stop();
            task = newTask;
        }
        timer.scheduleAtFixedRate(task, 0, HustAuth.INS.getConfigManager().getConfigData().loginInterval);
    }

    public static boolean stop() {
        TimerTask currentTask;
        synchronized (timer) {
            currentTask = task;
            task = null;
        }
        if (currentTask == null) {
            return false;
        }
        currentTask.cancel();
        timer.purge();
        return true;
    }

    public static void shutdown() {
        timer.cancel();
    }

}
