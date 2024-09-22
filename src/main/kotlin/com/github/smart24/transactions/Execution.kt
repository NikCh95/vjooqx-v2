package com.github.smart24.transactions

import rx.Single

interface Execution<E> {

    fun result(): Single<E>
}
