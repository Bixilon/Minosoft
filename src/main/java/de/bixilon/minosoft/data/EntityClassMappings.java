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
import javafx.util.Pair;

public final class EntityClassMappings {
    public static final HashBiMap<Class<? extends Entity>, Pair<String, String>> ENTITY_CLASS_MAPPINGS = HashBiMap.create();

    static {
        ENTITY_CLASS_MAPPINGS.put(AreaEffectCloud.class, new Pair<>("minecraft", "area_effect_cloud"));
        ENTITY_CLASS_MAPPINGS.put(ArmorStand.class, new Pair<>("minecraft", "armor_stand"));
        ENTITY_CLASS_MAPPINGS.put(Arrow.class, new Pair<>("minecraft", "arrow"));
        ENTITY_CLASS_MAPPINGS.put(Bat.class, new Pair<>("minecraft", "bat"));
        ENTITY_CLASS_MAPPINGS.put(Bee.class, new Pair<>("minecraft", "bee"));
        ENTITY_CLASS_MAPPINGS.put(Blaze.class, new Pair<>("minecraft", "blaze"));
        ENTITY_CLASS_MAPPINGS.put(Boat.class, new Pair<>("minecraft", "boat"));
        ENTITY_CLASS_MAPPINGS.put(Cat.class, new Pair<>("minecraft", "cat"));
        ENTITY_CLASS_MAPPINGS.put(CaveSpider.class, new Pair<>("minecraft", "cave_spider"));
        ENTITY_CLASS_MAPPINGS.put(Chicken.class, new Pair<>("minecraft", "chicken"));
        ENTITY_CLASS_MAPPINGS.put(Cod.class, new Pair<>("minecraft", "cod"));
        ENTITY_CLASS_MAPPINGS.put(Cow.class, new Pair<>("minecraft", "cow"));
        ENTITY_CLASS_MAPPINGS.put(Creeper.class, new Pair<>("minecraft", "creeper"));
        ENTITY_CLASS_MAPPINGS.put(Dolphin.class, new Pair<>("minecraft", "dolphin"));
        ENTITY_CLASS_MAPPINGS.put(Donkey.class, new Pair<>("minecraft", "donkey"));
        ENTITY_CLASS_MAPPINGS.put(DragonFireball.class, new Pair<>("minecraft", "dragon_fireball"));
        ENTITY_CLASS_MAPPINGS.put(Drowned.class, new Pair<>("minecraft", "drowned"));
        ENTITY_CLASS_MAPPINGS.put(ElderGuardian.class, new Pair<>("minecraft", "elder_guardian"));
        ENTITY_CLASS_MAPPINGS.put(EndCrystal.class, new Pair<>("minecraft", "end_crystal"));
        ENTITY_CLASS_MAPPINGS.put(EnderDragon.class, new Pair<>("minecraft", "ender_dragon"));
        ENTITY_CLASS_MAPPINGS.put(Enderman.class, new Pair<>("minecraft", "enderman"));
        ENTITY_CLASS_MAPPINGS.put(Endermite.class, new Pair<>("minecraft", "endermite"));
        ENTITY_CLASS_MAPPINGS.put(Evoker.class, new Pair<>("minecraft", "evoker"));
        ENTITY_CLASS_MAPPINGS.put(EvokerFangs.class, new Pair<>("minecraft", "evoker_fangs"));
        ENTITY_CLASS_MAPPINGS.put(ExperienceOrb.class, new Pair<>("minecraft", "experience_orb"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEyeOfEnder.class, new Pair<>("minecraft", "eye_of_ender"));
        ENTITY_CLASS_MAPPINGS.put(FallingBlock.class, new Pair<>("minecraft", "falling_block"));
        ENTITY_CLASS_MAPPINGS.put(FireworkRocketEntity.class, new Pair<>("minecraft", "firework_rocket"));
        ENTITY_CLASS_MAPPINGS.put(Fox.class, new Pair<>("minecraft", "fox"));
        ENTITY_CLASS_MAPPINGS.put(Ghast.class, new Pair<>("minecraft", "ghast"));
        ENTITY_CLASS_MAPPINGS.put(Giant.class, new Pair<>("minecraft", "giant"));
        ENTITY_CLASS_MAPPINGS.put(Guardian.class, new Pair<>("minecraft", "guardian"));
        ENTITY_CLASS_MAPPINGS.put(Hoglin.class, new Pair<>("minecraft", "hoglin"));
        ENTITY_CLASS_MAPPINGS.put(Horse.class, new Pair<>("minecraft", "horse"));
        ENTITY_CLASS_MAPPINGS.put(Husk.class, new Pair<>("minecraft", "husk"));
        ENTITY_CLASS_MAPPINGS.put(Illusioner.class, new Pair<>("minecraft", "illusioner"));
        ENTITY_CLASS_MAPPINGS.put(IronGolem.class, new Pair<>("minecraft", "iron_golem"));
        ENTITY_CLASS_MAPPINGS.put(ItemEntity.class, new Pair<>("minecraft", "item"));
        ENTITY_CLASS_MAPPINGS.put(ItemFrame.class, new Pair<>("minecraft", "item_frame"));
        ENTITY_CLASS_MAPPINGS.put(LargeFireball.class, new Pair<>("minecraft", "fireball"));
        ENTITY_CLASS_MAPPINGS.put(LeashFenceKnotEntity.class, new Pair<>("minecraft", "leash_knot"));
        ENTITY_CLASS_MAPPINGS.put(LightningBolt.class, new Pair<>("minecraft", "lightning_bolt"));
        ENTITY_CLASS_MAPPINGS.put(Llama.class, new Pair<>("minecraft", "llama"));
        ENTITY_CLASS_MAPPINGS.put(LlamaSpit.class, new Pair<>("minecraft", "llama_spit"));
        ENTITY_CLASS_MAPPINGS.put(MagmaCube.class, new Pair<>("minecraft", "magma_cube"));
        ENTITY_CLASS_MAPPINGS.put(Minecart.class, new Pair<>("minecraft", "minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartChest.class, new Pair<>("minecraft", "chest_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartCommandBlock.class, new Pair<>("minecraft", "command_block_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartFurnace.class, new Pair<>("minecraft", "furnace_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartHopper.class, new Pair<>("minecraft", "hopper_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartSpawner.class, new Pair<>("minecraft", "spawner_minecart"));
        ENTITY_CLASS_MAPPINGS.put(MinecartTNT.class, new Pair<>("minecraft", "tnt_minecart"));
        ENTITY_CLASS_MAPPINGS.put(Mule.class, new Pair<>("minecraft", "mule"));
        ENTITY_CLASS_MAPPINGS.put(Mooshroom.class, new Pair<>("minecraft", "mooshroom"));
        ENTITY_CLASS_MAPPINGS.put(Ocelot.class, new Pair<>("minecraft", "ocelot"));
        ENTITY_CLASS_MAPPINGS.put(Painting.class, new Pair<>("minecraft", "painting"));
        ENTITY_CLASS_MAPPINGS.put(Panda.class, new Pair<>("minecraft", "panda"));
        ENTITY_CLASS_MAPPINGS.put(Parrot.class, new Pair<>("minecraft", "parrot"));
        ENTITY_CLASS_MAPPINGS.put(Phantom.class, new Pair<>("minecraft", "phantom"));
        ENTITY_CLASS_MAPPINGS.put(Pig.class, new Pair<>("minecraft", "pig"));
        ENTITY_CLASS_MAPPINGS.put(Piglin.class, new Pair<>("minecraft", "piglin"));
        ENTITY_CLASS_MAPPINGS.put(PiglinBrute.class, new Pair<>("minecraft", "piglin_brute"));
        ENTITY_CLASS_MAPPINGS.put(Pillager.class, new Pair<>("minecraft", "pillager"));
        ENTITY_CLASS_MAPPINGS.put(PolarBear.class, new Pair<>("minecraft", "polar_bear"));
        ENTITY_CLASS_MAPPINGS.put(PrimedTNT.class, new Pair<>("minecraft", "tnt"));
        ENTITY_CLASS_MAPPINGS.put(PufferFish.class, new Pair<>("minecraft", "pufferfish"));
        ENTITY_CLASS_MAPPINGS.put(Rabbit.class, new Pair<>("minecraft", "rabbit"));
        ENTITY_CLASS_MAPPINGS.put(Ravenger.class, new Pair<>("minecraft", "ravager"));
        ENTITY_CLASS_MAPPINGS.put(Salmon.class, new Pair<>("minecraft", "salmon"));
        ENTITY_CLASS_MAPPINGS.put(Sheep.class, new Pair<>("minecraft", "sheep"));
        ENTITY_CLASS_MAPPINGS.put(Shulker.class, new Pair<>("minecraft", "shulker"));
        ENTITY_CLASS_MAPPINGS.put(ShulkerBullet.class, new Pair<>("minecraft", "shulker_bullet"));
        ENTITY_CLASS_MAPPINGS.put(Silverfish.class, new Pair<>("minecraft", "silverfish"));
        ENTITY_CLASS_MAPPINGS.put(Skeleton.class, new Pair<>("minecraft", "skeleton"));
        ENTITY_CLASS_MAPPINGS.put(SkeletonHorse.class, new Pair<>("minecraft", "skeleton_horse"));
        ENTITY_CLASS_MAPPINGS.put(Slime.class, new Pair<>("minecraft", "slime"));
        ENTITY_CLASS_MAPPINGS.put(SmallFireball.class, new Pair<>("minecraft", "small_fireball"));
        ENTITY_CLASS_MAPPINGS.put(SnowGolem.class, new Pair<>("minecraft", "snow_golem"));
        ENTITY_CLASS_MAPPINGS.put(ThrownSnowball.class, new Pair<>("minecraft", "snowball"));
        ENTITY_CLASS_MAPPINGS.put(SpectralArrow.class, new Pair<>("minecraft", "spectral_arrow"));
        ENTITY_CLASS_MAPPINGS.put(Spider.class, new Pair<>("minecraft", "spider"));
        ENTITY_CLASS_MAPPINGS.put(Squid.class, new Pair<>("minecraft", "squid"));
        ENTITY_CLASS_MAPPINGS.put(Stray.class, new Pair<>("minecraft", "stray"));
        ENTITY_CLASS_MAPPINGS.put(Strider.class, new Pair<>("minecraft", "strider"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEgg.class, new Pair<>("minecraft", "egg"));
        ENTITY_CLASS_MAPPINGS.put(ThrownEnderPearl.class, new Pair<>("minecraft", "ender_pearl"));
        ENTITY_CLASS_MAPPINGS.put(ThrownExperienceBottle.class, new Pair<>("minecraft", "experience_bottle"));
        ENTITY_CLASS_MAPPINGS.put(ThrownPotion.class, new Pair<>("minecraft", "potion"));
        ENTITY_CLASS_MAPPINGS.put(ThrownTrident.class, new Pair<>("minecraft", "trident"));
        ENTITY_CLASS_MAPPINGS.put(TraderLlama.class, new Pair<>("minecraft", "trader_llama"));
        ENTITY_CLASS_MAPPINGS.put(TropicalFish.class, new Pair<>("minecraft", "tropical_fish"));
        ENTITY_CLASS_MAPPINGS.put(Turtle.class, new Pair<>("minecraft", "turtle"));
        ENTITY_CLASS_MAPPINGS.put(Vex.class, new Pair<>("minecraft", "vex"));
        ENTITY_CLASS_MAPPINGS.put(Villager.class, new Pair<>("minecraft", "villager"));
        ENTITY_CLASS_MAPPINGS.put(Vindicator.class, new Pair<>("minecraft", "vindicator"));
        ENTITY_CLASS_MAPPINGS.put(WanderingTrader.class, new Pair<>("minecraft", "wandering_trader"));
        ENTITY_CLASS_MAPPINGS.put(Witch.class, new Pair<>("minecraft", "witch"));
        ENTITY_CLASS_MAPPINGS.put(WitherBoss.class, new Pair<>("minecraft", "wither"));
        ENTITY_CLASS_MAPPINGS.put(WitherSkeleton.class, new Pair<>("minecraft", "wither_skeleton"));
        ENTITY_CLASS_MAPPINGS.put(WitherSkull.class, new Pair<>("minecraft", "wither_skull"));
        ENTITY_CLASS_MAPPINGS.put(Wolf.class, new Pair<>("minecraft", "wolf"));
        ENTITY_CLASS_MAPPINGS.put(Zoglin.class, new Pair<>("minecraft", "zoglin"));
        ENTITY_CLASS_MAPPINGS.put(Zombie.class, new Pair<>("minecraft", "zombie"));
        ENTITY_CLASS_MAPPINGS.put(ZombieHorse.class, new Pair<>("minecraft", "zombie_horse"));
        ENTITY_CLASS_MAPPINGS.put(ZombieVillager.class, new Pair<>("minecraft", "zombie_villager"));
        ENTITY_CLASS_MAPPINGS.put(ZombifiedPiglin.class, new Pair<>("minecraft", "zombified_piglin"));
        ENTITY_CLASS_MAPPINGS.put(PlayerEntity.class, new Pair<>("minecraft", "player"));
        ENTITY_CLASS_MAPPINGS.put(FishingHook.class, new Pair<>("minecraft", "fishing_bobber"));
    }

    public static Class<? extends Entity> getByIdentifier(String mod, String identifier) {
        return ENTITY_CLASS_MAPPINGS.inverse().get(new Pair<>(mod, identifier));
    }

}
