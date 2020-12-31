@file:Suppress("unused")

package com.listen.otic.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io

fun <T> Observable<T>.ioToMain(): Observable<T> {
    return observeOn(mainThread())
            .subscribeOn(io())
}

class LifecycleAwareDisposable(
    private val disposable: Disposable
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = disposable.dispose()
}

fun LifecycleOwner.ownRx(disposable: Disposable) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        disposable.dispose()
        return
    }
    this.lifecycle.addObserver(LifecycleAwareDisposable(disposable))
}

fun Disposable.attachLifecycle(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.ownRx(this)
}
