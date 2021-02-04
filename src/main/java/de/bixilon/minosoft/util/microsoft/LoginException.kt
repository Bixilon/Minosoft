package de.bixilon.minosoft.util.microsoft

class LoginException(val errorCode: Int, override val message: String, val errorMessage: String) : Exception()
