package tech.quilt.base.comand.impl;

import bots.Bot;
import bots.BotManager;
import bots.BotStarter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.nexusguard.protection.annotations.Native;
import tech.quilt.base.comand.api.CommandAbstract;
import tech.quilt.utility.game.other.MessageUtil;

import java.util.List;

/**
 * Commands for managing bots
 * Usage: .bot <subcommand>
 */
public class BotCommand extends CommandAbstract {
    
    public BotCommand() {
        super("bot");
    }
    
    @Native
    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        // .bot connect <name> <ip> [port]
        builder.then(literal("connect").then(
            arg("name", StringArgumentType.word()).then(
                arg("ip", StringArgumentType.word()).executes((context) -> {
                    String name = context.getArgument("name", String.class);
                    String ip = context.getArgument("ip", String.class);
                    BotStarter.run(name, ip);
                    MessageUtil.displayInfo(Formatting.GRAY + "Starting bot " + Formatting.WHITE + name + Formatting.GRAY + " on server " + Formatting.WHITE + ip);
                    return 1;
                }).then(
                    arg("port", IntegerArgumentType.integer(1, 65535)).executes((context) -> {
                        String name = context.getArgument("name", String.class);
                        String ip = context.getArgument("ip", String.class);
                        int port = context.getArgument("port", Integer.class);
                        BotStarter.run(name, ip, port);
                        MessageUtil.displayInfo(Formatting.GRAY + "Starting bot " + Formatting.WHITE + name + Formatting.GRAY + " on " + Formatting.WHITE + ip + ":" + port);
                        return 1;
                    })
                )
            )
        ));
        
        // .bot stop <name>
        builder.then(literal("stop").then(
            arg("name", StringArgumentType.word()).suggests((context, suggestionsBuilder) -> {
                for (Bot bot : BotManager.getAllBots()) {
                    suggestionsBuilder.suggest(bot.getName());
                }
                return suggestionsBuilder.buildFuture();
            }).executes((context) -> {
                String name = context.getArgument("name", String.class);
                BotStarter.stop(name);
                MessageUtil.displayInfo(Formatting.GRAY + "Stopping bot " + Formatting.WHITE + name);
                return 1;
            })
        ));
        
        // .bot stopall
        builder.then(literal("stopall").executes((context) -> {
            BotStarter.stopAll();
            MessageUtil.displayInfo(Formatting.GRAY + "Stopping all bots");
            return 1;
        }));
        
        // .bot list
        builder.then(literal("list").executes((context) -> {
            List<Bot> bots = BotManager.getAllBots();
            if (bots.isEmpty()) {
                MessageUtil.displayInfo(Formatting.GRAY + "No bots");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(Formatting.GOLD).append("Active bots (").append(bots.size()).append("): ");
                for (Bot bot : bots) {
                    sb.append(Formatting.WHITE).append(bot.getName());
                    if (bot.isConnected()) {
                        sb.append(Formatting.GREEN).append(" [ONLINE]");
                    } else {
                        sb.append(Formatting.RED).append(" [OFFLINE]");
                    }
                    sb.append(Formatting.GRAY).append(", ");
                }
                MessageUtil.displayInfo(sb.substring(0, sb.length() - 2));
            }
            return 1;
        }));
        
        // .bot count
        builder.then(literal("count").executes((context) -> {
            MessageUtil.displayInfo(Formatting.GRAY + "Total bots: " + Formatting.WHITE + BotManager.getBotCount());
            return 1;
        }));
        
        // .bot control <name> - Switch camera to bot
        builder.then(literal("control").then(
            arg("name", StringArgumentType.word()).suggests((context, suggestionsBuilder) -> {
                for (Bot bot : BotManager.getAllBots()) {
                    if (bot.isConnected()) {
                        suggestionsBuilder.suggest(bot.getName());
                    }
                }
                return suggestionsBuilder.buildFuture();
            }).executes((context) -> {
                String name = context.getArgument("name", String.class);
                Bot bot = BotManager.getBotByName(name);
                MinecraftClient mc = MinecraftClient.getInstance();
                if (bot != null && bot.isConnected() && mc.player != null) {
                    try {
                        // TODO: Implement actual camera switching
                        // This requires accessing the bot's player entity and setting it as renderViewEntity
                        MessageUtil.displayInfo(Formatting.GRAY + "Switched to bot: " + Formatting.WHITE + name + Formatting.GRAY + " (placeholder)");
                    } catch (Exception e) {
                        MessageUtil.displayError(Formatting.RED + "Failed to switch to bot: " + e.getMessage());
                    }
                } else {
                    MessageUtil.displayError(Formatting.RED + "Bot " + Formatting.WHITE + name + Formatting.RED + " not found or not connected");
                }
                return 1;
            })
        ));
        
        // .bot return - Return to own player
        builder.then(literal("return").executes((context) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            try {
                if (mc.player != null && mc.world != null) {
                    // Reset to own player's camera
                    mc.setCameraEntity(mc.player);
                    MessageUtil.displayInfo(Formatting.GRAY + "Returned to own player");
                } else {
                    MessageUtil.displayError(Formatting.RED + "Cannot return: player or world is null");
                }
            } catch (Exception e) {
                MessageUtil.displayError(Formatting.RED + "Failed to return: " + e.getMessage());
            }
            return 1;
        }));
    }
}
