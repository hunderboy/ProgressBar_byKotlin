package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.co.everex.progressbarexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    var total = 0 // time





    fun ready(){
        val readyToProgressBar = scope.launch {
            for (i in 1..500) { // 1 to 500

                binding.progressBarHorizonLine.secondaryProgress = binding.progressBarHorizonLine.secondaryProgress +1 // i는 500까지 감

                delay(10) // 0.01 millisecond
                if(i == 500) {
                    Toast.makeText(applicationContext, "i = $i", Toast.LENGTH_SHORT).show()
                    // 운동 핸들러 시작
                    binding.progressBarHorizonLine.max = 1000 // 50초 = 5000 칸
                    binding.progressBarHorizonLine.secondaryProgress = 0 // 0으로 초기화
                    play()
                }
            }
        }
        readyToProgressBar.isActive
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료


        binding.button.setOnClickListener{

        }

        // 실제로 5초의 휴식 시간 이 꼭 무조건 적으로 정확해야 하는가?
        // 화면 안정화 상태에 접어든 뒤에 실행시키면, 실제로 5초동안 정확히 동작 하는가?


//        binding.progressBarHorizonLine.progress = total
//
//        val oneMin = 1 * 60 * 1000 // 1 minute in milli seconds
//
//        /** CountDownTimer starts with 1 minutes and every onTick is 1 second */
//        cdt = object : CountDownTimer(
//            oneMin.toLong(), 1000
//        ) {
//            override fun onTick(millisUntilFinished: Long) {
//                total = (timePassed / 60 * 100) as Int
//                binding.progressBarHorizonLine.progress = total
//            }
//            override fun onFinish() {
//                // DO something when 1 minute is up
//            }
//        }.start()

    }


}