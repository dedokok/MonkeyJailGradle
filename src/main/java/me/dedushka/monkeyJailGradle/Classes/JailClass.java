package me.dedushka.monkeyJailGradle.Classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JailClass {
    public int jail_id;
    public String jail_name;
    public String world;
    public String creatorName;
    public ArrayList<BlockPosClass> blocks = new ArrayList<>();
    public BlockPosClass spawnBlock;
    public JailClass(){}
    public JailClass(int jail_id,String jail_name, String world,String creatorName,ArrayList<BlockPosClass> blocks, BlockPosClass spawnBlock){
        this.jail_id = jail_id;
        this.jail_name=jail_name;
        this.world = world;
        this.creatorName=creatorName;
        this.blocks = blocks;
        this.spawnBlock = spawnBlock;
    }
}
