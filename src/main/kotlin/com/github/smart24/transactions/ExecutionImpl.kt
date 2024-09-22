package com.github.smart24.transactions

import rx.Single

class ExecutionImpl<E>(
    private val result: Single<E>
) : Execution<E> {
    override fun result(): Single<E> {
        return result
    }
}
