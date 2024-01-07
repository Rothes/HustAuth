package io.github.rothes.hustauth.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.rothes.hustauth.HustAuth;
import io.github.rothes.hustauth.config.ConfigData;
import io.github.rothes.hustauth.util.CollectionUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthHandler {

    private AuthHandler() {
        // private
    }

    public static AuthStatus check() {
        URI location = getFinalLocation();

        String[] split = location.getPath().split("/", 4);
        if (split.length > 2) {
            switch (split[2]) {
                case "success.jsp":
                    if (!checkUserInfo(location)) {
                        // We may check again if failed.
                        return check();
                    }
                    return AuthStatus.LOGGED_IN;
                case "index.jsp":
                    return AuthStatus.NOT_AUTHENTICATED;
            }
        }
        HustAuth.error("认证系统重定向至未知网页: " + location);
        throw new AssertionError("认证系统重定向至未知网页");
    }

    public static Result login() {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();
        return login(configData.userId, configData.password, configData.service, configData.passwordEncrypted);
    }

    public static Result login(String userId, String password, String service, boolean encrypted) {
        try (
                CloseableHttpClient client = HttpClients.createDefault()
        ) {
            return client.execute(getLoginPost(userId, password, service, encrypted), response -> {
                String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                HustAuth.debug("login: " + responseJson);
                JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
                String result = json.getAsJsonPrimitive("result").getAsString();
                String message = json.getAsJsonPrimitive("message").getAsString();
//                if (!message.isEmpty()) {
//                    HustAuth.log("登入操作返回信息: " + message);
//                }
                return new Result(result.equals("success"), message);
            });
        } catch (IOException e) {
            HustAuth.error("登入时发生异常", e);
            return new Result(false, "HustAuth failed to process");
        }
    }

    public static Result logOut() {
        try (
                CloseableHttpClient client = HttpClients.createDefault()
        ) {
            return client.execute(getLogOutPost(), response -> {
                String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                HustAuth.debug("logOut: " + responseJson);
                JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
                String result = json.getAsJsonPrimitive("result").getAsString();
                String message = json.getAsJsonPrimitive("message").getAsString();
//                if (!message.isEmpty()) {
//                    HustAuth.log("下线操作返回信息: " + message);
//                }
                return new Result(result.equals("success"), message);
            });
        } catch (IOException e) {
            HustAuth.error("下线时发生异常", e);
            return new Result(false, "HustAuth failed to process");
        }
    }

    public static boolean checkUserInfo(URI finalUri) {
        JsonObject userInfo = getUserInfo(finalUri);
        if (userInfo != null) {
            String result = userInfo.getAsJsonPrimitive("result").getAsString();
            if (!result.equals("wait") && !equals(userInfo, "userGroup", "获取失败")) {
//                String message = userInfo.getAsJsonPrimitive("message").getAsString();
//                if (!message.isEmpty()) {
//                    HustAuth.log("用户状态信息: " + message);
//                    // 获取用户信息失败，用户可能已经下线 : We can re-login now.
//                }
                return result.equals("success");
            }
        }

        HustAuth.log("等待认证服务器获取用户状态信息.");
        try {
            Thread.sleep(HustAuth.INS.getConfigManager().getConfigData().getUserInfoInterval);
        } catch (InterruptedException e) {
            HustAuth.error("检查状态信息时发生异常", e);
            return false;
        }
        return checkUserInfo(finalUri);
    }

    public static JsonObject getUserInfo() {
        return getUserInfo(getFinalLocation());
    }

    public static JsonObject getUserInfo(URI finalUri) {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();

        String url = configData.authLink + "eportal/InterFace.do?method=getOnlineUserInfo";
        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(Collections.singletonList(
                    new BasicNameValuePair("userIndex", finalUri.getRawQuery().split("=")[1])
            )));
            try (
                CloseableHttpClient client = HttpClients.createDefault()
            ) {
                return client.execute(post, response -> {
                    String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    HustAuth.debug("getUserInfo: " + responseJson);
                    JsonElement jsonElement = JsonParser.parseString(responseJson);
                    if (jsonElement.isJsonObject()) {
                        return jsonElement.getAsJsonObject();
                    }
                    return null;
                });
            }
        } catch (IOException e) {
            HustAuth.error("检查状态信息时发生异常", e);
            return null;
        }
    }

    public static String[] getServices(String user) {
        try (
                CloseableHttpClient client = HttpClients.createDefault()
        ) {
            return client.execute(getServicesPost(user), response -> {
                String responseText = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                HustAuth.debug("getServices: " + responseText);
                return responseText.split("@");
            });
        } catch (IOException e) {
            HustAuth.error("获取可用服务时发生异常", e);
            return new String[]{""};
        }
    }

    private static boolean equals(JsonObject jsonObject, String key, String value) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonPrimitive()) {
            return false;
        }
        return jsonObject.getAsJsonPrimitive(key).getAsString().equals(value);
    }

    private static URI getFinalLocation() {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();

        HttpClientContext context = HttpClientContext.create();
        try (
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(new HttpGet(configData.authLink), context)
        ) {
            URI last = CollectionUtils.getLast(context.getRedirectLocations());
            if (last.getPath().equals("/")) {
                // We are on a page that redirected by javascript.
                Pattern pattern = Pattern.compile("<script>top.self.location.href='(.+)'</script>");
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    return new URI(matcher.group(1));
                } else {
                    HustAuth.warn("重定向的网址 " + last + " 可能不包含所需的信息. 网页内容为:");
                    HustAuth.warn(body);
                }
            }
            return last;
        } catch (IOException | URISyntaxException e) {
            HustAuth.error("获取重定向链接时发生异常", e);
            return null;
        }
    }

    private static HttpPost getLoginPost(String userId, String password, String service, boolean encrypted) {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();

        String url = configData.authLink + "eportal/InterFace.do?method=login";
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("userId", userId),
                new BasicNameValuePair("password", password),
                new BasicNameValuePair("service", service),
                new BasicNameValuePair("queryString", getFinalLocation().getRawQuery()),
                new BasicNameValuePair("operatorPwd", ""),
                new BasicNameValuePair("operatorUserId", ""),
                new BasicNameValuePair("validcode", ""),
                new BasicNameValuePair("passwordEncrypt", String.valueOf(encrypted))
        ), StandardCharsets.UTF_8));
        return post;
    }

    private static HttpPost getLogOutPost() {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();

        String url = configData.authLink + "eportal/InterFace.do?method=logout";
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(Collections.singletonList(
                new BasicNameValuePair("userIndex", getFinalLocation().getRawQuery().split("=")[1])
        ), StandardCharsets.UTF_8));
        return post;
    }

    private static HttpPost getServicesPost(String userId) {
        ConfigData configData = HustAuth.INS.getConfigManager().getConfigData();

        String url = configData.authLink + "eportal/userV2.do?method=getServices";
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("username", userId),
                new BasicNameValuePair("search", getFinalLocation().getRawQuery())
        ), StandardCharsets.UTF_8));
        return post;
    }

    public enum AuthStatus {
        NOT_AUTHENTICATED,
        LOGGED_IN
    }

    public static class Result {
        private final boolean success;
        private final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

    }

}
