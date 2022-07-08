package com.example.learningrx01.app

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

interface SchedulerProvider {
    fun io(): Scheduler
    fun ui(): Scheduler
}

class SchedulerProviderImpl : SchedulerProvider {
    override fun io() = Schedulers.io()

    override fun ui() = AndroidSchedulers.mainThread()
}