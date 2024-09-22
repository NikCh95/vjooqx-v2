package com.github.smart24.exceptions

class EmptyResultSet : NullPointerException {

    constructor() : super()
    constructor(message: String?) : super(message)
}
