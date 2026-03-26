package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.slf4j.LoggerFactory

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(DemoApplication::class.java)
    logger.info("Starting Demo Application...")
    runApplication<DemoApplication>(*args)
    logger.info("Demo Application started successfully!")
}