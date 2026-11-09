package com.mayor.donationalerts.ui;

import com.mayor.donationalerts.DonationAlertsClient;
import com.mayor.donationalerts.donation.Donation;
import com.mayor.donationalerts.donation.DonationTier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class DonationSettingsScreen extends Screen {
    private final Screen parent;
    private int page;
    private int scroll;

    public DonationSettingsScreen(Screen parent) { super(Text.literal("DONATION ALERTS")); this.parent = parent; }

    @Override protected void init() { rebuild(); }

    private void rebuild() {
        clearChildren();
        int left = width / 2 - 155;
        int y = 48;
        var c = DonationAlertsClient.CONFIG;
        addDrawableChild(ButtonWidget.builder(Text.literal("Master: " + (c.enabled ? "ON" : "OFF")), b -> { c.enabled = !c.enabled; c.save(); rebuild(); }).dimensions(left, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Animations: " + (c.animations ? "ON" : "OFF")), b -> { c.animations = !c.animations; c.save(); rebuild(); }).dimensions(left + 160, y, 150, 20).build()); y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Sounds: " + (c.sounds ? "ON" : "OFF")), b -> { c.sounds = !c.sounds; c.save(); rebuild(); }).dimensions(left, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Screen shake: " + (c.screenShake ? "ON" : "OFF")), b -> { c.screenShake = !c.screenShake; c.save(); rebuild(); }).dimensions(left + 160, y, 150, 20).build()); y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Particles: " + (c.particles ? "ON" : "OFF")), b -> { c.particles = !c.particles; c.save(); rebuild(); }).dimensions(left, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Donor skins: " + (c.showSkins ? "ON" : "OFF")), b -> { c.showSkins = !c.showSkins; c.save(); rebuild(); }).dimensions(left + 160, y, 150, 20).build()); y += 27;
        addDrawableChild(new SliderWidget(left, y, 150, 20, Text.literal("Volume: " + Math.round(c.volume * 100) + "%"), c.volume) {
            { updateMessage(); }
            @Override protected void updateMessage() { setMessage(Text.literal("Volume: " + Math.round(value * 100) + "%")); }
            @Override protected void applyValue() { c.volume = (float) value; c.save(); }
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Duration: " + c.durationSeconds + "s"), b -> { c.durationSeconds = c.durationSeconds >= 20 ? 4 : c.durationSeconds + 2; c.save(); rebuild(); }).dimensions(left + 160, y, 150, 20).build()); y += 27;
        addDrawableChild(ButtonWidget.builder(Text.literal("Minimum: " + compact(c.minimumDonation)), b -> { c.minimumDonation = c.minimumDonation >= 10_000_000 ? 1_000_000 : c.minimumDonation + 1_000_000; c.save(); rebuild(); }).dimensions(left, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Position: " + c.position), b -> { c.position = c.position.equals("TOP_CENTER") ? "TOP_LEFT" : c.position.equals("TOP_LEFT") ? "TOP_RIGHT" : "TOP_CENTER"; c.save(); rebuild(); }).dimensions(left + 160, y, 150, 20).build()); y += 29;
        for (int i = 0; i < DonationTier.values().length; i++) {
            final int index = i;
            DonationTier tier = DonationTier.values()[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(tier.label + ": " + (c.tierEnabled[i] ? "ON" : "OFF")), b -> { c.tierEnabled[index] = !c.tierEnabled[index]; c.save(); rebuild(); }).dimensions(left + (i % 2) * 160, y + (i / 2) * 23, 150, 20).build());
        }
        y += 75;
        long[] previews = {2_000_000L, 7_000_000L, 15_000_000L, 50_000_000L, 150_000_000L, 500_000_000L};
        String[] names = {"COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHICAL", "GODLIKE"};
        for (int i = 0; i < previews.length; i++) {
            final long amount = previews[i];
            addDrawableChild(ButtonWidget.builder(Text.literal("Test " + names[i]), b -> preview(amount)).dimensions(left + (i % 3) * 105, y + (i / 3) * 23, 100, 20).build());
        }
        y += 49;
        addDrawableChild(ButtonWidget.builder(Text.literal("Detection: " + (c.detectionEnabled ? "ON" : "OFF")), b -> { c.detectionEnabled = !c.detectionEnabled; c.save(); rebuild(); }).dimensions(left, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear queue"), b -> { DonationAlertsClient.MANAGER.clearQueue(); }).dimensions(left + 160, y, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close()).dimensions(left, y + 24, 310, 20).build());
    }

    private void preview(long amount) { DonationAlertsClient.MANAGER.preview(amount, "Mayor"); }
    private static String compact(long value) { return value >= 1_000_000 ? (value / 1_000_000) + "M" : Long.toString(value); }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0xB909101B);
        int left = width / 2 - 170;
        ctx.fill(left, 15, left + 340, height - 15, 0xEE111D2B);
        ctx.fill(left, 15, left + 340, 17, 0xFFFFC247);
        ctx.drawCenteredTextWithShadow(textRenderer, "DONUTSMP  /  DONATION CONTROL", width / 2, 24, 0xFFFFD36B);
        ctx.drawCenteredTextWithShadow(textRenderer, "Client-side alerts  •  Fabric 1.21.1", width / 2, 36, 0xFF9FB5C8);
        super.render(ctx, mouseX, mouseY, delta);
        int y = height - 43;
        ctx.drawCenteredTextWithShadow(textRenderer, "RECENT DONATIONS", width / 2, y, 0xFF65D8F2);
        List<Donation> history = DonationAlertsClient.MANAGER.history();
        if (history.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "No donations yet — use /donationtest 250000000", width / 2, y + 12, 0xFFE4EDF5);
        } else {
            for (int i = 0; i < Math.min(3, history.size()); i++) {
                Donation item = history.get(i);
                ctx.drawCenteredTextWithShadow(textRenderer, item.username() + "  •  " + compact(item.amount()) + "  •  " + item.tier().label, width / 2, y + 12 + i * 10, 0xFFE4EDF5);
            }
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "Detection parser: config/donationalerts.json", width / 2, height - 19, 0xFF8194A7);
    }

    @Override public void close() { if (client != null) client.setScreen(parent); }
}
