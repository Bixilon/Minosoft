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
import de.bixilon.minosoft.game.datatypes.entities.objects.*;

public enum Entities {
    ITEM(1, ItemStack.class),
    XP_ORB(2, ExperienceOrb.class),
    AREA_EFFECT_CLOUD(3, AreaEffectCloud.class),
    ELDER_GUARDIAN(4, ElderGuardian.class),
    WITHER_SKELETON(5, WitherSkeleton.class),
    STRAY_SKELETON(6, StraySkeleton.class),
    THROWN_EGG(7, ThrownEgg.class),
    LEASH_KNOT(8, LeashKnot.class),
    PAINTING(9, Painting.class),
    ARROW(10, Arrow.class),
    SNOWBALL(11, Snowball.class),
    FIRE_BALL(12, FireBall.class),
    SMALL_FIRE_BALL(13, SmallFireBall.class),
    THROWN_ENDERPEARL(14, ThrownEnderpearl.class),
    EYE_OF_ENDER_SIGNAL(15, EyeOfEnder.class),
    THROWN_POTION(16, ThrownPotion.class),
    THROWN_EXP_BOTTLE(17, ThrownExpBottle.class),
    ITEM_FRAME(18, ItemFrame.class),
    WITHER_SKULL(19, WitherSkull.class),
    PRIMED_TNT(20, PrimedTNT.class),
    FALLING_BLOCK(21, FallingBlock.class),
    FIREWORK(22, Firework.class),
    HUSK(23, Husk.class),
    SPECTRAL_ARROW(24, SpectralArrow.class),
    SHULKER_BULLET(25, ShulkerBullet.class),
    DRAGON_FIRE_BALL(26, DragonFireball.class),
    ZOMBIE_VILLAGER(27, ZombieVillager.class),
    SKELETON_HORSE(28, SkeletonHorse.class),
    ZOMBIE_HORSE(29, ZombieHorse.class),
    ARMOR_STAND(30, ArmorStand.class),
    DONKEY(31, Donkey.class),
    MULE(32, Mule.class),
    EVOCATION_FANGS(33, EvocationFangs.class),
    EVOCATION_ILLAGER(34, Evoker.class),
    VEX(35, Vex.class),
    VINDICATION_ILLAGER(36, Vindicator.class),
    MINECART_COMMAND_BLOCK(40, MinecartCommandBlock.class),
    BOAT(41, Boat.class),
    MINECART_RIDE_ABLE(42, Minecart.class),
    MINECART_CHEST(43, Minecart.class),
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
    SHULKER(69, Shulker.class),
    PIG(90, Pig.class),
    SHEEP(91, Sheep.class),
    COW(92, Cow.class),
    CHICKEN(93, Chicken.class),
    SQUID(94, Squid.class),
    WOLF(95, Wolf.class),
    MOOSHROOM(96, Mooshroom.class),
    SNOW_GOLEM(97, SnowGolem.class),
    OCELOT(98, Ocelot.class),
    IRON_GOLEM(99, IronGolem.class),
    HORSE(100, Horse.class),
    RABBIT(101, Rabbit.class),
    POLAR_BEAR(102, PolarBear.class),
    LLAMA(103, Llama.class),
    LLAMA_SPIT(104, LlamaSpit.class),
    PLAYER(105, OtherPlayer.class),
    VILLAGER(120, Villager.class),
    ENDER_CRYSTAL(200, EnderCrystal.class),

    // not a thing anymore
    FALLING_DRAGON_EGG(-1, FallingDragonEgg.class),
    FIRE_CHARGE(-1, FireCharge.class),
    FISHING_FLOAT(-1, FishingFloat.class),
    ;

    final int type;
    final Class<? extends Entity> clazz;

    Entities(int type, Class<? extends Entity> clazz) {
        this.type = type;
        this.clazz = clazz;
    }


    public static Entities byType(int type) {
        for (Entities b : values()) {
            if (b.getType() == type) {
                return b;
            }
        }
        return null;
    }


    public int getType() {
        return type;
    }

    public Class<? extends Entity> getClazz() {
        return clazz;
    }
}
