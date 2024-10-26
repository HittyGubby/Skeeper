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
    private lateinit var textview: TextView
    private var custhour: Int = 6
    private var custmin: Int = 40
    private lateinit var userpref: SharedPreferences
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userpref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        custhour = userpref.getInt("customHour", 6)
        custmin = userpref.getInt("customMinute", 40)
        textview = findViewById(R.id.textviewdisplay)
        updateNumber()
        val serviceIntent = Intent(this, FloatingBarService::class.java)
        startService(serviceIntent)
        findViewById<View>(androidx.appcompat.R.id.action_bar).setOnClickListener {
            showDescriptionPopup() } }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            custhour = hourOfDay
            custmin = minute
            userpref.edit().apply {
                putInt("customHour", custhour)
                putInt("customMinute", custmin)
                apply() }
            updateNumber()
        }, custhour, custmin, true)
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
                val nowmilli = System.currentTimeMillis()
                val targetmilli = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, custhour)
                    set(Calendar.MINUTE, custmin)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= nowmilli) {add(Calendar.DATE, 1)}}.timeInMillis
                val timemilli = targetmilli - nowmilli
                val restmilli = 24 * 60 * 60 * 1000 - timemilli.toDouble()
                val number = restmilli / timemilli / 2 * 100
                val string = String.format("%.4f", number) + "%"
                textview.text = string
                FloatingBarService.updateFloatingBar(string)
                handler.postDelayed(this, 10)}})}
}