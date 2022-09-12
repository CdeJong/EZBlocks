package me.clip.ezblocks.storage;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class SQLStorage implements Storage {

    private static final String TABLE_PREFIX = ""; // default empty todo add to config
    private static final String TABLE_NAME = TABLE_PREFIX + "playerblocks";

    protected HikariDataSource hikari;

    protected void createTables() {
        String query = "CREATE TABLE IF NOT EXISTS ? (" +
                "uuid char(36) NOT NULL," +
                "blocksmined integer NOT NULL," +
                "PRIMARY KEY (`uuid`));";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, TABLE_NAME);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBlocksBroken(UUID uuid, int amount) {
        String query = "INSERT INTO ? (uuid, blocksmined) VALUES (?, ?) ON DUPLICATE KEY UPDATE blocksmined = ?;";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, TABLE_NAME);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setInt(3, amount);
            preparedStatement.setInt(4, amount);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getBlocksBroken(UUID uuid) {
        String query = "SELECT blocksmined FROM ? WHERE uuid = ?;";
        ResultSet resultSet;

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, TABLE_NAME);
            preparedStatement.setString(2, uuid.toString());

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return 0;
            }

            return resultSet.getInt("blocksmined");
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
