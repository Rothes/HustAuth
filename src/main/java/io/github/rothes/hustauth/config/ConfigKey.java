package io.github.rothes.hustauth.config;

public enum ConfigKey {

    DEBUG(false, "记录 debug 日志到 debug.log 中"),
    SHOW_CONSOLE_ON_LAUNCH(true, "启动应用时显示控制台窗口, 可设为 false 在托盘图标中手动打开"),
    CONSOLE_MAX_RECORDS(100, "控制台窗口保留多少行日志记录"),
    GUI_FONT_SIZE(14, "GUI 界面字体大小"),
    AUTH_LINK("http://123.123.123.123/", "校园网 Web eportal 认证链接"),
    USER_ID("user", "用户名"),
    PASSWORD("password", "密码, 明文或密文均在此处填写"),
    SERVICE("中国联通ChinaUnicom", "服务"),
    PASSWORD_ENCRYPTED(false, "填写的密码是否已经过加密." +
            "若密码是明文, 应设为 false . 安全起见, 应存储密文.\n" +
            "加密密码获取方式: 在 Web 登录时记住密码后返回登录界面,\n" +
            "F12 控制台内执行 document.getElementById(\"pwd\").value"),
    LOGIN_ON_LAUNCH(true, "启动程序时执行自动登入任务"),
    LOGIN_ON_LAUNCH_ONCE(3, "在每日开始的多少分钟内启动程序时, 要求必须成功登入一次."),
    DAILY_LOGIN(true, "自动在每日 00:00 时执行自动登入任务"),
    LOGIN_INTERVAL(1000, "每多少毫秒尝试执行一次登入操作"),
    GET_USER_INFO_INTERVAL(50, "获取用户状态信息返回等待时, 再次获取状态的间隔毫秒数");

    private final String path;
    private final Object def;
    private final String comment;

    ConfigKey(Object def, String comment) {
        this(def, null, comment);
    }

    ConfigKey(Object def, String path, String comment) {
        this.path = path != null ? path : "option." + name().toLowerCase().replace('_', '-');
        this.def = def;
        this.comment = comment;
    }

    public String getPath() {
        return path;
    }

    public Object getDefault() {
        return def;
    }

    public String getComment() {
        return comment;
    }

}
