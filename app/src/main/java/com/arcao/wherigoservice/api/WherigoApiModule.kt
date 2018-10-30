package com.arcao.wherigoservice.api

import org.koin.dsl.module.module
import org.koin.experimental.builder.create

val wherigoApiModule = module {
    factory<WherigoService> {
        create<WherigoServiceImpl>()
    }
}