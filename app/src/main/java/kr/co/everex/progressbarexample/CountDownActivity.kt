package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.co.everex.progressbarexample.databinding.ActivityCountDownBinding
import java.util.*


class CountDownActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountDownBinding
    private var mCountDownTimer: CountDownTimer? = null
    private var mTimerRunning = false // 카운트 다운 작동 여부

    /**
     * 1000 ms (밀리초) = 1초 => 00:01
     * 10000 ms (밀리초) = 1초 => 00:10
     * 59000 ms (밀리초) = 59초 => 00:59
     * 60000 ms (밀리초) = 60초(1분) = 01:00
     * 85000 ms (밀리초) = 60초(1분) = 01:25
     * 405000 ms (밀리초) = 60초(1분) = 06:45
     * 600000 ms (밀리초) = 600초(10분) = 10:00
     * 750000 ms (밀리초) = 600초(10분) = 12:30
     */
    private val START_TIME_IN_MILLIS: Long = 750000
    // 남은 시간 할당하기
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS

    private val testTime = "10:59"

    // 밀리세컨드로 변환 (10:30)
    private fun textToMillisecond(stringTime:String){
        val token = stringTime.split(':') // list
        // 분리 후 환산되어야 하는 초
        println(token[0]) // 10 (분) : 600000ms
        println(token[1]) // 30 (초) :  30000ms

        // 분 -> Millisecond
        val minuteMillisecond = token[0].toLong() * 60000
        Log.e("minuteMillisecond",minuteMillisecond.toString())
        // 초 -> Millisecond
        val secondMillisecond = token[1].toLong() * 1000
        Log.e("secondMillisecond",secondMillisecond.toString())

        // 시간 To Millisecond 환산 통합
        val total = minuteMillisecond + secondMillisecond
        Log.e("total",total.toString())
        mTimeLeftInMillis = total
    }


    // 시간 Text 업데이트
    private fun updateCountDownText() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60 // 분 단위 가져오기
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60 // 초 단위 가져오기
        Log.e("minutes",minutes.toString())
        Log.e("seconds",seconds.toString())

        val timeLeftFormatted: String =
            java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.textViewCountdown.text = timeLeftFormatted
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountDownBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        // 시작, 일시정지 버튼
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
        textToMillisecond(testTime)
        updateCountDownText()

    }

    // 시작되면 계속 카운트 다운
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
    // 타이머 일시 정지
    private fun pauseTimer() {
        mCountDownTimer!!.cancel()
        mTimerRunning = false
        binding.buttonStartPause.text = "Start"
        binding.buttonReset.visibility = View.VISIBLE
    }
    // 타이머 리셋
    private fun resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
        binding.buttonReset.visibility = View.INVISIBLE
        binding.buttonStartPause.visibility = View.VISIBLE
    }


}