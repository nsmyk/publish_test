First, you need to add Google Artifact Registry repository and plugin (the guide will be provided separately with your personal credentials).


To use the `Digi` library for Android, add the following dependency to the `build.gradle` file:

```
dependencies {
    implementation 'com.sandsiv:digi:1.0.0'
}
```


On the app start call the `DigiModule.init()` with the link to the runner script as an argument:

```
package com.example.test

import android.app.Application
import com.sandsiv.DigiModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DigiModule.initialization(
            url = "https://digi_example.com/digi_runner.js",
            context = this,
        )
        // Other initializations on app start
    }
}

```

`url` - link to the runner script 

`context` - application context


When you need to show the survey screen, call the `DigiModule.show()` with proper arguments:

```
package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.test.databinding.ActivityMainBinding
import com.sandsiv.digi.DigiModule

class TestActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        vb.openSurveyButton.setOnClickListener { showSurvey() }
    }

    private fun showSurvey() {
        val params = mapOf("customer_id" to "f233d2dasd", "param2" to 2, "param3" to true)
        val margins = DigiModule.Margins(top = 10, right = 20, left = 10, bottom = 10)
        DigiModule.show(
            surveyId = 4536,
            language = "en",
            params = params,
            margins = margins,
            context = this,
        )
    }
}
```

`surveyId`, `language` - is required

`params` - optional (additional params for questionnaire, for example some metadata etc)

`margins` - optional. Margins (dp) for the questionnaire window. If not defined, the questionnaire window will be shown in the full screen mode

`context` - android context to start Digi activity
