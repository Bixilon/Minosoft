/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.camera


import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rotate
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class Frustum(private val camera: Camera) {
    private val normals: MutableList<Vec3> = mutableListOf(
        camera.cameraFront.normalize(),
    )

    init {
        recalculate()
    }

    fun recalculate() {
        normals.clear()
        normals.add(camera.cameraFront.normalize())

        calculateSideNormals()
        calculateVerticalNormals()
    }

    private fun calculateSideNormals() {
        val cameraRealUp = (camera.cameraRight cross camera.cameraFront).normalize()
        val angle = (camera.fov - 90.0f).rad
        val sin = angle.sin
        val cos = angle.cos
        normals.add(camera.cameraFront.rotate(cameraRealUp, sin, cos).normalize())
        normals.add(camera.cameraFront.rotate(cameraRealUp, -sin, cos).normalize()) // negate angle -> negate sin
    }

    private fun calculateVerticalNormals() {
        val aspect = camera.renderWindow.screenDimensions.y.toFloat() / camera.renderWindow.screenDimensions.x // ToDo: x/y or y/x
        val angle = (camera.fov * aspect - 90.0f).rad
        val sin = angle.sin
        val cos = angle.cos
        normals.add(camera.cameraFront.rotate(camera.cameraRight, sin, cos).normalize())
        normals.add(camera.cameraFront.rotate(camera.cameraRight, -sin, cos).normalize()) // negate angle -> negate sin
    }

    private fun containsRegion(from: Vec3, to: Vec3): Boolean {
        val min = Vec3()
        for (normal in normals) {
            // get the point most likely to be in the frustum
            min.x = if (normal.x < 0) {
                from.x
            } else {
                to.x
            }
            min.y = if (normal.y < 0) {
                from.y
            } else {
                to.y
            }
            min.z = if (normal.z < 0) {
                from.z
            } else {
                to.z
            }

            if (normal dot (min - camera.cameraPosition) < 0.0f) {
                return false // region is outside of frustum
            }
        }
        return true
    }

    fun containsChunk(chunkPosition: Vec2i, lowestBlockHeight: Int, highestBlockHeight: Int): Boolean {
        if (!RenderConstants.FRUSTUM_CULLING_ENABLED) {
            return true
        }
        val from = Vec3(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X, lowestBlockHeight, chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z)
        val to = from + Vec3(ProtocolDefinition.SECTION_WIDTH_X, highestBlockHeight, ProtocolDefinition.SECTION_WIDTH_Z)
        return containsRegion(from, to)
    }
}
