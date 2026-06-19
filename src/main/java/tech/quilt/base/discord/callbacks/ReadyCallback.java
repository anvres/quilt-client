package tech.quilt.base.discord.callbacks;

import com.sun.jna.Callback;
import tech.quilt.base.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
   void apply(DiscordUser var1);
}
