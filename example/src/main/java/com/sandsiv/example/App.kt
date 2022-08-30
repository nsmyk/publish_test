package com.sandsiv.example

import android.app.Application
import com.sandsiv.digi.DigiModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DigiModule.init(
            url = "https://copy-customdemo-astra.surv.biz/digi_runner.js",
            context = this,
        )
    }
}