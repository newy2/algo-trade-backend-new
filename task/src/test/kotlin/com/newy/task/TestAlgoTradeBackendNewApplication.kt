package com.newy.task

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<AlgoTradeBackendNewApplication>().with(TestcontainersConfiguration::class).run(*args)
}
