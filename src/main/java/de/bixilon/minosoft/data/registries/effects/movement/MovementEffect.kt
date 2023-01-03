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

package de.bixilon.minosoft.data.registries.effects.movement

import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.effects.properties.categories.BeneficalEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.HarmfulEffect
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.text.formatting.color.Colored
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.util.KUtil.minecraft

interface MovementEffect {


    object Speed : StatusEffectType(), MovementEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("speed")
        override val color = 0x7CAFC6.asRGBColor()
    }

    object Slowness : StatusEffectType(), MovementEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("slowness")
        override val color = 0x5A6C81.asRGBColor()
    }

    object JumpBoost : StatusEffectType(), MovementEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("jump_boost")
        override val color = 0x22FF4C.asRGBColor()
    }

    object Levitation : StatusEffectType(), MovementEffect, Identified, Colored, HarmfulEffect {
        override val identifier = minecraft("levitation")
        override val color = 0xCEFFFF.asRGBColor()
    }

    object SlowFalling : StatusEffectType(), MovementEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("slow_falling")
        override val color = 0xFFEFD1.asRGBColor()
    }

    object DolphinsGrace : StatusEffectType(), MovementEffect, Identified, Colored, BeneficalEffect {
        override val identifier = minecraft("dolphins_grace")
        override val color = 0x88A3BE.asRGBColor()
    }
}
