package com.beerstoresystem.supply

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SupplyServiceApplication

fun main(args: Array<String>) {
    runApplication<SupplyServiceApplication>(*args)
}
