package tech.quilt.client.hud.elements.component;

import net.minecraft.util.Identifier;
import ru.nexusguard.UserProfile;
import ru.nexusguard.protection.annotations.Native;
import tech.quilt.Quilt;
import tech.quilt.base.font.Fonts;
import tech.quilt.base.theme.Theme;
import tech.quilt.client.hud.elements.draggable.DraggableHudElement;
import tech.quilt.client.modules.impl.misc.NameProtect;
import tech.quilt.utility.render.display.base.BorderRadius;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

public class WatermarkComponent extends DraggableHudElement {
    public WatermarkComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
        super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
    }

    @Native
    public void render(CustomDrawContext ctx) {
        float x = this.getX();
        float y = this.getY();
        Theme theme = Quilt.getInstance().getThemeManager().getCurrentTheme();

        String playerName = NameProtect.INSTANCE.isEnabled()
                ? NameProtect.getCustomName()
                : mc.player.getNameForScoreboard();
        String role = "[" + UserProfile.instance.roleName() + "]";
        String fpsText = mc.getCurrentFps() + "fps";
        String tpsText = String.format("%.1f", Quilt.getInstance().getServerHandler().getTPS()).replace(",", ".") + "tps";
        String serverText = mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null
                ? mc.getCurrentServerEntry().address
                : "Неизвестно";

        float fontSize = 7.25F;
        float iconSize = 6.0F;
        float avatarSize = 9.0F;
        float padH = 6.0F;
        float dotW = 2.0F;
        float dotPad = 5.0F;
        float iconTextGap = 3.5F;
        float height = 15.0F;

        String quiltText = "Quilt";
        float quiltW = Fonts.REGULAR.getWidth(quiltText, fontSize);
        float nameW = Fonts.REGULAR.getWidth(playerName, fontSize);
        float roleW = Fonts.REGULAR.getWidth(role, fontSize);
        float fpsW = Fonts.REGULAR.getWidth(fpsText, fontSize);
        float tpsW = Fonts.REGULAR.getWidth(tpsText, fontSize);
        float serverW = Fonts.REGULAR.getWidth(serverText, fontSize);
        float iconW = 7.0F;

        float totalWidth = padH
                + quiltW
                + dotPad + dotW + dotPad
                + avatarSize + iconTextGap + nameW + 4.0F + roleW
                + dotPad + dotW + dotPad
                + iconW + iconTextGap + fpsW
                + dotPad + dotW + dotPad
                + iconW + iconTextGap + tpsW
                + dotPad + dotW + dotPad
                + iconW + iconTextGap + serverW
                + padH;

        DrawUtil.drawBlur(ctx.getMatrices(), x, y, totalWidth, height, 5.0F, BorderRadius.all(4.0F), new ColorRGBA(14, 16, 18, 220));

        float textY = y + (height - fontSize) / 2.0F - 0.25F;
        float iconY = y + (height - iconSize) / 2.0F;
        float dotY = y + height / 2.0F - dotW / 2.0F;
        float avatarY = y + (height - avatarSize) / 2.0F;

        float cx = x + padH;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), quiltText, cx, textY, new ColorRGBA(255, 255, 255, 255));
        cx += quiltW;

        cx += dotPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, dotY, dotW, dotW, BorderRadius.all(0.5F), new ColorRGBA(160, 160, 160, 140));
        cx += dotW + dotPad;

        Identifier skin = mc.player.getSkinTextures().texture();
        DrawUtil.drawPlayerHeadWithRoundedShader(ctx.getMatrices(), skin, cx, avatarY, avatarSize, BorderRadius.all(2.5F), ColorRGBA.WHITE);
        cx += avatarSize + iconTextGap;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), playerName, cx, textY, new ColorRGBA(255, 255, 255, 255));
        cx += nameW + 4.0F;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), role, cx, textY, new ColorRGBA(160, 160, 160, 200));
        cx += roleW;

        cx += dotPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, dotY, dotW, dotW, BorderRadius.all(0.5F), new ColorRGBA(160, 160, 160, 140));
        cx += dotW + dotPad;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf624", cx, iconY, theme.getColor());
        cx += iconW + iconTextGap;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), fpsText, cx, textY, new ColorRGBA(255, 255, 255, 255));
        cx += fpsW;

        cx += dotPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, dotY, dotW, dotW, BorderRadius.all(0.5F), new ColorRGBA(160, 160, 160, 140));
        cx += dotW + dotPad;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf68f", cx, iconY, theme.getColor());
        cx += iconW + iconTextGap;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), tpsText, cx, textY, new ColorRGBA(255, 255, 255, 255));
        cx += tpsW;

        cx += dotPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, dotY, dotW, dotW, BorderRadius.all(0.5F), new ColorRGBA(160, 160, 160, 140));
        cx += dotW + dotPad;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf0ac", cx, iconY, theme.getColor());
        cx += iconW + iconTextGap;

        ctx.drawText(Fonts.REGULAR.getFont(fontSize), serverText, cx, textY, new ColorRGBA(255, 255, 255, 255));

        this.width = totalWidth;
        this.height = height;
    }
}
