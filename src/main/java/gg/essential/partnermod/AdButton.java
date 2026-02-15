/*
 * Copyright © 2025 ModCore Inc. All rights reserved.
 *
 * This code is part of ModCore Inc.’s Essential Partner Mod Integration
 * repository and is protected under copyright. For the full license, see:
 * https://github.com/EssentialGG/EssentialPartnerMod/tree/main/LICENSE
 *
 * You may modify, fork, and use the Mod, but may not retain ownership of
 * accepted contributions, claim joint ownership, or use Essential’s trademarks.
 */

package gg.essential.partnermod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

//#if MC>=12106
//$$ import net.minecraft.client.gl.RenderPipelines;
//#elseif MC>12102
//$$ import net.minecraft.client.render.RenderLayer;
//#endif

//#if MC>=11600
//$$ import com.mojang.blaze3d.matrix.MatrixStack;
//#endif

//#if MC>=11700
//$$ import net.minecraft.client.render.GameRenderer;
//#endif

//#if MC>=11900
//$$ import net.minecraft.text.Text;
//#elseif MC>=11600
//$$ import net.minecraft.util.text.StringTextComponent;
//#endif

//#if MC>=12000
//$$ import net.minecraft.client.gui.DrawContext;
//#endif

//#if MC>=12104
//$$ import net.minecraft.util.math.ColorHelper;
//#endif

public class AdButton extends GuiButton {

    public static ResourceLocation TEXTURE_MAIN_MENU = Resources.load("button_mainmenu.png");
    public static ResourceLocation TEXTURE_MULTIPLAYER = Resources.load("button_multiplayer.png");
    public static ResourceLocation TEXTURE_SINGLEPLAYER = Resources.load("button_singleplayer.png");

    // Note: Other mods may rely on this label to identify the button.
    private static final String LABEL = "<essential_partner_integration_button>";
    private static final int BUTTON_ID = 0xe4c164f1;

    private final ResourceLocation texture;
    private final String tooltip;
    //#if MC<11600
    private final Consumer<GuiButton> onPress;
    //#endif

    public AdButton(int x, int y, ResourceLocation texture, Consumer<GuiButton> onPress, String tooltip) {
        //#if MC>=11903
        //$$ super(x, y, 20, 20, net.minecraft.text.Text.literal(LABEL), onPress::accept, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        //#elseif MC>=11900
        //$$ super(x, y, 20, 20, Text.literal(LABEL), onPress::accept);
        //#elseif MC>=11600
        //$$ super(x, y, 20, 20, new StringTextComponent(LABEL), onPress::accept);
        //#else
        super(BUTTON_ID, x, y, 20, 20, LABEL);
        //#endif
        this.texture = texture;
        this.tooltip = tooltip;
        //#if MC<11600
        this.onPress = onPress;
        //#endif
    }

    //#if MC>=11600
    //$$ @Override
    //#if MC>=12000
    //$$ public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks) {
    //#else
    //$$ public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    //#endif
    //$$     if (visible) {
            //#if MC>=12000
            //#elseif MC>=11700
            //$$ RenderSystem.setShader(GameRenderer::getPositionTexShader);
            //$$ RenderSystem.setShaderTexture(0, texture);
            //#else
            //$$ Minecraft.getInstance().getTextureManager().bindTexture(texture);
            //#endif
    //$$         int x = 0;
    //$$         if (this.isHovered()) x += this.width;
    //$$
            //#if MC>=12104
            //#elseif MC>=11700
            //$$ RenderSystem.setShaderColor(1f, 1f, 1f, this.alpha);
            //#else
            //$$ RenderSystem.color4f(1f, 1f, 1f, this.alpha);
            //#endif
    //$$
            //#if MC>=12106
            //$$ context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), x, 0, width, height, width * 2, height, ColorHelper.getWhite(this.alpha));
            //#elseif MC>=12105
            //$$ context.drawTexture(RenderLayer::getGuiTextured, texture, this.getX(), this.getY(), x, 0, width, height, width * 2, height, ColorHelper.getWhite(this.alpha));
            //#elseif MC>=12102
            //$$ context.drawTexture(RenderLayer::getGuiTextured, texture, this.getX(), this.getY(), x, 0, width, height, width * 2, height);
            //#elseif MC>=12000
            //$$ context.drawTexture(texture, this.getX(), this.getY(), 0, x, 0, width, height, width * 2, height);
            //#elseif MC>=11903
            //$$ drawTexture(matrixStack, this.getX(), this.getY(), 0, x, 0, width, height, width * 2, height);
            //#elseif MC>=11800
            //$$ drawTexture(matrixStack, this.x, this.y, 0, x, 0, width, height, width * 2, height);
            //#else
            //$$ // Note: textureWidth/Height are flipped in MC code for this overload until 1.18
            //$$ blit(matrixStack, this.x, this.y, 0, x, 0, width, height, height, width * 2);
            //#endif
    //$$         if (this.isHovered()) {
                //#if MC>=12000
                //$$ Draw.deferred(it -> drawTooltip(it, MinecraftClient.getInstance(), TooltipPosition.ABOVE));
                //#else
                //$$ Draw.deferred(it -> drawTooltip(it, Minecraft.getInstance(), TooltipPosition.ABOVE));
                //#endif
    //$$         }
    //$$     }
    //$$ }
    //#else
    @Override
    public void drawButton(
        Minecraft mc, int mouseX, int mouseY
        //#if MC>=11200
        , float partialTicks
        //#endif
    ) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int x = 0;
            if (hovered) x += this.width;

            Draw draw = new Draw(mouseX, mouseY);
            draw.texturedRect(texture, this.x, this.y, this.width, this.height, x, 0, this.width * 2, this.height);

            if (hovered) {
                Draw.deferred(it -> drawTooltip(it, mc, TooltipPosition.ABOVE));
            }
        }
    }
    //#endif

    private void drawTooltip(Draw draw, Minecraft mc, TooltipPosition position) {
        String[] lines = this.tooltip.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, mc.fontRenderer.getStringWidth(line));
        }

        int width = maxWidth + 8;
        int height = 10 * lines.length + 4;

        int buttonX = UButton.getX(this);
        int buttonY = UButton.getY(this);

        int windowPadding = 2; // Note: actual padding is one less because of tooltip outline

        int x;
        int y;
        switch (position) {
            case ABOVE:
                x = buttonX + this.width / 2 - width / 2;
                y = buttonY - height - 5;
                if (y < windowPadding) {
                    position = TooltipPosition.BELOW;
                    y = buttonY + this.height + 5;
                }
                break;
            case BELOW:
                x = buttonX + this.width / 2 - width / 2;
                y = buttonY + this.height + 5;
                if (y + height > UResolution.getScaledHeight() - windowPadding) {
                    position = TooltipPosition.ABOVE;
                    y = buttonY - height - 5;
                }
                break;
            case LEFT:
                x = buttonX - width - 5;
                y = buttonY + this.height / 2 - height / 2;
                if (x < windowPadding) {
                    position = TooltipPosition.RIGHT;
                    x = buttonX + this.width + 5;
                }
                break;
            case RIGHT:
                x = buttonX + this.width + 5;
                y = buttonY + this.height / 2 - height / 2;
                if (x + width > UResolution.getScaledWidth() - windowPadding) {
                    position = TooltipPosition.LEFT;
                    x = buttonX - width - 5;
                }
                break;
            default:
                throw new IllegalStateException();
        }

        switch (position) {
            case ABOVE:
            case BELOW:
                x = Math.max(windowPadding, Math.min(x, UResolution.getScaledWidth() - windowPadding - width));
                break;
            case LEFT:
            case RIGHT:
                y = Math.max(windowPadding, Math.min(y, UResolution.getScaledHeight() - windowPadding - height));
                break;
        }

        int centerX = x + width / 2;
        int centerY = y + height / 2;

        draw.rect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        draw.rect(x, y, x + width, y + height, 0xFF232323);

        int textY = y;
        for (String line : lines) {
            int lineWidth = mc.fontRenderer.getStringWidth(line);
            draw.string(line, centerX - lineWidth / 2, textY + 3, 0xFFE5E5E5, 0xFF000000);
            textY += 10;
        }

        // The notch is always centered on the button, regardless of how the tooltip is offset
        centerX = buttonX + this.width / 2;
        centerY = buttonY + this.height / 2;

        for (int i = 0; i <= 2; i++) {
            switch (position) {
                case ABOVE:
                    draw.rect(centerX - (2 - i) - 1, y + height + i + 1, centerX + (2 - i), y + height + i + 2, 0xFF000000);
                    draw.rect(centerX - (2 - i) - 1, y + height + i, centerX + (2 - i), y + height + i + 1, 0xFF232323);
                    break;
                case BELOW:
                    draw.rect(centerX - (2 - i) - 1, y - i - 2, centerX + (2 - i), y - i - 1, 0xFF000000);
                    draw.rect(centerX - (2 - i) - 1, y - i - 1, centerX + (2 - i), y - i, 0xFF232323);
                    break;
                case LEFT:
                    draw.rect(x + width + i + 1, centerY - (2 - i) - 1, x + width + i + 2, centerY + (2 - i), 0xFF000000);
                    draw.rect(x + width + i, centerY - (2 - i) - 1, x + width + i + 1, centerY + (2 - i), 0xFF232323);
                    break;
                case RIGHT:
                    draw.rect(x - i - 2, centerY - (2 - i) - 1, x - i - 1, centerY + (2 - i), 0xFF000000);
                    draw.rect(x - i - 1, centerY - (2 - i) - 1, x - i, centerY + (2 - i), 0xFF232323);
                    break;
            }
        }
    }

    enum TooltipPosition {
        ABOVE,
        BELOW,
        LEFT,
        RIGHT,
    }

    //#if MC<11600
    public void onPress() {
        onPress.accept(this);
    }
    //#endif
}
