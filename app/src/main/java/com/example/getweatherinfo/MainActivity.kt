package com.example.getweatherinfo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.getweatherinfo.data.Location
import com.example.getweatherinfo.data.WeatherData
import com.example.getweatherinfo.databinding.ActivityMainBinding
import com.example.getweatherinfo.util.OkHttpUtil
import com.example.getweatherinfo.util.OkHttpUtil.Companion.mOkHttpClient
import com.google.gson.Gson
import okhttp3.Response
import okio.IOException


class MainActivity : AppCompatActivity() {
    val weatherUrl = BuildConfig.weather_url
    private var currentLocation: String = "臺北市"
    private var currentTime: String = "今天"
    private lateinit var weatherData: WeatherData
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //設定Viewbinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //搜尋資料
        selectData()

    }


    //    var elementIndex:Int = 0
    var timeIndex: Int = 0
    private fun selectData() {

        //set spinner
        //要找R.array.location_data並需用createFromResource
        val spLocationName = ArrayAdapter.createFromResource(
            this, R.array.location_data, android.R.layout.simple_spinner_dropdown_item
        )
        binding.spLocationName.adapter = spLocationName


        val spTimeData = ArrayAdapter.createFromResource(
            this, R.array.time_data, android.R.layout.simple_spinner_dropdown_item
        )
        binding.spTime.adapter = spTimeData

        //set spinner on Selected
        binding.spLocationName.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentLocation = binding.spLocationName.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        binding.spTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentTime = binding.spTime.selectedItem.toString()
                timeIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.btnSearch.setOnClickListener {
            Log.d("SpinnerT", currentLocation)
            Log.d("SpinnerT", currentTime)
            getWeatherData(currentLocation,currentTime)
        }

    }


    private fun getWeatherData(getLocation: String, getTime: String) {
        mOkHttpClient.getAsync(weatherUrl, object : OkHttpUtil.ICallback {
            override fun onResponse(response: Response) {
                val WeatherDataReponse = response.body?.string()
                weatherData =
                    Gson().fromJson(WeatherDataReponse, WeatherData::class.java)  //將資料轉換成Gson

                for (locationIndex in weatherData.records.location) {
                    if (locationIndex.locationName == getLocation) {
                        runOnUiThread {
                            //縣市
                            binding.tvLocation.text = locationIndex.locationName
                            //天氣名稱
                            binding.tvWeatherDescription.text =
                                locationIndex.weatherElement[0].time[timeIndex].parameter.parameterName
                            //最高溫
                            binding.tvHT.text =
                                locationIndex.weatherElement[4].time[timeIndex].parameter.parameterName
                            //最低溫
                            binding.tvLT.text =
                                locationIndex.weatherElement[2].time[timeIndex].parameter.parameterName
                            //降雨機率
                            binding.tvRainProbability.text =
                                locationIndex.weatherElement[1].time[timeIndex].parameter.parameterName+"%"
                        }
                        //執行'建議準備'
                        getSuggestion(locationIndex)
                    }
                }
            }

            override fun onFailure(e: IOException) {
                Log.d("HKT", "onFailure: $e")
            }
        })
    }

    private fun getSuggestion(location: Location) {
        mOkHttpClient.getAsync(weatherUrl, object : OkHttpUtil.ICallback {
            override fun onResponse(response: Response) {
                val WeatherDataReponse = response.body?.string()
                weatherData =
                    Gson().fromJson(WeatherDataReponse, WeatherData::class.java)  //將資料轉換成Gson

                var imgArray = arrayOfNulls<Int>(6)
                var imgArrayIndex = 0
                val bindingArray = arrayOf(binding.img1,binding.img2,binding.img3,binding.img4,binding.img5,binding.img6)
                //晴天 陽傘、防曬乳
                //比較熱 水、防蚊液
                //雨天 雨傘、防水鞋
                //比較冷 帶外套
                //冬天 圍巾、手套
                if(location.weatherElement[0].time[timeIndex].parameter.parameterName.contains("晴")){
                    println("有太陽")
                    imgArray[imgArrayIndex] = R.drawable.parasol
                    imgArrayIndex++
                    imgArray[imgArrayIndex] = R.drawable.sunscreenlotion
                    imgArrayIndex++
                }

                if(location.weatherElement[0].time[timeIndex].parameter.parameterName.contains("雨")){
                    println("可能下雨")
                    imgArray[imgArrayIndex] = R.drawable.umbrella
                    imgArrayIndex++
                    imgArray[imgArrayIndex] = R.drawable.waterproofshoes
                    imgArrayIndex++
                }

                if(location.weatherElement[4].time[timeIndex].parameter.parameterName.toInt()>=30){
                    println("太熱了")
                    imgArray[imgArrayIndex] = R.drawable.waterbottle
                    imgArrayIndex++
                    imgArray[imgArrayIndex] = R.drawable.mosquitorepellent
                    imgArrayIndex++
                }
                else if(location.weatherElement[4].time[timeIndex].parameter.parameterName.toInt()<=20){
                    println("太冷了")
                    imgArray[imgArrayIndex] = R.drawable.scarf
                    imgArrayIndex++
                    imgArray[imgArrayIndex] = R.drawable.gloves
                    imgArrayIndex++
                }

                if(location.weatherElement[2].time[timeIndex].parameter.parameterName.toInt()<=24){
                    println("溫度較低")
                    imgArray[imgArrayIndex] = R.drawable.jacket
                }
                
                //設定圖片
                runOnUiThread{
                    var bindingArrayIndex = 0
                    for(imgname in imgArray){
                        if (imgname != null) {
                            bindingArray[bindingArrayIndex].setBackgroundResource(imgname)
                        }
                        else{
                            bindingArray[bindingArrayIndex].setBackgroundResource(R.drawable.round_corner)
                        }
                        bindingArrayIndex++
                    }
                }
            }

            override fun onFailure(e: IOException) {

            }
        })
    }
}