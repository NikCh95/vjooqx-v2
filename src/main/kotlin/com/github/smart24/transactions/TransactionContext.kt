package com.github.smart24.transactions

import com.github.smart24.LoggingInterceptor
import com.github.smart24.json.JsonParser
import io.vertx.rxjava.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query

interface TransactionContext {

    fun getConnection(): SQLConnection

    fun getLoggingInterceptor(): LoggingInterceptor?

    fun getJsonParser(): JsonParser

    fun fetch(query: DSLContext.() -> Query): MapperStepTransaction

    fun execute(query: DSLContext.() -> Query): TransactionStep<Int>
}
