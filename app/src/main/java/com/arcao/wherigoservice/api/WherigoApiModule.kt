package com.arcao.wherigoservice.api

import org.koin.dsl.module.module

val wherigoApiModule = module {
    factory<WherigoService> {
        WherigoServiceImpl(get())
    }
}