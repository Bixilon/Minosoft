package de.bixilon.minosoft.config.config.game

import de.bixilon.minosoft.config.config.game.controls.ControlsGameConfig

data class GameConfig(
    var animations: AnimationsGameConfig = AnimationsGameConfig(),
    var other: OtherGameConfig = OtherGameConfig(),
    var hud: HUDGameConfig = HUDGameConfig(),
    var controls: ControlsGameConfig = ControlsGameConfig(),
    var camera: CameraGameConfig = CameraGameConfig(),
)
