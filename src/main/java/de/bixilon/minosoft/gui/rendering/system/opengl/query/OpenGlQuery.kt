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

package de.bixilon.minosoft.gui.rendering.system.opengl.query

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryStates
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.glDeleteQueries
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL33.GL_TIME_ELAPSED

class OpenGlQuery(
    override val type: QueryTypes,
) : RenderQuery {
    override var recordings = 0
        private set
    override var state = QueryStates.WAITING
        private set
    private var query = -1

    override val isReady: Boolean
        get() {
            assert(state == QueryStates.INITIALIZED)
            assert(recordings > 0)
            return gl { glGetQueryObjecti(query, GL_QUERY_RESULT_AVAILABLE) }.toBoolean()
        }


    override var result: Int = -1
        get() {
            assert(field >= 0)
            assert(recordings > 0)

            return field
        }
        private set

    override fun init() {
        assert(state == QueryStates.WAITING)
        assert(query < 0)
        query = gl { glGenQueries() }
        state = QueryStates.INITIALIZED
    }

    override fun destroy() {
        assert(state == QueryStates.INITIALIZED)
        gl { glDeleteQueries(query) }
        state = QueryStates.DESTROYED
    }


    override fun begin() {
        assert(state == QueryStates.INITIALIZED)
        result = -1
        gl { glBeginQuery(type.gl, query) }
        state = QueryStates.RECORDING
    }

    override fun end() {
        assert(state == QueryStates.RECORDING)
        gl { glEndQuery(type.gl) }
        recordings++
        state = QueryStates.INITIALIZED
    }

    override fun collect() {
        result = gl { glGetQueryObjecti(query, GL_QUERY_RESULT) }
    }


    companion object {

        val QueryTypes.gl
            get() = when (this) {
                QueryTypes.FRAGMENTS -> GL_SAMPLES_PASSED
                QueryTypes.TIME -> GL_TIME_ELAPSED
                else -> throw IllegalArgumentException("Query type $this not supported in opengl")
            }
    }
}
