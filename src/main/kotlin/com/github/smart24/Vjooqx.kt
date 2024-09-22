package com.github.smart24

import com.github.smart24.json.JsonParser
import com.github.smart24.transactions.TransactionContext
import com.github.smart24.transactions.TransactionContextImpl
import io.vertx.rxjava.ext.jdbc.JDBCClient
import io.vertx.rxjava.ext.sql.SQLConnection
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.conf.ParamType
import rx.Single

class Vjooqx(
    private val delegate: JDBCClient,
    private val dslContext: DSLContext,
    private val jsonParser: JsonParser,
    private val loggingInterceptor: LoggingInterceptor?
) {

    fun fetch(query: DSLContext.() -> Query): MapperStep {
        return MapperStepImpl(
            jsonParser,
            delegate
                .rxQuery(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED)
                    .apply { loggingInterceptor?.log("Database <----- : $this") }),
            loggingInterceptor
        )
    }

    fun execute(query: DSLContext.() -> Query): Single<Int> {

        return delegate
            .rxUpdate(query(dslContext).getSQL(ParamType.NAMED_OR_INLINED)
                .apply { loggingInterceptor?.log("Database <----- : $this") }
            )
            .map {
                it.updated
            }
    }

    fun transaction(): TransactionContext {
        return TransactionContextImpl(getConnection(), jsonParser, loggingInterceptor, dslContext)
    }

    private fun getConnection(): Single<SQLConnection> {
        return delegate.rxGetConnection()
    }
}
