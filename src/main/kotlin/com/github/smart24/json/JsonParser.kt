package com.github.smart24.json

interface JsonParser {

    fun <T> encode(json: String, pClass: Class<T>): T
}
