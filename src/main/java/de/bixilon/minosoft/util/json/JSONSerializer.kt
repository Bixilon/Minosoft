package de.bixilon.minosoft.util.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.bixilon.minosoft.config.config.Config

object JSONSerializer {
    private val MOSHI = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(AccountSerializer())
        .add(ServerSerializer())
        .add(ModIdentifierSerializer())
        .build()!!

    val CONFIG_ADAPTER = MOSHI.adapter(Config::class.java)!!
}
