package com.mayor.donationalerts.donation;

import com.mayor.donationalerts.config.ModConfig;
import com.mayor.donationalerts.skin.SkinCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class DonationManager {
    private final ModConfig config;
    private final Deque<Donation> queue = new ArrayDeque<>();
    private final List<Donation> history = new ArrayList<>();
    private Donation active;
    private int elapsedTicks;
    private Pattern parser;
    private String lastMessage = "";
    private long lastMessageAt;

    public DonationManager(ModConfig config) { this.config = config; compileParser(); }

    public void compileParser() {
        try { parser = Pattern.compile(config.parserRegex); }
        catch (PatternSyntaxException e) { parser = null; }
    }

    public boolean detect(String message) {
        if (!config.enabled || !config.detectionEnabled || parser == null || message == null) return false;
        long now = System.currentTimeMillis();
        if (message.equals(lastMessage) && now - lastMessageAt < 1800) return false;
        Matcher matcher = parser.matcher(message);
        if (!matcher.matches()) return false;
        try {
            String username = matcher.group(1);
            String number = matcher.group(2).replace(",", "");
            String suffix = matcher.group(3) == null ? "" : matcher.group(3).toUpperCase();
            double base = Double.parseDouble(number);
            double multiplier = switch (suffix) { case "K" -> 1_000d; case "M" -> 1_000_000d; case "B" -> 1_000_000_000d; case "T" -> 1_000_000_000_000d; default -> 1d; };
            long amount = (long) Math.min(Long.MAX_VALUE, base * multiplier);
            if (amount < config.minimumDonation || username == null || username.isBlank()) return false;
            lastMessage = message; lastMessageAt = now;
            enqueue(Donation.create(username, amount, false));
            return true;
        } catch (RuntimeException ignored) { return false; }
    }

    public void enqueue(Donation donation) {
        if (donation.amount() < config.minimumDonation || !config.tierEnabled[donation.tier().ordinal()]) return;
        queue.addLast(donation);
        if (queue.size() > 50) queue.removeFirst();
        if (history.size() >= 30) history.remove(history.size() - 1);
        history.add(0, donation);
        SkinCache.request(donation.username());
    }

    public void tick() {
        if (!config.enabled) return;
        if (active == null && !queue.isEmpty()) { active = queue.removeFirst(); elapsedTicks = 0; playTierSound(active.tier()); }
        if (active != null) {
            elapsedTicks++;
            if (elapsedTicks >= Math.max(3, config.durationSeconds * 20)) { active = null; elapsedTicks = 0; }
        }
    }

    private void playTierSound(DonationTier tier) {
        if (!config.sounds) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        var sound = switch (tier) {
            case COMMON -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case RARE -> SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
            case EPIC -> SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
            case LEGENDARY -> SoundEvents.ENTITY_WITHER_SPAWN;
            case MYTHICAL -> SoundEvents.BLOCK_ENDER_CHEST_OPEN;
            case GODLIKE -> SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
        };
        mc.player.playSound(sound, config.volume, tier == DonationTier.GODLIKE ? 0.65f : 1.0f);
    }

    public Donation active() { return active; }
    public int elapsedTicks() { return elapsedTicks; }
    public int queuedCount() { return queue.size(); }
    public List<Donation> history() { return List.copyOf(history); }
    public void clearQueue() { queue.clear(); }
    public void preview(long amount, String username) { enqueue(Donation.create(username, Math.max(1_000_000L, amount), true)); }
}
