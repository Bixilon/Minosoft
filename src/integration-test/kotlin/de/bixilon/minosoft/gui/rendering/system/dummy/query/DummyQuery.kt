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

package de.bixilon.minosoft.gui.rendering.system.dummy.query

import de.bixilon.minosoft.gui.rendering.system.base.query.QueryStates
import de.bixilon.minosoft.gui.rendering.system.base.query.QueryTypes
import de.bixilon.minosoft.gui.rendering.system.base.query.RenderQuery

class DummyQuery(
    override val type: QueryTypes,
) : RenderQuery {
    override var state = QueryStates.WAITING
    override val isReady get() = true

    override val recordings get() = 1
    override val result get() = 1

    override fun init() {
        state = QueryStates.INITIALIZED
    }

    override fun begin() {
        state = QueryStates.RECORDING
    }

    override fun end() {
        state = QueryStates.INITIALIZED
    }

    override fun collect() {
    }

    override fun destroy() {
    }
}
