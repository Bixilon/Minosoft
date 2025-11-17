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

package de.bixilon.minosoft.protocol.packets.s2c.play.world

import de.bixilon.kutil.hex.HexUtil.fromHexString
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test

@Test(groups = ["network"])
class ParticleS2CPTest {

    fun `read 1_8_9`() {
        val data = "0000001e01c23f61c942bebc50c02190f23f8000003f800000000000003f80000000000000".fromHexString()
        val buffer = PlayInByteBuffer(data, PlaySession::class.java.allocate().apply { this::registries.forceSet(IT.REGISTRIES_LEGACY); this::version.forceSet(Versions["1.8.9"]!!); })

        val packet = ParticleS2CP(buffer)
        assertEquals(packet.type.identifier, minecraft("reddust"))
    }
}
