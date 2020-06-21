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

    STAT_LEAVE_GAME(new Identifier("stat.leaveGame")),
    STAT_PLAY_ONE_MINUTE(new Identifier("stat.playOneMinute")),
    STAT_WALK_ONE_CM(new Identifier("stat.walkOneCm")),
    STAT_SWIM_ONE_CM(new Identifier("stat.swimOneCm")),
    STAT_FALL_ONE_CM(new Identifier("stat.fallOneCm")),
    STAT_CLIMB_ONE_CM(new Identifier("stat.climbOneCm")),
    STAT_FLY_ONE_CM(new Identifier("stat.flyOneCm")),
    STAT_DIVE_ONE_CM(new Identifier("stat.diveOneCm")),
    STAT_MINECART__ONE__CM(new Identifier("stat.minecartOneCm")),
    STAT_BOAT__ONE__CM(new Identifier("stat.boatOneCm")),
    STAT_PIG__ONE__CM(new Identifier("stat.pigOneCm")),
    STAT_HORSE__ONE__CM(new Identifier("stat.horseOneCm")),
    STAT_JUMP(new Identifier("stat.jump")),
    STAT_DROP(new Identifier("stat.drop")),
    STAT_DAMAGE_DEALT(new Identifier("stat.damageDealt")),
    STAT_DAMAGE_TAKEN(new Identifier("stat.damageTaken")),
    STAT_DEATHS(new Identifier("stat.deaths")),
    STAT_MOB_KILLS(new Identifier("stat.mobKills")),
    STAT_ANIMALS_BRED(new Identifier("stat.animalsBred")),
    STAT_PLAYER_KILLS(new Identifier("stat.playerKills")),
    STAT_FISH_CAUGHT(new Identifier("stat.fishCaught")),
    STAT_JUN_KFISHED(new Identifier("stat.junkFished")),
    STAT_TREASURE_FISHED(new Identifier("stat.treasureFished")),

    ;

    final Identifier identifier;

    Statistics(Identifier identifier) {
        this.identifier = identifier;
    }

    public static Statistics byIdentifier(Identifier identifier) {
        for (Statistics s : values()) {
            if (s.getIdentifier().equals(identifier)) {
                return s;
            }
        }
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

}
