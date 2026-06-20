package me.dedushka.monkeyJailGradle.Util.TrahodromWorld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;

public class SingleChunkWorldGenerator extends ChunkGenerator {

    private final JavaPlugin plugin;
    private final String structureName;

    public SingleChunkWorldGenerator(JavaPlugin plugin, String structureName) {
        this.plugin = plugin;
        this.structureName = structureName;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (chunkX == 0 && chunkZ == 0) {
                    biome.setBiome(x, z, org.bukkit.block.Biome.PLAINS);
                } else {
                    biome.setBiome(x, z, org.bukkit.block.Biome.THE_VOID);
                }
            }
        }
        return chunkData;
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (chunkX == 0 && chunkZ == 0) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunkData.setBlock(x, 63, z, Material.GRASS_BLOCK);
                    chunkData.setBlock(x, 62, z, Material.DIRT);
                    chunkData.setBlock(x, 61, z, Material.DIRT);
                }
            }
        }
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(new StructurePopulator(plugin, structureName));
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return x >= -16 && x <= 16 && z >= -16 && z <= 16;
    }

    private class StructurePopulator extends BlockPopulator {
        private final JavaPlugin plugin;
        private final String structureName;
        private boolean placed = false;

        public StructurePopulator(JavaPlugin plugin, String structureName) {
            this.plugin = plugin;
            this.structureName = structureName;
        }

        @Override
        public void populate(World world, Random random, org.bukkit.Chunk chunk) {
            if (placed) return;
            if (chunk.getX() != 0 || chunk.getZ() != 0) return;

            try {
                File structureFile = new File(plugin.getDataFolder(), "structures/" + structureName + ".nbt");

                if (!structureFile.exists()) {
                    return;
                }

                StructureManager manager = Bukkit.getStructureManager();
                Structure structure = manager.loadStructure(structureFile);

                if (structure != null) {
                    Location placeLocation = new Location(world, 3, 64, 7);
                    structure.place(placeLocation, true, StructureRotation.NONE,
                            Mirror.NONE, 0, 1.0f, random);
                    placed = true;
                }
            } catch (Exception e) {
                getLogger().warning("Не удалось получить структуру траходрома");
            }
        }
    }
}