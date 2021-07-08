package de.bixilon.minosoft.gui.rendering.system.base.buffer

interface RenderBuffer {
    val type: RenderBufferTypes

    fun init()
    fun initialUpload()
    fun upload()
    fun bind()
    fun unbind()

    fun unload()
}