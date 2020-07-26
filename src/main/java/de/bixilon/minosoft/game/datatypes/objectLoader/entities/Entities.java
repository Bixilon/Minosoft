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

package de.bixilon.minosoft.game.datatypes.objectLoader.entities;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.entities.Entity;
import de.bixilon.minosoft.game.datatypes.entities.ExperienceOrb;
import de.bixilon.minosoft.game.datatypes.entities.mob.*;
import de.bixilon.minosoft.game.datatypes.entities.objects.*;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class Entities {

    static HashBiMap<String, Class<? extends Entity>> entityClassMap = HashBiMap.create();
    static HashMap<ProtocolVersion, HashBiMap<Integer, String>> entityMapping = new HashMap<>();

    static {
        registerEntityClass("minecraft:item", ItemStack.class);
        registerEntityClass("minecraft:experience_orb", ExperienceOrb.class);
        registerEntityClass("minecraft:area_effect_cloud", AreaEffectCloud.class);
        registerEntityClass("minecraft:elder_guardian", ElderGuardian.class);
        registerEntityClass("minecraft:wither_skeleton", WitherSkeleton.class);
        registerEntityClass("minecraft:stray", StraySkeleton.class);
        registerEntityClass("minecraft:egg", ThrownEgg.class);
        registerEntityClass("minecraft:leash_knot", LeashKnot.class);
        registerEntityClass("minecraft:painting", Painting.class);
        registerEntityClass("minecraft:arrow", Arrow.class);
        registerEntityClass("minecraft:snowball", Snowball.class);
        registerEntityClass("minecraft:fireball", Fireball.class);
        registerEntityClass("minecraft:small_fireball", SmallFireball.class);
        registerEntityClass("minecraft:ender_pearl", ThrownEnderpearl.class);
        registerEntityClass("minecraft:eye_of_ender", EyeOfEnder.class);
        registerEntityClass("minecraft:potion", ThrownPotion.class);
        registerEntityClass("minecraft:experience_bottle", ThrownExperienceBottle.class);
        registerEntityClass("minecraft:item_frame", ItemFrame.class);
        registerEntityClass("minecraft:wither_skull", WitherSkull.class);
        registerEntityClass("minecraft:tnt", PrimedTNT.class);
        registerEntityClass("minecraft:falling_block", FallingBlock.class);
        registerEntityClass("minecraft:firework", Firework.class);
        registerEntityClass("minecraft:husk", Husk.class);
        registerEntityClass("minecraft:spectral_arrow", SpectralArrow.class);
        registerEntityClass("minecraft:shulker_bullet", ShulkerBullet.class);
        registerEntityClass("minecraft:dragon_fireball", DragonFireball.class);
        registerEntityClass("minecraft:zombie_villager", ZombieVillager.class);
        registerEntityClass("minecraft:skeleton_horse", SkeletonHorse.class);
        registerEntityClass("minecraft:zombie_horse", ZombieHorse.class);
        registerEntityClass("minecraft:armor_stand", ArmorStand.class);
        registerEntityClass("minecraft:donkey", Donkey.class);
        registerEntityClass("minecraft:mule", Mule.class);
        registerEntityClass("minecraft:evoker_fangs", EvocationFangs.class);
        registerEntityClass("minecraft:evoker", Evoker.class);
        registerEntityClass("minecraft:vex", Vex.class);
        registerEntityClass("minecraft:vindicator", Vindicator.class);
        registerEntityClass("minecraft:illusioner", Illusioner.class);
        registerEntityClass("minecraft:command_block_minecart", MinecartCommandBlock.class);
        registerEntityClass("minecraft:boat", Boat.class);
        registerEntityClass("minecraft:minecart", Minecart.class);
        registerEntityClass("minecraft:chest_minecart", MinecartChest.class);
        registerEntityClass("minecraft:creeper", Creeper.class);
        registerEntityClass("minecraft:skeleton", Skeleton.class);
        registerEntityClass("minecraft:spider", Spider.class);
        registerEntityClass("minecraft:giant", GiantZombie.class);
        registerEntityClass("minecraft:zombie", Zombie.class);
        registerEntityClass("minecraft:slime", Slime.class);
        registerEntityClass("minecraft:ghast", Ghast.class);
        registerEntityClass("minecraft:zombie_pigman", ZombiePigman.class);
        registerEntityClass("minecraft:enderman", Enderman.class);
        registerEntityClass("minecraft:cave_spider", CaveSpider.class);
        registerEntityClass("minecraft:silverfish", Silverfish.class);
        registerEntityClass("minecraft:blaze", Blaze.class);
        registerEntityClass("minecraft:magma_cube", MagmaCube.class);
        registerEntityClass("minecraft:ender_dragon", EnderDragon.class);
        registerEntityClass("minecraft:wither", Wither.class);
        registerEntityClass("minecraft:bat", Bat.class);
        registerEntityClass("minecraft:witch", Witch.class);
        registerEntityClass("minecraft:guardian", Guardian.class);
        registerEntityClass("minecraft:shulker", Shulker.class);
        registerEntityClass("minecraft:pig", Pig.class);
        registerEntityClass("minecraft:sheep", Sheep.class);
        registerEntityClass("minecraft:cow", Cow.class);
        registerEntityClass("minecraft:chicken", Chicken.class);
        registerEntityClass("minecraft:squid", Squid.class);
        registerEntityClass("minecraft:wolf", Wolf.class);
        registerEntityClass("minecraft:mooshroom", Mooshroom.class);
        registerEntityClass("minecraft:snow_golem", SnowGolem.class);
        registerEntityClass("minecraft:ocelot", Ocelot.class);
        registerEntityClass("minecraft:iron_golem", IronGolem.class);
        registerEntityClass("minecraft:horse", Horse.class);
        registerEntityClass("minecraft:rabbit", Rabbit.class);
        registerEntityClass("minecraft:polar_bear", PolarBear.class);
        registerEntityClass("minecraft:llama", Llama.class);
        registerEntityClass("minecraft:llama_spit", LlamaSpit.class);
        registerEntityClass("minecraft:player", OtherPlayer.class);
        registerEntityClass("minecraft:parrot", Parrot.class);
        registerEntityClass("minecraft:villager", Villager.class);
        registerEntityClass("minecraft:end_crystal", EnderCrystal.class);
        registerEntityClass("minecraft:cod", Cod.class);
        registerEntityClass("minecraft:dolphin", Dolphin.class);
        registerEntityClass("minecraft:drowned", Drowned.class);
        registerEntityClass("minecraft:endermite", Endermite.class);
        registerEntityClass("minecraft:furnace_minecart", MinecartFurnace.class);
        registerEntityClass("minecraft:hopper_minecart", MinecartHopper.class);
        registerEntityClass("minecraft:spawner_minecart", MinecartSpawner.class);
        registerEntityClass("minecraft:tnt_minecart", MinecartTNT.class);
        registerEntityClass("minecraft:pufferfish", Pufferfish.class);
        registerEntityClass("minecraft:salmon", Salmon.class);
        registerEntityClass("minecraft:tropical_fish", TropicalFish.class);
        registerEntityClass("minecraft:turtle", Turtle.class);
        registerEntityClass("minecraft:phantom", Phantom.class);
        registerEntityClass("minecraft:lightning_bolt", LightningBolt.class);
        registerEntityClass("minecraft:fishing_bobber", FishingBobber.class);
        registerEntityClass("minecraft:trident", Trident.class);
        registerEntityClass("minecraft:fox", Fox.class);
        registerEntityClass("minecraft:panda", Panda.class);
        registerEntityClass("minecraft:cat", Cat.class);
        registerEntityClass("minecraft:wandering_trader", WanderingTrader.class);
        registerEntityClass("minecraft:pillager", Pillager.class);
        registerEntityClass("minecraft:ravager", Ravager.class);
        registerEntityClass("minecraft:bee", Bee.class);

        // not a thing anymore
        registerEntityClass("minecraft:falling_dragon_Egg", FallingDragonEgg.class);
        registerEntityClass("minecraft:fire_charge", FireCharge.class);
        registerEntityClass("minecraft:fishing_float", FishingFloat.class);
    }

    private static void registerEntityClass(String identifier, Class<? extends Entity> clazz) {
        entityClassMap.put(identifier, clazz);
    }


    public static Class<? extends Entity> byId(int id, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return getClassByIdentifier(entityMapping.get(version).get(id));
    }

    public static String getIdentifierByClass(Class<? extends Entity> clazz) {
        return entityClassMap.inverse().get(clazz);
    }


    public static Class<? extends Entity> getClassByIdentifier(String identifier) {
        return entityClassMap.get(identifier);
    }

    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, String> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            versionMapping.put(json.getAsJsonObject(identifierName).get("id").getAsInt(), mod + ":" + identifierName);
        }
        entityMapping.put(version, versionMapping);
    }
}
