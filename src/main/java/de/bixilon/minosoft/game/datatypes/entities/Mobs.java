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

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.game.datatypes.entities.mob.*;

public enum Mobs {
    PLAYER(null, 92, OtherPlayer.class),
    CREEPER(new Identifier("creeper"), 50, Creeper.class),
    SKELETON(new Identifier("skeleton"), 51, Skeleton.class),
    SPIDER(new Identifier("spider"), 52, Spider.class),
    GIANT_ZOMBIE(new Identifier("giant"), 53, GiantZombie.class),
    ZOMBIE(new Identifier("zombie"), 54, Zombie.class),
    SLIME(new Identifier("slime"), 55, Slime.class),
    GHAST(new Identifier("ghast"), 56, Ghast.class),
    ZOMBIE_PIGMAN(new Identifier("zombie_pigman"), 57, ZombiePigman.class),
    ENDERMAN(new Identifier("enderman"), 58, EnderMan.class),
    CAVE_SPIDER(new Identifier("cave_spider"), 59, CaveSpider.class),
    SILVERFISH(new Identifier("silverfish"), 60, Silverfish.class),
    BLAZE(new Identifier("blaze"), 61, Blaze.class),
    MAGMA_CUBE(new Identifier("magma_cube"), 62, MagmaCube.class),
    ENDER_DRAGON(new Identifier("ender_dragon"), 63, EnderDragon.class),
    WITHER(new Identifier("wither"), 64, Wither.class),
    BAT(new Identifier("bat"), 65, Bat.class),
    WITCH(new Identifier("witch"), 66, Witch.class),
    GUARDIAN(new Identifier("witch"), 68, Guardian.class),
    PIG(new Identifier("pig"), 90, Pig.class),
    SHEEP(new Identifier("sheep"), 91, Sheep.class),
    COW(new Identifier("cow"), 92, Cow.class),
    CHICKEN(new Identifier("chicken"), 93, Chicken.class),
    SQUID(new Identifier("squid"), 94, Squid.class),
    WOLF(new Identifier("wolf"), 95, Wolf.class),
    MOOSHROOM(new Identifier("mooshroom"), 95, Mooshroom.class),
    SNOW_GOLEM(new Identifier("snow_golem"), 97, SnowGolem.class),
    OCELOT(new Identifier("ocelot"), 98, Ocelot.class),
    IRON_GOLEM(new Identifier("iron_golem"), 99, IronGolem.class),
    HORSE(new Identifier("horse"), 100, Horse.class),
    RABBIT(new Identifier("rabbit"), 101, Rabbit.class),
    VILLAGER(new Identifier("villager"), 120, Villager.class);

    final Identifier identifier;
    final int type;
    final Class<? extends Mob> clazz;

    Mobs(Identifier identifier, int type, Class<? extends Mob> clazz) {
        this.identifier = identifier;
        this.type = type;
        this.clazz = clazz;
    }

    public static Mobs byIdentifier(Identifier identifier) {
        for (Mobs b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return null;
    }

    public static Mobs byType(int type) {
        for (Mobs b : values()) {
            if (b.getType() == type) {
                return b;
            }
        }
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getType() {
        return type;
    }

    public Class<? extends Mob> getClazz() {
        return clazz;
    }
}
