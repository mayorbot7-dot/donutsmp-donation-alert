package com.mayor.donationalerts.donation;

public enum DonationTier {
    COMMON("COMMON", 0xFFFFC247, 0xFF8A5515, 0xFFFFE6A3, 1_000_000L),
    RARE("RARE", 0xFF45D9FF, 0xFF145C9A, 0xFFB8F3FF, 5_000_000L),
    EPIC("EPIC", 0xFFB58AFF, 0xFF572D9B, 0xFFE8D6FF, 10_000_000L),
    LEGENDARY("LEGENDARY", 0xFFFFA12B, 0xFF9C3218, 0xFFFFE0A3, 25_000_000L),
    MYTHICAL("MYTHICAL", 0xFFFFE45E, 0xFF8D6A16, 0xFFFFFFFF, 100_000_000L),
    GODLIKE("GODLIKE", 0xFFFF4DDF, 0xFF511C77, 0xFFFFFFFF, 250_000_000L);

    public final String label;
    public final int color;
    public final int darkColor;
    public final int glowColor;
    public final long minimum;

    DonationTier(String label, int color, int darkColor, int glowColor, long minimum) {
        this.label = label; this.color = color; this.darkColor = darkColor; this.glowColor = glowColor; this.minimum = minimum;
    }

    public static DonationTier fromAmount(long amount) {
        if (amount >= 250_000_000L) return GODLIKE;
        if (amount >= 100_000_000L) return MYTHICAL;
        if (amount >= 25_000_000L) return LEGENDARY;
        if (amount >= 10_000_000L) return EPIC;
        if (amount >= 5_000_000L) return RARE;
        return COMMON;
    }
}
