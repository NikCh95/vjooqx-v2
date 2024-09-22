package com.github.smart24.transactions

import io.reactivex.Single

interface Execution<E> {

    fun result(): Single<E>
}
