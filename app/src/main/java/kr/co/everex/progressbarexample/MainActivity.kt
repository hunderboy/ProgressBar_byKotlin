package kr.co.everex.progressbarexample

import android.content.Intent
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        // 프로그래스 바 TEST
        binding.button1.setOnClickListener{
            val intent = Intent(this, ProgressBarInItemActivity::class.java)
            startActivity(intent)
        }
        // 리사이클러뷰 안에 프로그래스 바
        binding.button2.setOnClickListener{
            val intent = Intent(this, ProgressBarInRecyclerViewActivity::class.java)
            startActivity(intent)
        }


    }

}