package me.clip.ezblocks.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.clip.ezblocks.block.BlockTopEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SQLStorage implements Storage {

    private static final String TABLE_PREFIX = ""; // default empty todo add to config
    private static final String TABLE_NAME = TABLE_PREFIX + "playerblocks";

    protected HikariDataSource hikari;

    protected void createTables() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "uuid char(36) NOT NULL," +
                "blocksmined integer NOT NULL," +
                "PRIMARY KEY (`uuid`));";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setBlocksBroken(UUID uuid, int amount) {
        String query = "INSERT INTO " + TABLE_NAME + " (uuid, blocksmined) VALUES (?, ?) ON DUPLICATE KEY UPDATE blocksmined = ?;";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, amount);
            preparedStatement.setInt(3, amount);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getBlocksBroken(UUID uuid) {
        String query = "SELECT blocksmined FROM " + TABLE_NAME + " WHERE uuid = ?;";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return 0;
            }

            return resultSet.getInt("blocksmined");
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Map<Integer, BlockTopEntry> getBlocksTop() {
        String query = "SELECT uuid, blocksmined FROM " + TABLE_NAME + " ORDER BY blocksmined DESC LIMIT 10;";
        Map<Integer, BlockTopEntry> blocksTop = new LinkedHashMap<>();

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            int position = 1;
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                int blocksBroken = resultSet.getInt("blocksmined");

                blocksTop.put(position, new BlockTopEntry(uuid, blocksBroken));
                position++;
            }

            return blocksTop;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
