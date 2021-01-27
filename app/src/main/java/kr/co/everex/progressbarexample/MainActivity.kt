package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import kr.co.everex.progressbarexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    var total = 0 // time
    private var cdt : object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료


        binding.button.setOnClickListener{
            cdt.
        }



        binding.progressBarHorizonLine.progress = total

        val oneMin = 1 * 60 * 1000 // 1 minute in milli seconds

        /** CountDownTimer starts with 1 minutes and every onTick is 1 second */
        cdt = object : CountDownTimer(
            oneMin.toLong(), 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                total = (timePassed / 60 * 100) as Int
                binding.progressBarHorizonLine.progress = total
            }
            override fun onFinish() {
                // DO something when 1 minute is up
            }
        }.start()

    }


}