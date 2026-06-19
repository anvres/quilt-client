package tech.quilt.utility.mixin.client.render.gui.screen;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Redirect(
        method = "sendMessage(Ljava/lang/String;Z)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addToMessageHistory(Ljava/lang/String;)V")
    )
    private void skipHistoryForDotCommands(net.minecraft.client.gui.hud.ChatHud chatHud, String text) {
        if (tech.quilt.Quilt.getInstance().isUnhooked() || !text.startsWith(".")) {
            chatHud.addToMessageHistory(text);
        }
    }
}
