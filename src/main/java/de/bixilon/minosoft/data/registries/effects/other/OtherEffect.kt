/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.effects.properties.categories.BeneficalEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.HarmfulEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.NeutralEffect
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.formatting.color.Colored
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor

interface OtherEffect {


    object WaterBreathing : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("water_breathing")
        override val color = 0x2E5299.asRGBColor()
    }

    object Invisibility : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("invisibility")
        override val color = 0x7F8392.asRGBColor()
    }

    object Hunger : StatusEffectType(), OtherEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("hunger")
        override val color = 0x587653.asRGBColor()
    }

    object Saturation : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("saturation")
        override val color = 0xF82423.asRGBColor()
    }

    object Glowing : StatusEffectType(), OtherEffect, Identified, Colored, NeutralEffect {
        override val identifier = minecraft("glowing")
        override val color = 0x94A061.asRGBColor()
    }

    object Luck : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("luck")
        override val color = 0x339900.asRGBColor()
    }

    object Unluck : StatusEffectType(), OtherEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("unluck")
        override val color = 0xC0A44D.asRGBColor()
    }

    object ConduitPower : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("conduit_power")
        override val color = 0x1DC2D1.asRGBColor()
    }

    object BadOmen : StatusEffectType(), OtherEffect, Identified, Colored, NeutralEffect {
        override val identifier = minecraft("bad_omen")
        override val color = 0x0B6138.asRGBColor()
    }

    object HeroOfTheVillage : StatusEffectType(), OtherEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("hero_of_the_village")
        override val color = 0x44FF44.asRGBColor()
    }
}
