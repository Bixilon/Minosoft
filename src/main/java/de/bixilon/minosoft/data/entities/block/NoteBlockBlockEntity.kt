/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Instruments
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.NoteParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

// TODO: is this needed in > 1.12?
class NoteBlockBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) : BlockEntity(session, position, state), BlockActionEntity {
    private val noteParticleType = session.registries.particleType[NoteParticle]
    var instrument: Instruments? = null
        private set
    var pitch: Int? = null
        private set
    private var showParticleNextTick = false

    private fun BlockState.getNote(): Int {
        return properties[BlockProperties.NOTE]?.toInt() ?: 0
    }

    override fun setBlockActionData(type: Int, data: Int) {
        instrument = when (type) {
            0 -> Instruments.HARP
            1 -> Instruments.BASS
            2 -> Instruments.SNARE
            3 -> Instruments.BANJO // ToDo: Was CLICKS_STICKS before
            4 -> Instruments.BASE_DRUM
            else -> null
        }

        pitch = data

        showParticleNextTick = true
        // ToDo: Play sound?
    }

    override fun tick() {
        if (!showParticleNextTick) {
            return
        }
        val particle = session.world.particle ?: return
        showParticleNextTick = false


        noteParticleType?.let {
            particle += NoteParticle(session, Vec3d(position) + Vec3d(0.5, 1.2, 0.5), state.getNote() / 24.0f, it.default())
        }
    }

    companion object : BlockEntityFactory<NoteBlockBlockEntity> {
        override val identifier = minecraft("note_block")

        override fun build(session: PlaySession, position: BlockPosition, state: BlockState) = NoteBlockBlockEntity(session, position, state)
    }

}
