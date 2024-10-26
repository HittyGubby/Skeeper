package com.hittygubby.skeeper
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*
class MainActivity : AppCompatActivity() {
    private lateinit var numberTextView: TextView
    private var customHour: Int = 6
    private var customMinute: Int = 40
    private lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        customHour = sharedPreferences.getInt("customHour", 6)
        customMinute = sharedPreferences.getInt("customMinute", 40)
        numberTextView = findViewById(R.id.numberTextView)
        updateNumber()
        val serviceIntent = Intent(this, FloatingBarService::class.java)
        startService(serviceIntent)
        findViewById<View>(androidx.appcompat.R.id.action_bar).setOnClickListener {
            showDescriptionPopup() } }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            customHour = hourOfDay
            customMinute = minute
            sharedPreferences.edit().apply {
                putInt("customHour", customHour)
                putInt("customMinute", customMinute)
                apply() }
            updateNumber()
        }, customHour, customMinute, true)
        timePickerDialog.show() }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> { showTimePickerDialog();true }else -> super.onOptionsItemSelected(item) } }

    private fun showDescriptionPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Description")
        builder.setMessage("Written for fun. \n\nThe percentage is for how much your awake time rated against sleep time compared to internationally recommended 8 hours of sleep.\n\nGo check your sleep and remind yourself using this tool.")
        builder.setPositiveButton("Gotcha", null)
        builder.show() }

    private fun updateNumber() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                val nowMillis = System.currentTimeMillis()
                val morningTimeMillis = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, customHour)
                    set(Calendar.MINUTE, customMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= nowMillis) {add(Calendar.DATE, 1)}}.timeInMillis
                val timePeriodMillis = morningTimeMillis - nowMillis
                val restMillis = 24 * 60 * 60 * 1000 - timePeriodMillis.toDouble()
                val number = restMillis / timePeriodMillis / 2 * 100
                val numberString = String.format("%.4f", number) + "%"
                numberTextView.text = numberString
                FloatingBarService.updateFloatingBar(numberString)
                handler.postDelayed(this, 10)}})}
}