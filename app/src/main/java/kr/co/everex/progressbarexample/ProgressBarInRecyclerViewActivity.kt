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

class ProgressBarInRecyclerViewActivity : AppCompatActivity(), MyRecyclerviewInterface {
    private lateinit var binding: ActivityProgressBarInRecyclerViewBinding
    private val TAG: String = "로그"


    // 리사이클러뷰 데이터 리스트
    var modelList = ArrayList<ExplainExerciseListModel>()
    private lateinit var explainExerciseListAdapter: ExplainExerciseListAdapter

    // 코루틴 scope
    private val scope = CoroutineScope(Dispatchers.Main)

    // 전역 변수
    private var time = 0    // 시간 변수. 0.01초 단위 = kotlin.concurrent.timer(period = 10)
    private var isRunning = false // 운동 진행중 여부
    private var countDownTimerRunning = false   // 카운트 다운 작동 여부

    // 타이머 Task
    private var readyTimerTask: Timer? = null       // 준비 타이머 Task
    private var exerciseTimerTask: Timer? = null        // 운동 타이머 Task
    private var mCountDownTimer: CountDownTimer? = null // 카운트 다운 타이머 Task
    // 남은 시간 할당하기
    private var mTimeLeftInMillis: Long? = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBarInRecyclerViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        // 시작 or 일시 정지
        binding.controlButton.setOnClickListener{
            if (isRunning) { // 카운트 중
                pause()
            } else {    // 카운트 중 아님
                restart()
            }
        }


        /**
         * 리사이클러뷰 세팅  ------------------------------------------------------------
         */
        for (i in 1..5){
            val imageUri = numberImageWhen(i)
            val readyProgressMaxValue = readyProgressDataWhen(i)
            val exerciseProgressMaxValue = exerciseProgressDataWhen(i)
            val exerciseTotalTime = totalTimeLongWhen(i)

            val explainExerciseListModel = ExplainExerciseListModel(
                exerciseName = "Exercise $i",
                exerciseImage = imageUri,
                readyProgressMaxValue = readyProgressMaxValue,
                exerciseProgressMaxValue = exerciseProgressMaxValue,
                exerciseTotalTime = exerciseTotalTime,
                exerciseTimeValue = exerciseTotalTime,
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
//        ready()
//        for (a in 0..4){
//            ready(a)
//        }


    }// onCreate 끝





    /**
     * ready progress 코루틴 비동기 처리
     * 각 아이템의 프로그래스 바를 실행 시킨다.
     * 맨 첫번째 아이템의 프로그래스바 실행 시킨후 실행이 끝나면
     * 그다음 index 의 아이템의 프로그래스 바를 실행시킨다.
     *
     */
    fun ready(){
        isRunning = true
        mTimeLeftInMillis = modelList[0].exerciseTimeValue // 남은시간 밀리초 초기 설정
        modelList[0].readyIsRunning = true
        val readyToProgressBar = scope.launch {
            readyTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                // 5.0 초 가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(time == modelList[0].readyProgressMaxValue){
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        modelList[0].readyProgressValue = 0
                        modelList[0].readyIsRunning = false
                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        readyTimerTask?.cancel()

                        play() // 운동 progress bar 시작
                        exerciseCountDownTimer() // 카운트 다운 타이머 시작
                    }
                } else {
                    runOnUiThread {
                        // 0.01초 마다 변경됨
                        modelList[0].readyProgressValue += 1
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        readyToProgressBar.isActive
    }
    /**
     * main progress 코루틴
     */
    fun play(){
        countDownTimerRunning = true // 카운트 다운 on
        modelList[0].exerciseIsRunning = true
        val playToProgressBar = scope.launch {
            exerciseTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                if(time == modelList[0].exerciseProgressMaxValue){  // 10초
                    runOnUiThread {
                        mCountDownTimer?.cancel() // 카운트 다운 타이머 중지
                        // 데이터 초기화
                        time = 0
                        modelList[0].exerciseProgressValue = 0
                        modelList[0].exerciseTimeValue = modelList[0].exerciseTotalTime
                        modelList[0].exerciseIsRunning = false
                        countDownTimerRunning = false

                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        exerciseTimerTask?.cancel()
                    }
                } else{
                    runOnUiThread {
                        // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
                        modelList[0].exerciseProgressValue += 1
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        playToProgressBar.isActive
    }

    /**
     * 카운트 다운
     */
    private fun exerciseCountDownTimer(){
        val countDown = scope.launch {
            mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis!!, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    mTimeLeftInMillis = millisUntilFinished

                    modelList[0].exerciseTimeValue = mTimeLeftInMillis!! // 데이터 변화
                    explainExerciseListAdapter.submitList(modelList)
                    explainExerciseListAdapter.notifyDataSetChanged()
                }
                override fun onFinish() {}
            }.start()
        }
        countDown.isActive
    }


    // 일시정지 함수
    private fun pause() {
        binding.controlButton.text = "시작"
        isRunning = false

        mCountDownTimer?.cancel()   // 카운트다운 타이머 중지
        readyTimerTask?.cancel()    // 준비 프로그래스바 중지
        exerciseTimerTask?.cancel() // 운동 프로그래스바 중지
    }

    private fun restart() {
        binding.controlButton.text = "일시정지"
        isRunning = true


        if(countDownTimerRunning){ // 카운트 타이머 동작중 여부 확인
            exerciseCountDownTimer() // 카운트다운 타이머 재진행
        }
        if(modelList[0].readyIsRunning){    // 준비 프로그래스바 작동중
            ready()
        }
        if(modelList[0].exerciseIsRunning){ // 운동 프로그래스바 작동중
            play()
        }
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
    // 준비 progressBar Max(채워져야 할 총 칸 개수) 값
    private fun readyProgressDataWhen(a: Any): Int {
        val value = when (a) {
            1 -> 500
            2 -> 700
            3 -> 800
            4 -> 1000
            5 -> 1200
            else -> 1000
        }
        return value
    }
    // 운동 progressBar Max(채워져야 할 총 칸 개수) 값
    private fun exerciseProgressDataWhen(a: Any): Int {
        val value = when (a) {
            1 -> 1000
            2 -> 1300
            3 -> 5000 // 50초
            4 -> 1500
            5 -> 3000
            else -> 1000
        }
        return value
    }
    // 실제 운동 시간
    private fun totalTimeLongWhen(a: Any): Long {
        val value = when (a) {
            1 -> 10000 // 10초
            2 -> 13000 // 13초
            3 -> 50000 // 50초
            4 -> 15000
            5 -> 30000
            else -> 1000
        }
        return value.toLong()
    }


    override fun onItemClicked(position: Int) {
        Log.d(TAG, "ExplainExerciseActivity - onItemClicked() called / position: $position")
    }


}