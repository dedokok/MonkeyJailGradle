package me.dedushka.monkeyJailGradle.Util.TrahodromWorld;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class OneChunkWorldManager {

    private final JavaPlugin plugin;
    private World customWorld;

    public OneChunkWorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    public void createWorld(String worldName, String structureName, Consumer<World> callback) {

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                WorldCreator creator = new WorldCreator(worldName);
                creator.generator(new SingleChunkWorldGenerator(plugin, structureName));
                creator.environment(World.Environment.NORMAL);
                creator.generateStructures(false);

                world = Bukkit.createWorld(creator);
            }

            World finalWorld = world;

            finalWorld.getChunkAtAsync(0, 0).thenAccept(chunk -> {

                Bukkit.getRegionScheduler().run(plugin, finalWorld, 0, 0, regionTask -> {

                    setupWorld(finalWorld,callback);

                    Bukkit.getGlobalRegionScheduler().run(plugin, globalTask -> {
                        finalWorld.setChunkForceLoaded(0, 0, true);
                        callback.accept(finalWorld);
                    });
                });

            }).exceptionally(ex -> {
                callback.accept(null);
                return null;
            });
        });
    }
    private void setupWorld(World world, Consumer<World> callback) {

        Bukkit.getRegionScheduler().run(plugin, world, 0, 0, task -> {

            world.getChunkAtAsync(0, 0).thenAccept(chunk -> {

                Bukkit.getGlobalRegionScheduler().run(plugin, task2 -> {

                    chunk.setForceLoaded(true);

                    world.setSpawnLocation(new Location(world, 8, 64, 8));

                    WorldBorder border = world.getWorldBorder();
                    border.setCenter(8, 8);
                    border.setSize(16);

                    world.setGameRule(GameRules.ADVANCE_TIME, false);
                    world.setGameRule(GameRules.ADVANCE_WEATHER, false);
                    world.setGameRule(GameRules.SPAWN_MOBS, false);

                    callback.accept(world);
                });

            }).exceptionally(ex -> {
                callback.accept(null);
                return null;
            });
        });
    }

    public World getWorld() {
        return customWorld;
    }

    public World getWorld(String worldName) {
        return Bukkit.getWorld(worldName);
    }
}