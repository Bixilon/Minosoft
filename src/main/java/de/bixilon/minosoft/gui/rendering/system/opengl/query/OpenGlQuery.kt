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
import de.bixilon.kutil.time.TimeUtil.sleep
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryStates
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.glDeleteQueries
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL33.GL_TIME_ELAPSED
import kotlin.time.Duration.Companion.milliseconds

class OpenGlQuery(
    override val type: QueryTypes,
) : RenderQuery {
    override val state = QueryStates.WAITING
    private var query = -1

    override val isReady: Boolean
        get() {
            assert(state == QueryStates.INITIALIZED)
            return gl { glGetQueryObjecti(query, GL_QUERY_RESULT_AVAILABLE) }.toBoolean()
        }


    override var result: Int = -1
        get() {
            assert(field > 0)

            return field
        }
        private set

    override fun init() {
        assert(state == QueryStates.WAITING)
        assert(query < 0)
        query = gl { glGenQueries() }
    }

    override fun destroy() {
        assert(state == QueryStates.INITIALIZED)
        gl { glDeleteQueries(query) }
    }


    override fun begin() {
        assert(state == QueryStates.INITIALIZED)
        result = -1
        glBeginQuery(type.gl, query)
    }

    override fun end() {
        assert(state == QueryStates.RECORDING)
        gl { glEndQuery(type.gl) }
    }

    override fun collect() {
        while (!isReady) {
            sleep(1.milliseconds)
        }
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
