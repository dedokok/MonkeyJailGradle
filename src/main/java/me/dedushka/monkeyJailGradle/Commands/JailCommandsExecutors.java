package me.dedushka.monkeyJailGradle.Commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.dedushka.monkeyJailGradle.*;
import me.dedushka.monkeyJailGradle.Classes.BlockPosClass;
import me.dedushka.monkeyJailGradle.Classes.JailProcessClass;
import me.dedushka.monkeyJailGradle.Config.Config;
import me.dedushka.monkeyJailGradle.Util.DataBaseManager;
import me.dedushka.monkeyJailGradle.Util.DiscordSRVManager;
import me.dedushka.monkeyJailGradle.Util.JailLogic;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.skinsrestorer.api.connections.MineSkinAPI;
import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinVariant;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;


public class JailCommandsExecutors {
    static DataBaseManager DBM = new DataBaseManager();
    public static HashMap<String, JailProcessClass> jailsCreationProcesses = new HashMap<>();
    public static MineSkinResponse response;

    public JailCommandsExecutors(){}

    // время в тики
    public static long parseTimeToTicks(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String input = ctx.getArgument("time", String.class);

        if (input == null || input.isEmpty()) {
            sender.sendRichMessage("<red>Время не может быть пустым!");
            return -1;
        }

        char last = input.charAt(input.length() - 1);

        if (Character.isDigit(last)) {
            try {
                long ticks = Long.parseLong(input);
                if (ticks < 0) {
                    sender.sendRichMessage("<red>Время не может быть отрицательным!");
                    return -1;
                }
                return ticks;
            } catch (NumberFormatException e) {
                sender.sendRichMessage("<red>Неправильное время!");
                return -1;
            }
        }

        char unit = last;
        long value;

        try {
            value = Long.parseLong(input.substring(0, input.length() - 1));
        } catch (NumberFormatException e) {
            sender.sendRichMessage("<red>Слишком большое число!");
            return -1;
        }

        if (value < 0) {
            sender.sendRichMessage("<red>Время не может быть меньше 0!");
            return -1;
        }

        long seconds;

        switch (unit) {
            case 's': seconds = value; break;
            case 'm': seconds = value * 60; break;
            case 'h': seconds = value * 60 * 60; break;
            case 'd': seconds = value * 60 * 60 * 24; break;
            case 'w': seconds = value * 60 * 60 * 24 * 7; break;
            case 'M': seconds = value * 60 * 60 * 24 * 30; break;
            case 'y': seconds = value * 60 * 60 * 24 * 365; break;
            default:
                sender.sendRichMessage("<red>Неизвестный суффикс времени!");
                return -1;
        }


        if (seconds > Long.MAX_VALUE / 20) {
            sender.sendRichMessage("<red>Время слишком большое!");
            return -1;
        }

        return seconds * 20;
    }

    // /createJail setName
    static int setNameCommandExecutor(CommandContext<CommandSourceStack> ctx){
        String jail_name = ctx.getArgument("jail_name",String.class);
        Player player = (Player) ctx.getSource().getSender();
        if(jailsCreationProcesses.containsKey(player.getName())){
            if(!JailLogic.jails.containsKey(jail_name)) {
                jailsCreationProcesses.get(player.getName()).jail_name = jail_name;
                player.sendRichMessage("<green>Тюрьме успешно установлено имя <gold>"+jail_name);
            }
            else{
                player.sendRichMessage("<red>Такая тюрьма уже существует!");
                return 0;
            }
        }
        return 1;
    }


    // /unjail
    static int unJailMonkeyCommandExecutor(CommandContext<CommandSourceStack> ctx, boolean isSUnjail, boolean isReason){
        Player player = (Player) ctx.getSource().getSender();
        String monkey_name = ctx.getArgument("monkey_name", String.class);
        String reason = "нет причины";
        if(isReason)reason=ctx.getArgument("reason",String.class);

        if(JailLogic.monkeyList.containsKey(monkey_name)) {
            if(JailLogic.monkeyList.get(monkey_name).time_left<=0){
                player.sendRichMessage("<red>Это не обезьяна!");
                return 0;
            }


            if (Bukkit.getPlayer(monkey_name) != null) {
                if (!isSUnjail) {
                    Bukkit.broadcastMessage("§c" + player.getName() + " выпустил обезьяну " + monkey_name + " из зоопарка по причине: " + reason + ".");
                }
                DiscordSRVManager.sendEmbed(player.getName(), monkey_name, "", "", reason, 2,false,isSUnjail);
            } else {
                if (!isSUnjail) {
                    Bukkit.broadcastMessage("§c" + player.getName() + " выпустил оффлайн-обезьяну " + monkey_name + " из зоопарка по причине: " + reason + ".");
                }
                DiscordSRVManager.sendEmbed(player.getName(), monkey_name, "", "", reason, 2,true,isSUnjail);
            }
            logCommand(ctx);

            JailLogic.monkeyList.get(monkey_name).time_left = 0;

            JailLogic.removeFromMonkeys(monkey_name);
        }
        return 1;
    }

    //showjail
    static int showJailCommandExecutor(final CommandContext<CommandSourceStack> ctx){
        Player player = (Player) ctx.getSource().getSender();
        String jail_name = ctx.getArgument("jail_name",String.class);
        if(!JailLogic.jails.containsKey(jail_name)){
            player.sendMessage("§cТакой тюрьмы нет!");
            return 0;
        }

        JailProcessClass jailShow = new JailProcessClass(JailLogic.jails.get(jail_name));
        showJailBorder(jailShow, player);
        return 1;
    }

    // /tpJail
    static int tpJailCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String jail_name = ctx.getArgument("jail_name", String.class);

        getLogger().info("Начал процесс телепорта");
        if (!JailLogic.jails.containsKey(jail_name)) {
            player.sendMessage("§cТакой тюрьмы нет!");
            return 0;
        }

        World jail_world = Bukkit.getWorld(JailLogic.jails.get(jail_name).world);
        BlockPosClass spawnBlock = JailLogic.jails.get(jail_name).spawnBlock;
        double x = spawnBlock.x, z = spawnBlock.z, y = spawnBlock.y;
        player.teleportAsync(new Location(jail_world, x, y, z));
        player.sendMessage("Вы были телепортированы в клетку " + jail_name);
        logCommand(ctx);
        return 1;
    }

    //удалить тюрьму
    static int deleteJailCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String jail_name = ctx.getArgument("jail_name", String.class);

        getLogger().info("Начал процесс удаления тюрьмы "+ jail_name);
        hideJailFromAllPlayers(jail_name);


        DBM.deleteJail(jail_name);
        JailLogic jL = new JailLogic();
        jL.updateJailList();
        jL.updateMonkeyList();
        player.sendMessage("§cТюрьма успешно удалена");
        logCommand(ctx);
        return 1;
    }

    // спртать тюрьму от всех игроков, если те включили её отображение (при удалении)
    static void hideJailFromAllPlayers(String jail_name){
        for(String username : JailLogic.showJails.keySet()){
            HashMap<String,ArrayList<Integer>> playerShows = JailLogic.showJails.get(username);
            if(playerShows.containsKey(jail_name)){
                Player player = Bukkit.getPlayer(username);
                if(player!=null) {
                    hideJailBorder(player,jail_name);
                }
            }
        }
    }


    // /fuck
    static int shreakCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String monkey_name = ctx.getArgument("monkey_name", String.class);


        if(player.getName().equals(monkey_name)){
            player.sendMessage("§cСебя нельзя отправить на траходром!");
            return 0;
        }

        if(JailLogic.monkeyList.containsKey(player.getName())){
            player.sendMessage("§cТы обезьяна!");
            return 0;
        }

        Player player_monkey = Bukkit.getPlayer(monkey_name);
        if (player_monkey == null) {
            player.sendMessage("§cИгрок не в сети!");
            return 0;
        }


        if(!player.getWorld().getName().equals(player_monkey.getWorld().getName())){
            player.sendMessage("§cВы должны быть возле обезьяны");
            return 0;
        }

        if(Config.fuckMonkeyDistance != -1 && player.getLocation().distance(player_monkey.getLocation())>Config.fuckMonkeyDistance){
            player.sendMessage("§cВы должны быть возле обезьяны");
            return 0;
        }

        if(!JailLogic.monkeyList.containsKey(monkey_name)){
            player.sendMessage("§cЭто не обезьяна!");
            return 0;
        }

        if (JailLogic.shreakPlayers.containsKey(player_monkey.getName())) {
            player.sendMessage("§cОбезьяна пока занята, подождите немного");
            return 0;
        }

        if(JailLogic.shreakPlayers.containsKey(player.getName())){
            player.sendRichMessage("<red>У вас уже есть процесс траходрома, подождите "+JailLogic.shreakPlayers.get(player.getName()).time_left/20+" секунд");
            return 0;
        }
        JailLogic.createShreakProcess(player, player_monkey);

        return 1;
    }

    // /timeMonkey [<ник>]
    static int timeCommandExecutor(CommandContext<CommandSourceStack> ctx, boolean isAnotherPlayer){
        Player player = (Player) ctx.getSource().getSender();
        String check_username = player.getName();
        if(isAnotherPlayer){
            check_username=ctx.getArgument("username",String.class);
            logCommand(ctx);
        }
        if(!JailLogic.monkeyList.containsKey(check_username)){
            player.sendRichMessage("<red>Такой обезьяны нет!");
            return 0;
        }
        player.sendRichMessage("<green>Обезьяне осталось сидеть <gold>"+(JailLogic.monkeyList.get(check_username).time_left/20)+" <green>секунд");
        return 1;
    }

    // /jail
    static int jailMonkeyCommandExecutor(CommandContext<CommandSourceStack> ctx, boolean isSJail, boolean isReason){
        Player player = (Player) ctx.getSource().getSender();
        String monkey_name = ctx.getArgument("monkey_name", String.class);
        String reason = "нет причины";
        if(isReason)reason=ctx.getArgument("reason",String.class);

        String jail_name=ctx.getArgument("jail_name",String.class);
        String time_string = ctx.getArgument("time",String.class);

        long time = parseTimeToTicks(ctx);
        if(time ==-1)return 0;

        if(JailLogic.monkeyList.containsKey(monkey_name)) {
            if (JailLogic.monkeyList.get(monkey_name).time_left <= 0) {
                player.sendRichMessage("<red>Это не обезьяна!");
                return 0;
            }
            DBM.removeMonkey(monkey_name);
            player.sendMessage("§cОбезьяна уже есть в этой тюрьме, пересадим с новым сроком");

        }
        if(!JailLogic.jails.containsKey(jail_name)){
            player.sendMessage("§cТакой тюрьмы не существует!");
            return 0;
        }


        if(MonkeyJailGradle.skinsRestorerAPI==null){return 0;}
        Player player_monkey = Bukkit.getPlayer(monkey_name);

            if (player_monkey!=null) {
                setSkinFromUrl(Bukkit.getPlayer(player_monkey.getName()), Config.defaultMonkeySkinURL);

                if (!isSJail) {
                    Bukkit.broadcastMessage("§c" + player.getName() + " посадил обезьяну " + monkey_name + " в зоопарк \"" + jail_name + "\" на " + time_string + " по причине: " + reason);
                }
                DiscordSRVManager.sendEmbed(player.getName(),monkey_name,jail_name,time_string,reason,1,false,isSJail);
            }
            else{
                if(!isSJail) {
                    Bukkit.broadcastMessage("§c" + player.getName() + " посадил в оффлайне обезьяну " + monkey_name + " в зоопарк \"" + jail_name + "\" на " + time_string + " по причине: " + reason);
                }
                DiscordSRVManager.sendEmbed(player.getName(),monkey_name,jail_name,time_string,reason,1,true,isSJail);
            }
        logCommand(ctx);

        DBM.addMonkey(jail_name,monkey_name,time, player.getName(),reason);
        JailLogic.updateMonkeyList();

        return 1;
    }


    // /monkey createJail start
    static int createStartCommandExecutor(CommandContext<CommandSourceStack> ctx, boolean isEdit) {
        Player player = (Player) ctx.getSource().getSender();
        String jail_name = ctx.getArgument("jail_name",String.class);
        if (jailsCreationProcesses.containsKey(player.getName())) {
            player.sendMessage("§cВы уже создаёте/редактируете тюрьму! Можете остановить процесс командой /monkey createJail/editJail stop");
            return 0;
        }

        JailProcessClass jailProcess;

        if (!isEdit) {
            if(JailLogic.jails.containsKey(jail_name)){
                player.sendMessage("§cТакая тюрьма уже существует!");
                return 0;
            }
            jailProcess = new JailProcessClass();
            jailProcess.world = player.getWorld().getName();
            jailProcess.jail_name = jail_name;
            player.sendMessage("Открыть меню помощи /monkey createJail help");
        } else {
            if(!JailLogic.jails.containsKey(jail_name)){
                player.sendMessage("§cТакой тюрьмы не существует!");
                return 0;
            }
            jailProcess = new JailProcessClass(DBM.loadJail(jail_name));
            showJailBorder(jailProcess, player);
        }
        openHelpBook(ctx);
        logCommand(ctx);
        jailsCreationProcesses.put(player.getName(),jailProcess);

        return 1;
    }

    // /monkey createJail setFA
    static int setFACommandExecutor(CommandContext<CommandSourceStack> ctx){
        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if (jailProcess!=null) {
            Location pL = player.getLocation();
            int x = (int) Math.floor(pL.getX());
            int y = (int) Math.floor(pL.getY());
            int z = (int) Math.floor(pL.getZ());
            jailProcess.angle1 = new BlockPosClass(x, y, z);
            if (jailProcess.angle2 == null) {
                jailProcess.angle2 = jailProcess.angle1;
            } else {
                jailProcess.angle2.y = jailProcess.angle1.y;
            }
            jailProcess.blocks.clear();
            if(getJailBlocks(jailProcess)==-1)return 0;
            if(jailProcess.isShow) {
                hideJailBorder(player, jailProcess.jail_name);
                showJailBorder(jailProcess, player);
            }
            jailProcess.isBlocksEdited=true;
            player.sendMessage("Первый угол успешно установлен");
            return 1;
        } else {
            player.sendMessage("§cНачните процесс создания/редактирования тюрьмы!");
        }
        return 0;
    }

    // /monkey createJail setSA
    static int setSACommandExecutor(CommandContext<CommandSourceStack> ctx){
        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess!=null) {
            Location pL = player.getLocation();
            int x = (int) Math.floor(pL.getX());
            int y = (int) Math.floor(pL.getY());
            int z = (int) Math.floor(pL.getZ());
            jailProcess.angle2 = new BlockPosClass(x, y, z);
            if (jailProcess.angle1 == null) {
                jailProcess.angle1 = jailProcess.angle2;
            } else {
                jailProcess.angle2.y = jailProcess.angle1.y;
            }
            jailProcess.blocks.clear();
            if(getJailBlocks(jailProcess)==-1)return 0;
            if(jailProcess.isShow) {
                hideJailBorder(player, jailProcess.jail_name);
                showJailBorder(jailProcess, player);
            }
            jailProcess.isBlocksEdited=true;
            player.sendMessage("Второй угол успешно установлен");
            return 1;
        } else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        return 0;
    }


    // /monkey createJail setHeight
    static int createSetHeightCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        int height = ctx.getArgument("height", Integer.class);
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if(height<=0){
            player.sendRichMessage("<red>Высота не может быть 0 и меньше!");
            return 0;
        }

        if (jailProcess != null) {

            jailProcess.angle2.y += height;
            extendHeight(jailProcess, height, player);
            if(jailProcess.isShow) {
                hideJailBorder(player, jailProcess.jail_name);
                showJailBorder(jailProcess, player);
            }
            jailProcess.isBlocksEdited=true;
            player.sendMessage("Высота успешно установлена");
            return 1;
        } else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
            return 0;
        }
    }

    // /monkey createJail show
    static int showCommandExecutor(CommandContext<CommandSourceStack> ctx){

        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if(jailProcess!=null){
            jailProcess = jailsCreationProcesses.get(player.getName());
        }
        else{
            player.sendMessage("§cНачните процесс создания тюрьмы!");
            return 0;
        }
        if(jailProcess==null){
            jailProcess =jailsCreationProcesses.get(player.getName());
        }

        if(jailProcess.isShow) {
            hideJailBorder(player,jailProcess.jail_name);
            jailProcess.isShow=false;
        }
        else{
            showJailBorder(jailProcess, player);
            jailProcess.isShow=true;
        }
        return 1;
    }



    // /monkey createJail removeB
    static int createRemoveBCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if (jailProcess != null) {
            Location location = player.getLocation();
            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());

            BlockPosClass target = new BlockPosClass(x, y, z);
            if (jailProcess.blocks.remove(target)) {
                if(jailProcess.isShow) {
                    hideJailBorder(player, jailProcess.jail_name);
                    showJailBorder(jailProcess, player);
                }
                player.sendMessage("Блок удалён");
                jailProcess.isBlocksEdited=true;
                //}
                return 1;
            }

            player.sendMessage("§cЭтого блока нет в тюрьме");
        }
        else {
            player.sendMessage("§cНачните процесс создания/редактирования тюрьмы!");
        }
        return 0;
    }

    // /monkeyJail reload
    public static int reloadCommandExecutor(CommandContext<CommandSourceStack> ctx){
        JailLogic.reloadPlugin(ctx);
        return 1;
    }
    // /monkey createJail addB
    public static int createAddBCommandExecutor(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if (jailProcess != null) {
            Location location = player.getLocation();
            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());
            for (BlockPosClass block : jailProcess.blocks) {
                if (block.x == x && block.y == y && block.z == z) {
                    player.sendMessage("§cЭтот блок уже есть в тюрьме");
                    return 0;
                }
            }
            player.sendMessage("Блок добавлен");
            jailProcess.blocks.add(new BlockPosClass(x, y, z));
            if(jailProcess.isShow) {
                hideJailBorder(player, jailProcess.jail_name);
                showJailBorder(jailProcess, player);
            }
            jailProcess.isBlocksEdited=true;
        }
        else {
            player.sendMessage("§cНачните процесс создания тюрьмы!");
        }
        return 1;
    }


    // /createJail done
    public static int createDoneCommandExecutor(CommandContext<CommandSourceStack> ctx){
        Player player = (Player) ctx.getSource().getSender();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());

        if(jailProcess==null){
            player.sendMessage("§cОшибка. Начните создание/редактирование тюрьмы");
            return 0;
        }
        if(jailProcess.jail_name !=null && jailProcess.world!=null && !jailProcess.blocks.isEmpty() && jailProcess.spawnBlock!=null){
            jailProcess.creatorName=player.getName();
            if(JailLogic.jails.containsKey(jailProcess.jail_name)) {
                player.sendRichMessage("<red>Такая тюрьма уже существует!");
                return 0;
            }
            addJailToDataBase(jailProcess, DBM.isJailExists(jailProcess.jail_name));

            stopProcess(ctx);
            JailLogic.updateJailList();
            player.sendMessage("Успешно создана тюрьма "+jailProcess.jail_name + " на координатах " + jailProcess.spawnBlock.x + " " + jailProcess.spawnBlock.y + " " + jailProcess.spawnBlock.z);
            logCommand(ctx);
            return 1;
        }
        else{
            //getLogger().info("Прошёл в ошибки")
            if(jailProcess.world==null){
                player.sendMessage("§cОшибка. Не указан мир");
            }
            if(jailProcess.blocks.isEmpty()){
                player.sendMessage("§cОшибка. Нет блоков");
            }
            if(jailProcess.spawnBlock==null){
                player.sendMessage("§cОшибка. Нет позиции спавна");
            }
            if(jailProcess.jail_name==null){
                player.sendMessage("§cОшибка. Нет имени тюрьмы");
            }
            return 0;
        }
    }


    // /createJail setSB
    public static int createSetSBCommandExecutor(CommandContext<CommandSourceStack> ctx){
        Player player = (Player) ctx.getSource().getSender();
        Location location = player.getLocation();
        JailProcessClass jailProcess = jailsCreationProcesses.get(player.getName());
        if(jailProcess!=null) {
            int x = (int) Math.floor(location.getX());
            int y = (int) Math.floor(location.getY());
            int z = (int) Math.floor(location.getZ());
            jailProcess.spawnBlock = new BlockPosClass(x, y, z);
            player.sendMessage("Спавн-блок установлен");
            return 1;
        }
        else{
            player.sendMessage("§cОшибка. Начните процесс создания/редактирования тюрьмы");
            return 0;
        }
    }


    //открыть книжку
    static int openHelpBook(CommandContext<CommandSourceStack> ctx){
        Player player = (Player)ctx.getSource().getSender();
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta != null) {
            meta.setTitle("Помощь по созданию клеток");
            meta.setAuthor("dedushka_1");
            meta.addPage("""
                            Все команды делать через §l/createJail§r
                            Открыть эту книгу: §lhelp§r.
                            §lСоздание тюрьмы:
                            §l1.§c§l!!!§r Начать: §lstart§r
                            §l2.§c§l!!!§r Сделать пол: §lsetFA§r и §l§r. Тут получится 2д прямоугольник
                            §l3.§r Изменить форму: addB§r и §lremoveB§r
                            """);
            meta.addPage("""
                            §l4.§r Сделать высоту: §lsetHeight <число>§r
                            §l5.§c§l!!!§r Установить имя: §lsetName <имя>§r
                            §l6.§c§l!!!§r Установить спавн-блок: §lsetSB§r
                            §l7.§c§l!!!§r Закончить: §ldone§r.
                            Закончить без сохранения: §lstop§r
                            Показать/спрятать чертёж: §lshow§r
                            """);

            book.setItemMeta(meta);

            player.openBook(book);
        }
        return 1;
    }

    //изменить высоту
    public static void extendHeight(JailProcessClass jailProcess, int height, Player player){
        int minHeight = jailProcess.blocks.stream()
                .mapToInt(block -> block.y)
                .min()
                .orElse(0);


        jailProcess.blocks.removeIf(block -> block.y > minHeight);
        hideJailBorder(player,jailProcess.jail_name);

        Set<BlockPosClass> newBlocks = new HashSet<>();
        for (BlockPosClass blockPos : jailProcess.blocks) {
            for (int j = 1; j < height; j++) {
                newBlocks.add(new BlockPosClass(blockPos.x, blockPos.y + j, blockPos.z));
            }
        }
        jailProcess.blocks.addAll(newBlocks);
    }

    //получаю блоки прямоугольной клетки
    public static int getJailBlocks(JailProcessClass jailProcess) {

        if (jailProcess.angle1 != null && jailProcess.angle2 != null) {
            int minX = Math.min(jailProcess.angle1.x, jailProcess.angle2.x);
            int minY = Math.min(jailProcess.angle1.y, jailProcess.angle2.y);
            int minZ = Math.min(jailProcess.angle1.z, jailProcess.angle2.z);
            int maxX = Math.max(jailProcess.angle1.x, jailProcess.angle2.x);
            int maxY = Math.max(jailProcess.angle1.y, jailProcess.angle2.y);
            int maxZ = Math.max(jailProcess.angle1.z, jailProcess.angle2.z);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        jailProcess.blocks.add(new BlockPosClass(x, y, z));
                    }
                }
            }
            return 1;

            //getLogger().info("Всего блоков в тюрьме: " + jailProcess.blocks.size());
        } else {
            getLogger().warning("Во время установки угла произошла ошибка");
            return -1;
        }
    }

    //спрятать тюрьму
    public static void hideJailBorder(Player player, String jail_name){
        if (player == null) return;
        if(JailLogic.showJails.containsKey(player.getName())){
            HashMap<String,ArrayList<Integer>> playerShows = JailLogic.showJails.get(player.getName());
            if(playerShows.containsKey(jail_name)){
                ArrayList<Integer> entitiesIds = playerShows.get(jail_name);
                for (int entityId : entitiesIds) {
                    hideFakeBlockDisplay(player, entityId);
                }
                entitiesIds.clear();
                playerShows.remove(jail_name);
            }
        }
    }


    //показать границы тюрьмы
    public static void showJailBorder(JailProcessClass jailProcess, Player player) {
        HashMap<String, ArrayList<Integer>> playerShows = null;
        if (JailLogic.showJails.containsKey(player.getName())) {
            playerShows = JailLogic.showJails.get(player.getName());
            if (playerShows.containsKey(jailProcess.jail_name)) {
                hideJailBorder(player, jailProcess.jail_name);
                playerShows.remove(jailProcess.jail_name);
                return;
            }
        }
        playerShows = new HashMap<>();
        ArrayList<Integer> entitiIds = new ArrayList<>();
        for (BlockPosClass block : jailProcess.blocks) {
            int id = spawnFakeBlockDisplay(player, block.x, block.y, block.z, Bukkit.getWorld(jailProcess.world));
            entitiIds.add(id);
        }
        playerShows.put(jailProcess.jail_name, entitiIds);
        JailLogic.showJails.put(player.getName(), playerShows);

    }

    //заспавнить фейковый блокдисплей пакетом
    public static int spawnFakeBlockDisplay(Player player, int x, int y, int z, World world) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();

        Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, nmsWorld);
        blockDisplay.setPos(x, y, z);

        blockDisplay.setBlockState(Blocks.RED_STAINED_GLASS.defaultBlockState());

        ServerGamePacketListenerImpl connection = serverPlayer.connection;
        net.minecraft.network.protocol.Packet<?> spawnPacket = blockDisplay.getAddEntityPacket(
        new net.minecraft.server.level.ServerEntity(
                nmsWorld, //тут убрал приведение, ошибок вроде нет
                blockDisplay,
                0,
                false,
                new net.minecraft.server.level.ServerEntity.Synchronizer() {
                    public void sendChanges(net.minecraft.network.protocol.Packet<?> packet) {}

                    public void sendDirtyEntityData() {}

                    public void sendToTrackingPlayersFiltered(
                            net.minecraft.network.protocol.Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener> packet,
                            java.util.function.Predicate<net.minecraft.server.level.ServerPlayer> predicate
                    ) {}

                    public void sendToTrackingPlayersAndSelf(
                            net.minecraft.network.protocol.Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener> packet
                    ) {}

                    public void sendToTrackingPlayers(
                            net.minecraft.network.protocol.Packet<? super net.minecraft.network.protocol.game.ClientGamePacketListener> packet
                    ) {}
                },
                java.util.Set.of()
        )
        );
        connection.send(spawnPacket);
        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(
                blockDisplay.getId(),
                blockDisplay.getEntityData().packDirty()
        );
        connection.send(metadataPacket);
        return blockDisplay.getId();
    }


    //спрятать блок от игрока по айди
    public static void hideFakeBlockDisplay(Player player, int entityId) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(entityId)
        );
    }

    //закончить процесс
    public static int stopProcess(CommandContext<CommandSourceStack> ctx){
        if(ctx!=null) {
            Player player = (Player) ctx.getSource().getSender();
            String username = player.getName();
            if(jailsCreationProcesses.containsKey(username)) {
                JailProcessClass jailProcess = jailsCreationProcesses.get(username);
                if (jailProcess != null) {
                    hideJailBorder(player, jailProcess.jail_name);
                    jailsCreationProcesses.remove(username);
                    player.sendRichMessage("<green>Процесс остановлен");
                } else {
                    return 0;
                }
            }
            else{
                player.sendMessage("§cОшибка. Начните процесс создания/редактирования тюрьмы");
                return 0;
            }
        }
        return 1;

    }

    //добавить тюрьму в БД
    public static void addJailToDataBase(JailProcessClass jailProcess, boolean isEdit){
        DBM.saveJail(jailProcess,isEdit, jailProcess.isBlocksEdited);
    }

    //поставить скин по ссылке
    public static void setSkinFromUrl(Player player, String url) {
        if(MonkeyJailGradle.skinsRestorerAPI==null) {return;}
        MineSkinAPI mineSkinAPI = MonkeyJailGradle.skinsRestorerAPI.getMineSkinAPI();
        try {
            if(response==null){
                response = mineSkinAPI.genSkin(url, SkinVariant.CLASSIC);
            }
            SkinProperty skinProperty = response.getProperty();
            MonkeyJailGradle.skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player, skinProperty);
        }
        catch(Exception e){
            getLogger().warning("Не удалось установить скин игроку "+player.getName());
        }
    }

    //кинуть факт использовани команды всем админам кроме пользователя (как /tp)
    public static void logCommand(CommandContext<CommandSourceStack> ctx){
        String command = ctx.getInput();
        for(Player player : Bukkit.getOnlinePlayers()){
            if(!player.getName().equals(ctx.getSource().getSender().getName()) && player.isOp()){
                player.sendRichMessage("<italic><gray>"+player.getName()+" использовал команду /"+command);
            }
        }

    }

}