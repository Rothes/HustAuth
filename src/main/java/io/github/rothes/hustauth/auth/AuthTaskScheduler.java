package io.github.rothes.hustauth.auth;

import io.github.rothes.hustauth.HustAuth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuthTaskScheduler {

    public static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    public static void schedule() {
        service.scheduleAtFixedRate(
                () -> {
                    HustAuth.log("开始执行日常登入任务.");
                    AuthTask.runNew(true);
                },
                LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS),
                24 * 60 * 60,
                TimeUnit.SECONDS);
        HustAuth.log("已计划日常登入任务.");
    }

    public static void shutdown() {
        service.shutdownNow();
    }

}
