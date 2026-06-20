package me.dedushka.monkeyJailGradle.Listeners;

import me.dedushka.monkeyJailGradle.Classes.ShreakPlayersClass;
import me.dedushka.monkeyJailGradle.Config.Config;
import me.dedushka.monkeyJailGradle.Util.JailLogic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

//мог бы использовать эти ивенты в EventListener, но меня жаба душит делать ивенты,
//которые половину времени не будут использоваться
//а shreakinglistener регистрируется только когда на траходроме кто-то есть

public class ShreakingListener implements Listener {
    public static boolean isActive = false;
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {//запретить обезьяне на траходроме двигаться (можно немного ворочать камерой)
        ShreakPlayersClass shreakPlayer = JailLogic.shreakPlayers.get(event.getPlayer().getName());
        if(shreakPlayer!=null && shreakPlayer.isMonkey){
            Location from = event.getFrom();
            Location to = event.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setCancelled(true);
                return;
            }
            if (to.getYaw()<-140 || to.getYaw()>-40) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {//запретить обезьяне слазить с шифта
        ShreakPlayersClass shreakPlayer = JailLogic.shreakPlayers.get(event.getPlayer().getName());
        if (shreakPlayer!=null && shreakPlayer.isMonkey) {
            event.setCancelled(true);
            event.getPlayer().setSneaking(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){//запретить игрокам на траходроме ломать блоки
        if(JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){//запретить игрокам на траходроме ставить блоки
        if(JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){//запретить игрокам на траходроме использовать предметы
        if(JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){//чтобы при выходе обезьяны траходром сбрасывался, а при выходе игрока нет
        if(JailLogic.shreakPlayers.containsKey(event.getPlayer().getName())) {
            if(JailLogic.monkeyList.containsKey(event.getPlayer().getName())){
                JailLogic.monkeyList.get(event.getPlayer().getName()).time_left+= Config.quietMonkeyTickIncrease;
                Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<red>Обезьяна "+event.getPlayer().getName()+" вышла из игры на траходроме и получила штрафное время в зоопарке!"));
                String partnerName = JailLogic.shreakPlayers.get(event.getPlayer().getName()).partnerUsername;
                if(JailLogic.shreakPlayers.containsKey(partnerName) && Bukkit.getPlayer(partnerName)!=null){
                    Bukkit.getPlayer(partnerName).teleportAsync(JailLogic.shreakPlayers.get(partnerName).location);
                    Bukkit.getPlayer(partnerName).hideBossBar(JailLogic.shreakPlayers.get(partnerName).halfBar);
                    JailLogic.shreakPlayers.remove(partnerName);
                }
                event.getPlayer().teleportAsync(JailLogic.shreakPlayers.get(event.getPlayer().getName()).location);
                JailLogic.shreakPlayers.remove(event.getPlayer().getName());
            }
            //сделал так, чтобы обычный игрок при выходе не удалялся из траходрома, т.к. он мог бы сразу другую обезьяну отрправить, опять перезайти и отправить ещё обезьян
        }
    }

}
