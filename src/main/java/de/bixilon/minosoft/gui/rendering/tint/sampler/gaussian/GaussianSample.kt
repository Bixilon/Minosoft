/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.tint.sampler.gaussian

import de.bixilon.kmath.vec.VecUtil.assertVec

@JvmInline
value class GaussianSample(val raw: Int) {

    constructor(x: Int, y: Int, z: Int, weight: Int) : this(((weight and MASK_WEIGHT) shl SHIFT_WEIGHT) or ((z and MASK_Z) shl SHIFT_Z) or ((y and MASK_Y) shl SHIFT_Y) or ((x and MASK_X) shl SHIFT_X)) {
        assertVec(x, -MAX_X, MAX_X)
        assertVec(y, -MAX_Y, MAX_Y)
        assertVec(z, -MAX_Z, MAX_Z)
        assertVec(weight, -MAX_WEIGHT, MAX_WEIGHT)
    }


    inline val x: Int get() = (((raw ushr SHIFT_X) and MASK_X) shl (Int.SIZE_BITS - BITS_X)) shr (Int.SIZE_BITS - BITS_X)
    inline val y: Int get() = (((raw ushr SHIFT_Y) and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    inline val z: Int get() = (((raw ushr SHIFT_Z) and MASK_Z) shl (Int.SIZE_BITS - BITS_Z)) shr (Int.SIZE_BITS - BITS_Z)
    inline val weight: Int get() = (((raw ushr SHIFT_WEIGHT) and MASK_WEIGHT) shl (Int.SIZE_BITS - BITS_WEIGHT)) shr (Int.SIZE_BITS - BITS_WEIGHT) // TODO: positive only?


    companion object {
        const val BITS_X = 5
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Y = 5
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X

        const val BITS_Z = 5
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X + BITS_Y

        const val BITS_WEIGHT = 17
        const val MASK_WEIGHT = (1 shl BITS_WEIGHT) - 1
        const val SHIFT_WEIGHT = BITS_X + BITS_Y + BITS_Z

        const val MAX_X = (1 shl BITS_X - 1) - 1
        const val MAX_Y = (1 shl BITS_Y - 1) - 1
        const val MAX_Z = (1 shl BITS_Z - 1) - 1
        const val MAX_WEIGHT = (1 shl BITS_WEIGHT - 1) - 1
    }
}
