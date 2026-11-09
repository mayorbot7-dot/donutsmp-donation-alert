package com.mayor.donationalerts.ui;

import com.mayor.donationalerts.DonationAlertsClient;
import com.mayor.donationalerts.donation.Donation;
import com.mayor.donationalerts.donation.DonationTier;
import com.mayor.donationalerts.skin.SkinCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import java.util.Locale;

public final class DonationHud {
    private DonationHud() {}

    public static void render(DrawContext ctx, float tickDelta) {
        var config = DonationAlertsClient.CONFIG;
        if (!config.enabled) return;
        Donation donation = DonationAlertsClient.MANAGER.active();
        if (donation == null || !config.tierEnabled[donation.tier().ordinal()]) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        DonationTier tier = donation.tier();
        float age = (DonationAlertsClient.MANAGER.elapsedTicks() + tickDelta) / (config.durationSeconds * 20f);
        float fade = Math.min(1f, Math.min(age * 9f, (1f - age) * 9f));
        if (fade < 0) fade = 0;
        int alpha = (int)(fade * 255);
        int x = config.position.equals("TOP_LEFT") ? 16 : config.position.equals("TOP_RIGHT") ? w - 356 : (w - 340) / 2;
        int y = 22;
        float pulse = (float)(1 + Math.sin((DonationAlertsClient.MANAGER.elapsedTicks() + tickDelta) * 0.22) * 0.025);
        if (config.screenShake && tier.ordinal() >= DonationTier.EPIC.ordinal() && age < 0.55f) {
            int shake = (int)(Math.sin((DonationAlertsClient.MANAGER.elapsedTicks() + tickDelta) * 2.7) * (tier == DonationTier.GODLIKE ? 4 : 2));
            x += shake;
        }
        int cardW = tier == DonationTier.GODLIKE ? 380 : tier == DonationTier.MYTHICAL ? 360 : 340;
        int cardH = tier.ordinal() >= DonationTier.LEGENDARY.ordinal() ? 104 : 92;
        x = Math.max(5, Math.min(w - cardW - 5, x));

        // Tier-specific full-screen atmosphere, drawn behind the alert.
        if (config.animations && tier.ordinal() >= DonationTier.LEGENDARY.ordinal()) {
            int overlay = (int)((tier == DonationTier.GODLIKE ? 0x38 : 0x20) * fade);
            ctx.fill(0, 0, w, h, (overlay << 24) | (tier.darkColor & 0x00FFFFFF));
            if (tier == DonationTier.GODLIKE) {
                for (int i = 0; i < 8; i++) {
                    int lx = (int)((i * 137 + DonationAlertsClient.MANAGER.elapsedTicks() * 3L) % Math.max(1, w));
                    ctx.fill(lx, 0, lx + 2, h, (alpha / 5 << 24) | 0x00FFE36A);
                }
            }
        }

        int border = (alpha << 24) | (tier.color & 0x00FFFFFF);
        int dark = (Math.min(alpha, 238) << 24) | 0x00101A28;
        ctx.fill(x - 2, y - 2, x + cardW + 2, y + cardH + 2, border);
        ctx.fill(x, y, x + cardW, y + cardH, dark);
        ctx.fill(x + 2, y + 2, x + cardW - 2, y + 4, border);
        ctx.fill(x + 2, y + cardH - 4, x + cardW - 2, y + cardH - 2, (alpha << 24) | (tier.darkColor & 0x00FFFFFF));

        if (config.animations && config.particles) drawTierEffects(ctx, x, y, cardW, cardH, donation, alpha, age);
        if (config.showSkins) drawSkin(ctx, donation, x + 13, y + 18, tier, alpha);
        int textX = x + (config.showSkins ? 67 : 16);
        int titleColor = (alpha << 24) | (tier.glowColor & 0x00FFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, tier.label + (donation.test() ? "  •  PREVIEW" : "  •  DONATION"), textX, y + 12, titleColor);
        ctx.drawTextWithShadow(mc.textRenderer, donation.username(), textX, y + 29, (alpha << 24) | 0x00FFFFFF);
        String amount = format(donation.amount());
        int amountColor = (alpha << 24) | (tier.color & 0x00FFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, "$" + amount, textX, y + 47, amountColor);
        if (tier == DonationTier.GODLIKE) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer, "✦  GODLIKE DONATION!  ✦", x + cardW / 2, y + 72, titleColor);
        } else if (tier == DonationTier.MYTHICAL) {
            ctx.drawTextWithShadow(mc.textRenderer, "TREASURE UNLOCKED", textX, y + 68, titleColor);
        } else if (tier == DonationTier.LEGENDARY) {
            ctx.drawTextWithShadow(mc.textRenderer, "LEGENDARY DROP", textX, y + 68, titleColor);
        }
        if (config.animations) {
            int progressW = (int)((cardW - 4) * (1f - age));
            ctx.fill(x + 2, y + cardH - 2, x + 2 + Math.max(0, progressW), y + cardH, titleColor);
            if (tier == DonationTier.GODLIKE && age < 0.72f) {
                ctx.getMatrices().push();
                ctx.getMatrices().translate(0.0, 0.0, 450.0);
                ctx.getMatrices().scale(2.25f, 2.25f, 1.0f);
                ctx.drawCenteredTextWithShadow(mc.textRenderer, "GODLIKE DONATION!", w / 2 / 2, h / 2 / 2, titleColor);
                ctx.getMatrices().pop();
            }
        }
    }

    private static void drawSkin(DrawContext ctx, Donation donation, int x, int y, DonationTier tier, int alpha) {
        int size = tier == DonationTier.GODLIKE ? 42 : 38;
        int color = (alpha << 24) | (tier.color & 0x00FFFFFF);
        ctx.fill(x - 3, y - 3, x + size + 3, y + size + 3, color);
        ctx.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF0A111B);
        Identifier skin = SkinCache.get(donation.username());
        ctx.drawTexture(skin, x, y, 8, 8, size, size, 64, 64);
        ctx.drawTexture(skin, x, y, 40, 8, size, size, 64, 64);
    }

    private static void drawTierEffects(DrawContext ctx, int x, int y, int w, int h, Donation donation, int alpha, float age) {
        int color = (alpha << 24) | (donation.tier().glowColor & 0x00FFFFFF);
        int tick = DonationAlertsClient.MANAGER.elapsedTicks();
        switch (donation.tier()) {
            case COMMON -> {
                for (int i = 0; i < 5; i++) { int px = x + w - 24 - ((tick * 3 + i * 19) % 65); int py = y + 16 + (i % 3) * 18; ctx.fill(px, py, px + 3, py + 3, color); }
            }
            case RARE -> {
                for (int i = 0; i < 9; i++) { double a = (tick * 0.12) + i * Math.PI * 2 / 9; int px = x + w - 18 + (int)(Math.cos(a) * 17); int py = y + 46 + (int)(Math.sin(a) * 23); ctx.fill(px, py, px + 4, py + 4, color); }
            }
            case EPIC -> {
                for (int i = 0; i < 12; i++) { int px = x + w - 20 - ((i * 23 + tick * 2) % 80); int py = y + 8 + ((i * 13 + tick) % 72); ctx.fill(px, py, px + 2, py + 5, color); }
                ctx.fill(x + w - 17, y + 15, x + w - 10, y + 22, color);
            }
            case LEGENDARY -> {
                for (int i = 0; i < 5; i++) { int lx = x + 20 + i * (w - 40) / 4; ctx.fill(lx, y - 9, lx + 2, y + h + 7, (alpha / 3 << 24) | 0x00FFE7A2); }
                ctx.fill(x + w - 32, y + 8, x + w - 15, y + 25, color);
            }
            case MYTHICAL -> {
                int chestX = x + w - 45; int chestY = y + 27;
                ctx.fill(chestX - 4, chestY - 10, chestX + 28, chestY + 25, (alpha / 2 << 24) | 0x00FFE46B);
                ctx.fill(chestX, chestY, chestX + 24, chestY + 18, color);
                ctx.fill(chestX + 9, chestY + 5, chestX + 15, chestY + 11, 0xFF3C2A12);
            }
            case GODLIKE -> {
                int cx = x + w - 45, cy = y + 44;
                for (int i = 0; i < 16; i++) { double a = i * Math.PI / 8 + tick * 0.04; int px = cx + (int)(Math.cos(a) * (24 + (tick % 9))); int py = cy + (int)(Math.sin(a) * (24 + (tick % 9))); ctx.fill(px, py, px + 5, py + 5, color); }
                ctx.fill(cx - 20, cy - 9, cx + 20, cy + 15, (alpha << 24) | 0x00DCA83B);
                ctx.fill(cx - 15, cy - 14, cx + 15, cy - 8, color);
                ctx.fill(cx - 3, cy - 2, cx + 3, cy + 7, 0xFF4B3214);
            }
        }
    }

    private static String format(long value) {
        if (value >= 1_000_000_000_000L) return String.format(Locale.ROOT, "%.2fT", value / 1_000_000_000_000d);
        if (value >= 1_000_000_000L) return String.format(Locale.ROOT, "%.2fB", value / 1_000_000_000d);
        if (value >= 1_000_000L) return String.format(Locale.ROOT, "%.2fM", value / 1_000_000d);
        if (value >= 1_000L) return String.format(Locale.ROOT, "%.1fK", value / 1_000d);
        return Long.toString(value);
    }
}
