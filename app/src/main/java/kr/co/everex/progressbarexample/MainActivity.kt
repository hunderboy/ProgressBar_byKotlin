package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kr.co.everex.progressbarexample.databinding.ActivityMainBinding
import java.util.*
import kotlin.system.measureTimeMillis


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // 스톱워치 변수
    private var time = 0
    private var isRunning = false
    private var timerTask: Timer? = null
    private var progressNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료


        // 일시정지 상태에서는 데이터 정지 + 일시정지 화면을 띄운다.
        binding.fabStart.setOnClickListener{
            isRunning = !isRunning
            if (isRunning) start() else pause()
        }
        binding.fabReset.setOnClickListener{
            reset()
        }
    }

    // 시작 함수
    private fun start() {
        binding.fabStart.text = "일시정지" // 텍스트 일시정지로 변경

        // period = 10 는 무엇을 뜻하는 것인가?
        timerTask = kotlin.concurrent.timer(period = 10) {
            time++
            progressNumber++
            val sec = time / 100
            val milli = time % 100 // 나머지 값 : 00 ~ 99


            // 지켜보고 있다가 데이터가 변하면 바로 적용
            runOnUiThread {
                binding.secText.text = "$sec"
                binding.milliText.text = "$milli"
                binding.progressBarHorizonLine.secondaryProgress = progressNumber // 0 ~ 500


            }
        }
    }

    // 일시정지 함수
    private fun pause() {
        binding.fabStart.text = "시작" // 텍스트 시작으로 변경
        timerTask?.cancel()
    }

    private fun reset() {
        timerTask?.cancel()

        binding.progressBarHorizonLine.secondaryProgress = 0
        progressNumber = 0

        time = 0
        isRunning = false
        binding.fabStart.text = "시작"
        binding.secText.text = "0"
        binding.milliText.text = "00"
    }



}