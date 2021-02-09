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

    // 기타
    private var exerciseTimerTask: Timer? = null        // 운동 타이머 Task
    private var countDownTimerRunning = false   // 카운트 다운 작동 여부
    private var mTimeLeftInMillis: Long? = 0 // 운동 남아있는 시간


    // 전역 변수
    private var time = 0    // 시간 변수. 0.01초 단위 = kotlin.concurrent.timer(period = 10)
    private var isRunning = false // 동작여부. 일시정지,재시작에 사용됨
    private var currentIndex = 0 // 현재 인덱스

    // 타이머 Task
    private var readyTimerTask: Timer? = null       // 준비 타이머 Task
    private var mCountDownTimer: CountDownTimer? = null // 카운트 다운 타이머 Task



    // 이전 남은 시간
    private var previousTime: Long? = null
    // 현재 남은 시간
    private var currentLeftTime: Long? = null
    // gap
    private var gap: Int = 0



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


        /**  리사이클러뷰 세팅  ----------------------------------------------------------- */
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


        /**  리사이클러뷰 세팅 완료 후 작업 ----------------------------------------------------------- */
        callExercise(0) // index = 0 번째 운동 시작

    }// onCreate 끝


    /**
     * 일시정지
     */
    private fun pause() {
        binding.controlButton.text = "시작"
        isRunning = false

        mCountDownTimer?.cancel()   // 카운트다운 타이머 중지
        readyTimerTask?.cancel()    // 준비 프로그래스바 중지
        exerciseTimerTask?.cancel() // 운동 프로그래스바 중지
    }
    /**
     * 재 시작
     */
    private fun restart(a: Int) {
        binding.controlButton.text = "일시정지"
        isRunning = true

        if(modelList[a].readyIsRunning){    // 준비 프로그래스바 작동중
            readyProgress(a)
        }
        else if(modelList[a].exerciseIsRunning){ // 운동 프로그래스바 작동중
            exerciseProgress(a)
        }
    }



    /**
     * ready progress 초기화 함수
     */
    private fun initReadyProgress(a: Int){
        isRunning = true
        modelList[a].readyIsRunning = true

        readyProgress(a) // 초기화 작업 진행 후, getReady 실행
    }
    /**
     *  각 Item의 Progress Bar 의 secondary progress 을 진행.( = 준비중 의미)
     */
    private fun readyProgress(a: Int){
        val readyToExercise = scope.launch {
            readyTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                // 5.0 초 가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(time == modelList[a].readyProgressMaxValue){
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        modelList[a].readyProgressValue = 0
                        modelList[a].readyIsRunning = false
                        changeItemData(modelList) // 데이터 변경 적용
                        readyTimerTask?.cancel() // readyProgress 종료
                        initExerciseProgress(a) // 운동 프로그래스 초기화 함수로 이동
                    }
                } else {
                    runOnUiThread {
                        modelList[a].readyProgressValue += 1
                        changeItemData(modelList) // 데이터 변경 적용
                    }
                }
            }
        }
        readyToExercise.isActive
    }


    /**
     * exercise progress 초기화 함수
     */
    private fun initExerciseProgress(a: Int){
        modelList[a].exerciseIsRunning = true

        previousTime = modelList[a].exerciseTotalTime
        currentLeftTime = modelList[a].exerciseTimeValue // 남은시간 밀리초 초기 설정

        exerciseProgress(a) // 초기화 작업 진행 후, exerciseProgress 실행
    }

    private fun exerciseProgress(a: Int){
        val playExercise = scope.launch {
            mCountDownTimer = object : CountDownTimer(currentLeftTime!!, 10) { // 0.01초 마다
                override fun onTick(millisUntilFinished: Long) {

                    currentLeftTime = millisUntilFinished // 0.01초 마다 환산된 값
                    Log.e("주기에 따른 값 확인 = ", currentLeftTime.toString()) // 1초마다 한번씩 찍힌다
                    modelList[a].exerciseTimeValue = currentLeftTime!! // 데이터 변화 9980, 9970, 9960, 9950


                    //Todo 여기서 새로운 코드 작성 필요
                    // 이전 측정값 확인
                    valueConversionByZone(previousTime!!)
                    // 현재 측정값 확인
                    valueConversionByZone(currentLeftTime!!)

                    // 현재 측정값 - 이전 측정값 (현재 측정값 >= 이전측정값 , 항상 양수)
                    gap = valueConversionByZone(currentLeftTime!!) - valueConversionByZone(previousTime!!)

                    if(gap < 0){ // 음수 일경우
                        gap += 10 // 차이만큼 더한다
                        // 00 대 넘어 가는 차이 값 생성
                    }

                    time += gap // 갭 만큼 더해서 유지
                    Log.e("time = ", time.toString()) // 1초마다 한번씩 찍힌다


                    runOnUiThread {
                        previousTime = currentLeftTime

                        // 0.01초 마다 변경됨 -- 변경 설정에서 Max 값 설정해야함
                        modelList[a].exerciseProgressValue += gap // gap 만큼 더한다
                        // 데이터 변경 적용
                        changeItemData(modelList)
                    }
                }// onTick 끝


                // 타이머 종료시에만 작동함
                override fun onFinish() {
                    Log.e("finish = ","확인")

                    runOnUiThread {
                        // 카운트 다운 타이머 중지
                        mCountDownTimer?.cancel()
                        // 데이터 초기화
                        time = 0
                        modelList[a].exerciseProgressValue = 0
                        modelList[a].exerciseTimeValue = modelList[a].exerciseTotalTime
                        modelList[a].exerciseIsRunning = false

                        // 데이터 변경 적용
                        changeItemData(modelList)

                        // 다음 운동 실행
                        callExercise(a + 1)
                    }
                }
            }.start()
        }
        playExercise.isActive
    }


    private fun changeItemData(arraylist : ArrayList<ExplainExerciseListModel>){
        explainExerciseListAdapter.submitList(arraylist)
        explainExerciseListAdapter.notifyDataSetChanged() // 데이터 변경 적용
    }

    /** 운동 콜 함수. */
    private fun callExercise(a: Int){
        return when (a) {
            0 -> {
                initReadyProgress(0) // 10초
                currentIndex = 0
            }
            1 -> {
                initReadyProgress(1)
                currentIndex = 1
            } // 10초
            2 -> {
                initReadyProgress(2) // 13초
                currentIndex = 2
            }
            3 -> {
                initReadyProgress(3) // 50초
                currentIndex = 3
            }
            4 -> {
                initReadyProgress(4)
                currentIndex = 4
            }
            else -> {
                initReadyProgress(a)
                currentIndex = a
            }
        }
    }
    /**
     * 파라미터 = 남은 Millisecond(:Long)
     * 파라미터를 Int 화 한후, 마지막 2자리 를 추출 해서
     * 각 구간에 어디 부분에 포함되는 지에 따라서
     * assignedValue 을 return 한다
     */
    private fun valueConversionByZone(leftMillis: Long): Int {
        val paramValue : Int = leftMillis.toInt()
        val twoDigits = paramValue % 100
        val assignedValue :Int? // 할당된 값


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
    // 실제 운동 시간 milliseconds
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

    // 클릭 리스너
    override fun onItemClicked(position: Int) {
        Log.d(TAG, "ExplainExerciseActivity - onItemClicked() called / position: $position")
    }




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

//                        initexerciseCountDown2(a)
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



}


///**
// * 일시정지
// */
//private fun pause() {
//    binding.controlButton.text = "시작"
//    isRunning = false
//
//    mCountDownTimer?.cancel()   // 카운트다운 타이머 중지
//    readyTimerTask?.cancel()    // 준비 프로그래스바 중지
//    exerciseTimerTask?.cancel() // 운동 프로그래스바 중지
//}
///**
// * 재 시작
// */
//private fun restart(a: Int) {
//    binding.controlButton.text = "일시정지"
//    isRunning = true
//
//    if(countDownTimerRunning){ // 카운트 타이머 동작중 여부 확인
////            exerciseCountDownTimer(a) // 카운트다운 타이머 재진행
//        exerciseCountDown2(a)
//    }
//    if(modelList[a].readyIsRunning){    // 준비 프로그래스바 작동중
//        ready(a)
//    }
//    if(modelList[a].exerciseIsRunning){ // 운동 프로그래스바 작동중
////            play(a)
//        exerciseCountDown2(a)
//    }
//}