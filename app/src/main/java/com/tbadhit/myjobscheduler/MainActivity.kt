package com.tbadhit.myjobscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tbadhit.myjobscheduler.databinding.ActivityMainBinding

// API From : http://openweathermap.org/current (daftar akun dulu agar bisa get api key)
// membuat sebuah proses terjadwal (scheduler task)

// Codelab :
// create new kotlin class "GetCurrentWeatherJobService"
// add code "GetCurrentWeatherJobService" (1)
// add http library "LoopJ" (build.gradle module) (1)
// update "activity_main.xml"
// add code "com.tbadhit.myjobscheduler.MainActivity" (MainActivity) (1)
// add permission "INTERNET" + Service (AndroidManifest)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            startJob()
        }

        binding.btnCancel.setOnClickListener {
            cancelJob()
        }
    }

    private fun cancelJob() {
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID)
        Toast.makeText(this, "Job Service Cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun startJob() {
        if (isJobRunning(this)) {
            Toast.makeText(this, "Job Service is already scheduled", Toast.LENGTH_SHORT).show()
            return
        }
        val mServiceComponent = ComponentName(this, GetCurrentWeatherJobService::class.java)
        val builder = JobInfo.Builder(JOB_ID, mServiceComponent)
        /*
        Kondisi network,
        NETWORK_TYPE_ANY, berarti tidak ada ketentuan tertentu
        NETWORK_TYPE_UNMETERED, adalah network yang tidak dibatasi misalnya wifi
        */
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        /*
        Kondisi device, secara default sudah pada false
        false, berarti device tidak perlu idle ketika job ke trigger
        true, berarti device perlu dalam kondisi idle ketika job ke trigger
        */
        builder.setRequiresDeviceIdle(false)
        /*
        Kondisi charging
        false, berarti device tidak perlu di charge
        true, berarti device perlu dicharge
        */
        builder.setRequiresCharging(false)

        /*
        Periode interval sampai ke trigger
        Dalam milisecond, 1000ms = 1detik
        */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(900000) //15 menit
        } else {
            builder.setPeriodic(180000) //3 menit
        }
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(builder.build())
        Toast.makeText(this, "Job Service started", Toast.LENGTH_SHORT).show()
    }

    // (Bagian startJob)
    // isJobRunning = digunakan untuk mengecek apakah job sudah berjalan atau belum, sehingga job tidak dibuat secara berulang-ulang
    private fun isJobRunning(context: Context): Boolean {
        var isSchedule = false

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == JOB_ID) {
                isSchedule = true
                break
            }
        }

        return isSchedule
    }

    companion object {
        private const val JOB_ID = 10
    }
}