package de.bixilon.minosoft.config.config.game

import com.squareup.moshi.Json

data class CameraGameConfig(
    @Json(name = "render_distance") var renderDistance: Int = 10,
    var fov: Float = 60f,
    @Json(name = "mouse_sensitivity") var moseSensitivity: Float = 0.1f,
)
