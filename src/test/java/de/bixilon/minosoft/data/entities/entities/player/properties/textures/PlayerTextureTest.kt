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

package de.bixilon.minosoft.data.entities.entities.player.properties.textures

import de.bixilon.kutil.url.URLUtil.toURL
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class PlayerTextureTest {

    @Test
    fun extractHash() {
        val texture = PlayerTexture("http://textures.minecraft.net/texture/3332bc99aa717270cbcb1a65426a515c97f7462fce85bca05b6fe82d90f3a82f".toURL())
        assertEquals("3332bc99aa717270cbcb1a65426a515c97f7462fce85bca05b6fe82d90f3a82f", texture.getHash())
    }

    @Test
    fun testLeading0Hash() {
        val texture = PlayerTexture("https://textures.minecraft.net/texture/f4639b3bb2b47f0e567e7a1ca094d38dfa57ce80d79b8cf507479d619ae67b7".toURL())
        assertEquals("0f4639b3bb2b47f0e567e7a1ca094d38dfa57ce80d79b8cf507479d619ae67b7", texture.getHash())
    }

    @Test
    fun invalidHash() {
        val texture = PlayerTexture("https://textures.minecraft.net/texture/r4639b3bb2b47f0e567e7a1ca094d38dfa57ce80d79b8cf507479d619ae67b7".toURL())
        assertThrows<IllegalArgumentException> { texture.getHash() }
    }
}
