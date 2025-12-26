package VanillaPlus.Database;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {

    private final JavaPlugin Plugin;
    private Connection Connection;
    private final String DbName = "VanillaPlusDatabase.db";

    public SQLite(JavaPlugin Plugin) {
        this.Plugin = Plugin;
        Initialize();
    }

    private void Initialize() {
        File DataFolder = new File(Plugin.getDataFolder(), DbName);
        if (!DataFolder.getParentFile().exists()) {
            DataFolder.getParentFile().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            Connection = DriverManager.getConnection("jdbc:sqlite:" + DataFolder.getAbsolutePath());
            CreateTables();
        } catch (Exception Ex) {
            Plugin.getLogger().severe("Failed to initialize SQLite database: " + Ex.getMessage());
            Ex.printStackTrace();
        }
    }

    private void CreateTables() {
        String BlockDataSql = "CREATE TABLE IF NOT EXISTS block_data (" +
                "location_hash TEXT PRIMARY KEY," +
                "custom_name TEXT," +
                "lore_json TEXT" +
                ");";

        String LecternLockSql = "CREATE TABLE IF NOT EXISTS lectern_locks (" +
                "location_hash TEXT PRIMARY KEY," +
                "owner_uuid TEXT" +
                ");";

        try (Statement Stmt = Connection.createStatement()) {
            Stmt.execute(BlockDataSql);
            Stmt.execute(LecternLockSql);
        } catch (SQLException Ex) {
            Plugin.getLogger().severe("Failed to create tables: " + Ex.getMessage());
        }
    }

    public void SaveBlockData(Location Loc, String CustomName, String LoreJson) {
        String Hash = GetLocationHash(Loc);
        String Sql = "INSERT OR REPLACE INTO block_data(location_hash, custom_name, lore_json) VALUES(?, ?, ?)";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            Psmt.setString(2, CustomName);
            Psmt.setString(3, LoreJson);
            Psmt.executeUpdate();
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
    }

    public BlockInfo GetBlockData(Location Loc) {
        String Hash = GetLocationHash(Loc);
        String Sql = "SELECT custom_name, lore_json FROM block_data WHERE location_hash = ?";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            ResultSet Rs = Psmt.executeQuery();

            if (Rs.next()) {
                return new BlockInfo(Rs.getString("custom_name"), Rs.getString("lore_json"));
            }
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
        return null;
    }

    public void RemoveBlockData(Location Loc) {
        String Hash = GetLocationHash(Loc);
        String Sql = "DELETE FROM block_data WHERE location_hash = ?";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            Psmt.executeUpdate();
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
    }

    public void SaveLecternLock(Location Loc, String OwnerUUID) {
        String Hash = GetLocationHash(Loc);
        String Sql = "INSERT OR REPLACE INTO lectern_locks(location_hash, owner_uuid) VALUES(?, ?)";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            Psmt.setString(2, OwnerUUID);
            Psmt.executeUpdate();
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
    }

    public String GetLecternLock(Location Loc) {
        String Hash = GetLocationHash(Loc);
        String Sql = "SELECT owner_uuid FROM lectern_locks WHERE location_hash = ?";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            ResultSet Rs = Psmt.executeQuery();

            if (Rs.next()) {
                return Rs.getString("owner_uuid");
            }
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
        return null;
    }

    public void RemoveLecternLock(Location Loc) {
        String Hash = GetLocationHash(Loc);
        String Sql = "DELETE FROM lectern_locks WHERE location_hash = ?";

        try (PreparedStatement Psmt = Connection.prepareStatement(Sql)) {
            Psmt.setString(1, Hash);
            Psmt.executeUpdate();
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
    }

    public void Close() {
        try {
            if (Connection != null && !Connection.isClosed()) {
                Connection.close();
            }
        } catch (SQLException Ex) {
            Ex.printStackTrace();
        }
    }

    private String GetLocationHash(Location Loc) {
        return Loc.getWorld().getName() + "_" + Loc.getBlockX() + "_" + Loc.getBlockY() + "_" + Loc.getBlockZ();
    }

    public static class BlockInfo {
        public final String CustomName;
        public final String LoreJson;

        public BlockInfo(String CustomName, String LoreJson) {
            this.CustomName = CustomName;
            this.LoreJson = LoreJson;
        }
    }
}
