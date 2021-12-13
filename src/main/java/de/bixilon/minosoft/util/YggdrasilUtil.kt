package de.bixilon.minosoft.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*


object YggdrasilUtil {
    lateinit var PUBLIC_KEY: PublicKey
        private set

    fun load() {
        check(!this::PUBLIC_KEY.isInitialized) { "Already loaded!" }
        val spec = X509EncodedKeySpec(Minosoft.MINOSOFT_ASSETS_MANAGER["minosoft:mojang/yggdrasil_session_pubkey.der".toResourceLocation()].readAllBytes())
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        PUBLIC_KEY = keyFactory.generatePublic(spec)
    }

    fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val signatureInstance = Signature.getInstance("SHA1withRSA")
        signatureInstance.initVerify(PUBLIC_KEY)
        signatureInstance.update(data)
        return signatureInstance.verify(signature)
    }

    fun verify(data: String, signature: String): Boolean {
        return verify(data.toByteArray(), Base64.getDecoder().decode(signature))
    }
}
