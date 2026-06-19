package tech.quilt.base.comand.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import tech.quilt.Quilt;
import tech.quilt.base.comand.api.CommandAbstract;
import tech.quilt.utility.game.other.MessageUtil;

public class UnhookCommand extends CommandAbstract {
    public UnhookCommand() {
        super("unhook");
    }

    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            MessageUtil.displayInfo(String.valueOf(Formatting.RED) + "Unhooking Quilt...");
            Quilt.getInstance().unhook();
            return 1;
        });
    }
}
