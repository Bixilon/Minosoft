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

package de.bixilon.minosoft.data.registries.effects.other

import de.bixilon.minosoft.data.registries.CompanionResourceLocation
import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.effects.properties.categories.BeneficalEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.HarmfulEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.NeutralEffect
import de.bixilon.minosoft.data.text.formatting.color.Colored
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.util.KUtil.minecraft

interface OtherEffects {


    object WaterBreathing : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("water_breathing")
        override val color = 0x2E5299.asRGBColor()
    }

    object Invisibility : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("invisibility")
        override val color = 0x7F8392.asRGBColor()
    }

    object Hunger : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, HarmfulEffect {
        override val RESOURCE_LOCATION = minecraft("hunger")
        override val color = 0x587653.asRGBColor()
    }

    object Saturation : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("saturation")
        override val color = 0xF82423.asRGBColor()
    }

    object Glowing : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, NeutralEffect {
        override val RESOURCE_LOCATION = minecraft("glowing")
        override val color = 0x94A061.asRGBColor()
    }

    object Luck : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("luck")
        override val color = 0x339900.asRGBColor()
    }

    object Unluck : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, HarmfulEffect {
        override val RESOURCE_LOCATION = minecraft("unluck")
        override val color = 0xC0A44D.asRGBColor()
    }

    object ConduitPower : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("conduit_power")
        override val color = 0x1DC2D1.asRGBColor()
    }

    object BadOmen : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, NeutralEffect {
        override val RESOURCE_LOCATION = minecraft("bad_omen")
        override val color = 0x0B6138.asRGBColor()
    }

    object HeroOfTheVillage : StatusEffectType(), OtherEffects, CompanionResourceLocation, Colored, BeneficalEffect {
        override val RESOURCE_LOCATION = minecraft("hero_of_the_village")
        override val color = 0x44FF44.asRGBColor()
    }
}
