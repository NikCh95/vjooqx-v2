package com.github.smart24.transactions

import rx.Completable
import rx.Single

class TransactionStepImpl<T>(
    private val result: Single<T>,
    private val transactionContext: TransactionContext
) : TransactionStep<T> {

    override fun commit(): Single<T> {

        return transactionContext.getConnection().rxCommit()
            .flatMap { result }
            .doAfterTerminate { transactionContext.getConnection().close() }
    }

    override fun <E> then(action: (T, TransactionContext) -> Execution<E>): TransactionStep<E> {

        return TransactionStepImpl(result.flatMap {
            action(it, transactionContext).result()
        }, transactionContext)
    }

    override fun thenCommit(action: (T) -> T): Single<T> {
        return result.flatMap { it ->
            val resultAfterAction = action(it)
            transactionContext.getConnection()
                .rxCommit()
                .flatMap {
                    transactionContext.getConnection().rxClose()
                }
                .flatMap {
                    Single.just(resultAfterAction)
                }
        }
    }

    override fun rollBackIf(action: (T) -> Boolean): Completable {
        return result.flatMapCompletable { it ->
            val connection = transactionContext.getConnection()
            if (action(it)) {
                connection.rxRollback()
                    .flatMapCompletable {
                        connection.rxClose().toCompletable()
                    }
            } else {
                connection.rxCommit()
                    .flatMapCompletable {
                        connection.rxClose().toCompletable()
                    }
            }
        }
    }

    override fun rollBackOnError(): Single<T> {
        return result.onErrorResumeNext { t ->
            val connection = transactionContext.getConnection()
            connection.rxRollback()
                .flatMap {
                    connection.rxClose()
                }.flatMap {
                    Single.error<T>(t)
                }
        }.flatMap { result ->
            val connection = transactionContext.getConnection()
            connection.rxCommit()
                .flatMap {
                    connection.rxClose()
                }.flatMap {
                    Single.just(result)
                }
        }
    }

    override fun result(): Single<T> {
        return result
    }
}
