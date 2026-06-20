package me.dedushka.monkeyJailGradle.Classes;

import org.bukkit.entity.BlockDisplay;

import java.util.HashMap;

public class JailProcessClass extends JailClass {
    public BlockPosClass angle1;
    public BlockPosClass angle2;
    public boolean isBlocksEdited = false;
    public boolean isShow = true;

    public JailProcessClass(){}
    public JailProcessClass(JailClass jail) {
        super(jail.jail_id, jail.jail_name, jail.world,
                jail.creatorName, jail.blocks, jail.spawnBlock);
    }
}