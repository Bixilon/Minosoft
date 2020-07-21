/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum Statistics {
    ACHIEVEMENT_OPEN_INVENTORY(new ChangeableIdentifier("achievement.openInventory")),
    ACHIEVEMENT_MINE_WOOD(new ChangeableIdentifier("achievement.mineWood")),
    ACHIEVEMENT_BUILD_WORKBENCH(new ChangeableIdentifier("achievement.buildWorkBench")),
    ACHIEVEMENT_BUILD_PICKAXE(new ChangeableIdentifier("achievement.buildPickaxe")),
    ACHIEVEMENT_BUILD_FURNACE(new ChangeableIdentifier("achievement.buildFurnace")),
    ACHIEVEMENT_ACQUIRE_IRON(new ChangeableIdentifier("achievement.acquireIron")),
    ACHIEVEMENT_BUILD_HOE(new ChangeableIdentifier("achievement.buildHoe")),
    ACHIEVEMENT_MAKE_BREAD(new ChangeableIdentifier("achievement.makeBread")),
    ACHIEVEMENT_BAKE_CAKE(new ChangeableIdentifier("achievement.bakeCake")),
    ACHIEVEMENT_BUILD_BETTER_PICKAXE(new ChangeableIdentifier("achievement.buildBetterPickaxe")),
    ACHIEVEMENT_COOK_FISH(new ChangeableIdentifier("achievement.cookFish")),
    ACHIEVEMENT_ON_A_RAIL(new ChangeableIdentifier("achievement.onARail")),
    ACHIEVEMENT_BUILD_SWORD(new ChangeableIdentifier("achievement.buildSword")),
    ACHIEVEMENT_KILL_ENEMY(new ChangeableIdentifier("achievement.killEnemy")),
    ACHIEVEMENT_KILL_COW(new ChangeableIdentifier("achievement.killCow")),
    ACHIEVEMENT_FLY_PIG(new ChangeableIdentifier("achievement.flyPig")),
    ACHIEVEMENT_SNIPE_SKELETON(new ChangeableIdentifier("achievement.snipeSkeleton")),
    ACHIEVEMENT_DIAMONDS(new ChangeableIdentifier("achievement.diamonds")),
    ACHIEVEMENT_DIAMONDS_TO_YOU(new ChangeableIdentifier("achievement.diamondsToYou")),
    ACHIEVEMENT_PORTAL(new ChangeableIdentifier("achievement.portal")),
    ACHIEVEMENT_GHAST(new ChangeableIdentifier("achievement.ghast")),
    ACHIEVEMENT_BLAZE_ROD(new ChangeableIdentifier("achievement.blazeRod")),
    ACHIEVEMENT_POTION(new ChangeableIdentifier("achievement.potion")),
    ACHIEVEMENT_THE_END(new ChangeableIdentifier("achievement.theEnd")),
    ACHIEVEMENT_THE_END2(new ChangeableIdentifier("achievement.theEnd2")),
    ACHIEVEMENT_ENCHANTMENTS(new ChangeableIdentifier("achievement.enchantments")),
    ACHIEVEMENT_OVERKILL(new ChangeableIdentifier("achievement.overkill")),
    ACHIEVEMENT_BOOKCASE(new ChangeableIdentifier("achievement.bookcase")),
    ACHIEVEMENT_BREED_COW(new ChangeableIdentifier("achievement.breedCow")),
    ACHIEVEMENT_SPAWN_WITHER(new ChangeableIdentifier("achievement.spawnWither")),
    ACHIEVEMENT_KILL_WITHER(new ChangeableIdentifier("achievement.killWither")),
    ACHIEVEMENT_FULL_BEACON(new ChangeableIdentifier("achievement.fullBeacon")),
    ACHIEVEMENT_EXPLORE_ALL_BIOMES(new ChangeableIdentifier("achievement.exploreAllBiomes")),

    LEAVE_GAME(new ChangeableIdentifier("stat.leaveGame", "minecraft.leave_game"), 0),
    PLAY_ONE_MINUTE(new ChangeableIdentifier("stat.playOneMinute", "minecraft.play_one_minute"), 1),
    TIME_SINCE_DEATH(new ChangeableIdentifier("minecraft.time_since_death", "minecraft.time_since_death"), 2),
    SNEAK_TIME(new ChangeableIdentifier("minecraft.sneak_Time"), 3),
    WALK_ONE_CM(new ChangeableIdentifier("stat.walkOneCm", "minecraft.walk_one_cm"), 4),
    SNEAK_ONE_CM(new ChangeableIdentifier("minecraft.crouch_one_cm"), 5),
    SPRINT_ONE_CM(new ChangeableIdentifier("minecraft.sprint_one_cm"), 6),
    SWIM_ONE_CM(new ChangeableIdentifier("stat.swimOneCm", "minecraft.swim_one_cm"), 7),
    FALL_ONE_CM(new ChangeableIdentifier("stat.fallOneCm", "minecraft.fall_one_cm"), 8),
    CLIMB_ONE_CM(new ChangeableIdentifier("stat.climbOneCm", "minecraft.climb_one_cm"), 9),
    FLY_ONE_CM(new ChangeableIdentifier("stat.flyOneCm", "minecraft.fly_one_cm"), 10),
    DIVE_ONE_CM(new ChangeableIdentifier("stat.diveOneCm", "minecraft.dive_one_cm"), 11),
    MINECART_ONE_CM(new ChangeableIdentifier("stat.minecartOneCm", "minecraft.minecart_one_cm"), 12),
    BOAT_ONE_CM(new ChangeableIdentifier("stat.boatOneCm", "minecraft.boat_one_cm"), 13),
    PIG_ONE_CM(new ChangeableIdentifier("stat.pigOneCm", "minecraft.pig_one_cm"), 14),
    HORSE_ONE_CM(new ChangeableIdentifier("stat.horseOneCm", "minecraft.horse_one_cm"), 15),
    AVIATE_ONE_CM(new ChangeableIdentifier("minecraft.aviate_one_cm", "minecraft.aviate_one_cm"), 16),
    JUMP(new ChangeableIdentifier("stat.jump", "minecraft.jump"), 17),
    DROP(new ChangeableIdentifier("stat.drop", "minecraft.drop"), 18),
    DAMAGE_DEALT(new ChangeableIdentifier("stat.damageDealt", "minecraft.damage_dealt"), 19),
    DAMAGE_TAKEN(new ChangeableIdentifier("stat.damageTaken", "minecraft.damage_taken"), 20),
    DEATHS(new ChangeableIdentifier("stat.deaths", "minecraft.deaths"), 21),
    MOB_KILLS(new ChangeableIdentifier("stat.mobKills", "minecraft.mob_kills"), 22),
    ANIMALS_BRED(new ChangeableIdentifier("stat.animalsBred", "minecraft.animals_bred"), 23),
    PLAYER_KILLS(new ChangeableIdentifier("stat.playerKills", "minecraft.player_kills"), 24),
    FISH_CAUGHT(new ChangeableIdentifier("stat.fishCaught", "minecraft.fish_caught"), 25),
    JUNK_FISHED(new ChangeableIdentifier("stat.junkFished")),
    TREASURE_FISHED(new ChangeableIdentifier("stat.treasureFished")),
    TALKED_TO_VILLAGER(new ChangeableIdentifier("minecraft.traded_with_villager"), 26),
    TRADED_WITH_VILLAGER(new ChangeableIdentifier("minecraft.traded_with_villager"), 27),
    EAT_CAKE_SLICE(new ChangeableIdentifier("minecraft.eat_cake_slice"), 28),
    FILL_CAULDRON(new ChangeableIdentifier("minecraft.fill_cauldron"), 29),
    USE_CAULDRON(new ChangeableIdentifier("minecraft.use_cauldron"), 30),
    CLEAN_ARMOR(new ChangeableIdentifier("minecraft.clean_armor"), 31),
    CLEAN_BANNER(new ChangeableIdentifier("minecraft.clean_banner"), 32),
    INTERACT_WITH_BREWING_STAND(new ChangeableIdentifier("minecraft.interact_with_brewingstand"), 33),
    INTERACT_WITH_BEACON(new ChangeableIdentifier("minecraft.interact_with_beacon"), 34),
    INSPECT_DROPPER(new ChangeableIdentifier("minecraft.inspect_dropper"), 35),
    INSPECT_HOPPER(new ChangeableIdentifier("minecraft.inspect_hopper"), 36),
    INSPECT_DISPENSER(new ChangeableIdentifier("minecraft.inspect_dispenser"), 37),
    PLAY_NOTE_BLOCK(new ChangeableIdentifier("minecraft.play_noteblock"), 38),
    TUNE_NOTEBLOCK(new ChangeableIdentifier("minecraft.tune_noteblock"), 39),
    POT_FLOWER(new ChangeableIdentifier("minecraft.pot_flower"), 40),
    TRIGGER_TRAPPED_CHEST(new ChangeableIdentifier("minecraft.trigger_trapped_chest"), 41),
    OPEN_ENDER_CHEST(new ChangeableIdentifier("minecraft.open_enderchest"), 42),
    ENCHANT_ITEM(new ChangeableIdentifier("minecraft.enchant_item"), 43),
    PLAY_RECORD(new ChangeableIdentifier("minecraft.play_record"), 44),
    INTERACT_WITH_FURNACE(new ChangeableIdentifier("minecraft.interact_with_furnace"), 45),
    INTERACT_WITH_CRAFTING_TABLE(new ChangeableIdentifier("minecraft.interact_with_crafting_table"), 46),
    OPEN_CHEST(new ChangeableIdentifier("minecraft.open_chest"), 47),
    SLEEP_IN_BED(new ChangeableIdentifier("minecraft.sleep_in_bed"), 48),
    OPEN_SHULKER_BOX(new ChangeableIdentifier("minecraft.open_shulker_box"), 49);

    final ChangeableIdentifier changeableIdentifier;
    final int id;

    Statistics(ChangeableIdentifier changeableIdentifier, int id) {
        this.changeableIdentifier = changeableIdentifier;
        this.id = id;
    }

    Statistics(ChangeableIdentifier changeableIdentifier) {
        this.changeableIdentifier = changeableIdentifier;
        this.id = -1;
    }

    public static Statistics byName(String name, ProtocolVersion version) {
        for (Statistics statistic : values()) {
            if (statistic.getChangeableIdentifier().isValidName(name, version)) {
                return statistic;
            }
        }
        return null;
    }

    public static Statistics byId(int id) {
        for (Statistics statistic : values()) {
            if (statistic.getId() == id) {
                return statistic;
            }
        }
        return null;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return changeableIdentifier;
    }

    public int getId() {
        return id;
    }
}
