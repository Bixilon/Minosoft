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

package de.bixilon.minosoft.game.datatypes.entities;

import de.bixilon.minosoft.game.datatypes.entities.mob.*;

public enum Mobs {
    PLAYER(92, OtherPlayer.class),
    CREEPER(50, Creeper.class),
    SKELETON(51, Skeleton.class),
    SPIDER(52, Spider.class),
    GIANT_ZOMBIE(53, GiantZombie.class),
    ZOMBIE(54, Zombie.class),
    SLIME(55, Slime.class),
    GHAST(56, Ghast.class),
    ZOMBIE_PIGMAN(57, ZombiePigman.class),
    ENDERMAN(58, EnderMan.class),
    CAVE_SPIDER(59, CaveSpider.class),
    SILVERFISH(60, Silverfish.class),
    BLAZE(61, Blaze.class),
    MAGMA_CUBE(62, MagmaCube.class),
    ENDER_DRAGON(63, EnderDragon.class),
    WITHER(64, Wither.class),
    BAT(65, Bat.class),
    WITCH(66, Witch.class),
    GUARDIAN(68, Guardian.class),
    PIG(90, Pig.class),
    SHEEP(91, Sheep.class),
    COW(92, Cow.class),
    CHICKEN(93, Chicken.class),
    SQUID(94, Squid.class),
    WOLF(95, Wolf.class),
    MOOSHROOM(95, Mooshroom.class),
    SNOW_GOLEM(97, SnowGolem.class),
    OCELOT(98, Ocelot.class),
    IRON_GOLEM(99, IronGolem.class),
    HORSE(100, Horse.class),
    RABBIT(101, Rabbit.class),
    VILLAGER(120, Villager.class);

    final int type;
    final Class<? extends Mob> clazz;

    Mobs(int type, Class<? extends Mob> clazz) {
        this.type = type;
        this.clazz = clazz;
    }


    public static Mobs byType(int type) {
        for (Mobs b : values()) {
            if (b.getType() == type) {
                return b;
            }
        }
        return null;
    }


    public int getType() {
        return type;
    }

    public Class<? extends Mob> getClazz() {
        return clazz;
    }
}
