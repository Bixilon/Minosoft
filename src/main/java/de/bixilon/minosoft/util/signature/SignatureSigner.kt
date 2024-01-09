/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.signature

import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*


abstract class SignatureSigner(
    val keyPath: ResourceLocation,
    val algorithm: String,
) {
    private var key: PublicKey? = null

    open fun load() {
        if (key != null) throw IllegalStateException("Already loaded!")
        val spec = X509EncodedKeySpec(IntegratedAssets.DEFAULT[keyPath].readAllBytes())
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        key = keyFactory.generatePublic(spec)
    }

    protected fun createInstance(): Signature {
        val instance = Signature.getInstance(algorithm)
        instance.initVerify(key)

        return instance
    }

    open fun verify(data: ByteArray, signature: ByteArray?): Boolean {
        if (signature == null) return false
        val instance = createInstance()
        instance.update(data)
        return instance.verify(signature)
    }

    fun require(data: ByteArray, signature: ByteArray?) {
        if (verify(data, signature)) return
        throw SignatureException()
    }

    fun verify(data: String, signature: String?): Boolean {
        return verify(data.toByteArray(), Base64.getDecoder().decode(signature))
    }

    fun require(data: String, signature: String?) {
        if (verify(data, signature)) return
        throw SignatureException()
    }
}
