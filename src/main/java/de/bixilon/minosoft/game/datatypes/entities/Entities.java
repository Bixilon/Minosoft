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

import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.game.datatypes.entities.mob.*;
import de.bixilon.minosoft.game.datatypes.entities.objects.*;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum Entities {
    ITEM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 32)}, ItemStack.class),
    XP_ORB(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 22)}, ExperienceOrb.class),
    AREA_EFFECT_CLOUD(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 3), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 0)}, AreaEffectCloud.class),
    ELDER_GUARDIAN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 4), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 15)}, ElderGuardian.class),
    WITHER_SKELETON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 5), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 84)}, WitherSkeleton.class),
    STRAY_SKELETON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 6), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 71)}, StraySkeleton.class),
    THROWN_EGG(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 7), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 74)}, ThrownEgg.class),
    LEASH_KNOT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 8), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 35)}, LeashKnot.class),
    PAINTING(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 9), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 49)}, Painting.class),
    ARROW(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 10), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 2)}, Arrow.class),
    SNOWBALL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 11), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 67)}, Snowball.class),
    FIREBALL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 12), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 34)}, Fireball.class),
    SMALL_FIREBALL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 13), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 65)}, SmallFireball.class),
    THROWN_ENDERPEARL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 14), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 75)}, ThrownEnderpearl.class),
    EYE_OF_ENDER_SIGNAL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 15), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 23)}, EyeOfEnder.class),
    THROWN_POTION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 16), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 77)}, ThrownPotion.class),
    THROWN_EXP_BOTTLE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 17), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 76)}, ThrownExpBottle.class),
    ITEM_FRAME(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 18), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 33)}, ItemFrame.class),
    WITHER_SKULL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 19), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 85)}, WitherSkull.class),
    PRIMED_TNT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 20), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 55)}, PrimedTNT.class),
    FALLING_BLOCK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 21), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 24)}, FallingBlock.class),
    FIREWORK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 22), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 25)}, Firework.class),
    HUSK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 23), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 30)}, Husk.class),
    SPECTRAL_ARROW(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 24), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 68)}, SpectralArrow.class),
    SHULKER_BULLET(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 25), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 60)}, ShulkerBullet.class),
    DRAGON_FIREBALL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 26), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 13)}, DragonFireball.class),
    ZOMBIE_VILLAGER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 27), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 89)}, ZombieVillager.class),
    SKELETON_HORSE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 28), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 63)}, SkeletonHorse.class),
    ZOMBIE_HORSE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 29), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 88)}, ZombieHorse.class),
    ARMOR_STAND(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 30), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 1)}, ArmorStand.class),
    DONKEY(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 31), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 11)}, Donkey.class),
    MULE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 32), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 46)}, Mule.class),
    EVOCATION_FANGS(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 33), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 20)}, EvocationFangs.class),
    EVOKER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 34), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 21)}, Evoker.class),
    VEX(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 35), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 78)}, Vex.class),
    VINDICATOR(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 36), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 81)}, Vindicator.class),
    ILLUSIONER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 37), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 31)}, Illusioner.class),
    MINECART_COMMAND_BLOCK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 40), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 41)}, MinecartCommandBlock.class),
    BOAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 41), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 5)}, Boat.class),
    MINECART_RIDE_ABLE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 42), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 39)}, Minecart.class),
    MINECART_CHEST(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 43), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 40)}, Minecart.class),
    CREEPER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 50), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 10)}, Creeper.class),
    SKELETON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 51), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 62)}, Skeleton.class),
    SPIDER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 52), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 69)}, Spider.class),
    GIANT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 53), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 27)}, GiantZombie.class),
    ZOMBIE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 54), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 87)}, Zombie.class),
    SLIME(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 55), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 64)}, Slime.class),
    GHAST(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 56), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 26)}, Ghast.class),
    ZOMBIE_PIGMAN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 57), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 53)}, ZombiePigman.class),
    ENDERMAN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 58), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 18)}, Enderman.class),
    CAVE_SPIDER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 9), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 6)}, CaveSpider.class),
    SILVERFISH(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 60), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 61)}, Silverfish.class),
    BLAZE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 61), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 4)}, Blaze.class),
    MAGMA_CUBE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 62), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 38)}, MagmaCube.class),
    ENDER_DRAGON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 63), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 17)}, EnderDragon.class),
    WITHER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 64), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 83)}, Wither.class),
    BAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 65), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 3)}, Bat.class),
    WITCH(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 66), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 82)}, Witch.class),
    GUARDIAN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 68), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 28)}, Guardian.class),
    SHULKER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 69), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 59)}, Shulker.class),
    PIG(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 90), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 51)}, Pig.class),
    SHEEP(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 91), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 58)}, Sheep.class),
    COW(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 92), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 9)}, Cow.class),
    CHICKEN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 93), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 7)}, Chicken.class),
    SQUID(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 94), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 70)}, Squid.class),
    WOLF(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 95), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 86)}, Wolf.class),
    MOOSHROOM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 96), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 47)}, Mooshroom.class),
    SNOW_GOLEM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 97), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 66)}, SnowGolem.class),
    OCELOT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 98), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 48)}, Ocelot.class),
    IRON_GOLEM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 99), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 80)}, IronGolem.class),
    HORSE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 100), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 29)}, Horse.class),
    RABBIT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 101), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 56)}, Rabbit.class),
    POLAR_BEAR(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 102), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 54)}, PolarBear.class),
    LLAMA(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 103), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 36)}, Llama.class),
    LLAMA_SPIT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_10, 104), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 37)}, LlamaSpit.class),
    PLAYER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, -1), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 92)}, OtherPlayer.class),
    PARROT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_12_2, 105), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 50)}, Parrot.class),
    VILLAGER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 120), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 79)}, Villager.class),
    ENDER_CRYSTAL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 200), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 16)}, EnderCrystal.class),
    COD(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 8)}, Cod.class),
    DOLPHIN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 12)}, Dolphin.class),
    DROWNED(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 14)}, Drowned.class),
    ENDERMITE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 67), new MapSet<>(ProtocolVersion.VERSION_1_13_2, 19)}, Endermite.class),
    MINECART_FURNACE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 42)}, MinecartFurnace.class),
    MINECART_HOPPER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 43)}, MinecartHopper.class),
    MINECART_SPAWNER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 44)}, MinecartSpawner.class),
    MINECART_TNT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 45)}, MinecartTNT.class),
    PUFFERFISH(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 52)}, Pufferfish.class),
    SALMON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 57)}, Salmon.class),
    TROPICAL_FISH(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 72)}, TropicalFish.class),
    TURTLE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 73)}, Turtle.class),
    PHANTOM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 90)}, Phantom.class),
    LIGHTNING_BOLT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 91)}, LightningBolt.class),
    FISHING_BOBBER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 93)}, FishingBobber.class),
    TRIDENT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_13_2, 94)}, Trident.class),

    // not a thing anymore
    FALLING_DRAGON_EGG(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, -1)}, FallingDragonEgg.class),
    FIRE_CHARGE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, -1)}, FireCharge.class),
    FISHING_FLOAT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, -1)}, FishingFloat.class);

    final VersionValueMap<Integer> valueMap;
    final Class<? extends Entity> clazz;

    Entities(MapSet<ProtocolVersion, Integer>[] values, Class<? extends Entity> clazz) {
        valueMap = new VersionValueMap<>(values, true);
        this.clazz = clazz;
    }


    public static Entities byId(int id, ProtocolVersion version) {
        for (Entities entity : values()) {
            if (entity.getValueMap().get(version) == id) {
                return entity;
            }
        }
        return null;
    }

    public VersionValueMap<Integer> getValueMap() {
        return valueMap;
    }

    public Class<? extends Entity> getClazz() {
        return clazz;
    }
}
