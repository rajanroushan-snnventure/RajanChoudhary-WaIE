package com.example.myapplication.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.NetworkUtils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.Date
import java.util.concurrent.TimeUnit

class APODViewModel : ViewModel() {

    private val repository = APODRepository()
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    private val _explanation = MutableLiveData<String>()
    val explanation: LiveData<String> get() = _explanation

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _mediaType = MutableLiveData<String>()
    val mediaType: LiveData<String> get() = _mediaType

    private val lastApiCallKey = "last_api_call_time"
    private val TITLE_LOCAL = "TITLE"
    private val EXPLANATION_LOCAL = "EXPLANATION"
    private val URL_LOCAL = "URL"
    private val MEDIA_TYPE = "media"


    fun fetchAPOD(context: Context) {
        viewModelScope.launch {
            RetrofitClient.service.getAPOD("DEMO_KEY").enqueue(object : Callback<APODResponse> {
                override fun onResponse(call: Call<APODResponse>, response: Response<APODResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _title.value = it.title
                            _explanation.value = it.explanation
                            _imageUrl.value = it.url
                            _mediaType.value = it.mediaType
                        }
                    }
                }

                override fun onFailure(call: Call<APODResponse>, t: Throwable) {
                    // Handle the error
                }
            })
        }
    }

    private fun getLastApiCallTime(context: Context): Long {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong(lastApiCallKey, 0)
    }

    fun setLastApiCallTime(timeMillis: Long, context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putLong(lastApiCallKey, timeMillis).apply()
    }

    fun setLocalData(context: Context, title: String, url: String, explanation: String, media:String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(TITLE_LOCAL, title).apply()
        sharedPreferences.edit().putString(EXPLANATION_LOCAL, explanation).apply()
        sharedPreferences.edit().putString(URL_LOCAL, url).apply()
        sharedPreferences.edit().putString(MEDIA_TYPE, media).apply()
    }

    private fun getLocalData(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        _title.value = sharedPreferences.getString(TITLE_LOCAL, "")
        _explanation.value = sharedPreferences.getString(EXPLANATION_LOCAL, "")
        _imageUrl.value = sharedPreferences.getString(URL_LOCAL, "")
        _mediaType.value = sharedPreferences.getString(MEDIA_TYPE, "")
    }

    fun fetchData(context: Context) {
        val currentTimeMillis = System.currentTimeMillis()
        val lastApiCallTime = getLastApiCallTime(context)
        val oneDayInMillis = TimeUnit.DAYS.toMillis(1)

        if (lastApiCallTime == 0L) {
            //call the api
            if (NetworkUtils.isNetworkAvailable(context)) {
                fetchAPOD(context)
            } else {
                _title.value = "No Internet Connectivity."
                _explanation.value = "No Internet Connectivity."
            }
        } else if (currentTimeMillis - lastApiCallTime >= oneDayInMillis) {
            //call the api
            if (NetworkUtils.isNetworkAvailable(context)) {
                fetchAPOD(context)
            } else {
                _title.value = "No Internet Connectivity."
                _explanation.value = "No Internet Connectivity."
            }
        } else {
            //call the local data
            getLocalData(context)
        }
    }

}
