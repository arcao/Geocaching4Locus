package com.arcao.wherigoservice.api

import org.koin.standalone.KoinComponent
import org.koin.standalone.get

@Deprecated("Use Koin")
object WherigoApiFactory : KoinComponent {
    @JvmStatic
    fun create() = get<WherigoService>()
}
