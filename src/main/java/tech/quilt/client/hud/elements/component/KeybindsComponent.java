package tech.quilt.client.hud.elements.component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screen.ChatScreen;
import tech.quilt.utility.render.display.Keyboard;
import tech.quilt.Quilt;
import tech.quilt.base.animations.base.Animation;
import tech.quilt.base.animations.base.Easing;
import tech.quilt.base.font.Fonts;
import tech.quilt.base.theme.Theme;
import tech.quilt.client.hud.elements.draggable.DraggableHudElement;
import tech.quilt.client.modules.api.Module;
import tech.quilt.utility.render.display.base.BorderRadius;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

public class KeybindsComponent extends DraggableHudElement {
    private final Animation alpha = new Animation(200L, Easing.CUBIC_OUT);
    private final Animation widthAnim = new Animation(200L, Easing.CUBIC_OUT);
    private final Animation heightAnim = new Animation(200L, Easing.CUBIC_OUT);
    private final Map<Module, Animation> moduleAnims = new HashMap<>();

    public KeybindsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, Align align) {
        super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
    }

    public void render(CustomDrawContext ctx) {
        if (mc.player == null) return;

        float x = getX();
        float y = getY();
        Theme theme = Quilt.getInstance().getThemeManager().getCurrentTheme();
        boolean inChat = mc.currentScreen instanceof ChatScreen;

        List<Module> modules = Quilt.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.getKeyCode() != -100)
                .sorted(Comparator.comparing(Module::getName))
                .collect(Collectors.toList());

        boolean hasVisible = inChat || modules.stream().anyMatch(m -> m.isEnabled());
        alpha.update(hasVisible ? 1.0F : 0.0F);
        float a = alpha.getValue();
        if (a <= 0.01F) {
            this.width = 0;
            this.height = 0;
            return;
        }

        for (Module m : modules) {
            Animation anim = moduleAnims.computeIfAbsent(m, k -> new Animation(250L, Easing.CUBIC_OUT));
            anim.update(m.isEnabled() ? 1.0F : 0.0F);
        }
        moduleAnims.keySet().removeIf(m -> !modules.contains(m));

        float headerH = 14.0F;
        float itemH = 11.0F;
        float maxW = 75.0F;
        float contentH = 0;

        for (Module m : modules) {
            Animation anim = moduleAnims.get(m);
            float v = anim != null ? anim.getValue() : (m.isEnabled() ? 1.0F : 0.0F);
            if (v > 0.01F) {
                float keyW = Fonts.REGULAR.getWidth(getKeyName(m.getKeyCode()), 6.5F);
                float nameW = Fonts.REGULAR.getWidth(m.getName(), 6.5F);
                float totalW = nameW + keyW + 12.0F;
                if (totalW > maxW) maxW = totalW;
                contentH += itemH * v;
            }
        }

        widthAnim.update(maxW + 8.0F);
        float w = widthAnim.getValue();
        float totalH = headerH + contentH;

        DrawUtil.drawBlur(ctx.getMatrices(), x, y, w, headerH, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * a));
        DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 12.0F, y + 1.0F, 0.5F, headerH - 1.5F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * a));
        ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uF11C", x + 3.0F, y + 4.5F, theme.getColor().withAlpha(255.0F * a));
        ctx.drawText(Fonts.SEMIBOLD.getFont(6.75F), "\u0411\u0438\u043D\u0434\u044B", x + 16.0F, y + 4.0F, new ColorRGBA(255, 255, 255, (int)(255.0F * a)));

        float cy = y + headerH;
        for (Module m : modules) {
            Animation anim = moduleAnims.get(m);
            float v = anim != null ? anim.getValue() : (m.isEnabled() ? 1.0F : 0.0F);
            if (v <= 0.01F) continue;

            String keyName = getKeyName(m.getKeyCode());
            float keyW = Fonts.REGULAR.getWidth(keyName, 6.5F);

            ctx.drawText(Fonts.REGULAR.getFont(6.5F), m.getName(), x + 4.0F, cy + 2.5F, ColorRGBA.WHITE.withAlpha(v * 255.0F * a));
            ctx.drawText(Fonts.REGULAR.getFont(6.5F), keyName, x + w - keyW - 4.0F, cy + 2.5F, theme.getColor().withAlpha(v * 255.0F * a));

            cy += itemH * v;
        }

        this.width = w;
        this.height = totalH;
    }

    private String getKeyName(int keyCode) {
        if (keyCode == -1) return "\u041D\u0435\u0442";
        try {
            return Keyboard.getKeyName(keyCode).toUpperCase();
        } catch (Exception e) {
            return "\u041A" + keyCode;
        }
    }
}
