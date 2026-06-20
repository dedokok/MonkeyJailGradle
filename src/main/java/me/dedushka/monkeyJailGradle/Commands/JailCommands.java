package me.dedushka.monkeyJailGradle.Commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

public class JailCommands {

    public static void buildCommands(ReloadableRegistrarEvent<Commands> commands) {
        commands.registrar().register(
                Commands.literal("monkeyJail")
                        .then(showJailCommandBuilder())
                        .then(tpJailCommandBuilder())
                        .then(deleteJailCommandBuilder())
                        .then(jailCommandBuilder())
                        .then(shreakCommandBuilder())
                        .then(unjailCommandBuilder())
                        .then(createCommandBuilder())
                        .then(editCommandBuilder())
                        .then(sJailCommandBuilder())
                        .then(timeCommandBuilder())
                        .then(reloadCommandBuilder())
                        .build()
        );

        commands.registrar().register(tpJailCommandBuilder().build());
        commands.registrar().register(showJailCommandBuilder().build());
        commands.registrar().register(deleteJailCommandBuilder().build());
        commands.registrar().register(jailCommandBuilder().build());
        commands.registrar().register(shreakCommandBuilder().build());
        commands.registrar().register(unjailCommandBuilder().build());
        commands.registrar().register(createCommandBuilder().build());
        commands.registrar().register(editCommandBuilder().build());
        commands.registrar().register(sJailCommandBuilder().build());
        commands.registrar().register(sUnjailCommandBuilder().build());
        commands.registrar().register(timeCommandBuilder().build());


    }

    static LiteralArgumentBuilder<CommandSourceStack> reloadCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("reload")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.reload"))
                .executes(JailCommandsExecutors::reloadCommandExecutor);
        return command;
    }

    static LiteralArgumentBuilder<CommandSourceStack> showJailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("showJail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.showjail"))
                .then(Commands.argument("jail_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::jailListSuggestion)
                        .executes(JailCommandsExecutors::showJailCommandExecutor)
                );
        return command;
    }


    //tpjail
    static LiteralArgumentBuilder<CommandSourceStack> tpJailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("tpJail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.tpjail"))
                .then(Commands.argument("jail_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::jailListSuggestion)
                        .executes(JailCommandsExecutors::tpJailCommandExecutor)
                );
        return command;
    }


    //deletejail
    static LiteralArgumentBuilder<CommandSourceStack> deleteJailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("deleteJail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.deleteJail"))
                .then(Commands.argument("jail_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::jailListSuggestion)
                        .executes(JailCommandsExecutors::deleteJailCommandExecutor)
                );
        return command;
    }


    //fuck
    static LiteralArgumentBuilder<CommandSourceStack> shreakCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("fuck")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.fuck"))
                .then(Commands.argument("monkey_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::onlineMonkeyListSuggestion)
                        .executes(JailCommandsExecutors::shreakCommandExecutor)
                );
        return command;
    }

    //time
    static LiteralArgumentBuilder<CommandSourceStack> timeCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("timeMonkey")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.time"))
                .executes(ctx->JailCommandsExecutors.timeCommandExecutor(ctx,false))
                .then(Commands.argument("username", StringArgumentType.word())
                        .requires(sender -> sender.getSender().hasPermission("monkeyjail.timeanotherplayer"))
                        .suggests(CommandsSuggestions::monkeyListSuggestion)
                        .executes(ctx->JailCommandsExecutors.timeCommandExecutor(ctx,true))
                );
        return command;
    }


    //jail
    static LiteralArgumentBuilder<CommandSourceStack> jailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("jail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.jail"))
                .then(Commands.argument("monkey_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::onlinePlayersSuggestion)
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .suggests(CommandsSuggestions::jailListSuggestion)
                                .then(Commands.argument("time", StringArgumentType.word())
                                        .executes(ctx->JailCommandsExecutors.jailMonkeyCommandExecutor(ctx,false,false))
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(ctx->JailCommandsExecutors.jailMonkeyCommandExecutor(ctx,false,true))
                                        )
                                )

                        )
                );
        return command;
    }


    //sjail
    static LiteralArgumentBuilder<CommandSourceStack> sJailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("sjail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.jail"))
                .then(Commands.argument("monkey_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::onlinePlayersSuggestion)
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .suggests(CommandsSuggestions::jailListSuggestion)
                                .then(Commands.argument("time", StringArgumentType.word())
                                        .executes(ctx->JailCommandsExecutors.jailMonkeyCommandExecutor(ctx,true,false))
                                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                                .executes(ctx->JailCommandsExecutors.jailMonkeyCommandExecutor(ctx,true,false))
                                        )
                                )

                        )
                );
        return command;
    }




    //unjail
    static LiteralArgumentBuilder<CommandSourceStack> unjailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("unjail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.unjail"))
                .then(Commands.argument("monkey_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::monkeyListSuggestion)
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx->JailCommandsExecutors.unJailMonkeyCommandExecutor(ctx,false,true))
                        )
                        .executes(ctx->JailCommandsExecutors.unJailMonkeyCommandExecutor(ctx,false,false))
                );
        return command;
    }




    //sunjail
    static LiteralArgumentBuilder<CommandSourceStack> sUnjailCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("sunjail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.unjail"))
                .then(Commands.argument("monkey_name", StringArgumentType.word())
                        .suggests(CommandsSuggestions::monkeyListSuggestion)
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx->JailCommandsExecutors.unJailMonkeyCommandExecutor(ctx,true,true))
                        )
                        .executes(ctx->JailCommandsExecutors.unJailMonkeyCommandExecutor(ctx,true,false))
                );
        return command;
    }



    //create
    static LiteralArgumentBuilder<CommandSourceStack> createCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("createJail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.createjail"))
                .then(Commands.literal("help")
                        .executes(JailCommandsExecutors::openHelpBook)
                )
                .then(Commands.literal("start")
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .executes(ctx -> JailCommandsExecutors.createStartCommandExecutor(ctx, false))
                        )
                )
                .then(Commands.literal("setFA")
                        .executes(JailCommandsExecutors::setFACommandExecutor)
                )
                .then(Commands.literal("setSA")
                        .executes(JailCommandsExecutors::setSACommandExecutor)
                )
                .then(Commands.literal("setHeight")
                        .then(Commands.argument("height", IntegerArgumentType.integer())
                                .executes(JailCommandsExecutors::createSetHeightCommandExecutor)
                        )
                )
                .then(Commands.literal("show")
                        .executes(JailCommandsExecutors::showCommandExecutor)
                )
                .then(Commands.literal("stop")
                        .executes(JailCommandsExecutors::stopProcess)
                )
                .then(Commands.literal("removeB")
                        .executes(JailCommandsExecutors::createRemoveBCommandExecutor)
                )
                .then(Commands.literal("addB")
                        .executes(JailCommandsExecutors::createAddBCommandExecutor)
                )
                .then(Commands.literal("done")
                        .executes(JailCommandsExecutors::createDoneCommandExecutor)
                )
                .then(Commands.literal("setSB")
                        .executes(JailCommandsExecutors::createSetSBCommandExecutor)
                )
                .then(Commands.literal("setName")
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .executes(JailCommandsExecutors::setNameCommandExecutor)
                        )
                );
        return command;
    }


    //editJail
    static LiteralArgumentBuilder<CommandSourceStack> editCommandBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("editJail")
                .requires(sender -> sender.getSender().hasPermission("monkeyjail.editjail"))
                .then(Commands.literal("help")
                        .executes(JailCommandsExecutors::openHelpBook)
                )
                .then(Commands.literal("start")
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .suggests(CommandsSuggestions::jailListSuggestion)
                                .executes(ctx -> JailCommandsExecutors.createStartCommandExecutor(ctx, true))
                        )
                )
                .then(Commands.literal("setFA")
                        .executes(JailCommandsExecutors::setFACommandExecutor)
                )
                .then(Commands.literal("setSA")
                        .executes(JailCommandsExecutors::setSACommandExecutor)
                )
                .then(Commands.literal("setHeight")
                        .then(Commands.argument("height", IntegerArgumentType.integer())
                                .executes(JailCommandsExecutors::createSetHeightCommandExecutor)
                        )
                )
                .then(Commands.literal("show")
                        .executes(JailCommandsExecutors::showCommandExecutor)
                )
                .then(Commands.literal("stop")
                        .executes(JailCommandsExecutors::stopProcess)
                )
                .then(Commands.literal("removeB")
                        .executes(JailCommandsExecutors::createRemoveBCommandExecutor)
                )
                .then(Commands.literal("addB")
                        .executes(JailCommandsExecutors::createAddBCommandExecutor)
                )
                .then(Commands.literal("done")
                        .executes(JailCommandsExecutors::createDoneCommandExecutor)
                )
                .then(Commands.literal("setSB")
                        .executes(JailCommandsExecutors::createSetSBCommandExecutor)
                )
                .then(Commands.literal("setName")
                        .then(Commands.argument("jail_name", StringArgumentType.word())
                                .executes(JailCommandsExecutors::setNameCommandExecutor)
                        )
                );

        return command;
    }
}