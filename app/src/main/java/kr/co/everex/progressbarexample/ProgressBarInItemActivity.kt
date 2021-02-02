package kr.co.everex.progressbarexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.adapter.ExplainExerciseListAdapter
import kr.co.everex.progressbarexample.databinding.ActivityProgressBarInItemBinding
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import java.util.*
import kotlin.properties.Delegates

class ProgressBarInItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBarInItemBinding




    // 스톱워치 변수
    private var time = 0
    private var isRunning = false
    private var timerTask: Timer? = null




    // 코루틴 scope
    private val scope = CoroutineScope(Dispatchers.Main)

    // 싱글톤
    companion object {
        // 싱글톤 object
        private val timeData = TimeData()
    }

    // 내부 클래스
    private class TimeData {
        // 클래스 속성(second, millisecond)
        var timeDataProgressValue = 0
        var sec = 0

        // Todo milli 데이터 변경 할 때마다 실행되는 코드
        var milli: Int by Delegates.observable(0, { props, old, new ->
            timeDataProgressValue += 1
        })
    }




    /**
     * ready progress 코루틴
     */
    fun ready(){
        val readyToProgressBar = scope.launch {
            binding.fabStart.text = "일시정지" // 텍스트 일시정지로 변경

            timerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨
                timeData.sec = time / 100   // 초 단위 값
                timeData.milli = time % 100 // (나머지 값) : 0 ~ 99 값

                // 5가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(timeData.sec == 5){
                    binding.progressBarHorizonLine.secondaryProgress = 0 // ready progress '0' 초기화
                    binding.progressBarHorizonLine.max = 1000 // 1000 칸으로 변경 = 10초
                    timerTask?.cancel()
                    play()
                }

                // timeData.milli 데이터가 계속 변경 되기때문에, 0.01초 마다 변경됨
                runOnUiThread {
                    binding.secText.text = "${timeData.sec}"
                    binding.milliText.text = "${timeData.milli}"

                    binding.progressBarHorizonLine.secondaryProgress =
                        binding.progressBarHorizonLine.secondaryProgress + 1 // 0.01 초에 1칸씩 채워진다.
                }
            }

        }
        readyToProgressBar.isActive
    }

    /**
     * main progress 코루틴
     */
    fun play(){
        val playToProgressBar = scope.launch {

            timerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨
                timeData.sec = time / 100   // 초 단위 값
                timeData.milli = time % 100 // (나머지 값) : 0 ~ 99 값

                // 5가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(timeData.sec == 15){
                    binding.progressBarHorizonLine.progress = 0 // main progress '0' 초기화
                    timerTask?.cancel()
                }

                runOnUiThread {
                    binding.secText.text = "${timeData.sec}"
                    binding.milliText.text = "${timeData.milli}"

                    binding.progressBarHorizonLine.progress =
                        binding.progressBarHorizonLine.progress + 1 // 0.01 초에 1칸씩 채워진다.
                }
            }

        }
        playToProgressBar.isActive
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBarInItemBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료


        // 일시정지 상태에서는 데이터 정지 + 일시정지 화면을 띄운다.
        binding.fabStart.setOnClickListener{
            isRunning = !isRunning
            if (isRunning) ready() else pause()
        }
        binding.fabReset.setOnClickListener{
            reset()
        }


    }// onCreate



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