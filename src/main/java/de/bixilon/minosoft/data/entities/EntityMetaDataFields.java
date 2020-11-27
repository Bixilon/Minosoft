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

package de.bixilon.minosoft.data.entities;

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.entities.entities.vehicle.Boat;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.data.text.ChatComponent;

public enum EntityMetaDataFields {
    ENTITY_FLAGS((byte) 0),
    ENTITY_AIR_SUPPLY(300),
    ENTITY_CUSTOM_NAME,
    ENTITY_CUSTOM_NAME_VISIBLE(false),
    ENTITY_SILENT(false),
    ENTITY_NO_GRAVITY(false),
    ENTITY_POSE(Poses.STANDING),
    ENTITY_TICKS_FROZEN(0),

    LIVING_ENTITY_FLAGS((byte) 0),
    LIVING_ENTITY_HEALTH(1.0f),
    LIVING_ENTITY_EFFECT_COLOR(0),
    LIVING_ENTITY_EFFECT_AMBIENCE(false),
    LIVING_ENTITY_ARROW_COUNT(0),
    LIVING_ENTITY_ABSORPTION_HEARTS(0),
    LIVING_ENTITY_BED_POSITION,

    MOB_FLAGS((byte) 0),

    ZOMBIE_IS_BABY(false),
    ZOMBIE_SPECIAL_TYPE(0),
    ZOMBIE_DROWNING_CONVERSION(false),

    THROWABLE_ITEM_PROJECTILE_ITEM,

    THROWN_POTION_ITEM,

    FALLING_BLOCK_SPAWN_POSITION,

    AREA_EFFECT_CLOUD_RADIUS(0.5f),
    AREA_EFFECT_CLOUD_COLOR(0),
    AREA_EFFECT_CLOUD_WAITING(false),
    AREA_EFFECT_CLOUD_PARTICLE(new ParticleData(new Particle("effect"))),

    ABSTRACT_ARROW_FLAGS((byte) 0),
    ABSTRACT_ARROW_PIERCE_LEVEL((byte) 0),
    ABSTRACT_ARROW_OWNER_UUID,


    FISHING_HOOK_HOOKED_ENTITY(0),
    FISHING_HOOK_CATCHABLE(false),

    ARROW_EFFECT_COLOR(-1),

    THROWN_TRIDENT_LOYALTY_LEVEL(0),
    THROWN_TRIDENT_FOIL(false),


    BOAT_HURT(0),
    BOAT_HURT_DIRECTION(1),
    BOAT_DAMAGE_TAKEN(0f),
    BOAT_MATERIAL(Boat.BoatMaterials.OAK.ordinal()),
    BOAT_PADDLE_LEFT(false),
    BOAT_PADDLE_RIGHT(false),
    BOAT_BUBBLE_TIME(0),

    END_CRYSTAL_BEAM_TARGET,
    END_CRYSTAL_SHOW_BOTTOM(true),

    FIREBALL_ITEM,

    WITHER_SKULL_DANGEROUS(false),

    FIREWORK_ROCKET_ENTITY_ITEM,
    FIREWORK_ROCKET_ENTITY_ATTACHED_ENTITY(0),
    FIREWORK_ROCKET_ENTITY_SHOT_AT_ANGLE(false),

    ITEM_FRAME_ITEM,
    ITEM_FRAME_ROTATION(0),

    ITEM_ITEM,

    PLAYER_ABSORPTION_HEARTS(0f),
    PLAYER_SCORE(0),
    PLAYER_SKIN_PARTS_FLAGS((byte) 0),
    PLAYER_SKIN_MAIN_HAND((byte) 1),
    PLAYER_LEFT_SHOULDER_DATA,
    PLAYER_RIGHT_SHOULDER_DATA,

    ARMOR_STAND_FLAGS((byte) 0),
    ARMOR_STAND_HEAD_ROTATION(new EntityRotation(0.0F, 0.0F, 0.0F)),
    ARMOR_STAND_BODY_ROTATION(new EntityRotation(0.0F, 0.0F, 0.0F)),
    ARMOR_STAND_LEFT_ARM_ROTATION(new EntityRotation(-10.0F, 0.0F, -10.0F)),
    ARMOR_STAND_RIGHT_ARM_ROTATION(new EntityRotation(-15.0F, 0.0F, 10.0F)),
    ARMOR_STAND_LEFT_LAG_ROTATION(new EntityRotation(-1.0F, 0.0F, -1.0F)),
    ARMOR_STAND_RIGHT_LAG_ROTATION(new EntityRotation(1.0F, 0.0F, 1.0F)),

    BAT_FLAGS((byte) 0),

    AGEABLE_IS_BABY(false),

    DOLPHIN_TREASURE_POSITION,
    DOLPHIN_HAS_FISH(false),
    DOLPHIN_MOISTNESS_LEVEL(2400),

    ABSTRACT_FISH_FROM_BUCKET(false),

    PUFFERFISH_PUFF_STATE(0),

    TROPICAL_FISH_VARIANT(0),

    ABSTRACT_HORSE_FLAGS((byte) 0),
    ABSTRACT_HORSE_OWNER_UUID,

    HORSE_VARIANT(0),

    ABSTRACT_CHESTED_HORSE_HAS_CHEST(false),

    LLAMA_STRENGTH(0),
    LLAMA_CARPET_COLOR(-1),
    LLAMA_VARIANT(0),

    BEE_FLAGS((byte) 0),
    BEE_REMAINING_ANGER_TIME(0),

    FOX_VARIANT(0),
    FOX_FLAGS((byte) 0),
    FOX_TRUSTED_1,
    FOX_TRUSTED_2,

    OCELOT_IS_TRUSTING(false),

    PANDA_UNHAPPY_TIMER(0),
    PANDA_SNEEZE_TIMER(0),
    PANDA_EAT_TIMER(0),
    PANDA_MAIN_GENE((byte) 0),
    PANDA_HIDDEN_GAME((byte) 0),
    PANDA_FLAGS((byte) 0),

    PIG_HAS_SADDLE(false),
    PIG_BOOST_TIME(0),

    RABBIT_VARIANT(0),

    TURTLE_HOME_POSITION,
    TURTLE_HAS_EGG(false),
    TURTLE_IS_LAYING_EGG(false),
    TURTLE_TRAVEL_POSITION,
    TURTLE_IS_GOING_HOME(false),
    TURTLE_IS_TRAVELING(false),

    POLAR_BEAR_STANDING(false),

    HOGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    MOOSHROOM_VARIANT("red"),

    SHEEP_FLAGS((byte) 0),

    STRIDER_TIME_TO_BOOST(0),
    STRIDER_IS_SUFFOCATING(false),
    STRIDER_HAS_SADDLE(false),

    TAMABLE_ENTITY_FLAGS((byte) 0),
    TAMABLE_ENTITY_OWNER_UUID,

    CAT_VARIANT(1),
    CAT_IS_LYING(false),
    CAT_IS_RELAXED(false),
    CAT_GET_COLLAR_COLOR(14), // RED

    WOLF_IS_BEGGING(false),
    WOLF_COLLAR_COLOR(14),
    WOLF_ANGER_TIME(0),
    WOLF_HEALTH(0.0f),

    PARROT_VARIANT(0),

    ABSTRACT_VILLAGER_UNHAPPY_TIMER(0),

    VILLAGER_VILLAGER_DATA(new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE)),

    IRON_GOLEM_FLAGS((byte) 0),

    SNOW_GOLEM_FLAGS((byte) 0),

    SHULKER_ATTACH_FACE(Directions.DOWN),
    SHULKER_ATTACHMENT_POSITION,
    SHULKER_PEEK((byte) 0),
    SHULKER_COLOR((byte) 10),

    ABSTRACT_PIGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    PIGLIN_IS_BABY(false),
    PIGLIN_IS_CHARGING_CROSSBOW(false),
    PIGLIN_IS_DANCING(false),
    PIGLIN_IMMUNE_TO_ZOMBIFICATION(false),

    BLAZE_FLAGS((byte) 0),

    CREEPER_STATE(-1),
    CREEPER_IS_CHARGED(false),
    CREEPER_IS_IGNITED(false),

    GUARDIAN_IS_MOVING(false),
    GUARDIAN_TARGET_ENTITY_ID(0),

    RAIDER_IS_CELEBRATING(false),

    SPELLCASTER_ILLAGER_SPELL(0),

    WITCH_IS_DRINKING_POTION(0),

    VEX_FLAGS((byte) 0),

    SPIDER_FLAGS((byte) 0),

    WITHER_BOSS_CENTER_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_LEFT_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_RIGHT_HEAD_TARGET_ENTITY_ID(0),
    WITHER_BOSS_INVULNERABLE_TIME(0),

    ZOGLIN_IS_BABY(false),

    ZOMBIE_VILLAGER_IS_CONVERTING(false),
    ZOMBIE_VILLAGER_DATA(new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE)),

    ENDERMAN_CARRIED_BLOCK,
    ENDERMAN_IS_SCREAMING(false),
    ENDERMAN_IS_STARRING(false),

    ENDER_DRAGON_PHASE(10),

    GHAST_IS_ATTACKING(false),

    PHANTOM_SIZE(0),

    SLIME_SIZE(0),

    MINECART_HURT(0),
    MINECART_HURT_DIRECTION(1),
    MINECART_DAMAGE_TAKEN(0.0f),
    MINECART_BLOCK_ID(0),
    MINECART_BLOCK_Y_OFFSET(6),
    MINECART_SHOW_BLOCK(false),

    MINECART_FURNACE_HAS_FUEL(false),

    MINECART_COMMAND_BLOCK_COMMAND(""),
    MINECART_COMMAND_BLOCK_LAST_OUTPUT(ChatComponent.fromString("")),

    PRIMED_TNT_FUSE_TIME(80),

    PILLAGER_IS_CHARGING_CROSSBOW(false),

    THROWN_EYE_OF_ENDER_ITEM,


    // pretty old stuff here. 1.8 mostly (or even after, I don't know and I don't care)
    LEGACY_SKELETON_TYPE((byte) 0),
    LEGACY_ENDERMAN_CARRIED_BLOCK(0),
    LEGACY_ENDERMAN_CARRIED_BLOCK_DATA(0),
    LEGACY_WITCH_IS_AGGRESSIVE(false),
    LEGACY_GUARDIAN_FLAGS((byte) 0),
    LEGACY_OCELOT_TYPE((byte) 0),
    LEGACY_HORSE_OWNER_NAME(""),
    LEGACY_HORSE_SPECIAL_TYPE((byte) 0),
    LEGACY_HORSE_ARMOR(0),
    LEGACY_VILLAGE_PROFESSION(0),
    LEGACY_END_CRYSTAL_HEALTH(5),
    LEGACY_LIVING_ENTITY_AI_DISABLED((byte) 0),
    LEGACY_AGEABLE_ENTITY_AGE((byte) 0),
    LEGACY_AGEABLE_OWNER_NAME("");

    private final Object defaultValue;

    EntityMetaDataFields() {
        defaultValue = null;
    }

    EntityMetaDataFields(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <K> K getDefaultValue() {
        return (K) defaultValue;
    }
}
