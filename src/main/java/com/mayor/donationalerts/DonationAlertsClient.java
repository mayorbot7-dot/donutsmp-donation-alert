package com.mayor.donationalerts;

import com.mayor.donationalerts.config.ModConfig;
import com.mayor.donationalerts.donation.DonationManager;
import com.mayor.donationalerts.ui.DonationHud;
import com.mayor.donationalerts.ui.DonationSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public final class DonationAlertsClient implements ClientModInitializer {
    public static ModConfig CONFIG;
    public static DonationManager MANAGER;

    private static KeyBinding toggleKey, animationsKey, soundsKey, previewKey, settingsKey, clearKey;

    @Override public void onInitializeClient() {
        CONFIG = ModConfig.load();
        MANAGER = new DonationManager(CONFIG);
        String category = "category.donationalerts.main";
        toggleKey = register("key.donationalerts.toggle", GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        animationsKey = register("key.donationalerts.animations", GLFW.GLFW_KEY_UNKNOWN, category);
        soundsKey = register("key.donationalerts.sounds", GLFW.GLFW_KEY_UNKNOWN, category);
        previewKey = register("key.donationalerts.preview", GLFW.GLFW_KEY_UNKNOWN, category);
        settingsKey = register("key.donationalerts.settings", GLFW.GLFW_KEY_UNKNOWN, category);
        clearKey = register("key.donationalerts.clear", GLFW.GLFW_KEY_UNKNOWN, category);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> MANAGER.detect(message.getString()));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> MANAGER.detect(message.getString()));
        HudRenderCallback.EVENT.register(DonationHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) { CONFIG.enabled = !CONFIG.enabled; CONFIG.save(); notify(client, "Donation Alerts " + (CONFIG.enabled ? "enabled" : "disabled")); }
            while (animationsKey.wasPressed()) { CONFIG.animations = !CONFIG.animations; CONFIG.save(); notify(client, "Animations " + (CONFIG.animations ? "enabled" : "disabled")); }
            while (soundsKey.wasPressed()) { CONFIG.sounds = !CONFIG.sounds; CONFIG.save(); notify(client, "Sounds " + (CONFIG.sounds ? "enabled" : "disabled")); }
            while (previewKey.wasPressed()) MANAGER.preview(25_000_000L, "Mayor");
            while (settingsKey.wasPressed()) client.setScreen(new DonationSettingsScreen(client.currentScreen));
            while (clearKey.wasPressed()) { MANAGER.clearQueue(); notify(client, "Donation queue cleared"); }
            MANAGER.tick();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("donationtest")
                .then(argument("amount", LongArgumentType.longArg(1))
                    .executes(ctx -> test(MinecraftClient.getInstance(), LongArgumentType.getLong(ctx, "amount"), "Mayor"))
                    .then(argument("username", StringArgumentType.word())
                        .executes(ctx -> test(MinecraftClient.getInstance(), LongArgumentType.getLong(ctx, "amount"), StringArgumentType.getString(ctx, "username"))))));
            dispatcher.register(literal("donationalerts").executes(ctx -> {
                MinecraftClient.getInstance().setScreen(new DonationSettingsScreen(MinecraftClient.getInstance().currentScreen)); return 1;
            }));
        });
    }

    private static KeyBinding register(String translationKey, int key, String category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, key, category));
    }

    private static int test(MinecraftClient client, long amount, String username) {
        MANAGER.preview(amount, username);
        if (client.player != null) client.player.sendMessage(Text.literal("[Donation Alerts] Preview queued: " + username + " — $" + amount).formatted(Formatting.GOLD), false);
        return 1;
    }

    private static void notify(MinecraftClient client, String message) {
        if (client.player != null) client.player.sendMessage(Text.literal("[Donation Alerts] " + message).formatted(Formatting.AQUA), true);
    }
}
