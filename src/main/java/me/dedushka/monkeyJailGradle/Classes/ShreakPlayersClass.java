package me.dedushka.monkeyJailGradle.Classes;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;

import java.util.HashSet;

public class ShreakPlayersClass {
    public int time_left;
    public String username;
    public Location location;
    public boolean isMonkey;
    public BossBar halfBar;
    public String partnerUsername;
    public HashSet<String> hiddenPlayers;
    public ShreakPlayersClass(int time_left, String username, Location location,
                              boolean isMonkey, BossBar halfBar, String partnerUsername,
                              HashSet<String>hiddenPlayers){
        this.time_left=time_left;
        this.username=username;
        this.location=location;
        this.isMonkey=isMonkey;
        this.halfBar=halfBar;
        this.partnerUsername=partnerUsername;
        this.hiddenPlayers=hiddenPlayers;
    }
}
