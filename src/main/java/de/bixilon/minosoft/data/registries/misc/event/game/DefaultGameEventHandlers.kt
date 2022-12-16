/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.misc.event.game

import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.misc.event.game.handler.gamemode.GamemodeChangeHandler
import de.bixilon.minosoft.data.registries.misc.event.game.handler.gradients.RainGradientSetHandler
import de.bixilon.minosoft.data.registries.misc.event.game.handler.gradients.ThunderGradientSetHandler
import de.bixilon.minosoft.data.registries.misc.event.game.handler.rain.RainStartHandler
import de.bixilon.minosoft.data.registries.misc.event.game.handler.rain.RainStopHandler
import de.bixilon.minosoft.data.registries.misc.event.world.handler.win.WinGameHandler

object DefaultGameEventHandlers : DefaultFactory<GameEventHandler>(
    GamemodeChangeHandler,
    RainStartHandler,
    RainStopHandler,
    RainGradientSetHandler,
    ThunderGradientSetHandler,
    WinGameHandler,
)
