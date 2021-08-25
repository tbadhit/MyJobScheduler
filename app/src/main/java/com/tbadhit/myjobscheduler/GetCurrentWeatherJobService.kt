package com.tbadhit.myjobscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.DecimalFormat

// (1) Extend JobService
// all (1)
class GetCurrentWeatherJobService : JobService() {

    // (onStartJob) = adalah metode yang akan dipanggil ketika scheduler berjalan
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStartJob()")
        getCurrentWeather(params)
        return true
    }

    // (onStopJob) akan dipanggil ketika job sedang berjalan akan tetapi belum selesai dikarenakan kondisi nya tidak terpenuhi.
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob()")
        return true
    }

    private fun getCurrentWeather(job: JobParameters) {
        Log.d(TAG, "getCurrentWeather: Mulai...")
        val client = AsyncHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=$APP_ID"
        Log.d(TAG, "getCurrentWeather: $url")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray
            ) {
                val result = String(responseBody)
                Log.d(TAG, result)

                try {
                    val responseObject = JSONObject(result)

                    val currentWeather =
                        responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0)
                        .getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")

                    val tempInCelsius = tempInKelvin - 273
                    // Kode di bawah digunakan untuk memformat tampilan agar hanya ada dua nilai dibelakang koma
                    val temperature = DecimalFormat("##.##").format(tempInCelsius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celsius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId)

                    Log.d(TAG, "onSuccess: Selesai.....")
                    // ketika proses selesai, maka perlu dipanggil jobFinished dengan parameter false;
                    jobFinished(job, false)

                } catch (e: Exception) {
                    Log.d(TAG, "onSuccess: Gagal.....")
                    // ketika terjadi error, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                    jobFinished(job, true)
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.d(TAG, "onFailure: Gagal.....")
                jobFinished(job, true)
            }

        })
    }

    private fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_replay_30_black_24dp)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }
        val notification = builder.build()

        notificationManagerCompat.notify(notifId, notification)
    }


    // (1)
    companion object {
        private val TAG = GetCurrentWeatherJobService::class.java.simpleName

        // Isikan nama kota
        internal const val CITY = "Jakarta"

        // Isikan API Key
        internal const val APP_ID = "23373b345890d776e2d25e7e052061c1"
    }
}