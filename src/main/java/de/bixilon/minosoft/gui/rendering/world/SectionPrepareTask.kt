package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import glm_.vec2.Vec2i

class SectionPrepareTask(
    val chunkPosition: Vec2i,
    val runnable: ThreadPoolRunnable,
)
