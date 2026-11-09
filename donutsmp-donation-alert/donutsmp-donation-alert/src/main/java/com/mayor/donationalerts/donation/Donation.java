package com.mayor.donationalerts.donation;

public record Donation(String username, long amount, DonationTier tier, long createdAt, boolean test) {
    public static Donation create(String username, long amount, boolean test) {
        return new Donation(username, amount, DonationTier.fromAmount(amount), System.currentTimeMillis(), test);
    }
}
