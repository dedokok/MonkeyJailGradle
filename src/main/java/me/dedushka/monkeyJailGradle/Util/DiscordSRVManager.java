package me.dedushka.monkeyJailGradle.Util;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.dedushka.monkeyJailGradle.Config.Config;

import java.awt.*;

import static org.bukkit.Bukkit.getLogger;

public class DiscordSRVManager {
    static TextChannel channel;
    static boolean isEnabled = true;
    static String channel_id = Config.discordHistoryChannelId;

    public void updateValues(){
        channel_id = Config.discordHistoryChannelId;
        JDA jda = DiscordSRV.getPlugin().getJda();
        if(channel_id!=null) {
            try {
                channel = jda.getTextChannelById(channel_id);
                if (channel == null) {
                    getLogger().warning("Не удалось получить канал");
                    isEnabled = true;
                }
            } catch (Exception e) {
                getLogger().warning("ID канала истории неверный, история наказаний отключена!");
                channel = null;
                channel_id = null;
            }
        }
        else{
            getLogger().warning("Не удалось получить айди канала");
            isEnabled=false;
        }
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        updateValues();
    }

    public static void sendMessage(String message){
        if(isEnabled) {
            DiscordUtil.sendMessage(channel, message);
        }
    }
    public static void sendEmbed(String username, String monkey_name, String jail_name, String time, String reason, int message_type, boolean isOffline, boolean isSilent) {
        if (channel == null) return;

        String description = "";
        Color color = Color.BLACK;
        if(message_type==1){
            description = "**"+username+"**";
            if(isSilent)description+=" тихо";
            description+=" посадил ";
            if(isOffline)description+="оффлайн-";
            description += "игрока **"+monkey_name+"** в клетку **"+jail_name+"** на **"+time +"** по причине: **"+reason+"**";
            color = Color.RED;
        }
        else if(message_type==2){
            description = "**"+username+"**";
            if(isSilent)description+=" тихо";
        description+= " выпустил ";
        if(isOffline)description+="оффлайн-";
        description+="обезьяну **"+monkey_name+"** из клетки по причине: **"+reason+"**";
            color = Color.GREEN;
        }
        MessageEmbed embed = new EmbedBuilder()
                .setDescription(description)
                .setColor(color)
                .build();

        channel.sendMessageEmbeds(embed).queue();
    }
}
