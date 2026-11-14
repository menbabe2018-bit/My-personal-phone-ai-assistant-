package com.myai.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Color
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var transcript: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech
    private var speech: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildScreen()
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 40)
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 36, 28, 28); setBackgroundColor(Color.rgb(9, 18, 36)) }
        val heading = TextView(this).apply { text = "MY PERSONNEL PHONE AI\nYour personal phone assistant"; textSize = 26f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; setPadding(0, 12, 0, 24) }
        transcript = TextView(this).apply { text = "Hello! I'm My personnel phone AI.\n\nI can listen, speak back and open apps in this starter build. Full AI planning and on-screen actions are not connected yet. I'll always ask before sensitive actions."; textSize = 16f; setTextColor(Color.rgb(226, 235, 248)); setPadding(18, 18, 18, 18); setBackgroundColor(Color.rgb(20, 34, 57)) }
        val spacer = Space(this)
        input = EditText(this).apply { hint = "Tell me what to do…"; setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); textSize = 16f; setSingleLine(false); setPadding(14, 12, 14, 12); setBackgroundColor(Color.rgb(31, 48, 73)) }
        val run = Button(this).apply { text = "Send instruction"; setOnClickListener { handle(input.text.toString()) } }
        val voice = Button(this).apply { text = "Speak"; setOnClickListener { listen() } }
        val settings = Button(this).apply { text = "Set up phone-control access"; setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; addView(run, LinearLayout.LayoutParams(0, -2, 1f)); addView(voice, LinearLayout.LayoutParams(0, -2, 1f)) }
        root.addView(heading); root.addView(transcript, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(spacer, LinearLayout.LayoutParams(-1, 16)); root.addView(input); root.addView(buttons); root.addView(settings)
        setContentView(root)
    }

    private fun handle(raw: String) {
        val command = raw.trim()
        if (command.isEmpty()) return
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(input.windowToken, 0)
        input.setText("")
        when {
            command.equals("open settings", true) || command.equals("phone settings", true) -> { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)); reply("Opening your phone settings.") }
            command.startsWith("open ", true) -> {
                val wanted = command.substringAfter(' ').trim().lowercase(Locale.ROOT)
                val launchable = packageManager.getInstalledApplications(PackageManager.GET_META_DATA).mapNotNull { app -> packageManager.getLaunchIntentForPackage(app.packageName)?.let { Triple(app, it, packageManager.getApplicationLabel(app).toString()) } }.firstOrNull { it.third.lowercase(Locale.ROOT) == wanted || it.third.lowercase(Locale.ROOT).contains(wanted) }
                if (launchable != null) { startActivity(launchable.second); reply("Opening ${launchable.third}.") } else reply("I couldn't find an app called $wanted. Try its exact name, such as ‘open Camera’.")
            }
            command.equals("go home", true) -> reply("Use your phone's Home button for now. More phone controls are planned for a later version.")
            else -> reply("I heard: ‘$command’. The AI task planner is not connected yet, so I won't pretend I've completed that task. For now try ‘open Settings’ or ‘open Camera’.")
        }
    }

    private fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { reply("Speech recognition isn't available on this phone right now."); return }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 40); return }
        speech?.destroy(); speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { reply("Listening…") }
            override fun onBeginningOfSpeech() { }
            override fun onRmsChanged(rmsdB: Float) { }
            override fun onBufferReceived(buffer: ByteArray?) { }
            override fun onEndOfSpeech() { }
            override fun onError(error: Int) { reply("I couldn't catch that. Please try again.") }
            override fun onResults(results: Bundle?) { val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); input.setText(words); if (words.isNotBlank()) handle(words) }
            override fun onPartialResults(partialResults: Bundle?) { }
            override fun onEvent(eventType: Int, params: Bundle?) { }
        })
        speech?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell My personnel phone AI what to do") })
    }

    private fun reply(message: String) { transcript.text = message; if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "personnel-phone-ai-reply") }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale.getDefault() }
    override fun onDestroy() { speech?.destroy(); if (::tts.isInitialized) tts.shutdown(); super.onDestroy() }
}
