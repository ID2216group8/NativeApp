package com.example.shouldiwearshortstoday

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private var mVelocityTracker: VelocityTracker? = null
    private lateinit var storage: Storage
    private lateinit var layout: RelativeLayout
    private lateinit var weather: Weather
    private var temp: String = "5"
    private var condition: String = "Clear Sky"
    private var clothingType: String = "comfortable"
    private var shownNoInternet: Boolean = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storage = Storage(this)
        weather = Weather()
        initValues()
        getWeather()
        val slider = findViewById<RangeSlider>(R.id.slider)
        slider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            getWeather()
        })
        layout = findViewById(R.id.relativeLayout)
        layout.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                storage.swipeRight()
                getWeather()
                updateNavbar()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                storage.swipeLeft()
                getWeather()
                updateNavbar()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                openWeatherActivity()
            }

            override fun onSwipeDown() {
                super.onSwipeDown()

            }
        })
        var mannequin = findViewById<ImageView>(R.id.mannequin)
        mannequin.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                storage.swipeRight()
                getWeather()
                updateNavbar()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                storage.swipeLeft()
                getWeather()
                updateNavbar()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                openWeatherActivity()
            }

            override fun onSwipeDown() {
                super.onSwipeDown()

            }

            override fun onClick() {
                openChangingclothes(findViewById(R.id.mannequin))
            }
        })
        getCurrentWeather()
    }

    override fun onResume() {
        super.onResume()
        storage = Storage(this)
        initValues()
        getCurrentWeather()
    }
    private fun isNetworkConnected(): Boolean {
        val connManager = this.applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
        return networkCapabilities != null
    }
    fun getCurrentWeather() {
        if(isNetworkConnected()) {
            val weatherCoroutine = lifecycleScope.async {
                val coord = storage.cities.get(storage.currentCity)
                val lat = coord!![0]
                val long = coord!![1]
                weather.getCurrentWeather(lat, long)
            }
            weatherCoroutine.invokeOnCompletion {
                val c = weatherCoroutine.getCompleted()
                temp = c[0]
                condition = c[1]
            }
        }
        else{
            showNetDialog()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWeather() {
        if(isNetworkConnected()) {
            val weatherCoroutine = lifecycleScope.async {
                val startEnd = findViewById<RangeSlider>(R.id.slider).values
                val coord = storage.cities.get(storage.currentCity)
                val lat = coord!![0]
                val long = coord!![1]
                weather.clothingAlgorithm(
                    startEnd[0].toInt(),
                    startEnd[1].toInt(),
                    lat,
                    long,
                    storage
                )
            }
            weatherCoroutine.invokeOnCompletion {
                setClothing(weatherCoroutine.getCompleted())
            }
        }
        else{
            showNetDialog()
        }
    }
    fun showNetDialog(){
        if(!shownNoInternet) {
            shownNoInternet = true
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Internet Connection")
            builder.setMessage("Can't retrieve weather data")
            builder.setPositiveButton("OK") { dialog, which ->
                shownNoInternet = false
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
    fun initValues() {
        storage.getValuesFromStorage()
        val slider = findViewById<RangeSlider>(R.id.slider)
        slider.values = listOf(storage.defaultStart.toFloat(), storage.defaultEnd.toFloat())
        findViewById<TextView>(R.id.currentCity).text = storage.currentCity
        updateNavbar()
        updateClothing()
    }

    fun updateClothing() {
        findViewById<ImageView>(R.id.hat).setImageResource(storage.hat)
        findViewById<ImageView>(R.id.scarf).setImageResource(storage.scarf)
        findViewById<ImageView>(R.id.tshirt).setImageResource(storage.tshirt)
        findViewById<ImageView>(R.id.hoodie).setImageResource(storage.hoodie)
        findViewById<ImageView>(R.id.winterJacket).setImageResource(storage.winterJacket)
        findViewById<ImageView>(R.id.shorts).setImageResource(storage.shorts)
        findViewById<ImageView>(R.id.trousers).setImageResource(storage.trousers)
        findViewById<ImageView>(R.id.umbrella).setImageResource(storage.umbrella)
    }

    fun setClothingToInvisible(): Array<ImageView> {
        val clothes = arrayOf(
            findViewById<ImageView>(R.id.hat),
            findViewById<ImageView>(R.id.scarf),
            findViewById<ImageView>(R.id.tshirt),
            findViewById<ImageView>(R.id.hoodie),
            findViewById<ImageView>(R.id.winterJacket),
            findViewById<ImageView>(R.id.shorts),
            findViewById<ImageView>(R.id.trousers),
            findViewById<ImageView>(R.id.umbrella)
        )
        for (element in clothes) {
            element.visibility = View.INVISIBLE
        }
        return clothes
    }

    fun setClothing(weather: Array<String>) {
        val umbrella = weather[1]
        val tempType = weather[0]
        clothingType = tempType
        val clothes = setClothingToInvisible()
        if (tempType == "freezing") {
            clothes[0].visibility = View.VISIBLE
            clothes[1].visibility = View.VISIBLE
            clothes[4].visibility = View.VISIBLE
            clothes[6].visibility = View.VISIBLE
        } else if (tempType == "cold") {
            clothes[4].visibility = View.VISIBLE
            clothes[6].visibility = View.VISIBLE
        } else if (tempType == "comfortable") {
            clothes[3].visibility = View.VISIBLE
            clothes[6].visibility = View.VISIBLE
        } else if (tempType == "warm") {
            clothes[2].visibility = View.VISIBLE
            clothes[6].visibility = View.VISIBLE
        } else {
            clothes[2].visibility = View.VISIBLE
            clothes[5].visibility = View.VISIBLE
        }
        if (umbrella == "true") {
            clothes[7].visibility = View.VISIBLE
        }
    }

    fun openSettingsActivity(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun openWeatherActivity(view: View) {
        val coord = storage.cities.get(storage.currentCity)
        val lat = coord!![0]
        val lon = coord!![1]
        if(isNetworkConnected()) {
            val weatherCoroutine = lifecycleScope.async {
                val coord = storage.cities.get(storage.currentCity)
                val lat = coord!![0]
                val long = coord!![1]
                weather.getCurrentWeather(lat, long)
            }
            weatherCoroutine.invokeOnCompletion {
                val c = weatherCoroutine.getCompleted()
                showWeatherPopup(c[0], c[1])
            }
        }
        else{
            showNetDialog()
        }
    }

    fun openWeatherActivity() {
        showWeatherPopup(temp, condition)
    }

    fun updateNavbar() {
        val currentCityIndex = storage.cities.keys.indexOf(storage.currentCity)
        val citiesSize = storage.cities.size
        var currentCityIndicator = ""
        for (i in 0 until citiesSize) {
            if (i == currentCityIndex)
                currentCityIndicator += "x"
            else
                currentCityIndicator += "-"
        }
        findViewById<TextView>(R.id.cityIndicator).text = currentCityIndicator
        findViewById<TextView>(R.id.currentCity).text = storage.currentCity
    }

    fun openChangingclothes(view: View) {
        val intent = Intent(this, Changingactivity::class.java)
        startActivity(intent)
    }

    fun openCityActivity(view: View) {
        val intent = Intent(this, CityActivity::class.java)
        startActivity(intent)
    }

    private fun showWeatherPopup(temperature: String, condition: String) {
        runOnUiThread {
            val weatherView = LayoutInflater.from(this).inflate(R.layout.weather_window, null)
            val weatherWindow = PopupWindow(
                weatherView,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            weatherWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#80808080")))
            weatherWindow.isFocusable = true


            weatherWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

            val temperatureTextView = weatherView.findViewById<TextView>(R.id.temperature_textview)
            val conditionTextView = weatherView.findViewById<TextView>(R.id.condition_textview)
            temperatureTextView.setTextColor(Color.RED)
            conditionTextView.setTextColor(Color.BLUE)
            temperatureTextView.text = "$temperature°C"
            conditionTextView.text = condition.toString()

            val cancelButton = weatherView.findViewById<Button>(R.id.ok_button)
            cancelButton.setOnClickListener {
                weatherWindow.dismiss()
            }
        }
    }

    fun showFeedback(view: View) {
        runOnUiThread {
            val feedbackView = LayoutInflater.from(this).inflate(R.layout.feedback_window, null)
            val feedbackWindow = PopupWindow(
                feedbackView,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            feedbackWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#80808080")))
            feedbackWindow.isFocusable = true


            feedbackWindow.showAtLocation(
                findViewById(android.R.id.content),
                Gravity.CENTER,
                0,
                0
            )
            val comfortable = feedbackView.findViewById<Button>(R.id.comfortable_button)
            comfortable.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Thank you")
                builder.setMessage("The algorithm has been adjusted")
                builder.setPositiveButton("OK") { dialog, which ->
                    feedbackWindow.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
            val warm = feedbackView.findViewById<Button>(R.id.toowarm_button)
            warm.setOnClickListener {
                if(clothingType.equals("freezing")) storage.freezing -= 2
                else if(clothingType.equals("cold")) storage.cold -= 2
                else if(clothingType.equals("comfortable")) storage.comfortable -= 2
                else if(clothingType.equals("warm")) storage.warm -= 2
                storage.setTempType()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Thank you")
                builder.setMessage("The algorithm has been adjusted")
                builder.setPositiveButton("OK") { dialog, which ->
                    feedbackWindow.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
            val cold = feedbackView.findViewById<Button>(R.id.toocold_button)
            cold.setOnClickListener {
                if(clothingType.equals("hot")) storage.warm += 2
                else if(clothingType.equals("cold")) storage.freezing += 2
                else if(clothingType.equals("comfortable")) storage.cold += 2
                else if(clothingType.equals("warm")) storage.comfortable += 2
                storage.setTempType()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Thank you")
                builder.setMessage("The algorithm has been adjusted")
                builder.setPositiveButton("OK") { dialog, which ->
                    feedbackWindow.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
            val cancelButton = feedbackView.findViewById<Button>(R.id.ok_button)
            cancelButton.setOnClickListener {
                feedbackWindow.dismiss()
            }
        }
    }
}
