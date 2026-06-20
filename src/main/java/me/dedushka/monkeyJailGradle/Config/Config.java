package me.dedushka.monkeyJailGradle.Config;

import me.dedushka.monkeyJailGradle.MonkeyJailGradle;

public class Config {
    public static MonkeyJailGradle MJ;

    public static int globalTimerDelay = 1;
    public static int timeInShreakingMachine=200;
    public static int doEveryLoopDelay = 4;
    public static int quietMonkeyTickIncrease = 10*20*60; //10 минут
    public static int shreakTimerDelay = 20; //1 секунда
    public static String trahodromName = "trahodrom";
    public static int updateMonkeysTimeDelay = 1200; //1 минута
    public static int fuckMonkeyDistance = 10;
    public static String defaultMonkeySkinURL;
    public static String discordHistoryChannelId = "xxx";


    public static void updateConfigValues(){
        MJ = MonkeyJailGradle.instance;

        Config.globalTimerDelay = MJ.getConfig().getInt("globalTimerDelay");
        Config.timeInShreakingMachine = MJ.getConfig().getInt("timeInShreakingMachine");
        Config.doEveryLoopDelay = MJ.getConfig().getInt("doEveryLoopDelay");
        Config.quietMonkeyTickIncrease = MJ.getConfig().getInt("quietMonkeyTickIncrease");
        Config.trahodromName = MJ.getConfig().getString("trahodromName");
        Config.updateMonkeysTimeDelay = MJ.getConfig().getInt("updateMonkeysTimeDelay");
        Config.fuckMonkeyDistance = MJ.getConfig().getInt("fuckMonkeyDistance");
        Config.defaultMonkeySkinURL = MJ.getConfig().getString("defaultMonkeySkinURL");
        Config.discordHistoryChannelId = MJ.getConfig().getString("history_discord_channel_id");
    }
}
