package org.hermes.fragments

interface HasOnCreateViewCallbacks {

    fun addCreateViewCallback(callback: () -> Unit)

}