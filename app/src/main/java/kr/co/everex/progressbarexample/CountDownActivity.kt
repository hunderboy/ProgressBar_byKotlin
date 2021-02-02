package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kr.co.everex.progressbarexample.databinding.ActivityCountDownBinding
import java.util.*


class CountDownActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountDownBinding

    private val START_TIME_IN_MILLIS: Long = 600000
    private var mCountDownTimer: CountDownTimer? = null
    private var mTimerRunning = false
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountDownBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        binding.buttonStartPause.setOnClickListener {
            if (mTimerRunning) { // 카운트 중
                pauseTimer()
            } else {    // 카운트 중 아님
                startTimer()
            }
        }
        binding.buttonReset.setOnClickListener {
            resetTimer()
        }
        updateCountDownText()
    }


    private fun startTimer() {
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                mTimerRunning = false
                binding.buttonStartPause.text = "Start"
                binding.buttonStartPause.visibility = View.INVISIBLE
                binding.buttonReset.visibility = View.VISIBLE
            }
        }.start()
        mTimerRunning = true
        binding.buttonStartPause.text = "pause"
        binding.buttonReset.visibility = View.INVISIBLE
    }

    private fun pauseTimer() {
        mCountDownTimer!!.cancel()
        mTimerRunning = false
        binding.buttonStartPause.text = "Start"
        binding.buttonReset.visibility = View.VISIBLE
    }

    private fun resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
        binding.buttonReset.visibility = View.INVISIBLE
        binding.buttonStartPause.visibility = View.VISIBLE
    }

    // 시작되면 계속 카운트 다운
    private fun updateCountDownText() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60
        val timeLeftFormatted: String =
            java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.textViewCountdown.text = timeLeftFormatted
    }

}