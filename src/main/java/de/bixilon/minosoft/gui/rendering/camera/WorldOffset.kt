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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class WorldOffset(private val camera: Camera) : Drawable {
    var offset by observed(Vec3i(100, 100, 100))
        private set

    override fun draw() {
        val previous = offset
        val position = camera.view.view.eyePosition
    }


    companion object {
        const val MAX_DISTANCE = (World.MAX_RENDER_DISTANCE * 2) * ProtocolDefinition.SECTION_WIDTH_X // coordinates higher than that value are not allowed
        const val MIN_DISTANCE = MAX_DISTANCE - (World.MAX_RENDER_DISTANCE / 4) * ProtocolDefinition.SECTION_WIDTH_X // only if value is lower that that value coordinates will be back offset
    }
}
