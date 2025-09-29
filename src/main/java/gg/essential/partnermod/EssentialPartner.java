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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.essential.partnermod.data.PartnerModData;
import gg.essential.partnermod.modal.AdModal;
import gg.essential.partnermod.modal.ModalManager;
import gg.essential.partnermod.modal.TwoButtonModal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

//#if MC>=11600
//$$ import net.minecraft.client.gui.widget.Widget;
//$$ import net.minecraft.util.SharedConstants;
//#endif

//#if NEOFORGE
//$$ import net.neoforged.neoforge.common.NeoForge;
//$$ import net.neoforged.bus.api.SubscribeEvent;
//$$ import net.neoforged.neoforge.client.event.ScreenEvent;
//#endif

//#if FORGE
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//#if MC>=11600
//#else
import net.minecraftforge.common.ForgeVersion;
//#endif

//#if MC>=11800
//$$ import net.minecraftforge.client.event.ScreenEvent;
//#else
import net.minecraftforge.client.event.GuiScreenEvent;
//#endif
//#endif

//#if FABRIC
//$$ import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//$$ import net.fabricmc.fabric.api.client.screen.v1.Screens;
//#endif

public class EssentialPartner {

    public static final Logger LOGGER = LogManager.getLogger("EssentialPartnerMod");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final PartnerModConfig CONFIG = PartnerModConfig.load();

    private static final Set<String> FABRIC_SUPPORTED_VERSIONS = new HashSet<>(Arrays.asList(
        "1.16.5",
        "1.17.1",
        "1.18", "1.18.1", "1.18.2",
        "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.4", "1.20.6",
        "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9"
    ));

    private static final Set<String> NEOFORGE_SUPPORTED_VERSIONS = new HashSet<>(Arrays.asList(
        "1.20.4", "1.20.6",
        "1.21.1", "1.21.3", "1.21.4", "1.21.5"
    ));

    private static final Set<String> FORGE_SUPPORTED_VERSIONS = new HashSet<>(Arrays.asList(
        "1.8.9",
        "1.12.2",
        "1.16.5",
        "1.17.1",
        "1.18.2",
        "1.19.2", "1.19.3", "1.19.4",
        "1.20.1", "1.20.2", "1.20.4", "1.20.6",
        "1.21.1", "1.21.3", "1.21.4", "1.21.5"
    ));

    private static final Set<String> MAIN_MENU_BUTTONS = new HashSet<>(Collections.singletonList("menu.multiplayer"));
    private static final Set<String> PAUSE_MENU_BUTTONS = new HashSet<>(Arrays.asList(
        //#if MC>=12100
        //$$ "menu.server_links", // 1.21+, replaces the report bugs buttons when a server uses server links
        //#endif
        //#if MC>=11600
        //$$ "menu.reportBugs" // 1.16+
        //#else
        "menu.shareToLan" // 1.12.2 and 1.8.9
        //#endif
    ));

    private CompletableFuture<PartnerModData> partnerModDataFuture = null;
    private List<PartnerModData.PartnerMod> partnerMods = null;

    public EssentialPartner() {
        if (EssentialUtil.isEssentialOrContainerLoaded()) return;
        if (CONFIG.shouldHideButtons()) return;

        //#if MC>=11600
        //$$ String version = SharedConstants.getVersion().getId();
        //#else
        String version;
        try {
            // Accessing via reflection so the compiler does not inline the value at build time.
            version = (String) ForgeVersion.class.getDeclaredField("mcVersion").get(null);
        } catch (Exception e) {
            LOGGER.error("Failed to determine Minecraft version", e);
            return;
        }
        //#endif

        //#if FABRIC
        //$$ if (!FABRIC_SUPPORTED_VERSIONS.contains(version)) {
        //#elseif NEOFORGE
        //$$ if (!NEOFORGE_SUPPORTED_VERSIONS.contains(version)) {
        //#else
        if (!FORGE_SUPPORTED_VERSIONS.contains(version)) {
        //#endif
            LOGGER.info("Minecraft version {} is not supported by Essential, disabling Partner Mod", version);
            return;
        }

        //#if NEOFORGE
        //$$ NeoForge.EVENT_BUS.register(this);
        //$$ NeoForge.EVENT_BUS.register(ModalManager.INSTANCE);
        //#elseif FORGE
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ModalManager.INSTANCE);
        //#else
        //$$ ScreenEvents.AFTER_INIT.register(this::afterScreenInit);
        //$$ ModalManager.INSTANCE.registerEvents();
        //#endif

        partnerModDataFuture = EssentialAPI.fetchPartnerModData();
    }

    private void createButton(
        GuiScreen screen,
        //#if MC>=11600
        //$$ List<Widget> buttonList,
        //#else
        List<GuiButton> buttonList,
        //#endif
        BiConsumer<GuiButton, Integer> adder
    ) {
        if (EssentialUtil.isEssentialOrContainerLoaded()) return;
        if (CONFIG.shouldHideButtons()) return;

        int width = screen.width;
        int height = screen.height;

        int buttonSpacing = 4;

        ResourceLocation texture;
        String tooltip;
        int x;
        int y;
        int index = buttonList.size();
        if (screen instanceof GuiMainMenu) {
            texture = AdButton.TEXTURE_MAIN_MENU;
            tooltip = "Enhance your Minecraft\nwith Essential Mod";

            GuiButton multiplayerButton = UButton.findButton(buttonList, MAIN_MENU_BUTTONS);

            if (multiplayerButton != null) {
                x = UButton.getX(multiplayerButton) + UButton.getWidth(multiplayerButton) + buttonSpacing;
                y = UButton.getY(multiplayerButton);
                index = buttonList.indexOf(multiplayerButton) + 1;
            } else {
                // Fallback, position using vanilla positioning
                x = width / 2 + 100 + buttonSpacing;
                y = height / 4 + 48 + 24;
            }
        } else if (screen instanceof GuiIngameMenu) {
            if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                // On a server
                texture = AdButton.TEXTURE_MULTIPLAYER;
                tooltip = "Stay connected with your friends,\nall inside of Minecraft";
            } else {
                // In singleplayer
                texture = AdButton.TEXTURE_SINGLEPLAYER;
                tooltip = "Host worlds for free,\nand play with your friends";
            }

            GuiButton reportBugs = UButton.findButton(buttonList, PAUSE_MENU_BUTTONS);

            if (reportBugs != null) {
                x = UButton.getX(reportBugs) + UButton.getWidth(reportBugs) + buttonSpacing;
                y = UButton.getY(reportBugs);
                index = buttonList.indexOf(reportBugs) + 1;
            } else {
                // Fallback, position using vanilla positioning
                x = width / 2 + 100 + buttonSpacing;
                y = height / 4 + 72 - 16;
            }
        } else {
            return;
        }

        // If we overlap with another button, move to the right, repeatedly
        // If we can't find a free space, try moving to the left instead
        boolean movingRight = true;
        boolean foundFreeSpace;
        do {
            foundFreeSpace = true;
            //#if MC>=11600
            //$$ for (Widget widget : buttonList) {
            //$$     if (!(widget instanceof Button)) continue;
            //$$     Button other = (Button) widget;
            //#else
            for (GuiButton other : buttonList) {
            //#endif
                if (overlaps(x, y, 20, 20, other)) {
                    foundFreeSpace = false;
                    if (movingRight) {
                        int newX = UButton.getX(other) + UButton.getWidth(other) + buttonSpacing;
                        if (newX + 20 <= width) {
                            x = newX;
                        } else {
                            movingRight = false;
                        }
                    } else {
                        int newX = UButton.getX(other) - buttonSpacing - 20;
                        if (newX >= 0) {
                            x = newX;
                        } else {
                            // give up
                            foundFreeSpace = true;
                        }
                    }
                    break;
                }
            }
        } while (!foundFreeSpace);

        adder.accept(new AdButton(x, y, texture, button ->
        {
            //#if MC>=11600
            //$$ // Delay since the element is focused after the click is processed.
            //$$ Minecraft.getInstance().enqueue(() -> screen.setListener(null));
            //#endif
            if (EssentialUtil.installationCompleted()) {
                ModalManager.INSTANCE.setModal(TwoButtonModal.postInstall());
            } else {
                PartnerModData data;
                try {
                    data = partnerModDataFuture.join();
                } catch (CompletionException e) {
                    // This should only happen if the fallback data fails to load, which shouldn't happen.
                    ModalManager.INSTANCE.setModal(TwoButtonModal.installFailed());
                    return;
                }
                ModalManager.INSTANCE.setModal(new AdModal(data.getModal(), getPartnerMods(data)));
            }
        }, tooltip), index);
    }

    private boolean overlaps(int x1, int y1, int w1, int h1, GuiButton button) {
        int x2 = UButton.getX(button);
        int y2 = UButton.getY(button);
        int w2 = UButton.getWidth(button);
        int h2 = UButton.getHeight(button);
        return x1 < x2 + w2 && x2 < x1 + w1 && y1 < y2 + h2 && y2 < y1 + h1;
    }

    //#if FORGELIKE
    @SubscribeEvent
    public void screenInitEvent(
        //#if MC>=11900
        //$$ ScreenEvent.Init.Post event
        //#elseif MC>=11800
        //$$ ScreenEvent.InitScreenEvent.Post event
        //#else
        GuiScreenEvent.InitGuiEvent.Post event
        //#endif
    ) {
        //#if MC>=11800
        //$$ createButton(event.getScreen(), event.getListenersList().stream().filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast).toList(), ((button, __) -> event.addListener(button)));
        //#elseif MC>=11700
        //$$ createButton(event.getGui(), event.getWidgetList().stream().filter(AbstractWidget.class::isInstance).map(AbstractWidget.class::cast).toList(), ((button, __) -> event.addWidget(button)));
        //#elseif MC>=11600
        //$$ createButton(event.getGui(), event.getWidgetList(), ((button, __) -> event.addWidget(button)));
        //#elseif MC>=11200
        createButton(event.getGui(), event.getButtonList(), ((button, idx) -> event.getButtonList().add(idx, button)));
        //#else
        //$$ createButton(event.gui, event.buttonList, ((button, idx) -> event.buttonList.add(idx, button)));
        //#endif
    }
    //#else
    //$$ private void afterScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
    //$$     createButton(screen, Screens.getButtons(screen), (button, idx) -> {
    //$$         Screens.getButtons(screen).add(idx, button);
    //$$     });
    //$$ }
    //#endif

    //#if MC<11600
    @SubscribeEvent
    public void buttonPressed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        //#if MC>=11200
        GuiButton button = event.getButton();
        //#else
        //$$ GuiButton button = event.button;
        //#endif
        if (button instanceof AdButton) {
            ((AdButton) button).onPress();
        }
    }
    //#endif

    private List<PartnerModData.PartnerMod> getPartnerMods(PartnerModData data) {
        if (partnerMods != null) return partnerMods;
        return partnerMods = data.getPartneredMods().stream()
            .filter(mod -> ModLoaderUtil.isModLoaded(mod.getId()))
            .collect(Collectors.toList());
    }

}
