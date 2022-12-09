/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.effects

import de.bixilon.minosoft.data.registries.effects.damage.DamageEffect
import de.bixilon.minosoft.data.registries.effects.mining.MiningEffect
import de.bixilon.minosoft.data.registries.effects.other.OtherEffects
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.registries.integrated.IntegratedRegistry

object IntegratedStatusEffects : IntegratedRegistry<StatusEffectType>(
    DamageEffect.Absorption,
    DamageEffect.FireResistance,
    DamageEffect.HealthBoost,
    DamageEffect.InstantDamage,
    DamageEffect.InstantHealth,
    DamageEffect.Poison,
    DamageEffect.Regeneration,
    DamageEffect.Resistance,
    DamageEffect.Strength,
    DamageEffect.Weakness,
    DamageEffect.Wither,

    MiningEffect.Haste,
    MiningEffect.MiningFatigue,

    VisionEffect.Blindness,
    VisionEffect.Darkness,
    VisionEffect.Nausea,
    VisionEffect.NightVision,

    OtherEffects.BadOmen,
    OtherEffects.ConduitPower,
    OtherEffects.Glowing,
    OtherEffects.HeroOfTheVillage,
    OtherEffects.Hunger,
    OtherEffects.Invisibility,
    OtherEffects.Luck,
    OtherEffects.Saturation,
    OtherEffects.Unluck,
    OtherEffects.WaterBreathing,
)
