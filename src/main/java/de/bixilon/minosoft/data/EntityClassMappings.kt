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
package de.bixilon.minosoft.data

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.entities.EvokerFangs
import de.bixilon.minosoft.data.entities.entities.AreaEffectCloud
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.ExperienceOrb
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.data.entities.entities.ambient.Bat
import de.bixilon.minosoft.data.entities.entities.animal.*
import de.bixilon.minosoft.data.entities.entities.animal.hoglin.Hoglin
import de.bixilon.minosoft.data.entities.entities.animal.horse.*
import de.bixilon.minosoft.data.entities.entities.animal.water.*
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EnderDragon
import de.bixilon.minosoft.data.entities.entities.boss.wither.WitherBoss
import de.bixilon.minosoft.data.entities.entities.decoration.ArmorStand
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame
import de.bixilon.minosoft.data.entities.entities.decoration.LeashFenceKnotEntity
import de.bixilon.minosoft.data.entities.entities.decoration.Painting
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.monster.*
import de.bixilon.minosoft.data.entities.entities.monster.piglin.Piglin
import de.bixilon.minosoft.data.entities.entities.monster.piglin.PiglinBrute
import de.bixilon.minosoft.data.entities.entities.monster.raid.*
import de.bixilon.minosoft.data.entities.entities.npc.villager.Villager
import de.bixilon.minosoft.data.entities.entities.npc.villager.WanderingTrader
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.projectile.*
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.data.mappings.ResourceLocation

object EntityClassMappings {
    @SuppressWarnings("deprecation")
    private val ENTITY_CLASS_MAPPINGS: HashBiMap<Class<out Entity>, ResourceLocation> = HashBiMap.create(
        mapOf(
            AreaEffectCloud::class.java to ResourceLocation("area_effect_cloud"),
            ArmorStand::class.java to ResourceLocation("armor_stand"),
            Arrow::class.java to ResourceLocation("arrow"),
            Axolotl::class.java to ResourceLocation("axolotl"),
            Bat::class.java to ResourceLocation("bat"),
            Bee::class.java to ResourceLocation("bee"),
            Blaze::class.java to ResourceLocation("blaze"),
            Boat::class.java to ResourceLocation("boat"),
            Cat::class.java to ResourceLocation("cat"),
            CaveSpider::class.java to ResourceLocation("cave_spider"),
            Chicken::class.java to ResourceLocation("chicken"),
            Cod::class.java to ResourceLocation("cod"),
            Cow::class.java to ResourceLocation("cow"),
            Creeper::class.java to ResourceLocation("creeper"),
            Dolphin::class.java to ResourceLocation("dolphin"),
            Donkey::class.java to ResourceLocation("donkey"),
            DragonFireball::class.java to ResourceLocation("dragon_fireball"),
            Drowned::class.java to ResourceLocation("drowned"),
            ElderGuardian::class.java to ResourceLocation("elder_guardian"),
            EndCrystal::class.java to ResourceLocation("end_crystal"),
            EnderDragon::class.java to ResourceLocation("ender_dragon"),
            Enderman::class.java to ResourceLocation("enderman"),
            Endermite::class.java to ResourceLocation("endermite"),
            Evoker::class.java to ResourceLocation("evoker"),
            EvokerFangs::class.java to ResourceLocation("evoker_fangs"),
            ExperienceOrb::class.java to ResourceLocation("experience_orb"),
            ThrownEyeOfEnder::class.java to ResourceLocation("eye_of_ender"),
            FallingBlock::class.java to ResourceLocation("falling_block"),
            FireworkRocketEntity::class.java to ResourceLocation("firework_rocket"),
            Fox::class.java to ResourceLocation("fox"),
            Ghast::class.java to ResourceLocation("ghast"),
            Giant::class.java to ResourceLocation("giant"),
            Guardian::class.java to ResourceLocation("guardian"),
            Hoglin::class.java to ResourceLocation("hoglin"),
            Horse::class.java to ResourceLocation("horse"),
            Husk::class.java to ResourceLocation("husk"),
            Illusioner::class.java to ResourceLocation("illusioner"),
            IronGolem::class.java to ResourceLocation("iron_golem"),
            ItemEntity::class.java to ResourceLocation("item"),
            ItemFrame::class.java to ResourceLocation("item_frame"),
            LargeFireball::class.java to ResourceLocation("fireball"),
            LeashFenceKnotEntity::class.java to ResourceLocation("leash_knot"),
            LightningBolt::class.java to ResourceLocation("lightning_bolt"),
            Llama::class.java to ResourceLocation("llama"),
            LlamaSpit::class.java to ResourceLocation("llama_spit"),
            MagmaCube::class.java to ResourceLocation("magma_cube"),
            Minecart::class.java to ResourceLocation("minecart"),
            MinecartChest::class.java to ResourceLocation("chest_minecart"),
            MinecartCommandBlock::class.java to ResourceLocation("command_block_minecart"),
            MinecartFurnace::class.java to ResourceLocation("furnace_minecart"),
            MinecartHopper::class.java to ResourceLocation("hopper_minecart"),
            MinecartSpawner::class.java to ResourceLocation("spawner_minecart"),
            MinecartTNT::class.java to ResourceLocation("tnt_minecart"),
            Mule::class.java to ResourceLocation("mule"),
            Mooshroom::class.java to ResourceLocation("mooshroom"),
            Ocelot::class.java to ResourceLocation("ocelot"),
            Painting::class.java to ResourceLocation("painting"),
            Panda::class.java to ResourceLocation("panda"),
            Parrot::class.java to ResourceLocation("parrot"),
            Phantom::class.java to ResourceLocation("phantom"),
            Pig::class.java to ResourceLocation("pig"),
            Piglin::class.java to ResourceLocation("piglin"),
            PiglinBrute::class.java to ResourceLocation("piglin_brute"),
            Pillager::class.java to ResourceLocation("pillager"),
            PolarBear::class.java to ResourceLocation("polar_bear"),
            PrimedTNT::class.java to ResourceLocation("tnt"),
            PufferFish::class.java to ResourceLocation("pufferfish"),
            Rabbit::class.java to ResourceLocation("rabbit"),
            Ravenger::class.java to ResourceLocation("ravager"),
            Salmon::class.java to ResourceLocation("salmon"),
            Sheep::class.java to ResourceLocation("sheep"),
            Shulker::class.java to ResourceLocation("shulker"),
            ShulkerBullet::class.java to ResourceLocation("shulker_bullet"),
            Silverfish::class.java to ResourceLocation("silverfish"),
            Skeleton::class.java to ResourceLocation("skeleton"),
            SkeletonHorse::class.java to ResourceLocation("skeleton_horse"),
            Slime::class.java to ResourceLocation("slime"),
            SmallFireball::class.java to ResourceLocation("small_fireball"),
            SnowGolem::class.java to ResourceLocation("snow_golem"),
            ThrownSnowball::class.java to ResourceLocation("snowball"),
            SpectralArrow::class.java to ResourceLocation("spectral_arrow"),
            Spider::class.java to ResourceLocation("spider"),
            Squid::class.java to ResourceLocation("squid"),
            Stray::class.java to ResourceLocation("stray"),
            Strider::class.java to ResourceLocation("strider"),
            ThrownEgg::class.java to ResourceLocation("egg"),
            ThrownEnderPearl::class.java to ResourceLocation("ender_pearl"),
            ThrownExperienceBottle::class.java to ResourceLocation("experience_bottle"),
            ThrownPotion::class.java to ResourceLocation("potion"),
            ThrownTrident::class.java to ResourceLocation("trident"),
            TraderLlama::class.java to ResourceLocation("trader_llama"),
            TropicalFish::class.java to ResourceLocation("tropical_fish"),
            Turtle::class.java to ResourceLocation("turtle"),
            Vex::class.java to ResourceLocation("vex"),
            Villager::class.java to ResourceLocation("villager"),
            Vindicator::class.java to ResourceLocation("vindicator"),
            WanderingTrader::class.java to ResourceLocation("wandering_trader"),
            Witch::class.java to ResourceLocation("witch"),
            WitherBoss::class.java to ResourceLocation("wither"),
            WitherSkeleton::class.java to ResourceLocation("wither_skeleton"),
            WitherSkull::class.java to ResourceLocation("wither_skull"),
            Wolf::class.java to ResourceLocation("wolf"),
            Zoglin::class.java to ResourceLocation("zoglin"),
            Zombie::class.java to ResourceLocation("zombie"),
            ZombieHorse::class.java to ResourceLocation("zombie_horse"),
            ZombieVillager::class.java to ResourceLocation("zombie_villager"),
            ZombiePigman::class.java to ResourceLocation("zombie_pigman"),
            ZombifiedPiglin::class.java to ResourceLocation("zombified_piglin"),
            PlayerEntity::class.java to ResourceLocation("player"),
            FishingHook::class.java to ResourceLocation("fishing_bobber"),
            GlowSquid::class.java to ResourceLocation("glow_squid")
        )
    )

    fun getByResourceLocation(namespace: String, path: String): Class<out Entity>? {
        return ENTITY_CLASS_MAPPINGS.inverse()[ResourceLocation(namespace, path)]
    }

    fun getByResourceLocation(resourceLocation: ResourceLocation): Class<out Entity>? {
        return ENTITY_CLASS_MAPPINGS.inverse()[resourceLocation]
    }
}
