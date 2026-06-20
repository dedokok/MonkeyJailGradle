    package me.dedushka.monkeyJailGradle;

    import github.scarsz.discordsrv.DiscordSRV;
    import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
    import me.dedushka.monkeyJailGradle.Commands.JailCommands;
    import me.dedushka.monkeyJailGradle.Config.Config;
    import me.dedushka.monkeyJailGradle.Listeners.EventListener;
    import me.dedushka.monkeyJailGradle.Util.DataBaseManager;
    import me.dedushka.monkeyJailGradle.Util.JailLogic;
    import me.dedushka.monkeyJailGradle.Util.TrahodromWorld.OneChunkWorldManager;
    import net.skinsrestorer.api.SkinsRestorer;
    import net.skinsrestorer.api.SkinsRestorerProvider;
    import org.bukkit.*;
    import org.bukkit.plugin.Plugin;
    import org.bukkit.plugin.java.JavaPlugin;

    import java.io.File;
    import java.io.IOException;
    import java.io.InputStream;
    import java.nio.file.Files;

    public final class MonkeyJailGradle extends JavaPlugin {

    private DataBaseManager DBM = new DataBaseManager();
    private OneChunkWorldManager worldManager;
    public static MonkeyJailGradle instance;
    public static SkinsRestorer skinsRestorerAPI;
    public static Server server = Bukkit.getServer();
    public static World trahWorld;
    public static EventListener eventListener = new EventListener();
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;
        extractStructure();
        checkDataBase();
        Config.updateConfigValues();
        DiscordSRV.api.subscribe(JailLogic.discordSRVManager);
        new JailLogic(this).startJail();
        registerCommands();
        checkSkinsRestorer();


        getServer().getPluginManager().registerEvents(eventListener, this);
    }




    @Override
    public void onDisable() {
        JailLogic.tpAllFromShreakingMachine();
        DBM.updateMonkeyTable(JailLogic.updatedTimeMonkeys);
    }


    public void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            JailCommands.buildCommands(commands);
        });
    }

    public void checkSkinsRestorer(){
        Plugin skinsRest = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
        if (skinsRest != null) {
            this.skinsRestorerAPI = SkinsRestorerProvider.get();
            getLogger().info("SkinsRestorer загружен");
        } else {
            getLogger().info("SkinsRestorer не загружен. У обезьян не будет скина");
        }
    }


    public void checkDataBase() {
        File dataFolder = getDataFolder();
        File dbFile = new File(dataFolder, "database.db");
        if (dbFile.exists()) {
            DBM.connectDB();
            getLogger().info("База данных найдена");
        } else {
            getLogger().info("Файл базы данных не найден, будет создан новый");
            DBM.createDB();
        }
    }

    public static MonkeyJailGradle getInstance() {
        return instance;
    }

    public OneChunkWorldManager getWorldManager() {
        return worldManager;
    }

    public void extractStructure() {
        File structureDir = new File(getDataFolder(), "structures");
        if (!structureDir.exists()) {
            structureDir.mkdirs();
        }

        File targetFile = new File(structureDir, "shreakmachine.nbt");

        if (!targetFile.exists()) {
            try (InputStream inputStream = getResource("structures/shreakmachine.nbt")) {
                if (inputStream == null) {
                    getLogger().severe("Файл structures/shreakmachine.nbt не найден в ресурсах плагина!");
                    getLogger().severe("Создайте папку src/main/resources/structures/ и положите туда shreakmachine.nbt");
                    return;
                }
                Files.copy(inputStream, targetFile.toPath());
                getLogger().info("Структура shreakmachine.nbt скопирована в папку плагина!");
            } catch (IOException e) {
                getLogger().warning("Не удалось получить структуру траходрома");
            }
        }
    }
}

