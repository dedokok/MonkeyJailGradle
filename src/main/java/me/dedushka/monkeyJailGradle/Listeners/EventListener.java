package me.dedushka.monkeyJailGradle.Listeners;

import io.canvasmc.canvas.event.world.WorldUnloadAsyncEvent;
import me.dedushka.monkeyJailGradle.*;
import me.dedushka.monkeyJailGradle.Classes.JailProcessClass;
import me.dedushka.monkeyJailGradle.Classes.ShreakPlayersClass;
import me.dedushka.monkeyJailGradle.Commands.JailCommandsExecutors;
import me.dedushka.monkeyJailGradle.Config.Config;
import me.dedushka.monkeyJailGradle.Util.JailLogic;
import me.dedushka.monkeyJailGradle.Util.TrahodromWorld.OneChunkWorldManager;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerLoadEvent;

import java.io.File;

import static org.bukkit.Bukkit.getLogger;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        //установить скин обезьяны
        if(JailLogic.monkeyList.containsKey(player.getName())){
            try {
                JailCommandsExecutors.setSkinFromUrl(Bukkit.getPlayer(player.getName()), Config.defaultMonkeySkinURL);
            }
            catch(Exception e ){
                getLogger().warning("Не удалось получить дефолтный скин");
            }
        }
        //если зашедший игрок находится на траходроме, но не должен, его тп на место спавна
        if(player.getWorld().getName().equals("trahodrom")){
            if(!JailLogic.shreakPlayers.containsKey(player.getName())){
                Bukkit.getGlobalRegionScheduler().run(MonkeyJailGradle.instance, task -> {
                    player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
                });
            }
            else{
                JailLogic.hideShreakPlayers(player);
                player.showBossBar(JailLogic.shreakPlayers.get(player.getName()).halfBar);
            }
        }
        if(JailCommandsExecutors.jailsCreationProcesses.containsKey(player.getName())){
            player.sendMessage("У вас имеется процесс создания/редактирования клетки.");
            JailProcessClass jailProcess = JailCommandsExecutors.jailsCreationProcesses.get(player.getName());
            if(jailProcess.isShow){
                JailCommandsExecutors.showJailBorder(jailProcess,player);
            }
        }
    }


    @EventHandler
    public void onUseEvent(PlayerInteractEvent event){//запретить обезьянам использовать
        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlaceEvent(BlockPlaceEvent event){//запретить обезьянам ставить
        if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
            event.setCancelled(true);
        }
    }

        @EventHandler
        public void playerDeathEvent(PlayerDeathEvent event) {//запретить обезьянам терять вещи при смерти
            if (JailLogic.monkeyList.containsKey(event.getPlayer().getName()) || JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
                processDeath(event);
            }
            if (JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
                ShreakPlayersClass shreaking_player = JailLogic.shreakPlayers.get(event.getPlayer().getName());
                shreaking_player.time_left = 0;
                JailLogic.shreakPlayers.get(shreaking_player.partnerUsername).time_left = 0;
                Player player = event.getPlayer();
                io.papermc.paper.threadedregions.scheduler.ScheduledTask task =
                        Bukkit.getRegionScheduler().run(MonkeyJailGradle.instance, player.getLocation(), scheduledTask -> {
                            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                            net.minecraft.server.MinecraftServer.getServer().getPlayerList().respawn(
                                    serverPlayer,
                                    false,
                                    net.minecraft.world.entity.Entity.RemovalReason.KILLED,
                                    PlayerRespawnEvent.RespawnReason.DEATH
                            );
                        });

            }

        }
        void processDeath(PlayerDeathEvent event){
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDeathMessage(null);
            if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())) {
                event.getPlayer().sendRichMessage("<red>Пока вы в стадии смерти, вам не засчитывается время");
            }
        }
    @EventHandler
    public void onHitEvent(EntityDamageByEntityEvent event){//запретить обезьянам бить
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (damager instanceof Player && victim instanceof Player) {
            Player attacker = (Player) damager;
            Player target = (Player) victim;
            if(JailLogic.monkeyList.containsKey(attacker.getName())){
                if(!JailLogic.monkeyList.containsKey(target.getName())){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onServerEnable(ServerLoadEvent event){//создание/загрузка мира траходрома
        boolean isTrahodromExists = worldFolderExists(Config.trahodromName);
        //я ебал эти шедулеры. Изза их логики тут будет пизда
        Bukkit.getRegionScheduler().run(MonkeyJailGradle.instance, Bukkit.getWorld("world"), 0, 0, task -> {
            OneChunkWorldManager worldManager = new OneChunkWorldManager(MonkeyJailGradle.instance);
            worldManager.createWorld(Config.trahodromName, "shreakmachine", world -> {

                if (world == null) {
                    Bukkit.getLogger().warning("Не удалось создать мир "+Config.trahodromName+"!");
                    return;
                }
                MonkeyJailGradle.trahWorld=world;

                //тут делаю так, чтобы при создании мира активировался поршень
                if(!isTrahodromExists) {
                    int x = 2;
                    int y = 64;
                    int z = 8;

                    Bukkit.getRegionScheduler().execute(MonkeyJailGradle.instance, world, x >> 4, z >> 4, () -> {
                        world.getBlockAt(x, y, z).setType(Material.REDSTONE_BLOCK);

                        Bukkit.getRegionScheduler().runDelayed(MonkeyJailGradle.instance, world, x >> 4, z >> 4, task2 -> {
                            world.getBlockAt(x, y, z).setType(Material.AIR);
                        }, 2);
                    });
                }
            });
        });
    }
    public boolean worldFolderExists(String worldName) {
        File folder = new File(Bukkit.getWorldContainer(), worldName);
        return folder.exists() && folder.isDirectory();
    }

    @EventHandler
    public void worldUnloadEvent(WorldUnloadAsyncEvent event){
        JailCommandsExecutors.stopProcess(null);
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        JailLogic.showJails.remove(player.getName());
    }
}