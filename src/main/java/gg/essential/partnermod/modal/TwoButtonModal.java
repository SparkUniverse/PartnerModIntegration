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

package gg.essential.partnermod.modal;

import gg.essential.partnermod.Draw;
import gg.essential.partnermod.EssentialPartner;
import gg.essential.partnermod.EssentialUtil;
import gg.essential.partnermod.UDesktop;
import gg.essential.partnermod.UResolution;
import net.minecraft.client.Minecraft;

public class TwoButtonModal extends Modal {

    private final String text;
    private final ButtonProducer leftButtonProducer;
    private final ButtonProducer rightButtonProducer;

    public TwoButtonModal(String text, ButtonProducer leftButtonProducer, ButtonProducer rightButtonProducer) {
        this.text = text;
        this.leftButtonProducer = leftButtonProducer;
        this.rightButtonProducer = rightButtonProducer;
    }

    @Override
    public void init() {
        super.init();

        int textLines = text.split("\n").length;
        int textHeight = 10 * textLines + 2 * (textLines - 1);

        setDimensions(224, 70 + textHeight);

        int buttonY = startY + 20 + textHeight + 17;
        buttonList.add(leftButtonProducer.make(centreX - 95, buttonY, 91));
        buttonList.add(rightButtonProducer.make(centreX + 4, buttonY, 91));
    }

    @Override
    public void draw(Draw draw) {
        super.draw(draw);

        draw.multilineCentredString(text, centreX, startY + 20, 12, 0xFFe5e5e5, 0xFF000000);
    }

    public static TwoButtonModal postInstall() {
        return new TwoButtonModal(
            "Essential Mod will install the next time\nyou launch the game.",
            (x, y, width) -> {
                ModalButton button = new ModalButton(x, y, width, ButtonColor.GRAY, "Quit & Install", () -> {
                    ModalManager.INSTANCE.setModal(null);
                    EssentialUtil.shutdown();
                });
                button.setTooltip("This will close your game!");
                return button;
            },
            (x, y, width) -> new ModalButton(x, y, width, ButtonColor.BLUE, "Okay", () -> ModalManager.INSTANCE.setModal(null))
        );
    }

    public static TwoButtonModal removeAds(AdModal adModal) {
        boolean[] removed = new boolean[1];
        return new TwoButtonModal(
            "Do you want to remove all\n'Get Essential Mod' buttons?",
            (x, y, width) -> new ModalButton(x, y, width, ButtonColor.GRAY, "Back", () -> {
                ModalManager.INSTANCE.setModal(null);
            }),
            (x, y, width) -> new ModalButton(x, y, width, ButtonColor.RED, "Remove", () -> {
                EssentialPartner.CONFIG.hideButtons();
                removed[0] = true;
                ModalManager.INSTANCE.setModal(null);
                //#if MC>=12111
                //$$ MinecraftClient.getInstance().currentScreen.resize(UResolution.getScaledWidth(), UResolution.getScaledHeight());
                //#else
                Minecraft.getMinecraft().currentScreen.onResize(Minecraft.getMinecraft(), UResolution.getScaledWidth(), UResolution.getScaledHeight());
                //#endif
            })
        ) {
            @Override
            public void close() {
                super.close();
                if (!removed[0]) {
                    ModalManager.INSTANCE.setModal(adModal);
                }
            }
        };
    }

    public static TwoButtonModal installFailed() {
        return new TwoButtonModal(
            "An issue occurred while installing\nEssential Mod. Download it from\nour website instead.",
            (x, y, width) -> new ModalButton(x, y, width, ButtonColor.GRAY, "Okay", () -> {
                ModalManager.INSTANCE.setModal(null);
            }),
            (x, y, width) -> new ModalButton(x, y, width, ButtonColor.BLUE, "Download", () -> {
                UDesktop.browse("https://essential.gg/download");
                ModalManager.INSTANCE.setModal(null);
            })
        );
    }

    public interface ButtonProducer {
        ModalButton make(int x, int y, int width);
    }
}
