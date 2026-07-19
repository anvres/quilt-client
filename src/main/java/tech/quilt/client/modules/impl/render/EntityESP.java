package tech.quilt.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import org.joml.Vector4f;
import tech.quilt.Quilt;
import tech.quilt.base.events.impl.render.EventRender2D;
import tech.quilt.base.font.Font;
import tech.quilt.base.font.Fonts;
import tech.quilt.client.modules.api.Category;
import tech.quilt.client.modules.api.Module;
import tech.quilt.client.modules.api.ModuleAnnotation;
import tech.quilt.client.modules.api.setting.impl.BooleanSetting;
import tech.quilt.client.modules.api.setting.impl.NumberSetting;
import tech.quilt.client.modules.impl.misc.NameProtect;
import tech.quilt.client.modules.impl.misc.ScoreboardHealth;
import tech.quilt.utility.game.other.ReplaceUtil;
import tech.quilt.utility.game.player.PlayerIntersectionUtil;
import tech.quilt.utility.math.ProjectionUtil;
import tech.quilt.utility.render.display.base.BorderRadius;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(
   name = "NameTags",
   category = Category.RENDER,
   description = "Показывает информацию о игроке"
)
public final class EntityESP extends Module {
   public static final EntityESP INSTANCE = new EntityESP();
   
   // Настройки
   private final NumberSetting maxPlayerDistance = new NumberSetting("Max Player Distance", 0.0F, 64.0F, 256.0F, 1.0F);
   private final NumberSetting maxItemDistance = new NumberSetting("Max Item Distance", 0.0F, 16.0F, 64.0F, 1.0F);
   private final BooleanSetting showOwnNameTag = new BooleanSetting("Показывать свой тег", false);
   private final BooleanSetting showArmor = new BooleanSetting("Показывать броню", true);
   private final BooleanSetting showEnchants = new BooleanSetting("Показывать чары", true);
   
   // Кэшированные объекты
   private final HashMap<Entity, Vector4f> positions = new HashMap<>();
   private Font cachedFont6_5;
   private Font cachedFont6;
   
   // Кэшированные цвета для rare предметов
   private static final Formatting[] RARITY_FORMATTING = {
       Formatting.WHITE,      // COMMON
       Formatting.YELLOW,     // UNCOMMON
       Formatting.AQUA,       // RARE
       Formatting.LIGHT_PURPLE // EPIC
   };
   
   // Кэшированный цвет для кастомных чаров
   private static final int CUSTOM_ENCHANT_COLOR = new ColorRGBA(212, 45, 43, 255).getRGB();

   private EntityESP() {
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      positions.clear();
   }

   private void ensureFontsLoaded() {
      try {
         if (cachedFont6_5 == null) {
            cachedFont6_5 = Fonts.REGULAR.getFont(6.5F);
         }
         if (cachedFont6 == null) {
            cachedFont6 = Fonts.REGULAR.getFont(6.0F);
         }
      } catch (Exception e) {
         // Font loading failed, will retry next frame
      }
   }

   @EventTarget
   private void onRender(EventRender2D e) {
      if (mc.world == null || mc.player == null) return;
      
      ensureFontsLoaded();
      
      float tickDelta = e.getTickDelta();
      renderPlayerTags(tickDelta, e);
      renderItemTags(tickDelta, e);
   }

   private void renderPlayerTags(float tickDelta, EventRender2D e) {
      float maxPlayerDist = maxPlayerDistance.getCurrent();
      double maxPlayerDistSq = (double)maxPlayerDist * (double)maxPlayerDist;
      MatrixStack matrices = e.getContext().getMatrices();
      
      for (PlayerEntity entity : mc.world.getPlayers()) {
         // Пропускаем себя если выключено
         if (entity == mc.player && !showOwnNameTag.isEnabled() && !mc.getEntityRenderDispatcher().camera.isThirdPerson()) {
            continue;
         }
         
         // Проверяем расстояние
         double distSq = mc.player.squaredDistanceTo(entity);
         if (distSq > maxPlayerDistSq) {
            continue;
         }
         
         // Проверяем видимость
         if (!ProjectionUtil.canSee(entity.getBoundingBox().getCenter())) {
            continue;
         }
         
         // Получаем позицию на экране
         double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
         double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.2D;
         double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
         Vec3d screenPos = ProjectionUtil.worldSpaceToScreenSpace(new Vec3d(x, y, z));
         
         if (screenPos.z <= 0.0D || screenPos.z >= 1.0D) {
            continue;
         }
         
         Vector4d position = ProjectionUtil.getVector4D(entity);
         float posY = (float)(position.y - 11.0D);
         
         // HP
         float hp = ScoreboardHealth.INSTANCE.isEnabled() && entity != mc.player 
                 ? PlayerIntersectionUtil.getHealth(entity) 
                 : entity.getHealth();
         
         // Имя
         Text name = entity == mc.player && NameProtect.INSTANCE.isEnabled() 
                 ? Text.literal(NameProtect.getCustomName()) 
                 : ReplaceUtil.replaceSymbols(entity.getDisplayName());
         
         Text nameWithHp = ((Text)name).copy()
                 .append(Text.literal(" [").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                 .append(Text.literal(String.valueOf((int)hp)).setStyle(Style.EMPTY.withColor(Formatting.RED)))
                 .append(Text.literal("]").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
         
         // Ширина текста
         float textWidth = cachedFont6_5.width(nameWithHp);
         
         // Фон
         ColorRGBA bgColor = Quilt.getInstance().getFriendManager().isFriend(entity.getNameForScoreboard()) 
                 ? new ColorRGBA(0, 166, 0, 123) 
                 : new ColorRGBA(0, 0, 0, 123);
         
         DrawUtil.drawRoundedRect(matrices, 
                 (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F) - 3.0D), 
                 posY - 2.5F, 
                 textWidth + 5.0F, 
                 10.0F, 
                 BorderRadius.ZERO, 
                 bgColor);
         
         // Текст
         e.getContext().drawText(cachedFont6_5, nameWithHp, 
                 (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F)), 
                 posY, 
                 255.0F);
         
         // Показываем броню если включено
         if (showArmor.isEnabled()) {
            renderPlayerArmor(entity, position, posY, e, matrices);
         }
      }
   }

   private void renderPlayerArmor(PlayerEntity entity, Vector4d position, float posY, EventRender2D e, MatrixStack matrices) {
      // Собираем предметы брони и рук
      ItemStack[] itemArray = new ItemStack[6];
      int itemCount = 0;
      
      // Броня
      EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
      for (EquipmentSlot slot : armorSlots) {
         ItemStack stack = entity.getEquippedStack(slot);
         if (!stack.isEmpty()) {
            itemArray[itemCount++] = stack;
         }
      }
      
      // Руки
      ItemStack mainHand = entity.getMainHandStack();
      if (!mainHand.isEmpty()) {
         itemArray[itemCount++] = mainHand;
      }
      
      ItemStack offHand = entity.getOffHandStack();
      if (!offHand.isEmpty()) {
         itemArray[itemCount++] = offHand;
      }
      
      if (itemCount == 0) return;
      
      float iconSize = 16.0F;
      float spacing = 0.0F;
      float totalWidth = (float)itemCount * iconSize + (float)(itemCount - 1) * spacing;
      float startX = (float)(position.x + (position.z - position.x) / 2.0D - (double)(totalWidth / 2.0F) + 7.5D);
      float iconY = posY - 12.0F;

      for (int i = 0; i < itemCount; ++i) {
         ItemStack stack = itemArray[i];
         if (stack == null || stack.isEmpty()) continue;
         
         float x2 = startX + (float)i * (iconSize + spacing);
         
         // Рендерим чары если включено
         if (showEnchants.isEnabled()) {
            ItemEnchantmentsComponent enchComp = EnchantmentHelper.getEnchantments(stack);
            if (!enchComp.isEmpty()) {
               float enchantmentY = iconY - 16.0F;
               Map<RegistryEntry<Enchantment>, Integer> enchMap = enchComp.getEnchantmentEntries().stream()
                       .collect(Collectors.toMap(
                           Map.Entry::getKey, 
                           it.unimi.dsi.fastutil.objects.Object2IntMap.Entry::getIntValue
                       ));
               
               for (Map.Entry<RegistryEntry<Enchantment>, Integer> enchEntry : enchMap.entrySet()) {
                  int lvl = enchEntry.getValue();
                  if (lvl <= 0) continue;
                  
                  String fullName = Enchantment.getName(enchEntry.getKey(), lvl).getString();
                  String shortName = fullName.length() > 2 ? fullName.substring(0, 2) : fullName;
                  String enchantmentText = shortName + lvl;
                  float enchantmentTextWidth = cachedFont6.width(enchantmentText);
                  int color = -1;
                  
                  // Специальные цвета для сильных чаров
                  if (shortName.equalsIgnoreCase("Sh") && lvl > 5 || 
                      shortName.equalsIgnoreCase("Pr") && lvl > 4) {
                     color = CUSTOM_ENCHANT_COLOR;
                  }
                  
                  e.getContext().drawText(cachedFont6, enchantmentText, 
                          x2 - enchantmentTextWidth / 2.0F, enchantmentY, 
                          new ColorRGBA(color));
                  enchantmentY -= 8.0F;
               }
            }
         }
         
         // Рендерим иконку предмета
         DrawUtil.drawRoundedRect(matrices, x2 - 7.0F, iconY - 7.0F, 14.0F, 14.0F, 
                 BorderRadius.all(3.0F), new ColorRGBA(0, 0, 0, 123));
         
         float scale = 0.7F;
         matrices.push();
         matrices.translate(x2, iconY, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         
         e.getContext().drawItem(stack, 0, 0);
         e.getContext().drawStackOverlay(mc.textRenderer, stack, 0, 0);
         
         matrices.pop();
      }
   }

   private void renderItemTags(float tickDelta, EventRender2D e) {
      if (mc.player == null) return;
      
      float maxItemDist = maxItemDistance.getCurrent();
      double maxItemDistSq = (double)maxItemDist * (double)maxItemDist;
      MatrixStack matrices = e.getContext().getMatrices();
      
      for (Entity entity : mc.world.getEntities()) {
         if (!(entity instanceof ItemEntity)) continue;
         
         ItemEntity itemEntity = (ItemEntity)entity;
         
         // Проверяем расстояние
         double distSq = mc.player.squaredDistanceTo(entity);
         if (distSq > maxItemDistSq) {
            continue;
         }
         
         // Проверяем видимость
         if (!ProjectionUtil.canSee(itemEntity.getBoundingBox().getCenter())) {
            continue;
         }
         
         // Получаем позицию на экране
         double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
         double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.1D;
         double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
         Vec3d screenPos = ProjectionUtil.worldSpaceToScreenSpace(new Vec3d(x, y, z));
         
         if (screenPos.z <= 0.0D || screenPos.z >= 1.0D) {
            continue;
         }
         
         Vector4d position = ProjectionUtil.getVector4D(entity);
         float posY = (float)(position.y - 11.0D);
         
         ItemStack stack = itemEntity.getStack();
         if (stack.isEmpty()) continue;
         
         // Определяем цвет по rare
         int rarityOrdinal = Math.min(stack.getRarity().ordinal(), RARITY_FORMATTING.length - 1);
         Formatting rarityColor = RARITY_FORMATTING[rarityOrdinal];
         
         String itemName = stack.getName().getString();
         Text nameText = Text.literal(itemName).setStyle(Style.EMPTY.withColor(rarityColor));
         
         // Если есть кастомное имя
         if (!stack.getName().getSiblings().isEmpty()) {
            nameText = stack.getName();
         }
         
         // Добавляем количество
         Text countComponent = stack.getCount() > 1 
                 ? Text.literal(" х" + stack.getCount()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)) 
                 : Text.empty();
         Text textComponent = ((Text)nameText).copy().append(countComponent);
         
         float textWidth = cachedFont6_5.width(textComponent);
         
         DrawUtil.drawRoundedRect(matrices, 
                 (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F) - 3.0D), 
                 (float)(position.y - 13.5D), 
                 textWidth + 4.0F, 
                 10.0F, 
                 BorderRadius.ZERO, 
                 new ColorRGBA(0, 0, 0, 123));
         
         e.getContext().drawText(cachedFont6_5, textComponent, 
                 (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F)), 
                 (float)position.y - 11.0F, 
                 255.0F);
      }
   }

   public static void drawBox(double x, double y, double width, double height, double size, int color, BufferBuilder bufferbuilder) {
      drawRectBuilding(x + size, y, width - size, y + size, color, bufferbuilder);
      drawRectBuilding(x, y, x + size, height, color, bufferbuilder);
      drawRectBuilding(width - size, y, width, height, color, bufferbuilder);
      drawRectBuilding(x + size, height - size, width - size, height, color, bufferbuilder);
   }

   public static void drawBoxTest(double x, double y, double width, double height, double size, Vector4f colors, BufferBuilder bufferbuilder) {
      drawMCHorizontalBuilding(x + size, y, width - size, y + size, (int)colors.x(), (int)colors.y(), bufferbuilder);
      drawMCVerticalBuilding(width - size, y + size, width, height - size, (int)colors.y(), (int)colors.z(), bufferbuilder);
      drawMCHorizontalBuilding(x + size, height - size, width - size, height, (int)colors.w(), (int)colors.z(), bufferbuilder);
      drawMCVerticalBuilding(x, y + size, x + size, height - size, (int)colors.x(), (int)colors.w(), bufferbuilder);
   }

   public static void drawRectBuilding(double left, double top, double right, double bottom, int color, BufferBuilder bufferbuilder) {
      if (left > right) {
         double temp = left;
         left = right;
         right = temp;
      }

      if (top > bottom) {
         double temp = top;
         top = bottom;
         bottom = temp;
      }

      float f3 = (float)(color >> 24 & 255) / 255.0F;
      float f = (float)(color >> 16 & 255) / 255.0F;
      float f1 = (float)(color >> 8 & 255) / 255.0F;
      float f2 = (float)(color & 255) / 255.0F;
      bufferbuilder.vertex((float)left, (float)bottom, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)right, (float)bottom, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)right, (float)top, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)left, (float)top, 0.0F).color(f, f1, f2, f3);
   }

   public static void drawMCHorizontalBuilding(double x1, double y1, double x2, double y2, int start, int end, BufferBuilder bufferbuilder) {
      float a1 = (float)(start >> 24 & 255) / 255.0F;
      float r1 = (float)(start >> 16 & 255) / 255.0F;
      float g1 = (float)(start >> 8 & 255) / 255.0F;
      float b1 = (float)(start & 255) / 255.0F;
      float a2 = (float)(end >> 24 & 255) / 255.0F;
      float r2 = (float)(end >> 16 & 255) / 255.0F;
      float g2 = (float)(end >> 8 & 255) / 255.0F;
      float b2 = (float)(end & 255) / 255.0F;
      bufferbuilder.vertex((float)x1, (float)y2, 0.0F).color(r1, g1, b1, a1);
      bufferbuilder.vertex((float)x2, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y1, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x1, (float)y1, 0.0F).color(r1, g1, b1, a1);
   }

   public static void drawMCVerticalBuilding(double x1, double y1, double x2, double y2, int start, int end, BufferBuilder bufferbuilder) {
      float a1 = (float)(start >> 24 & 255) / 255.0F;
      float r1 = (float)(start >> 16 & 255) / 255.0F;
      float g1 = (float)(start >> 8 & 255) / 255.0F;
      float b1 = (float)(start & 255) / 255.0F;
      float a2 = (float)(end >> 24 & 255) / 255.0F;
      float r2 = (float)(end >> 16 & 255) / 255.0F;
      float g2 = (float)(end >> 8 & 255) / 255.0F;
      float b2 = (float)(end & 255) / 255.0F;
      bufferbuilder.vertex((float)x1, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y1, 0.0F).color(r1, g1, b1, a1);
      bufferbuilder.vertex((float)x1, (float)y1, 0.0F).color(r1, g1, b1, a1);
   }
}
