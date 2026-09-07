# StatsSpoof

Fabric client mod (MC 26.2 / Fabric Loader 0.19.3 / Loom 1.17) that only replaces the
**displayed** values on the statistics screen (stats key from the pause menu). Nothing
is written to `stats/<uuid>.json`, nothing is sent to the server: it's purely
cosmetic on the client side.

## Why it works without touching StatsScreen

Since Minecraft is no longer obfuscated (26.1+), one might be tempted to mixin
directly into `StatsScreen` — but its internal structure (lists, entries, lambdas)
changes often from one version to another and these methods are largely private and
compiler-generated, making them fragile as mixin targets.

Instead, we intercept the single point through which *everything* passes to read a
stat: `StatsCounter#getValue(Stat<?>)`. The screen, the HUD, recipe unlock toasts...
everything calls this method. So a single mixin is enough, regardless of the tab.

**Safeguard** : the mixin only applies to the local player's `StatsCounter` instance
on the client side (`Minecraft.getInstance().player.getStats()`), checked by object
identity. Even in single-player, the integrated server's `StatsCounter` (used for
advancements and saving) is a separate instance in memory — never affected.

## Command

```
/statsspoof set <type> <id> <value>       # sets the displayed value
/statsspoof add <type> <id> <value>       # adds to the currently displayed value
/statsspoof remove <type> <id> <value>    # subtracts from the currently displayed value
/statsspoof clear <type> <id>             # removes the override, reverts to the real value
/statsspoof clearall                      # removes all overrides
```

`<type>` is one of the vanilla stat types: `custom`, `mined`, `crafted`, `used`,
`broken`, `picked_up`, `dropped`, `killed`, `killed_by`.

`<id>` is the vanilla identifier concerned, with or without namespace:
- `custom` -> a custom stat, e.g. `jump`, `deaths`, `play_time`, `walk_one_cm`
- `mined` -> a block, e.g. `stone`, `diamond_ore`
- `crafted` / `used` / `broken` / `picked_up` / `dropped` -> an item, e.g. `diamond_pickaxe`
- `killed` / `killed_by` -> an entity, e.g. `zombie`, `ender_dragon`

Examples:
```
/statsspoof set custom jump 999999
/statsspoof add killed zombie 50
/statsspoof remove mined stone 10
/statsspoof clear custom jump
```

## CI (GitHub Actions)

`.github/workflows/build.yml` compiles the mod on every push/PR to `main` and
publishes the jar as an artifact (Actions tab of the repo). If you push a `vX.Y.Z`
tag, it also creates a GitHub release with the jar attached. The repo doesn't bundle
`gradlew`: the workflow provisions Gradle 9.5.1 directly via
`gradle/actions/setup-gradle`, so there's no need to commit the wrapper.

## Build

1. Copy this folder into your environment (like `create_skyworks` or `HyprRank`).
2. Check the latest exact Fabric API builds for 26.2 on
   https://fabricmc.net/develop (the `fabric_version` in `gradle.properties` is a
   starting point, not a last-minute guarantee).
3. `./gradlew build`

No Yarn dependency, and not even a `mappings` block is needed anymore: since 26.1,
Minecraft is distributed non-obfuscated with the Mojang names directly readable, so
Loom compiles the jar as is. This also means a plugin id change:
`net.fabricmc.fabric-loom` (new, for non-obfuscated versions) instead of the old
`fabric-loom` / `net.fabricmc.fabric-loom-remap`. The `fabric-loader` and
`fabric-api` dependencies use classic `implementation` since there's no remap to do
(`modImplementation` is no longer needed).

Java 25 is required since 26.1 (before: Java 21); the `build.gradle` and CI workflow
target JDK 25.
