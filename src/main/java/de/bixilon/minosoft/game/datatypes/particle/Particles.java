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

package de.bixilon.minosoft.game.datatypes.particle;

import de.bixilon.minosoft.game.datatypes.ChangeableIdentifier;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum Particles {
    AMBIENT_ENTITY_EFFECT(new ChangeableIdentifier("ambient_entity_effect", "ambiententityeffect"), 0),
    ANGRY_VILLAGER(new ChangeableIdentifier("angry_villager", "angryvillager"), 1),
    BARRIER(new ChangeableIdentifier("barrier"), 2),
    BLOCK(new ChangeableIdentifier("block"), 3, BlockParticle.class),
    BUBBLE(new ChangeableIdentifier("barrier"), 4),
    CLOUD(new ChangeableIdentifier("cloud"), 5),
    CRIT(new ChangeableIdentifier("crit"), 6),
    DAMAGE_INDICATOR(new ChangeableIdentifier("damageindicator", "damage_indicator"), 7),
    DRAGON_BREATH(new ChangeableIdentifier("dragonbreath", "dragon_breath"), 8),
    DRIPPING_LAVA(new ChangeableIdentifier("drippinglava", "dripping_lava"), 9),
    FALLING_LAVA(new ChangeableIdentifier("fallinglava", "falling_lava"), 10),
    LANDING_LAVA(new ChangeableIdentifier("landinglava", "landing_lava"), 11),
    DRIPPING_WATER(new ChangeableIdentifier("drippingwater", "dripping_water"), 12),
    FALLING_WATER(new ChangeableIdentifier("fallingwater", "falling_water"), 13),
    DUST(new ChangeableIdentifier("dust"), 14, DustParticle.class),
    EFFECT(new ChangeableIdentifier("effect"), 15),
    ELDER_GUARDIAN(new ChangeableIdentifier("elderguardian", "elder_guardian"), 16),
    ENCHANTED_HIT(new ChangeableIdentifier("enchantedhit", "enchanted_hit"), 17),
    ENCHANT(new ChangeableIdentifier("enchant"), 18),
    END_ROD(new ChangeableIdentifier("endrod", "end_rod"), 19),
    ENTITY_EFFECT(new ChangeableIdentifier("entityeffect", "entity_effect"), 20),
    EXPLOSION_EMITTER(new ChangeableIdentifier("explosionemitter", "explosion_emitter"), 21),
    EXPLOSION(new ChangeableIdentifier("explosion"), 22),
    FALLING_DUST(new ChangeableIdentifier("fallingdust", "falling_dust"), 14, FallingDustParticle.class),
    FIREWORK(new ChangeableIdentifier("firework"), 24),
    FISHING(new ChangeableIdentifier("fishing"), 25),
    FLAME(new ChangeableIdentifier("flame"), 26),
    FLASH(new ChangeableIdentifier("flash"), 27),
    HAPPY_VILLAGER(new ChangeableIdentifier("happyvillager", "happy_villager"), 28),
    COMPOSTER(new ChangeableIdentifier("composter"), 29),
    HEART(new ChangeableIdentifier("heart"), 30),
    INSTANT_EFFECT(new ChangeableIdentifier("instanteffect", "instant_effect"), 31),
    ITEM(new ChangeableIdentifier("item"), 32, ItemParticle.class),
    ITEM_SLIME(new ChangeableIdentifier("itemslime", "item_slime"), 33),
    ITEM_SNOWBALL(new ChangeableIdentifier("itemsnowball", "item_snowball"), 34),
    LARGE_SMOKE(new ChangeableIdentifier("largesmoke", "large_smoke"), 35),
    LAVA(new ChangeableIdentifier("lava"), 36),
    MYCELIUM(new ChangeableIdentifier("mycelium"), 37),
    NOTE(new ChangeableIdentifier("note"), 38),
    POOF(new ChangeableIdentifier("poof"), 39),
    PORTAL(new ChangeableIdentifier("portal"), 40),
    RAIN(new ChangeableIdentifier("rain"), 41),
    SMOKE(new ChangeableIdentifier("smoke"), 42),
    SNEEZE(new ChangeableIdentifier("sneeze"), 43),
    SPIT(new ChangeableIdentifier("spit"), 44),
    SQUID_INK(new ChangeableIdentifier("squidink", "squid_ink"), 45),
    SWEEP_ATTACK(new ChangeableIdentifier("sweepattack", "sweep_attack"), 46),
    TOTEM_OF_UNDYING(new ChangeableIdentifier("totemofundying", "totem_of_undying"), 47),
    UNDERWATER(new ChangeableIdentifier("underwater"), 48),
    SPLASH(new ChangeableIdentifier("splash"), 49),
    WITCH(new ChangeableIdentifier("witch"), 50),
    BUBBLE_POP(new ChangeableIdentifier(null, "bubble_pop"), 51),
    CURRENT_DOWN(new ChangeableIdentifier(null, "current_down"), 52),
    BUBBLE_COLUMN_UP(new ChangeableIdentifier(null, "bubble_column_up"), 53),
    NAUTILUS(new ChangeableIdentifier(null, "nautilus"), 54),
    DOLPHIN(new ChangeableIdentifier(null, "dolphin"), 55),
    CAMPFIRE_COSY_SMOKE(new ChangeableIdentifier(null, "campfire_cosy_smoke"), 56),
    CAMPFIRE_SIGNAL_SMOKE(new ChangeableIdentifier(null, "campfire_signal_smoke"), 57),
    DRIPPING_HONEY(new ChangeableIdentifier(null, "dripping_honey"), 58),
    FALLING_HONEY(new ChangeableIdentifier(null, "falling_honey"), 59),
    LANDING_HONEY(new ChangeableIdentifier(null, "landing_honey"), 60),
    FALLING_NECTAR(new ChangeableIdentifier(null, "falling_nectar"), 61);

    // ToDo: 1.8 names, etc

    final ChangeableIdentifier changeableIdentifier;
    final int id;
    final Class<? extends Particle> clazz;

    Particles(ChangeableIdentifier changeableIdentifier, int id, Class<? extends Particle> clazz) {
        this.changeableIdentifier = changeableIdentifier;
        this.id = id;
        this.clazz = clazz;
    }

    Particles(ChangeableIdentifier changeableIdentifier, int id) {
        this.changeableIdentifier = changeableIdentifier;
        this.id = id;
        this.clazz = OtherParticles.class;
    }

    public static Particles byName(String name, ProtocolVersion version) {
        for (Particles particle : values()) {
            if (particle.getChangeableIdentifier().isValidName(name, version)) {
                return particle;
            }
        }
        return null;
    }

    public static Particles byId(int id) {
        for (Particles particle : values()) {
            if (particle.getId() == id) {
                return particle;
            }
        }
        return null;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return changeableIdentifier;
    }

    public int getId() {
        return id;
    }

    public Class<? extends Particle> getClazz() {
        return clazz;
    }
}
