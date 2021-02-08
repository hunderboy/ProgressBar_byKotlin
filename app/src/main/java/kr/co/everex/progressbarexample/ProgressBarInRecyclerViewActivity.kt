package kr.co.everex.progressbarexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.adapter.ExplainExerciseListAdapter
import kr.co.everex.progressbarexample.databinding.ActivityProgressBarInRecyclerViewBinding
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import java.util.*

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
    private var currentIndex = 0 // 현재 인덱스

    // 타이머 Task
    private var readyTimerTask: Timer? = null       // 준비 타이머 Task
    private var exerciseTimerTask: Timer? = null        // 운동 타이머 Task
    private var mCountDownTimer: CountDownTimer? = null // 카운트 다운 타이머 Task
    // 남은 시간 할당하기
    private var mTimeLeftInMillis: Long? = 0

    // 이전 남은 시간
    private var previousTime: Long? = null
    // 현재 남은 시간
    private var currentTime: Long? = null
    // gap
    private var gap: Int = 0



    /** 운동 콜 함수.
      사이즈 5 일경우
      a = 0 1 2 3 4 설정됨.

     */
    private fun callExercise(a: Int){
        return when (a) {
            0 -> {
                ready(0) // 10초
                currentIndex = 0
            }
            1 -> {
                ready(1)
                currentIndex = 1
            } // 10초
            2 -> {
                ready(2) // 13초
                currentIndex = 2
            }
            3 -> {
                ready(3) // 50초
                currentIndex = 3
            }
            4 -> {
                ready(4)
                currentIndex = 4
            }
            else -> {
                ready(a)
                currentIndex = a
            }
        }
    }


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
                restart(currentIndex)
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
            layoutManager = LinearLayoutManager(
                this@ProgressBarInRecyclerViewActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            // 어답터 장착
            adapter = explainExerciseListAdapter
        }

        // 리사이클러뷰 세팅 완료 후 작업 ------------------------------------------------------------
//        ready()
        // index = 0 번째 운동 시작
        callExercise(0)
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
    private fun ready(a: Int){
        isRunning = true
        mTimeLeftInMillis = modelList[a].exerciseTimeValue // 남은시간 밀리초 초기 설정
        modelList[a].readyIsRunning = true
        val readyToProgressBar = scope.launch {
            readyTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                // 5.0 초 가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(time == modelList[a].readyProgressMaxValue){
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        modelList[a].readyProgressValue = 0
                        modelList[a].readyIsRunning = false
                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        readyTimerTask?.cancel()

//                        play(a) // 운동 progress bar 시작
//                        exerciseCountDownTimer(a) // 카운트 다운 타이머 시작
//                        exerciseCountDown2(a)
                        initexerciseCountDown2(a)
                    }
                } else {
                    runOnUiThread {
                        // 0.01초 마다 변경됨
                        modelList[a].readyProgressValue += 1
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
    private fun play(a: Int){
        countDownTimerRunning = true // 카운트 다운 on
        modelList[a].exerciseIsRunning = true
        val playToProgressBar = scope.launch {
            exerciseTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                if(time == modelList[a].exerciseProgressMaxValue){  // 10초
                    runOnUiThread {
                        mCountDownTimer?.cancel() // 카운트 다운 타이머 중지
                        // 데이터 초기화
                        time = 0
                        modelList[a].exerciseProgressValue = 0
                        modelList[a].exerciseTimeValue = modelList[a].exerciseTotalTime
                        modelList[a].exerciseIsRunning = false
                        countDownTimerRunning = false

                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged()
                        exerciseTimerTask?.cancel()

                        // 다음 운동 실행
                        callExercise(a + 1)
                    }
                } else{
                    runOnUiThread {
                        // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
                        modelList[a].exerciseProgressValue += 1
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
    private fun exerciseCountDownTimer(a: Int){
        val countDown = scope.launch {
            mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis!!, 1000) { // 1초 마다
                // 단위 10milli
                // 단위 1000milli
                // 카운트 주기 로직에 맞춰서 log 찍어 봐야
                override fun onTick(millisUntilFinished: Long) {
                    mTimeLeftInMillis = millisUntilFinished

                    Log.e("주기에 따른 값 확인 = ", mTimeLeftInMillis.toString()) // 1초마다 한번씩 찍힌다

                    modelList[a].exerciseTimeValue = mTimeLeftInMillis!! // 데이터 변화
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

    private fun restart(a: Int) {
        binding.controlButton.text = "일시정지"
        isRunning = true


        if(countDownTimerRunning){ // 카운트 타이머 동작중 여부 확인
//            exerciseCountDownTimer(a) // 카운트다운 타이머 재진행
            exerciseCountDown2(a)
        }
        if(modelList[a].readyIsRunning){    // 준비 프로그래스바 작동중
            ready(a)
        }
        if(modelList[a].exerciseIsRunning){ // 운동 프로그래스바 작동중
//            play(a)
            exerciseCountDown2(a)
        }
    }

    private fun valueConversionByZone(leftMillis: Long): Int {
        val paramValue : Int = leftMillis.toInt()
        val twoDigits = paramValue % 100
        // 할당된 값
        val assignedValue :Int?


        if (twoDigits in 81..90){ // 81~90 사이에 있다
            assignedValue = 1
        }else if (twoDigits in 71..80){
            assignedValue = 2
        }else if (twoDigits in 61..70){
            assignedValue = 3
        }else if (twoDigits in 51..60){
            assignedValue = 4
        }else if (twoDigits in 41..50){
            assignedValue = 5
        }else if (twoDigits in 31..40){
            assignedValue = 6
        }else if (twoDigits in 21..30){
            assignedValue = 7
        }else if (twoDigits in 11..20){
            assignedValue = 8
        }else if (twoDigits in 1..10){
            assignedValue = 9
        }else{ // 0 or 99~91
            assignedValue = 0
        }
        return assignedValue
    }



    /**
     * 1개로 합쳐서 플레이한다.
     * 운동 progress 컨트롤
     */
    private fun initexerciseCountDown2(a: Int){
        countDownTimerRunning = true // 카운트 다운 on
        modelList[a].exerciseIsRunning = true
        previousTime = modelList[a].exerciseTotalTime

        exerciseCountDown2(a)
    }

    private fun exerciseCountDown2(a: Int){

        val countDown = scope.launch {
            mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis!!, 10) { // 0.01초 마다
                override fun onTick(millisUntilFinished: Long) {

                    currentTime = millisUntilFinished
                    mTimeLeftInMillis = millisUntilFinished // 0.01초 마다 환산된 값
                    Log.e("주기에 따른 값 확인 = ", mTimeLeftInMillis.toString()) // 1초마다 한번씩 찍힌다
                    modelList[a].exerciseTimeValue = mTimeLeftInMillis!! // 데이터 변화 9980, 9970, 9960, 9950


                    //Todo 여기서 새로운 코드 작성 필요
                    // 이전 측정값 확인
                    valueConversionByZone(previousTime!!)
                    // 현재 측정값 확인
                    valueConversionByZone(currentTime!!)

                    // 현재 측정값 - 이전 측정값 (현재 측정값 >= 이전측정값 , 항상 양수)
                    gap = valueConversionByZone(currentTime!!) - valueConversionByZone(previousTime!!)

                    if(gap < 0){ // 음수 일경우
                        gap += 10 // 차이만큼 더한다
                        // 00 대 넘어 가는 차이 값 생성
                    }

                    time += gap // 갭 만큼 더해서 유지
                    Log.e("time = ", time.toString()) // 1초마다 한번씩 찍힌다


                    runOnUiThread {
                        previousTime = currentTime
                        Log.e("previousTime = ", previousTime.toString())
                        Log.e("currentTime = ", currentTime.toString())
                        // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
                        modelList[a].exerciseProgressValue += gap // gap 만큼 더한다
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged() // 데이터 적용
                    }



//                    if(time == modelList[a].exerciseProgressMaxValue){  // 10초
//                        runOnUiThread {
//                            // 카운트 다운 타이머 중지
//                            mCountDownTimer?.cancel()
//                            // 데이터 초기화
//                            time = 0
//                            modelList[a].exerciseProgressValue = 0
//                            modelList[a].exerciseTimeValue = modelList[a].exerciseTotalTime
//                            modelList[a].exerciseIsRunning = false
//                            countDownTimerRunning = false
//                            // 데이터 적용
//                            explainExerciseListAdapter.submitList(modelList)
//                            // 다음 운동 실행
//                            callExercise(a + 1)
//                        }
//                    } else{
//                        runOnUiThread {
//                            previousTime = currentTime
//                            // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
//                            modelList[a].exerciseProgressValue += gap // gap 만큼 더한다
//                            explainExerciseListAdapter.submitList(modelList)
//                        }
//                    }
//                    explainExerciseListAdapter.notifyDataSetChanged() // 데이터 적용
                }
                override fun onFinish() {
                    // 이 finish 가
                    // 1. 모든 프로세스가 끝났을때만 나오는지?
                    // 2. cancel 상황시에 무조건 뜨는 건지?? (일시중지 상황에서도 뜨는 건가?) = 그렇지 않은 것 같다
                    Log.e("finish = ","확인")

                    runOnUiThread {
                        // 카운트 다운 타이머 중지
                        mCountDownTimer?.cancel()
                        // 데이터 초기화
                        time = 0
                        modelList[a].exerciseProgressValue = 0
                        modelList[a].exerciseTimeValue = modelList[a].exerciseTotalTime
                        modelList[a].exerciseIsRunning = false
                        countDownTimerRunning = false
                        // 데이터 적용
                        explainExerciseListAdapter.submitList(modelList)
                        explainExerciseListAdapter.notifyDataSetChanged() // 데이터 적용
                        // 다음 운동 실행
                        callExercise(a + 1)
                    }
                }
            }.start()
        }
        countDown.isActive
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
    // 준비 readyProgressBar Max(채워져야 할 총 칸 개수) 값
    private fun readyProgressDataWhen(a: Any): Int {
        return when (a) {
            1 -> 500
            2 -> 700
            3 -> 800
            4 -> 1000
            5 -> 1200
            else -> 1000
        }
    }
    // 운동 exerciseProgressBar Max(채워져야 할 총 칸 개수) 값
    private fun exerciseProgressDataWhen(a: Any): Int {
        return when (a) {
            1 -> 1000
            2 -> 1300
            3 -> 5000 // 50초
            4 -> 1500
            5 -> 3000
            else -> 1000
        }
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