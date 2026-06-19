package tech.quilt.base.events.impl.player;

import lombok.Generated;
import net.minecraft.util.math.Vec3d;
import tech.quilt.base.events.callables.EventCancellable;

public class EventOnTravelPost extends EventCancellable {
   private Vec3d oldVelocity;

   @Generated
   public EventOnTravelPost(Vec3d oldVelocity) {
      this.oldVelocity = oldVelocity;
   }

   @Generated
   public Vec3d getOldVelocity() {
      return this.oldVelocity;
   }

   @Generated
   public void setOldVelocity(Vec3d oldVelocity) {
      this.oldVelocity = oldVelocity;
   }
}
