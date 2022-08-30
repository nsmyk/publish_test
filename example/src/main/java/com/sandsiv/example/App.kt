package com.sandsiv.example

import android.app.Application
import com.sandsiv.digi.DigiModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DigiModule.init(url = "https://stage2-astra.surv.biz/digi_runner.js", this)
    }
}