package com.mayor.donationalerts.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Mojang profile/texture lookup. Network work runs away from the render thread. */
public final class SkinCache {
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Map<String, Identifier> SKINS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> REQUESTED = new ConcurrentHashMap<>();
    private static final Identifier UNKNOWN = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    private SkinCache() {}

    public static void request(String username) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}") || REQUESTED.putIfAbsent(username.toLowerCase(), true) != null) return;
        CompletableFetch.fetch(username);
    }

    public static Identifier get(String username) { return SKINS.getOrDefault(username.toLowerCase(), UNKNOWN); }

    private static final class CompletableFetch {
        static void fetch(String username) {
            HTTP.sendAsync(HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username)).header("User-Agent", "DonationAlerts/1.0").GET().build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) throw new IllegalStateException("Profile lookup failed");
                    JsonObject profile = JsonParser.parseString(response.body()).getAsJsonObject();
                    return profile.get("id").getAsString();
                })
                .thenCompose(uuid -> HTTP.sendAsync(HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")).GET().build(), HttpResponse.BodyHandlers.ofString()))
                .thenApply(response -> {
                    if (response.statusCode() != 200) throw new IllegalStateException("Session lookup failed");
                    JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                    String encoded = root.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                    JsonObject textures = JsonParser.parseString(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8)).getAsJsonObject();
                    return textures.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                })
                .thenCompose(url -> HTTP.sendAsync(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray()))
                .thenAccept(response -> {
                    if (response.statusCode() != 200) return;
                    try {
                        NativeImage image = NativeImage.read(new ByteArrayInputStream(response.body()));
                        MinecraftClient.getInstance().execute(() -> {
                            try {
                                Identifier id = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("donor_" + username.toLowerCase(), new NativeImageBackedTexture(image));
                                SKINS.put(username.toLowerCase(), id);
                            } catch (Exception e) { image.close(); }
                        });
                    } catch (Exception ignored) { }
                }).exceptionally(error -> null);
        }
    }
}
