# DonutSMP Donation Alerts

A client-side Fabric mod for Minecraft Java 1.21.1. It listens to normal incoming game-chat messages, queues matching payments, displays animated tier alerts, and retrieves donor skins from Mojang's public profile/session services.

## Compatibility

- Minecraft Java: **1.21.1**
- Loader: **Fabric** (0.16.0+)
- Java: **21**
- Fabric API: included as a required mod dependency
- Dawn Client: not independently verified. This build targets standard Fabric 1.21.1; it works with Dawn only if that Dawn installation supports Fabric 1.21.1 mods.

This mod does not send payments, access server internals, or read private server data. Detection is based only on chat text delivered to the local Minecraft client.

## Build

1. Install Java 21.
2. Run `./gradlew build` (Windows: `gradlew.bat build`). The included launcher uses an installed Gradle or bootstraps Gradle 8.10.2 when network access is available.
3. The mod JAR is created in `build/libs/`.

## Install

1. Install Fabric Loader for Minecraft 1.21.1 and Fabric API for 1.21.1.
2. Put `donutsmp-donation-alert-1.0.0.jar` in `.minecraft/mods`.
3. Launch the Fabric 1.21.1 profile.
4. Use **Right Shift** to toggle alerts, or `/donationalerts` to open settings.

## Donation detection

The default parser matches the anchored incoming-chat format in either system/game chat or player chat:

`Player paid you $2.5m`

It accepts optional `has` between the username and `paid you`, and K/M/B/T suffixes. It deliberately does not match messages such as `You paid Player $2.5m`, because outgoing payments must never trigger an alert.

The exact DonutSMP message format can vary. If the server uses another format, copy the exact incoming message and edit `parserRegex` in `.minecraft/config/donationalerts.json`. The regex must have three capture groups in this order: donor username, numeric amount, suffix (K/M/B/T or empty). It must match the entire message. Restart the client after changing the parser.

## Commands

- `/donationtest 1000000`
- `/donationtest 250000000 Mayor`
- `/donationalerts`

Test donations are local previews and do not send money.

## Keybinds

All keys are registered with Minecraft's normal Controls > Key Binds menu under **Donation Alerts**:

- Right Shift: Toggle mod
- Unbound: Toggle animations
- Unbound: Toggle sounds
- Unbound: Preview donation
- Unbound: Open settings
- Unbound: Clear donation queue

## Settings

Settings are stored in `config/donationalerts.json`. The in-game screen includes master/animation/sound/particle/skin toggles, tier toggles, volume, duration, minimum amount, alert position, preview buttons, queue clear, and recent donation history.

## Notes / limitations

- Tier boundaries are Common: $1M–$5M, Rare: $5M–$10M, Epic: $10M–$25M, Legendary: $25M–$100M, Mythical: $100M–$250M, Godlike: $250M+.
- The alert HUD uses custom procedural Minecraft-style effects and vanilla sound events. It does not ship custom audio or texture assets yet.
- Minecraft skin lookup requires an internet connection and a public Mojang profile. A Steve placeholder is shown if lookup fails.
- Screen shake is limited to the alert presentation; it does not modify the game's camera.
- Detection cannot be guaranteed until the parser is checked against an actual DonutSMP incoming payment message.
