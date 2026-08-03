# StatsSpoof

Mod client Fabric (MC 26.2 / Fabric Loader 0.19.3 / Loom 1.17) qui remplace uniquement
les valeurs **affichées** sur l'écran des statistiques (touche stats en pause). Rien
n'est écrit dans `stats/<uuid>.json`, rien n'est envoyé au serveur : c'est purement
cosmétique côté client.

## Pourquoi ça marche sans toucher à StatsScreen

Depuis que Minecraft n'est plus obfusqué (26.1+), on pourrait être tenté de mixin
directement dans `StatsScreen` — mais sa structure interne (listes, entrées, lambdas)
change souvent d'une version à l'autre et ces méthodes sont en grande partie privées
et générées par le compilateur, donc fragiles comme cibles de mixin.

À la place, on intercepte le point unique par lequel *tout* passe pour lire une stat :
`StatsCounter#getValue(Stat<?>)`. L'écran, le HUD, les toasts d'unlock de recette...
tout appelle cette méthode. Un seul mixin suffit donc, quel que soit l'onglet.

**Garde-fou** : le mixin ne s'applique qu'à l'instance `StatsCounter` du joueur local
côté client (`Minecraft.getInstance().player.getStats()`), vérifiée par identité
d'objet. Même en solo, le `StatsCounter` du serveur intégré (utilisé pour les
advancements et la sauvegarde) est une instance séparée en mémoire — jamais concernée.

## Commande

```
/statsspoof set <type> <id> <valeur>      # fixe la valeur affichée
/statsspoof add <type> <id> <valeur>      # ajoute a la valeur actuellement affichee
/statsspoof remove <type> <id> <valeur>   # soustrait a la valeur actuellement affichee
/statsspoof clear <type> <id>             # retire l'override, revient a la vraie valeur
/statsspoof clearall                      # retire tous les overrides
```

`<type>` est un des types de stat vanilla : `custom`, `mined`, `crafted`, `used`,
`broken`, `picked_up`, `dropped`, `killed`, `killed_by`.

`<id>` est l'identifiant vanilla concerné, avec ou sans namespace :
- `custom` -> un stat custom, ex. `jump`, `deaths`, `play_time`, `walk_one_cm`
- `mined` -> un bloc, ex. `stone`, `diamond_ore`
- `crafted` / `used` / `broken` / `picked_up` / `dropped` -> un item, ex. `diamond_pickaxe`
- `killed` / `killed_by` -> une entité, ex. `zombie`, `ender_dragon`

Exemples :
```
/statsspoof set custom jump 999999
/statsspoof add killed zombie 50
/statsspoof remove mined stone 10
/statsspoof clear custom jump
```

## CI (GitHub Actions)

`.github/workflows/build.yml` compile le mod a chaque push/PR sur `main` et publie le
jar en artifact (onglet Actions du repo). Si tu pousses un tag `vX.Y.Z`, il cree aussi
une release GitHub avec le jar attache. Le repo n'embarque pas de `gradlew` : le
workflow provisionne directement Gradle 9.5.1 via `gradle/actions/setup-gradle`, donc
pas besoin de committer le wrapper.

## Build

1. Copie ce dossier dans ton environnement (comme `create_skyworks` ou `HyprRank`).
2. Vérifie les derniers builds exacts de Fabric API pour 26.2 sur
   https://fabricmc.net/develop (le `fabric_version` dans `gradle.properties` est un
   point de départ, pas une garantie de dernière minute).
3. `./gradlew build`

Aucune dépendance à Yarn, et même plus besoin d'un bloc `mappings` du tout :
depuis 26.1, Minecraft est distribué non-obfusqué avec les noms Mojang directement
lisibles, donc Loom compile le jar tel quel. Ça implique aussi un changement d'id
de plugin : `net.fabricmc.fabric-loom` (nouveau, pour les versions non-obfusquées)
et non plus l'ancien `fabric-loom` / `net.fabricmc.fabric-loom-remap`. Les
dépendances `fabric-loader` et `fabric-api` passent en `implementation` classique
puisqu'il n'y a plus de remap à faire (`modImplementation` n'est plus nécessaire).

Java 25 est requis depuis 26.1 (avant : Java 21), le `build.gradle` et le workflow
CI ciblent JDK 25.
