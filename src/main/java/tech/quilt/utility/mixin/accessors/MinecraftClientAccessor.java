package tech.quilt.utility.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
   @Accessor("itemUseCooldown")
   int getUseCooldown();

   @Accessor("itemUseCooldown")
   void setUseCooldown(int val);

   @Accessor("renderTickCounter")
   RenderTickCounter.Dynamic getRenderTickCounter();
}
