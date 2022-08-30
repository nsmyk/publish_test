package com.sandsiv.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sandsiv.digi.DigiModule
import com.sandsiv.digi.Margins
import com.sandsiv.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        vb.openSurveyButton.setOnClickListener { showSurvey() }
    }

    private fun showSurvey() {
        val params: HashMap<String, Any> = hashMapOf("full_screen" to true)
        val margins = Margins()
        DigiModule.show(
            surveyId = 4536,
            language = "en",
            params = params,
            margins = margins,
            context = this,
        )
    }
}