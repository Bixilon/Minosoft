/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.game

import de.bixilon.minosoft.config.config.game.controls.ControlsGameConfig
import de.bixilon.minosoft.config.config.game.entities.EntitiesConfig
import de.bixilon.minosoft.config.config.game.graphics.GraphicsGameConfig
import de.bixilon.minosoft.config.config.game.hud.HUDGameConfig
import de.bixilon.minosoft.config.config.game.world.WorldConfig

data class GameConfig(
    var graphics: GraphicsGameConfig = GraphicsGameConfig(),
    var other: OtherGameConfig = OtherGameConfig(),
    var hud: HUDGameConfig = HUDGameConfig(),
    var controls: ControlsGameConfig = ControlsGameConfig(),
    var camera: CameraGameConfig = CameraGameConfig(),
    var entities: EntitiesConfig = EntitiesConfig(),
    var world: WorldConfig = WorldConfig(),
    var light: LightConfig = LightConfig(),
    var skin: SkinConfig = SkinConfig(),
)
