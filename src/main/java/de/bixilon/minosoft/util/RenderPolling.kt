package de.bixilon.minosoft.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.rendering.Rendering

object RenderPolling {
    val RENDERING_LATCH = CountUpAndDownLatch(Int.MAX_VALUE shr 1)
    var rendering: Rendering? = null


    /**
     * Polls rendering (if opengl context is forced on the main thread)
     */
    internal fun pollRendering() {
        check(Thread.currentThread() == Minosoft.MAIN_THREAD) { "Current thread is not the main thread!" }
        while (true) {
            RENDERING_LATCH.waitForChange()
            rendering?.start() ?: continue
            this.rendering = null
        }
    }
}
