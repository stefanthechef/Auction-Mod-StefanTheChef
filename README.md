# DonutSMP Auction Overlay (Fabric, Minecraft 1.21.11) - client-side

* Press **K** (or type `/ac`) -> menu: search + pick the auction block, Start/Stop, Reset totals, HUD on/off.
* While the auction is LIVE every chat line "`<Player> paid you $<amount>`" (k/m/b/t supported) is added to that
  player's TOTAL. The HUD shows: item, TOP BIDDER, their TOTAL paid (all payments combined), plus 2 runners-up.
* Commands: `/ac`, `/ac start`, `/ac stop`, `/ac reset`, `/ac hud`
* `config/dsauction.json` -> `deniedBlocks` = blocks hidden from the picker (edit to match what is obtainable on the server).

## Build
1. Install JDK 21 and IntelliJ IDEA (Community is fine).
2. Easiest: generate a blank 1.21.11 Fabric project at https://fabricmc.net/develop/template , then copy this
   project's `src/` folder over it (and replace its `gradle.properties`/`build.gradle` with these if the build complains).
   Or open this folder directly in IntelliJ and let it import Gradle (needs a Gradle wrapper: run `gradle wrapper`).
3. Run `./gradlew build` -> your jar is in `build/libs/dsauction-1.0.0.jar` (not the `-sources` one).
4. Put the jar in `.minecraft/mods` with Fabric Loader 0.18+ and Fabric API for 1.21.11.

Only reads your own chat - no server changes, no automation. Check DonutSMP's rules on client mods before streaming with it.

## No-install build (GitHub does it for you)
1. Make a free GitHub account, create a new repository, and upload everything in this folder (including the hidden `.github` folder).
2. Open the repo's **Actions** tab -> the "build" run -> download the **dsauction-mod** artifact.
3. Unzip it: the file ending in `.jar` (not `-sources.jar`) is the mod. Put it in `.minecraft/mods`.


## Added features
- Item picker includes registry items intended for normal survival acquisition, excluding spawn eggs, potion items, and configured denied items. DonutSMP-specific availability may differ from vanilla survival.
- Search by item name.
- Press the configured `K` key (change it in Minecraft Controls) or use `/ac` to open the menu.
- Click **TP to winner** to put `/tpa WinnerName` into chat for review; you still press Enter yourself. Requires DonutSMP to support `/tpa`.
- HUD positioning/width commands: `/ac left`, `/ac right`, `/ac up`, `/ac down`, `/ac wider`, `/ac narrower`; `/ac hud` toggles it.
