package me.dedushka.monkeyJailGradle.Util;

import me.dedushka.monkeyJailGradle.Classes.BlockPosClass;
import me.dedushka.monkeyJailGradle.Classes.JailClass;
import me.dedushka.monkeyJailGradle.Classes.MonkeyClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.bukkit.Bukkit.getLogger;

public class DataBaseManager {
    private static Connection connection;
    public DataBaseManager(){}


    public static void createDB(){
        try {
            String url = "jdbc:sqlite:plugins/MonkeyJail/database.db";
            connection = DriverManager.getConnection(url);

            String sql = "CREATE TABLE IF NOT EXISTS jails (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "jail_name TEXT NOT NULL," +
                    "creator_name TEXT NOT NULL, " +
                    "world TEXT NOT NULL, " +
                    "spawn_key INTEGER NOT NULL)";
            connection.prepareStatement(sql).executeUpdate();

            sql = "CREATE TABLE IF NOT EXISTS blocks (" +
                    "block_key INTEGER NOT NULL, " +
                    "jail_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (jail_id, block_key), " +
                    "FOREIGN KEY (jail_id) REFERENCES jails(id) ON DELETE CASCADE)";
            connection.prepareStatement(sql).executeUpdate();

            sql =   "CREATE TABLE IF NOT EXISTS monkeys (id INTEGER PRIMARY KEY AUTOINCREMENT, jail_name TEXT NOT NULL, username TEXT NOT NULL," +
                    "time_left INTEGER, admin_username TEXT, reason TEXT)";
            connection.prepareStatement(sql).executeUpdate();



        }
        catch( SQLException e){
            getLogger().warning("Не удалось создать таблицы");
        }
    }

    //подключение к бд обезьян
    public static void connectDB() {
        try {
            String url = "jdbc:sqlite:plugins/MonkeyJail/database.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            getLogger().warning("Не удалось подключиться к БД");
        }
    }


    //добавить обезьяну в БД
    public static void addMonkey(String jail_name, String username, long time_left, String admin_username, String reason){
        String sql = "INSERT INTO monkeys (jail_name,username,time_left,admin_username,reason) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, jail_name);
            pstmt.setString(2, username);
            pstmt.setLong(3, time_left);
            pstmt.setString(4, admin_username);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
            //getLogger().info("Добавил запись в обезьянник");
            //getLogger().info("3 Connection is "+ (connection==null ? true : false));
        }
        catch(SQLException e){
            getLogger().warning("Не удалось добавить обезьяну");
        }
    }

    //удалить обезьяну из БД
    public static void removeMonkey(String username){
        String sql = "DELETE FROM monkeys WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    //получить список обезьян
    public static HashMap<String,MonkeyClass> getAllMonkeys(int monkeyAmount, int lastMonkeyId){
        HashMap<String,MonkeyClass> monkeyList = new HashMap<>();
        String sql = "SELECT * FROM monkeys ORDER BY id DESC";
        if(monkeyAmount!=-1){sql+=" LIMIT ?";}
        if(lastMonkeyId!=-1){sql+=" WHERE id < ?";}
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String jail_name = rs.getString("jail_name");
                String username = rs.getString("username");
                long time_left = rs.getLong("time_left");
                String admin_username = rs.getString("admin_username");
                String reason = rs.getString("reason");

                monkeyList.put(username,new MonkeyClass(jail_name,username, time_left, admin_username, reason));
            }
        } catch (SQLException e) {
            getLogger().warning("Не удалось получить список обезьян");
        }

        return monkeyList;
    }

    public static long toKey(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    public static int xFromKey(long key) {
        int raw = (int)(key >> 38) & 0x3FFFFFF;
        return (raw << 6) >> 6;
    }

    public static int zFromKey(long key) {
        int raw = (int)(key >> 12) & 0x3FFFFFF;
        return (raw << 6) >> 6;
    }

    public static int yFromKey(long key) {
        int raw = (int) key & 0xFFF;
        return (raw << 20) >> 20;
    }

    public static void saveJailBlocks(int jailId, JailClass jail, boolean isEdit) {
        //String jail_name = jail.jail_name;
        //getLogger().info("Сохранение блоков тюрьмы. Количество: "+ jail.blocks.size());
        String deleteSql = "DELETE FROM blocks WHERE jail_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, jailId);
            deleteStmt.executeUpdate();
        }
        catch(Exception e){
            getLogger().warning("Не удалось удалить старые блоки тюрьмы");
        }


        String insertSql = "INSERT INTO blocks (jail_id, block_key) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            int count = 0;
            for (BlockPosClass block : jail.blocks) {
                //getLogger().info("Сохранил блок "+count);
                long key = toKey(block.x, block.y, block.z);
                insertStmt.setInt(1, jailId);
                insertStmt.setLong(2, key);
                insertStmt.addBatch();
                if (count % 1000 == 0) {
                    insertStmt.executeBatch();
                }
                count++;
            }


            insertStmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        }
        catch(Exception e){
            getLogger().warning("Не удалось сохранить блоки тюрьмы");
        }
    }


    public static int saveJail(JailClass jail, boolean isEdit, boolean isBlockEdited) {
        String sql = "";
        int jailId = -1;
        if(isEdit){
            //getLogger().info("Прошёл в edit");
            sql = "UPDATE jails SET spawn_key = ?, jail_name = ? WHERE id = ?";

            String sql_get_jail_id = "SELECT * FROM jails WHERE jail_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql_get_jail_id)){
                pstmt.setString(1, jail.jail_name);

                ResultSet rs = pstmt.executeQuery();;

                while(rs.next()) {
                    jailId = rs.getInt("id");
                    //getLogger().info("id: "+jailId);
                }
            } catch (SQLException e) {
                getLogger().warning("Не удалось получить айди редактируемой тюрьмы");
                return 0;
            }

        }
        else {
            //getLogger().info("Прошёл в create");

            sql = "INSERT INTO jails (jail_name, creator_name, world, spawn_key) VALUES (?, ?, ?, ?)";
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if(isEdit) {
                //getLogger().info("Прошёл в ещё в edit");
                stmt.setLong(1,toKey(jail.spawnBlock.x, jail.spawnBlock.y, jail.spawnBlock.z));
                stmt.setString(2, jail.jail_name);
                stmt.setInt(3, jail.jail_id);
            }
            else{
                stmt.setString(1, jail.jail_name);
                stmt.setString(2, jail.creatorName);
                stmt.setString(3, jail.world);
                stmt.setLong(4, toKey(jail.spawnBlock.x, jail.spawnBlock.y, jail.spawnBlock.z));
            }
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if(jailId==-1) {
                //getLogger().info("Прошёл в -1");
                jailId = keys.next() ? keys.getInt(1) : -1;
            }
            if(isBlockEdited) {
                //getLogger().info("Прошёл в готовый id");
                saveJailBlocks(jailId, jail, isEdit);
            }
            return jailId;
        }
        catch(Exception e){
            getLogger().warning("Не удалось сохранить тюрьму");
        }
        return -1;
    }

    // Загрузить все тюрьмы
    public static HashMap<String, JailClass> loadAllJails() {
        HashMap<String, JailClass> jails = new HashMap<>();

        String sql = "SELECT id, jail_name, creator_name, world, spawn_key FROM jails ORDER BY id ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int jail_id = rs.getInt("id");
                String jail_name = rs.getString("jail_name");
                String creator = rs.getString("creator_name");
                String worldName = rs.getString("world");
                long spawnKey = rs.getLong("spawn_key");

                BlockPosClass spawn = new BlockPosClass(xFromKey(spawnKey), yFromKey(spawnKey), zFromKey(spawnKey));
                ArrayList<BlockPosClass> blocks = loadJailBlocks(jail_id);

                jails.put(jail_name, new JailClass(jail_id,jail_name, worldName, creator, blocks, spawn));
            }
        } catch (Exception e) {
            getLogger().info("Ошибка loadAllJails");
        }

        return jails;
    }


    //получить одну тюрьму
    public static JailClass loadJail(String jail_name) {
        JailClass jail = null;
        String sql = "SELECT id, creator_name, world, spawn_key FROM jails WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jail_name);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int jail_id = rs.getInt("id");
                String creator_name = rs.getString("creator_name");
                String world_name = rs.getString("world");
                long spawnKey = rs.getLong("spawn_key");


                BlockPosClass spawn_block = new BlockPosClass(xFromKey(spawnKey), yFromKey(spawnKey), zFromKey(spawnKey));
                ArrayList<BlockPosClass> blocks = loadJailBlocks(jail_id);
                jail = new JailClass(jail_id,jail_name,world_name,creator_name,blocks,spawn_block);
            }
        } catch (Exception e) {
            getLogger().info("Ошибка loadAllJails");
        }

        return jail;
    }

    //получить блоки тюрьмы
    public static ArrayList<BlockPosClass> loadJailBlocks(int jailId) {
        ArrayList<BlockPosClass> blocks = new ArrayList<>();

        String sql = "SELECT block_key FROM blocks WHERE jail_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, jailId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long key = rs.getLong("block_key");
                blocks.add(new BlockPosClass(xFromKey(key), yFromKey(key), zFromKey(key)));
            }
        }
        catch(Exception e){
            getLogger().warning("Не удалось получить блоки тюрьмы");
        }

        return blocks;
    }

    // Удалить тюрьму (блоки удалятся сами)
    public static boolean deleteJail(String jailName) {
        String sql = "DELETE FROM jails WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jailName);
            stmt.executeUpdate();
        }
        catch(Exception e){
            getLogger().warning("Не удалось удалить тюрьму");
            return false;
        }
        sql = "UPDATE monkeys SET time_left = 0 WHERE jail_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jailName);
            stmt.executeUpdate();
            return true;
        }
        catch(Exception e){
            getLogger().warning("Не удалось убрать время обезьянам");
            return false;
        }
    }


    //проверить, есть ли в зоопарке данная обезьяна
    public static boolean isMonkeyInJail(String username){
        String sql = "SELECT 1 FROM monkeys WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
        catch(SQLException e){
            getLogger().warning(e.getMessage());
        }
        return false;
    }


    //проверить, есть ли уже такая тюрьма
    public static boolean isJailExists(String jail_name){
        String sql = "SELECT 1 FROM jails WHERE jail_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, jail_name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
                return false;
            }
        }
        catch(SQLException e){
            getLogger().warning(e.getMessage());
        }

        return false;
    }


    public static void updateMonkeyTable(HashMap<String,MonkeyClass> updatedMonkeyList){
        for(MonkeyClass monkey : updatedMonkeyList.values()){
            String sql = "UPDATE monkeys SET time_left = ? WHERE username = ? AND jail_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, monkey.time_left);
                pstmt.setString(2, monkey.username);
                pstmt.setString(3, monkey.jail_name);
                pstmt.executeUpdate();
            }
            catch(SQLException e){
                getLogger().warning(e.getMessage());
            }
        }
    }
}