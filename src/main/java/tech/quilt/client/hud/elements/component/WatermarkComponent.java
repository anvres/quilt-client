package tech.quilt.client.hud.elements.component;

import tech.quilt.Quilt;
import tech.quilt.base.font.Fonts;
import tech.quilt.client.hud.elements.draggable.DraggableHudElement;
import tech.quilt.utility.render.display.base.BorderRadius;
import tech.quilt.utility.render.display.base.CustomDrawContext;
import tech.quilt.utility.render.display.base.color.ColorRGBA;
import tech.quilt.utility.render.display.shader.DrawUtil;

public class WatermarkComponent extends DraggableHudElement {
    public WatermarkComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, Align align) {
        super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
    }

    public void render(CustomDrawContext ctx) {
        if (mc.player == null) return;
        float x = getX();
        float y = getY();

        ColorRGBA themeColor = Quilt.getInstance().getThemeManager().getCurrentTheme().getColor();
        ColorRGBA bgOuter = new ColorRGBA(16, 16, 17, 255);
        ColorRGBA bgInner = new ColorRGBA(19, 19, 20, 255);
        ColorRGBA sep = new ColorRGBA(36, 36, 38, 255);

        String clientName = "Quilt";
        String username = mc.player.getNameForScoreboard();
        int fps = mc.getCurrentFps();
        int ping = mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().ping != -1
                ? (int) mc.getCurrentServerEntry().ping : 0;


        float iconSize = 6.0F;
        float fontSize = 7.25F;
        float padH = 8.0F;
        float iconW = 7.0F;
        float iconTextGap = 3.5F;
        float sepPad = 5.0F;
        float sepW = 1.5F;
        float height = 15.0F;

        String fpsStr = fps + "fps";
        String pingStr = ping + "ms";

        float quiltW = mc.textRenderer.getWidth(clientName);
        float nameW = mc.textRenderer.getWidth(username);
        float fpsW = mc.textRenderer.getWidth(fpsStr);
        float pingW = mc.textRenderer.getWidth(pingStr);

        float totalWidth = padH
                + iconW + iconTextGap + quiltW
                + sepPad + sepW + sepPad
                + nameW
                + sepPad + sepW + sepPad
                + iconW + iconTextGap + fpsW
                + sepPad + sepW + sepPad
                + iconW + iconTextGap + pingW
                + padH;

        DrawUtil.drawRoundedRect(ctx.getMatrices(), x - 0.5F, y - 0.5F, totalWidth + 1.0F, height + 1.0F, BorderRadius.all(8.0F), bgOuter);
        DrawUtil.drawRoundedRect(ctx.getMatrices(), x, y, totalWidth, height, BorderRadius.all(8.0F), bgInner);

        float textY = y + (height - mc.textRenderer.fontHeight) / 2.0F;
        float iconY = y + (height - Fonts.ICONS2.getFont(iconSize).height()) / 2.0F;

        float cx = x + padH;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf007", cx, iconY, themeColor);
        cx += iconW + iconTextGap;
        ctx.drawText(mc.textRenderer, clientName, (int)cx, (int)textY, 0xFFFFFFFF, false);
        cx += quiltW + sepPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, y + (height - 7.0F) / 2.0F, sepW, 7.0F, BorderRadius.all(0.5F), sep);
        cx += sepW + sepPad;

        ctx.drawText(mc.textRenderer, username, (int)cx, (int)textY, 0xFFFFFFFF, false);
        cx += nameW + sepPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, y + (height - 7.0F) / 2.0F, sepW, 7.0F, BorderRadius.all(0.5F), sep);
        cx += sepW + sepPad;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf624", cx, iconY, themeColor);
        cx += iconW + iconTextGap;
        ctx.drawText(mc.textRenderer, fpsStr, (int)cx, (int)textY, 0xFFFFFFFF, false);
        cx += fpsW + sepPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, y + (height - 7.0F) / 2.0F, sepW, 7.0F, BorderRadius.all(0.5F), sep);
        cx += sepW + sepPad;

        ctx.drawText(Fonts.ICONS2.getFont(iconSize), "\uf0ac", cx, iconY, themeColor);
        cx += iconW + iconTextGap;
        ctx.drawText(mc.textRenderer, pingStr, (int)cx, (int)textY, 0xFFFFFFFF, false);
        cx += pingW + sepPad;
        DrawUtil.drawRoundedRect(ctx.getMatrices(), cx, y + (height - 7.0F) / 2.0F, sepW, 7.0F, BorderRadius.all(0.5F), sep);
        cx += sepW + sepPad;



        this.width = totalWidth;
        this.height = height;
    }


}
