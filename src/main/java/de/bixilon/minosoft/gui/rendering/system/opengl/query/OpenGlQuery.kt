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

package de.bixilon.minosoft.gui.rendering.system.opengl.query

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.glDeleteQueries
import org.lwjgl.opengl.GL30.*

class OpenGlQuery(
    override val type: QueryTypes,
) : RenderQuery {
    private var query = -1

    override val isReady: Boolean
        get() = gl { glGetQueryObjecti(query, GL_QUERY_RESULT_AVAILABLE) }.toBoolean()


    override val result: Int
        get() = gl { glGetQueryObjecti(query, GL_QUERY_RESULT) }

    fun init() {
        assert(query < 0)
        query = gl { glGenQueries() }
    }

    fun destroy() {
        assert(query >= 0)
        gl { glDeleteQueries(query) }
    }


    fun begin() {
        assert(query >= 0)
        glBeginQuery(type.gl, query)
    }

    fun end() {
        assert(query >= 0)
        gl { glEndQuery(type.gl) }
    }


    companion object {

        val QueryTypes.gl
            get() = when (this) {
                QueryTypes.FRAGMENTS_PASSED -> GL_SAMPLES_PASSED
                else -> throw IllegalArgumentException("Query type $this not supported in opengl")
            }
    }
}
