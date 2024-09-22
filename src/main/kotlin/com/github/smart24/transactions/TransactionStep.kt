package com.github.smart24.transactions

import rx.Completable
import rx.Single

interface TransactionStep<T> : Execution<T> {

    fun commit(): Single<T>

    fun <E> then(action: (T, TransactionContext) -> Execution<E>): TransactionStep<E>

    fun thenCommit(action: (T) -> T): Single<T>

    fun rollBackIf(action: (T) -> Boolean): Completable

    fun rollBackOnError(): Single<T>
}
