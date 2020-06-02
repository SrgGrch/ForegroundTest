package com.example.foregroundtest

object TestObject {
    var onNewToken: ((String) -> Unit)? = null
    var messageReceived: ((String) -> Unit)? = null
}
