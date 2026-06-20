package me.dedushka.monkeyJailGradle.Commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.dedushka.monkeyJailGradle.Classes.MonkeyClass;
import me.dedushka.monkeyJailGradle.Util.JailLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CommandsSuggestions {

    public static CompletableFuture<Suggestions> onlinePlayersSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        for(Player player : Bukkit.getOnlinePlayers()){
            builder.suggest(player.getName());
        }
        return builder.buildFuture();
    }


    public static CompletableFuture<Suggestions> monkeyListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        for(MonkeyClass monkey : JailLogic.monkeyList.values()){
            if(monkey.time_left<=0)continue;
            builder.suggest(monkey.username);
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> onlineMonkeyListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        for(MonkeyClass monkey : JailLogic.monkeyList.values()){
            if(monkey.time_left<=0)continue;
            if(Bukkit.getPlayer(monkey.username)!=null) {
                builder.suggest(monkey.username);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> jailListSuggestion(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder){
        for(String jail_name : JailLogic.jails.keySet()){
            builder.suggest(jail_name);
        }
        return builder.buildFuture();
    }
}
