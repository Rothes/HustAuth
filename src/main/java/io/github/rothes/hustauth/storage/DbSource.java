package io.github.rothes.hustauth.storage;

import io.github.rothes.hustauth.HustAuth;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbSource {

    public DbSource() {
        initDriver();
    }

    public void initDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            HustAuth.error("无法加载 jdbc 驱动", e);
        }
    }

    private Connection getConnection() throws SQLException {
        File file = new File("data.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                HustAuth.error("无法创建数据库文件", e);
                return null;
            }
        }
        String url = "jdbc:sqlite:" + file.getAbsolutePath();
        return DriverManager.getConnection(url);
    }

    public void createTable() {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
                ) {
            statement.execute("CREATE TABLE IF NOT EXISTS account_records (" +
                    "userId VARCHAR(32)," +
                    "password VARCHAR(256)," +
                    "service VARCHAR(32)," +
                    "encrypted INTEGER" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AccountRecord> getRecords() {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM account_records");
                ResultSet rs = statement.executeQuery()
        ) {
            List<AccountRecord> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new AccountRecord(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4) == 1));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRecord(AccountRecord record) {
        deleteRecord(record);
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO account_records (userId, password, service, encrypted) VALUES (?, ?, ?, ?)")
        ) {
            statement.setString(1, record.getUserId());
            statement.setString(2, record.getPassword());
            statement.setString(3, record.getService());
            statement.setInt(4, record.isEncrypted() ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(AccountRecord record) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM account_records WHERE userId = ?")
        ) {
            statement.setString(1, record.getUserId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
