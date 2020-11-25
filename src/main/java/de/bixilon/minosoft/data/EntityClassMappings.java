/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.entities.EvokerFangs;
import de.bixilon.minosoft.data.entities.entities.AreaEffectCloud;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.ExperienceOrb;
import de.bixilon.minosoft.data.entities.entities.LightningBolt;
import de.bixilon.minosoft.data.entities.entities.ambient.Bat;
import de.bixilon.minosoft.data.entities.entities.animal.*;
import de.bixilon.minosoft.data.entities.entities.animal.hoglin.Hoglin;
import de.bixilon.minosoft.data.entities.entities.animal.horse.*;
import de.bixilon.minosoft.data.entities.entities.animal.water.*;
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal;
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EnderDragon;
import de.bixilon.minosoft.data.entities.entities.boss.wither.WitherBoss;
import de.bixilon.minosoft.data.entities.entities.decoration.ArmorStand;
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame;
import de.bixilon.minosoft.data.entities.entities.decoration.LeashFenceKnotEntity;
import de.bixilon.minosoft.data.entities.entities.decoration.Painting;
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock;
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity;
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT;
import de.bixilon.minosoft.data.entities.entities.monster.*;
import de.bixilon.minosoft.data.entities.entities.monster.piglin.Piglin;
import de.bixilon.minosoft.data.entities.entities.monster.piglin.PiglinBrute;
import de.bixilon.minosoft.data.entities.entities.monster.raid.*;
import de.bixilon.minosoft.data.entities.entities.npc.Villager;
import de.bixilon.minosoft.data.entities.entities.npc.WanderingTrader;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.entities.entities.projectile.*;
import de.bixilon.minosoft.data.entities.entities.vehicle.*;
import de.bixilon.minosoft.data.mappings.ModIdentifier;

public final class EntityClassMappings {
    public static final HashBiMap<Class<? extends Entity>, ModIdentifier> ENTITY_CLASS_MAPPINGS = HashBiMap.create();

    static {
        ENTITY_CLASS_MAPPINGS.put(AreaEffectCloud.class, new ModIdentifier("area_effect_cloud"));
        ENTITY_CLASS_MAPPINGS.put(ArmorStand.class, new ModIdentifier("armor_stand"));
        ENTITY_CLASS_MAPPINGS.put(Arrow.class, new ModIdentifier("arrow"));
        ENTITY_CLASS_MAPPINGS.put(Bat.class, new ModIdentifier("bat"));
        ENTITY_CLASS_MAPPINGS.put(Bee.class, new ModIdentifier("bee"));
        ENTITY_CLASS_MAPPINGS.put(Blaze.class, new ModIdentifier("blaze"));
        ENTITY_CLASS_MAPPINGS.put(Boat.class, new ModIdentifier("boat"));
        ENTITY_CLASS_MAPPINGS.put(Cat.class, new ModIdentifier("cat"));
        ENTITY_CLASS_MAPPINGS.put(CaveSpider.class, new ModIdentifier("cave_spider"));
        ENTITY_CLASS_MAPPINGS.put(Chicken.class, new ModIdentifier("chicken"));
        ENTITY_CLASS_MAPPINGS.put(Cod.class, new ModIdentifier("cod"));
        ENTITY_CLASS_MAPPINGS.put(Cow.class, new ModIdentifier("cow"));
        ENTITY_CLASS_MAPPINGS.put(Creeper.class, new ModIdentifier("creeper"));
        ENTITY_CLASS_MAPPINGS.put(Dolphin.class, new ModIdentifier("dolphin"));
        ENTITY_CLASS_MAPPINGS.put(Donkey.class, new ModIdentifier("donkey"));
        ENTITY_CLASS_MAPPINGS.put(DragonFireball.class, new ModIdentifier("dragon_fireball"));
        ENTITY_CLASS_MAPPINGS.put(Drowned.class, new ModIdentifier("drowned"));
        ENTITY_CLASS_MAPPINGS.put(ElderGuardian.class, new ModIdentifier("elder_guardian"));
        ENTITY_CLASS_MAPPINGS.put(EndCrystal.class, new ModIdentifier("end_crystal"));
        ENTITY_CLASS_MAPPINGS.put(EnderDragon.class, new ModIdentifier("ender_dragon"));
        ENTITY_CLASS_MAPPINGS.put(Enderman.class, new ModIdentifier("enderman"));
        ENTITY_CLASS_MAPPINGS.put(Endermite.class, new ModIdentifier("endermite"));
        ENTITY_CLASS_MAPPINGS.put(Evoker.class, new ModIdentifier("evoker"));
        ENTITY_CLASS_MAPPINGS.put(EvokerFangs.class, new ModIdentifier("evoker_fangs"));
        ENTITY_CLASS_MAPPINGS.put(ExperienceOrb.class, new ModIdentifier("experience_orb"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEyeOfEnder.class, new ModIdentifier("eye_of_ender"));
        ENTITY_CLASS_MAPPINGS.put(FallingBlock.class, new ModIdentifier("falling_block"));
        ENTITY_CLASS_MAPPINGS.put(FireworkRocketEntity.class, new ModIdentifier("firework_rocket"));
        ENTITY_CLASS_MAPPINGS.put(Fox.class, new ModIdentifier("fox"));
        ENTITY_CLASS_MAPPINGS.put(Ghast.class, new ModIdentifier("ghast"));
        ENTITY_CLASS_MAPPINGS.put(Giant.class, new ModIdentifier("giant"));
        ENTITY_CLASS_MAPPINGS.put(Guardian.class, new ModIdentifier("guardian"));
        ENTITY_CLASS_MAPPINGS.put(Hoglin.class, new ModIdentifier("hoglin"));
        ENTITY_CLASS_MAPPINGS.put(Horse.class, new ModIdentifier("horse"));
        ENTITY_CLASS_MAPPINGS.put(Husk.class, new ModIdentifier("husk"));
        ENTITY_CLASS_MAPPINGS.put(Illusioner.class, new ModIdentifier("illusioner"));
        ENTITY_CLASS_MAPPINGS.put(IronGolem.class, new ModIdentifier("iron_golem"));
        ENTITY_CLASS_MAPPINGS.put(ItemEntity.class, new ModIdentifier("item"));
        ENTITY_CLASS_MAPPINGS.put(ItemFrame.class, new ModIdentifier("item_frame"));
        ENTITY_CLASS_MAPPINGS.put(LargeFireball.class, new ModIdentifier("fireball"));
        ENTITY_CLASS_MAPPINGS.put(LeashFenceKnotEntity.class, new ModIdentifier("leash_knot"));
        ENTITY_CLASS_MAPPINGS.put(LightningBolt.class, new ModIdentifier("lightning_bolt"));
        ENTITY_CLASS_MAPPINGS.put(Llama.class, new ModIdentifier("llama"));
        ENTITY_CLASS_MAPPINGS.put(LlamaSpit.class, new ModIdentifier("llama_spit"));
        ENTITY_CLASS_MAPPINGS.put(MagmaCube.class, new ModIdentifier("magma_cube"));
        ENTITY_CLASS_MAPPINGS.put(Minecart.class, new ModIdentifier("minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartChest.class, new ModIdentifier("chest_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartCommandBlock.class, new ModIdentifier("command_block_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartFurnace.class, new ModIdentifier("furnace_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartHopper.class, new ModIdentifier("hopper_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartSpawner.class, new ModIdentifier("spawner_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartTNT.class, new ModIdentifier("tnt_minecart"));
        ENTITY_CLASS_MAPPINGS.put(Mule.class, new ModIdentifier("mule"));
        ENTITY_CLASS_MAPPINGS.put(Mooshroom.class, new ModIdentifier("mooshroom"));
        ENTITY_CLASS_MAPPINGS.put(Ocelot.class, new ModIdentifier("ocelot"));
        ENTITY_CLASS_MAPPINGS.put(Painting.class, new ModIdentifier("painting"));
        ENTITY_CLASS_MAPPINGS.put(Panda.class, new ModIdentifier("panda"));
        ENTITY_CLASS_MAPPINGS.put(Parrot.class, new ModIdentifier("parrot"));
        ENTITY_CLASS_MAPPINGS.put(Phantom.class, new ModIdentifier("phantom"));
        ENTITY_CLASS_MAPPINGS.put(Pig.class, new ModIdentifier("pig"));
        ENTITY_CLASS_MAPPINGS.put(Piglin.class, new ModIdentifier("piglin"));
        ENTITY_CLASS_MAPPINGS.put(PiglinBrute.class, new ModIdentifier("piglin_brute"));
        ENTITY_CLASS_MAPPINGS.put(Pillager.class, new ModIdentifier("pillager"));
        ENTITY_CLASS_MAPPINGS.put(PolarBear.class, new ModIdentifier("polar_bear"));
        ENTITY_CLASS_MAPPINGS.put(PrimedTNT.class, new ModIdentifier("tnt"));
        ENTITY_CLASS_MAPPINGS.put(PufferFish.class, new ModIdentifier("pufferfish"));
        ENTITY_CLASS_MAPPINGS.put(Rabbit.class, new ModIdentifier("rabbit"));
        ENTITY_CLASS_MAPPINGS.put(Ravenger.class, new ModIdentifier("ravager"));
        ENTITY_CLASS_MAPPINGS.put(Salmon.class, new ModIdentifier("salmon"));
        ENTITY_CLASS_MAPPINGS.put(Sheep.class, new ModIdentifier("sheep"));
        ENTITY_CLASS_MAPPINGS.put(Shulker.class, new ModIdentifier("shulker"));
        ENTITY_CLASS_MAPPINGS.put(ShulkerBullet.class, new ModIdentifier("shulker_bullet"));
        ENTITY_CLASS_MAPPINGS.put(Silverfish.class, new ModIdentifier("silverfish"));
        ENTITY_CLASS_MAPPINGS.put(Skeleton.class, new ModIdentifier("skeleton"));
        ENTITY_CLASS_MAPPINGS.put(SkeletonHorse.class, new ModIdentifier("skeleton_horse"));
        ENTITY_CLASS_MAPPINGS.put(Slime.class, new ModIdentifier("slime"));
        ENTITY_CLASS_MAPPINGS.put(SmallFireball.class, new ModIdentifier("small_fireball"));
        ENTITY_CLASS_MAPPINGS.put(SnowGolem.class, new ModIdentifier("snow_golem"));
        ENTITY_CLASS_MAPPINGS.put(ThrownSnowball.class, new ModIdentifier("snowball"));
        ENTITY_CLASS_MAPPINGS.put(SpectralArrow.class, new ModIdentifier("spectral_arrow"));
        ENTITY_CLASS_MAPPINGS.put(Spider.class, new ModIdentifier("spider"));
        ENTITY_CLASS_MAPPINGS.put(Squid.class, new ModIdentifier("squid"));
        ENTITY_CLASS_MAPPINGS.put(Stray.class, new ModIdentifier("stray"));
        ENTITY_CLASS_MAPPINGS.put(Strider.class, new ModIdentifier("strider"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEgg.class, new ModIdentifier("egg"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEnderPearl.class, new ModIdentifier("ender_pearl"));
        ENTITY_CLASS_MAPPINGS.put(ThrownExperienceBottle.class, new ModIdentifier("experience_bottle"));
        ENTITY_CLASS_MAPPINGS.put(ThrownPotion.class, new ModIdentifier("potion"));
        ENTITY_CLASS_MAPPINGS.put(ThrownTrident.class, new ModIdentifier("trident"));
        ENTITY_CLASS_MAPPINGS.put(TraderLlama.class, new ModIdentifier("trader_llama"));
        ENTITY_CLASS_MAPPINGS.put(TropicalFish.class, new ModIdentifier("tropical_fish"));
        ENTITY_CLASS_MAPPINGS.put(Turtle.class, new ModIdentifier("turtle"));
        ENTITY_CLASS_MAPPINGS.put(Vex.class, new ModIdentifier("vex"));
        ENTITY_CLASS_MAPPINGS.put(Villager.class, new ModIdentifier("villager"));
        ENTITY_CLASS_MAPPINGS.put(Vindicator.class, new ModIdentifier("vindicator"));
        ENTITY_CLASS_MAPPINGS.put(WanderingTrader.class, new ModIdentifier("wandering_trader"));
        ENTITY_CLASS_MAPPINGS.put(Witch.class, new ModIdentifier("witch"));
        ENTITY_CLASS_MAPPINGS.put(WitherBoss.class, new ModIdentifier("wither"));
        ENTITY_CLASS_MAPPINGS.put(WitherSkeleton.class, new ModIdentifier("wither_skeleton"));
        ENTITY_CLASS_MAPPINGS.put(WitherSkull.class, new ModIdentifier("wither_skull"));
        ENTITY_CLASS_MAPPINGS.put(Wolf.class, new ModIdentifier("wolf"));
        ENTITY_CLASS_MAPPINGS.put(Zoglin.class, new ModIdentifier("zoglin"));
        ENTITY_CLASS_MAPPINGS.put(Zombie.class, new ModIdentifier("zombie"));
        ENTITY_CLASS_MAPPINGS.put(ZombieHorse.class, new ModIdentifier("zombie_horse"));
        ENTITY_CLASS_MAPPINGS.put(ZombieVillager.class, new ModIdentifier("zombie_villager"));
        ENTITY_CLASS_MAPPINGS.put(ZombiePigman.class, new ModIdentifier("zombie_pigman"));
        ENTITY_CLASS_MAPPINGS.put(ZombifiedPiglin.class, new ModIdentifier("zombified_piglin"));
        ENTITY_CLASS_MAPPINGS.put(PlayerEntity.class, new ModIdentifier("player"));
        ENTITY_CLASS_MAPPINGS.put(FishingHook.class, new ModIdentifier("fishing_bobber"));
    }

    public static Class<? extends Entity> getByIdentifier(String mod, String identifier) {
        return ENTITY_CLASS_MAPPINGS.inverse().get(new ModIdentifier(mod, identifier));
    }

}
