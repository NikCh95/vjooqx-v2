package com.github.smart24.transactions

import io.reactivex.Completable
import io.reactivex.Single

class TransactionStepImpl<T>(
    private val result: Single<T>,
    private val transactionContext: TransactionContext
) : TransactionStep<T> {

    override fun commit(): Single<T> {

        return transactionContext.getConnection().rxCommit()
            .toSingle { true }
            .flatMap { result }
            .doAfterTerminate { transactionContext.getConnection().close() }
    }

    override fun <E> then(action: (T, TransactionContext) -> Execution<E>): TransactionStep<E> {

        return TransactionStepImpl(result.flatMap {
            action(it, transactionContext).result()
        }, transactionContext)
    }

    override fun thenCommit(action: (T) -> T): Single<T> {

        return result.flatMap {
            action(it)
            transactionContext.getConnection()
                .rxCommit()
                .andThen(transactionContext.getConnection().rxClose())
                .andThen(Single.just(action(it)))
        }
    }

    override fun rollBackIf(action: (T) -> Boolean): Completable {

        return result.flatMapCompletable {
            if (action(it)) {
                transactionContext.getConnection()
                    .rxRollback().andThen(transactionContext.getConnection().rxClose())
            } else {
                transactionContext.getConnection().rxCommit().andThen(
                    transactionContext.getConnection().rxClose()
                )
            }
        }
    }

    override fun rollBackOnError(): Single<T> {
        return result.onErrorResumeNext { t ->
            transactionContext.getConnection()
                .rxRollback()
                .andThen(transactionContext.getConnection().rxClose())
                .andThen(Single.error<T>(t))
        }.flatMap {
            transactionContext.getConnection()
                .rxCommit()
                .andThen(transactionContext.getConnection().rxClose())
                .andThen(Single.just(it))
        }
    }

    override fun result(): Single<T> {
        return result
    }
}
