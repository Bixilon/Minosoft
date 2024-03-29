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

package de.bixilon.minosoft.gui.rendering.textures.properties

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.ByteArrayInputStream

@Test(groups = ["textures"])
class AnimationPropertiesTest {

    fun `read and collect animation properties`() {
        val json = """{
  "animation": {
    "interpolate": true,
    "frametime": 4,
    "frames": [
      {
        "index": 2,
        "time": 2
      },
      1, 2, 3
    ]
  }
}"""
        val properties: ImageProperties = ByteArrayInputStream(json.encodeToByteArray()).readJson(reader = ImageProperties.READER)

        val data = properties.animation!!.create(Vec2i(16, 64))
        assertEquals(data, AnimationProperties.FrameData(listOf(
            AnimationProperties.Frame(0.1f, 2),
            AnimationProperties.Frame(0.2f, 1),
            AnimationProperties.Frame(0.2f, 2),
            AnimationProperties.Frame(0.2f, 3),
        ), 4, Vec2i(16, 16)))

    }
}
