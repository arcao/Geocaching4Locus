package com.arcao.wherigoservice.api

import org.koin.dsl.module

val wherigoApiModule = module {
    factory<WherigoService> {
        WherigoServiceImpl(get())
    }
}