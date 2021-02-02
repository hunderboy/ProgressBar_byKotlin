package kr.co.everex.progressbarexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.adapter.ExplainExerciseListAdapter
import kr.co.everex.progressbarexample.databinding.ActivityProgressBarInRecyclerViewBinding
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import java.util.*
import android.os.CountDownTimer
import kotlin.properties.Delegates

class ProgressBarInRecyclerViewActivity : AppCompatActivity(), MyRecyclerviewInterface {
    private lateinit var binding: ActivityProgressBarInRecyclerViewBinding
    val TAG: String = "로그"


    // 리사이클러뷰 데이터 리스트
    var modelList = ArrayList<ExplainExerciseListModel>()
    private lateinit var explainExerciseListAdapter: ExplainExerciseListAdapter

    // 코루틴 scope
    private val scope = CoroutineScope(Dispatchers.Main)




    // 스톱워치 변수
    private var time = 0
    private var sec = 0
    private var isRunning = false

    private var downTimerTask: CountDownTimer? = null

    // 준비
    private var readyTimerTask: Timer? = null

    // 운동
    private var exerciseTimerTask: Timer? = null



    // 일시정지 함수
//    private fun pause() {
//        binding.fabStart.text = "시작" // 텍스트 시작으로 변경
//        timerTask?.cancel()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBarInRecyclerViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        // 시작 일시 정지
        binding.controlButton.setOnClickListener{
//            isRunning = !isRunning
//            if (isRunning) ready() else pause()
        }


        /**
         * 리사이클러뷰 세팅  ------------------------------------------------------------
         */
        for (i in 1..5){
            val imageUri = numberImageWhen(i)
            val readyProgressMaxValue = readyProgressDataWhen(i)
            val exerciseProgressMaxValue = exerciseProgressDataWhen(i)

            val explainExerciseListModel = ExplainExerciseListModel(
                exerciseName = "Exercise $i",
                exerciseImage = imageUri,
                readyProgressMaxValue = readyProgressMaxValue,
                exerciseProgressMaxValue = exerciseProgressMaxValue,
            )
            this.modelList.add(explainExerciseListModel)
        }
        // 어답터 인스턴스 생성
        explainExerciseListAdapter = ExplainExerciseListAdapter(this)
        explainExerciseListAdapter.submitList(this.modelList)
        // 리사이클러뷰 설정
        binding.RecyclerViewPlayExerciseList.apply {
            // 리사이클러뷰 방향 등 설정
            layoutManager = LinearLayoutManager(this@ProgressBarInRecyclerViewActivity, LinearLayoutManager.VERTICAL, false)
            // 어답터 장착
            adapter = explainExerciseListAdapter
        }

        // 리사이클러뷰 세팅 완료 후 작업 ------------------------------------------------------------
        ready()

//        var downTimerTask = CountDownTimer(40000, 1000){
//        }.start()


    }// onCreate 끝



    /**
     * ready progress 코루틴 비동기 처리
     * 각 아이템의 프로그래스 바를 실행 시킨다.
     * 맨 첫번째 아이템의 프로그래스바 실행 시킨후 실행이 끝나면
     * 그다음 index 의 아이템의 프로그래스 바를 실행시킨다.
     *
     */
    fun ready(){
        val readyToProgressBar = scope.launch {
            readyTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨
                sec = time / 100   // 초 단위 값

                // 5.0 초 가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(sec == 5){
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        sec = 0
                        modelList[0].readyProgressValue = 0
                        modelList[0].readyIsRunning = false
                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        readyTimerTask?.cancel()
                        play() // 운동 progress bar 시작
                    }
                }
                runOnUiThread {
                    // 0.01초 마다 변경됨
                    modelList[0].readyIsRunning = true
                    modelList[0].readyProgressValue += 1
                    explainExerciseListAdapter.submitList(modelList)
                    explainExerciseListAdapter.notifyDataSetChanged()
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
            exerciseTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨
                sec = time / 100   // 초 단위 값

                if(sec == 10){  // 10초
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        sec = 0
                        modelList[0].exerciseProgressValue = 0
                        modelList[0].exerciseIsRunning = false
                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        exerciseTimerTask?.cancel()
                    }
                }
                runOnUiThread {
                    // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
                    modelList[0].exerciseIsRunning = true
                    modelList[0].exerciseProgressValue += 1
                    explainExerciseListAdapter.submitList(modelList)
                    explainExerciseListAdapter.notifyDataSetChanged()
                }
            }
        }
        playToProgressBar.isActive
    }




    /**
     * 리사이클러뷰 리스트 데이터 설정
     */
    private fun numberImageWhen(a: Any): String {
        val value = when (a) {
            1 -> "https://i.ibb.co/Pcnp65t/exercise-list-1.png"
            2 -> "https://i.ibb.co/W5NLCYp/exercise-list-2.png"
            3 -> "https://i.ibb.co/QC2nYBy/exercise-list-3.png"
            4 -> "https://i.ibb.co/8bRGjkn/exercise-list-4.png"
            5 -> "https://i.ibb.co/R0YZjvN/exercise-list-5.png"
            else ->
                "https://img1.daumcdn.net/thumb/C100x100.mplusfriend/?fname=http%3A%2F%2Fk.kakaocdn.net%2Fdn%2FIxxPp%2FbtqC9MkM3oH%2FPpvHOkfOiOpKUwvvWcxhJ0%2Fimg_s.jpg"
        }
        return value
    }
    // 준비 시간
    private fun readyProgressDataWhen(a: Any): Int {
        val value = when (a) {
            1 -> 500
            2 -> 1000
            3 -> 700
            4 -> 1000
            5 -> 800
            else -> 1000
        }
        return value
    }
    // 실제 운동 시간
    private fun exerciseProgressDataWhen(a: Any): Int {
        val value = when (a) {
            1 -> 1000
            2 -> 1500
            3 -> 5000 // 50초
            4 -> 2000
            5 -> 3000
            else -> 1000
        }
        return value
    }



    override fun onItemClicked(position: Int) {
        Log.d(TAG, "ExplainExerciseActivity - onItemClicked() called / position: $position")
//        var name: String? = null
//
//        // 값이 비어있으면 ""를 넣는다.
//        // unwrapping - 언랩핑
//
//        val title: String = this.modelList[position].exerciseName ?: ""
//
////        val title: String = name ?: "호호호"
//
//        AlertDialog.Builder(this)
//            .setTitle(title)
//            .setMessage("$title 와 함께하는 빡코딩! :)")
//            .setPositiveButton("오케이") { dialog, id ->
//                Log.d(TAG, "ExplainExerciseActivity - 다이얼로그 확인 버튼 클릭했음")
//            }
//            .show()
    }


}