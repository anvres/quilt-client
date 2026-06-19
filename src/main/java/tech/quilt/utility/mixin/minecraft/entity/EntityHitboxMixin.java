package tech.quilt.utility.mixin.minecraft.entity;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.quilt.base.events.impl.player.EventEntityHitBox;

@Mixin({Entity.class})
public class EntityHitboxMixin {
    
    @Inject(
        method = {"getBoundingBox"},
        at = {@At("RETURN")},
        cancellable = true
    )
    public void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        Box original = cir.getReturnValue();
        if (original == null) return;
        
        EventEntityHitBox event = new EventEntityHitBox((Entity) (Object) this, 0.0F);
        EventManager.call(event);
        
        float expandAmount = event.getSize();
        if (expandAmount != 0.0F) {
            double expand = expandAmount;
            
            Box expanded = new Box(
                original.minX - expand,
                original.minY - expand,
                original.minZ - expand,
                original.maxX + expand,
                original.maxY + expand,
                original.maxZ + expand
            );
            cir.setReturnValue(expanded);
        }
    }
}