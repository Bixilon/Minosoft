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

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class FluidModelLoader(private val loader: ModelLoader) {
    private val context: RenderContext = loader.context


    fun load(latch: AbstractLatch) {
        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loading fluid models..." }

        for (fluid in context.connection.registries.fluid) {
            load(fluid)
        }
    }

    private fun load(fluid: Fluid) {
        if (fluid.model != null) return
        val model = fluid.createModel() ?: return
        fluid.model = model
        model.load(context)
    }
}
