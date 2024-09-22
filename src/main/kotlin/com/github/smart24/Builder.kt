package com.github.smart24

import com.github.smart24.json.JsonParserFactory
import io.vertx.rxjava.ext.jdbc.JDBCClient
import org.jooq.DSLContext

interface Builder {

    fun setupDelegate(delegate: JDBCClient): Builder

    fun dsl(dslContext: DSLContext): Builder

    fun create(): Vjooqx

    fun jsonFactory(jsonParserFactory: JsonParserFactory): Builder

    fun addLoggingInterceptor(loggingInterceptor: LoggingInterceptor): Builder
}
