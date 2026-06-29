package tech.quilt.client.hud.elements.component;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.nexusguard.protection.annotations.Native;
import tech.quilt.Quilt;
import tech.quilt.base.animations.base.Animation;
import tech.quilt.base.animations.base.Easing;
import tech.quilt.base.events.impl.other.EventWindowResize;
import tech.quilt.base.font.Fonts;
import tech.quilt.base.theme.Theme;
import tech.quilt.client.hud.elements.draggable.DraggableHudElement;
import tech.quilt.utility.render.display.base.BorderRadius;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

public class InformationComponent extends DraggableHudElement {
   private final Animation yAnimation;

   public InformationComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.yAnimation = new Animation(200L, Easing.CUBIC_OUT);
      EventManager.register(this);
   }

   @EventTarget
   private void onWindowResized(EventWindowResize e) {
      if (mc.currentScreen instanceof ChatScreen) {
         this.yAnimation.setValue((float)(mc.getWindow().getScaledHeight() - 15));
         this.yAnimation.setStartValue((float)(mc.getWindow().getScaledHeight() - 15));
      } else {
         this.yAnimation.setStartValue((float)mc.getWindow().getScaledHeight());
         this.yAnimation.setValue((float)mc.getWindow().getScaledHeight());
      }

   }

   @Native
   public void render(CustomDrawContext ctx) {
      Theme theme = Quilt.getInstance().getThemeManager().getCurrentTheme();
      if (mc.currentScreen instanceof ChatScreen) {
         this.yAnimation.update((float)(mc.getWindow().getScaledHeight() - 15));
      } else {
         this.yAnimation.update((float)mc.getWindow().getScaledHeight());
      }

      if (mc.player == null) return;

      int px = (int)Math.floor(mc.player.getX());
      int py = (int)Math.floor(mc.player.getY());
      int pz = (int)Math.floor(mc.player.getZ());
      double speed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
      String coordsText = String.format(Locale.US, "%d %d %d", px, py, pz);
      String speedText = String.format("%.2f", speed * 20.0D).replace(",", ".") + " Б/С";

      float iconSize = 7.5F;
      float iconW = 7.5F;
      float iconTextGap = 4.0F;
      float dotSep = 5.0F;
      float padL = 7.75F;
      float padR = 7.0F;

      float coordW = Fonts.REGULAR.getWidth(coordsText, 7.5F);
      float speedW = Fonts.REGULAR.getWidth(speedText, 7.5F);
      float totalW = padL + iconW + iconTextGap + coordW + dotSep + 2.0F + dotSep + iconW + iconTextGap + speedW + padR;

      float yPos = this.yAnimation.getValue();
      float bgY = yPos - 17.0F;
      float textY = yPos - 12.5F;
      float dotY = yPos - 11.0F;

      DrawUtil.drawBlur(ctx.getMatrices(), 4.0F, bgY, totalW, 14.0F, 11.0F, BorderRadius.all(2.0F), new ColorRGBA(80, 80, 80, 255));

      float cx = padL;
      ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf57d", cx, textY, theme.getColor());
      cx += iconW + iconTextGap;
      ctx.drawText(Fonts.REGULAR.getFont(7.5F), coordsText, cx, textY, ColorRGBA.WHITE);
      cx += coordW + dotSep;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, dotY, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      cx += dotSep + 2.0F;
      ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf70c", cx, textY, theme.getColor());
      cx += iconW + iconTextGap;
      ctx.drawText(Fonts.REGULAR.getFont(7.5F), speedText, cx, textY, ColorRGBA.WHITE);
   }
}
