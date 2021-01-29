package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kr.co.everex.progressbarexample.databinding.ActivityMainBinding
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.measureTimeMillis


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // 스톱워치 변수
    private var time = 0
    private var isRunning = false
    private var timerTask: Timer? = null
//    private var sec = 0
//    private var milli = 0

    // secondary = true
    // progress = false
    private var progressBooleanType = true


    // 싱글톤
    companion object {
        // 싱글톤 object
        val timeData = TimeData()
    }

    // 내부 클래스
    class TimeData {
        // 클래스 속성(second, millisecond)
        var timeDataProgressValue = 0
        var sec = 0
        var milli: Int by Delegates.observable(0, {props, old, new ->
            // 데이터 변경 시에 실행되는 코드
            timeDataProgressValue += 1
        })
    }



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
            time++ // 계속 변경됨
            // 변경 될때 마다 새롭게 변수 생성되고 연산되고, 연산된 변수 할당됨
            timeData.sec = time / 100
            timeData.milli = time % 100 // 나머지 값 : 00 ~ 99

            /**
             * sec = 5 가 되는 순간
             * secondaryProgress = 0 초기화
             * Progress +1 씩 증가
             */
             if(timeData.sec == 5){
                 binding.progressBarHorizonLine.secondaryProgress = 0 // 칸 초기화
                 binding.progressBarHorizonLine.max = 1000 // 1000 칸으로
                 timeData.timeDataProgressValue = 0
                 progressBooleanType = false
             }
            /**
             * sec = 15초 가 되는 순간
             * Progress = 0 초기화
             * timerTask 중지
             */
            else if (timeData.sec == 15){
                 timeData.timeDataProgressValue = 0
                 progressBooleanType = true
                 timerTask?.cancel() // 테스크 종료
            }

            // 지켜보고 있다가 데이터가 변하면 바로 적용
            runOnUiThread {
                binding.secText.text = "${timeData.sec}"
                binding.milliText.text = "${timeData.milli}"

                // Todo true
                if(progressBooleanType){ // progressbar secondaryProgress 진행
                    binding.progressBarHorizonLine.secondaryProgress =
                        timeData.timeDataProgressValue
                // Todo false
                }else { // progressbar Main Progress 진행
                    binding.progressBarHorizonLine. progress =
                        timeData.timeDataProgressValue
                }
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

        binding.progressBarHorizonLine.max = 500 // 500 칸으로


        binding.progressBarHorizonLine.progress = 0
        binding.progressBarHorizonLine.secondaryProgress = 0
        timeData.timeDataProgressValue = 0

        time = 0
        isRunning = false
        binding.fabStart.text = "시작"
        binding.secText.text = "0"
        binding.milliText.text = "00"
    }



}