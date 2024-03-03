package io.github.rothes.hustauth;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length == 0 && Runtime.getRuntime().maxMemory() / 1024 / 1024 > 32) {
            HustAuth.log("-Xmx 参数过大(" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m), 自动重启并限制在 18M 以内.");
            HustAuth.log("在 -jar 后添加任意参数忽略此限制.");
            String currentPath = Main.class
                    .getProtectionDomain()
                    .getCodeSource().getLocation()
                    .toURI().getPath()
                    .replace('/', File.separator.charAt(0)).substring(1);
            Runtime.getRuntime().exec("javaw -Xms1m -Xmx18m -jar \"" + currentPath + "\" restart"); // limit is 13312k
            return;
        }

        try {
            HustAuth.INS.start();
        } catch (Throwable throwable) {
            HustAuth.error("程序异常退出", throwable);
        }
    }

}
