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

package de.bixilon.minosoft.terminal.commands.rendering

import de.bixilon.minosoft.commands.nodes.LiteralNode

object ReloadCommand : RenderingCommand {
    override var node = LiteralNode("reload", setOf("rl"))
        .addChild(LiteralNode("shaders", executor = {
            val context = it.session.rendering?.context ?: throw IllegalStateException("Rendering is not loaded!")
            context.queue += {
                context.system.shader.reload()
                it.session.util.sendDebugMessage("Shaders reloaded!")
            }
        }))
        .addChild(LiteralNode("textures", executor = {
            val context = it.session.rendering?.context ?: throw IllegalStateException("Rendering is not loaded!")
            context.queue += {
                context.textures.reload()
                it.session.util.sendDebugMessage("Textures reloaded!")
            }
        }))
}
