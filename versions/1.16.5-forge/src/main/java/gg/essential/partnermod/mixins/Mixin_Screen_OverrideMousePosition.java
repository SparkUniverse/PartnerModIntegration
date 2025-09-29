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

package gg.essential.partnermod.mixins;

import gg.essential.partnermod.modal.ModalManager;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameRenderer.class)
public class Mixin_Screen_OverrideMousePosition {

    @Unique
    private ModalManager.DrawEvent event;

    @ModifyVariable(
        method = "updateCameraAndRender",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Rendering overlay")),
        ordinal = 0
    )
    public int captureMouseX(int mouseX) {
        event = new ModalManager.DrawEvent(mouseX, -1);
        return mouseX;
    }

    @ModifyVariable(
        method = "updateCameraAndRender",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Rendering overlay")),
        ordinal = 1
    )
    public int captureMouseY(int mouseY) {
        event = new ModalManager.DrawEvent(event.mouseX, mouseY);
        ModalManager.INSTANCE.handleMousePos(event);
        return mouseY;
    }

    // FIXME these technically aren't correct, since we don't transform from raw to mc coordinates,
    //       but that doesn't matter for the FAKE_MOUSE_POS constant

    @ModifyVariable(
        method = "updateCameraAndRender",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Rendering overlay")),
        ordinal = 0
    )
    public int modifyMouseX(int mouseX) {
        if (event.mouseXChanged()) {
            return event.mouseX;
        }
        return mouseX;
    }

    @ModifyVariable(
        method = "updateCameraAndRender",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=Rendering overlay")),
        ordinal = 0
    )
    public int modifyMouseY(int mouseY) {
        if (event.mouseYChanged()) {
            return event.mouseY;
        }
        return mouseY;
    }
}
