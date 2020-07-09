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
    ACHIEVEMENT_OPEN_INVENTORY(new Identifier("achievement.openInventory")),
    ACHIEVEMENT_MINE_WOOD(new Identifier("achievement.mineWood")),
    ACHIEVEMENT_BUILD_WORKBENCH(new Identifier("achievement.buildWorkBench")),
    ACHIEVEMENT_BUILD_PICKAXE(new Identifier("achievement.buildPickaxe")),
    ACHIEVEMENT_BUILD_FURNACE(new Identifier("achievement.buildFurnace")),
    ACHIEVEMENT_ACQUIRE_IRON(new Identifier("achievement.acquireIron")),
    ACHIEVEMENT_BUILD_HOE(new Identifier("achievement.buildHoe")),
    ACHIEVEMENT_MAKE_BREAD(new Identifier("achievement.makeBread")),
    ACHIEVEMENT_BAKE_CAKE(new Identifier("achievement.bakeCake")),
    ACHIEVEMENT_BUILD_BETTER_PICKAXE(new Identifier("achievement.buildBetterPickaxe")),
    ACHIEVEMENT_COOK_FISH(new Identifier("achievement.cookFish")),
    ACHIEVEMENT_ON_A_RAIL(new Identifier("achievement.onARail")),
    ACHIEVEMENT_BUILD_SWORD(new Identifier("achievement.buildSword")),
    ACHIEVEMENT_KILL_ENEMY(new Identifier("achievement.killEnemy")),
    ACHIEVEMENT_KILL_COW(new Identifier("achievement.killCow")),
    ACHIEVEMENT_FLY_PIG(new Identifier("achievement.flyPig")),
    ACHIEVEMENT_SNIPE_SKELETON(new Identifier("achievement.snipeSkeleton")),
    ACHIEVEMENT_DIAMONDS(new Identifier("achievement.diamonds")),
    ACHIEVEMENT_DIAMONDS_TO_YOU(new Identifier("achievement.diamondsToYou")),
    ACHIEVEMENT_PORTAL(new Identifier("achievement.portal")),
    ACHIEVEMENT_GHAST(new Identifier("achievement.ghast")),
    ACHIEVEMENT_BLAZE_ROD(new Identifier("achievement.blazeRod")),
    ACHIEVEMENT_POTION(new Identifier("achievement.potion")),
    ACHIEVEMENT_THE_END(new Identifier("achievement.theEnd")),
    ACHIEVEMENT_THE_END2(new Identifier("achievement.theEnd2")),
    ACHIEVEMENT_ENCHANTMENTS(new Identifier("achievement.enchantments")),
    ACHIEVEMENT_OVERKILL(new Identifier("achievement.overkill")),
    ACHIEVEMENT_BOOKCASE(new Identifier("achievement.bookcase")),
    ACHIEVEMENT_BREED_COW(new Identifier("achievement.breedCow")),
    ACHIEVEMENT_SPAWN_WITHER(new Identifier("achievement.spawnWither")),
    ACHIEVEMENT_KILL_WITHER(new Identifier("achievement.killWither")),
    ACHIEVEMENT_FULL_BEACON(new Identifier("achievement.fullBeacon")),
    ACHIEVEMENT_EXPLORE_ALL_BIOMES(new Identifier("achievement.exploreAllBiomes")),

    LEAVE_GAME(new Identifier("stat.leaveGame", "minecraft.leave_game"), 0),
    PLAY_ONE_MINUTE(new Identifier("stat.playOneMinute", "minecraft.play_one_minute"), 1),
    TIME_SINCE_DEATH(new Identifier("minecraft.time_since_death", "minecraft.time_since_death"), 2),
    SNEAK_TIME(new Identifier("minecraft.sneak_Time"), 3),
    WALK_ONE_CM(new Identifier("stat.walkOneCm", "minecraft.walk_one_cm"), 4),
    SNEAK_ONE_CM(new Identifier("minecraft.crouch_one_cm"), 5),
    SPRINT_ONE_CM(new Identifier("minecraft.sprint_one_cm"), 6),
    SWIM_ONE_CM(new Identifier("stat.swimOneCm", "minecraft.swim_one_cm"), 7),
    FALL_ONE_CM(new Identifier("stat.fallOneCm", "minecraft.fall_one_cm"), 8),
    CLIMB_ONE_CM(new Identifier("stat.climbOneCm", "minecraft.climb_one_cm"), 9),
    FLY_ONE_CM(new Identifier("stat.flyOneCm", "minecraft.fly_one_cm"), 10),
    DIVE_ONE_CM(new Identifier("stat.diveOneCm", "minecraft.dive_one_cm"), 11),
    MINECART_ONE_CM(new Identifier("stat.minecartOneCm", "minecraft.minecart_one_cm"), 12),
    BOAT_ONE_CM(new Identifier("stat.boatOneCm", "minecraft.boat_one_cm"), 13),
    PIG_ONE_CM(new Identifier("stat.pigOneCm", "minecraft.pig_one_cm"), 14),
    HORSE_ONE_CM(new Identifier("stat.horseOneCm", "minecraft.horse_one_cm"), 15),
    AVIATE_ONE_CM(new Identifier("minecraft.aviate_one_cm", "minecraft.aviate_one_cm"), 16),
    JUMP(new Identifier("stat.jump", "minecraft.jump"), 17),
    DROP(new Identifier("stat.drop", "minecraft.drop"), 18),
    DAMAGE_DEALT(new Identifier("stat.damageDealt", "minecraft.damage_dealt"), 19),
    DAMAGE_TAKEN(new Identifier("stat.damageTaken", "minecraft.damage_taken"), 20),
    DEATHS(new Identifier("stat.deaths", "minecraft.deaths"), 21),
    MOB_KILLS(new Identifier("stat.mobKills", "minecraft.mob_kills"), 22),
    ANIMALS_BRED(new Identifier("stat.animalsBred", "minecraft.animals_bred"), 23),
    PLAYER_KILLS(new Identifier("stat.playerKills", "minecraft.player_kills"), 24),
    FISH_CAUGHT(new Identifier("stat.fishCaught", "minecraft.fish_caught"), 25),
    JUNK_FISHED(new Identifier("stat.junkFished")),
    TREASURE_FISHED(new Identifier("stat.treasureFished")),
    TALKED_TO_VILLAGER(new Identifier("minecraft.traded_with_villager"), 26),
    TRADED_WITH_VILLAGER(new Identifier("minecraft.traded_with_villager"), 27),
    EAT_CAKE_SLICE(new Identifier("minecraft.eat_cake_slice"), 28),
    FILL_CAULDRON(new Identifier("minecraft.fill_cauldron"), 29),
    USE_CAULDRON(new Identifier("minecraft.use_cauldron"), 30),
    CLEAN_ARMOR(new Identifier("minecraft.clean_armor"), 31),
    CLEAN_BANNER(new Identifier("minecraft.clean_banner"), 32),
    INTERACT_WITH_BREWING_STAND(new Identifier("minecraft.interact_with_brewingstand"), 33),
    INTERACT_WITH_BEACON(new Identifier("minecraft.interact_with_beacon"), 34),
    INSPECT_DROPPER(new Identifier("minecraft.inspect_dropper"), 35),
    INSPECT_HOPPER(new Identifier("minecraft.inspect_hopper"), 36),
    INSPECT_DISPENSER(new Identifier("minecraft.inspect_dispenser"), 37),
    PLAY_NOTE_BLOCK(new Identifier("minecraft.play_noteblock"), 38),
    TUNE_NOTEBLOCK(new Identifier("minecraft.tune_noteblock"), 39),
    POT_FLOWER(new Identifier("minecraft.pot_flower"), 40),
    TRIGGER_TRAPPED_CHEST(new Identifier("minecraft.trigger_trapped_chest"), 41),
    OPEN_ENDER_CHEST(new Identifier("minecraft.open_enderchest"), 42),
    ENCHANT_ITEM(new Identifier("minecraft.enchant_item"), 43),
    PLAY_RECORD(new Identifier("minecraft.play_record"), 44),
    INTERACT_WITH_FURNACE(new Identifier("minecraft.interact_with_furnace"), 45),
    INTERACT_WITH_CRAFTING_TABLE(new Identifier("minecraft.interact_with_crafting_table"), 46),
    OPEN_CHEST(new Identifier("minecraft.open_chest"), 47),
    SLEEP_IN_BED(new Identifier("minecraft.sleep_in_bed"), 48),
    OPEN_SHULKER_BOX(new Identifier("minecraft.open_shulker_box"), 49);

    final Identifier identifier;
    final int id;

    Statistics(Identifier identifier, int id) {
        this.identifier = identifier;
        this.id = id;
    }

    Statistics(Identifier identifier) {
        this.identifier = identifier;
        this.id = -1;
    }

    public static Statistics byName(String name, ProtocolVersion version) {
        for (Statistics statistic : values()) {
            if (statistic.getIdentifier().isValidName(name, version)) {
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

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getId() {
        return id;
    }
}
