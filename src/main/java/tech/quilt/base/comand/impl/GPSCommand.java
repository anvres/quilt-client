package tech.quilt.base.comand.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import tech.quilt.Quilt;
import tech.quilt.base.comand.api.CommandAbstract;
import tech.quilt.base.waypoint.Waypoint;
import tech.quilt.utility.game.other.MessageUtil;

public class GPSCommand extends CommandAbstract {
   public GPSCommand() {
      super("gps");
   }

   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(arg("X", IntegerArgumentType.integer()).then(arg("Z", IntegerArgumentType.integer()).executes((context) -> {
         int x = (Integer)context.getArgument("X", Integer.class);
         int z = (Integer)context.getArgument("Z", Integer.class);
         Waypoint waypoint = new Waypoint((double)x, (double)z);
         Quilt.getInstance().getWaypointManager().set(waypoint);
         MessageUtil.displayInfo("GPS создан и указывает на XZ: %s, %s".formatted(new Object[]{x, z}));
         return 1;
      })))).then(literal("player").then(arg("name", StringArgumentType.word()).then(arg("X", IntegerArgumentType.integer()).then(arg("Z", IntegerArgumentType.integer()).executes((context) -> {
         String name = (String)context.getArgument("name", String.class);
         int x = (Integer)context.getArgument("X", Integer.class);
         int z = (Integer)context.getArgument("Z", Integer.class);
         Waypoint waypoint = new Waypoint(name, (double)x, (double)z);
         Quilt.getInstance().getWaypointManager().setPlayerWaypoint(waypoint);
         MessageUtil.displayInfo("GPS создан и указывает на XZ: %s, %s".formatted(new Object[]{x, z}));
         return 1;
      })))))).then(literal("player").then(literal("remove").executes((context) -> {
         if (!Quilt.getInstance().getWaypointManager().isEmptyPlayerWaypoint()) {
            Quilt.getInstance().getWaypointManager().clearPlayerWaypoint();
            MessageUtil.displayInfo("GPS удален");
         } else {
            MessageUtil.displayInfo("Нет активного GPS");
         }

         return 1;
      })))).then(literal("remove").executes((context) -> {
         if (!Quilt.getInstance().getWaypointManager().isEmpty()) {
            Quilt.getInstance().getWaypointManager().clear();
            MessageUtil.displayInfo("GPS на точку игрока удален");
         } else {
            MessageUtil.displayInfo("Нет активного на игрока GPS");
         }

         return 1;
      }));
   }
}
