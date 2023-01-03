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

package de.bixilon.minosoft.data.registries.effects.damage

import de.bixilon.minosoft.data.registries.effects.InstantEffect
import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.effects.properties.categories.BeneficalEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.HarmfulEffect
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.text.formatting.color.Colored
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.util.KUtil.minecraft

interface DamageEffect {


    object Strength : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("strength")
        override val color = 0x932423.asRGBColor()
    }

    object InstantHealth : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect, InstantEffect {
        override val identifier = minecraft("instant_health")
        override val color = 0xF82423.asRGBColor()
    }

    object InstantDamage : StatusEffectType(), DamageEffect, Identified, Colored, HarmfulEffect, InstantEffect {
        override val identifier = minecraft("instant_damage")
        override val color = 0x430A09.asRGBColor()
    }

    object Regeneration : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("regeneration")
        override val color = 0xCD5CAB.asRGBColor()
    }

    object Resistance : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("resistance")
        override val color = 0x99453A.asRGBColor()
    }

    object FireResistance : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("fire_resistance")
        override val color = 0xE49A3A.asRGBColor()
    }

    object Weakness : StatusEffectType(), DamageEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("weakness")
        override val color = 0x484D48.asRGBColor()
    }

    object Poison : StatusEffectType(), DamageEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("poison")
        override val color = 0x4E9331.asRGBColor()
    }

    object Wither : StatusEffectType(), DamageEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("wither")
        override val color = 0x352A27.asRGBColor()
    }

    object HealthBoost : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("heath_boost")
        override val color = 0xF87D23.asRGBColor()
    }

    object Absorption : StatusEffectType(), DamageEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("absorption")
        override val color = 0x2552A5.asRGBColor()
    }
}
