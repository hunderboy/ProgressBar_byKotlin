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

    // 전역 변수 설정
    private var currentIndex = 0    // 준비 or 운동 중인 item index
    private var time = 0            // 시간 변수.
    private var isRunning = false   // 현재 운동중 여부. 일시정지,재시작에 사용됨
    // 타이머 Task
    private var readyTimerTask: Timer? = null               // 준비 타이머 Task
    private var countDownTimerTask: CountDownTimer? = null  // 운동 카운트 다운 타이머 Task
    // Time 변수
    private var previousTime: Long? = null      // 이전 남은 시간
    private var currentLeftTime: Long? = null   // 현재 남은 시간
    // gap (== 현재시간 할당 구간 값 - 이전시간 할당 구간 값)
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

        // 어댑터 인스턴스 생성
        explainExerciseListAdapter = ExplainExerciseListAdapter(this)
        explainExerciseListAdapter.submitList(this.modelList)
        // 리사이클러뷰 설정
        binding.RecyclerViewPlayExerciseList.apply {
            layoutManager = LinearLayoutManager(
                this@ProgressBarInRecyclerViewActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = explainExerciseListAdapter
        }


        /**  리사이클러뷰 세팅 완료 후 작업 ----------------------------------------------------------- */
//        callExercise(currentIndex) // index = 0 번째 운동 부터 시작
        callExercise(0) // index = 0 번째 운동 부터 시작

    }// onCreate 끝


    /*** 일시정지*/
    private fun pause() {
        binding.controlButton.text = "시작"
        isRunning = false

        readyTimerTask?.cancel()    // 준비 프로그래스바 중지
        countDownTimerTask?.cancel()   // 운동 카운트다운 타이머 중지
    }
    /*** 재 시작*/
    private fun restart(index: Int) {
        binding.controlButton.text = "일시정지"
        isRunning = true

        if(modelList[index].readyIsRunning){    // 준비 프로그래스바 작동 중인 경우
            readyProgress(index)
        }
        else if(modelList[index].exerciseIsRunning){    // 운동 프로그래스바 작동 중인 경우
            exerciseProgress(index)
        }
    }



    /**
     * ready progress 초기화 함수
     */
    private fun initReadyProgress(index: Int){
        // 포커스 슬라이드 설정
        val scrollValue = 80 * index
        Log.e("scrollValue", scrollValue.toString())
        binding.RecyclerViewPlayExerciseList.smoothScrollBy(0,scrollValue+50)
//        binding.RecyclerViewPlayExerciseList.smoothScrollToPosition(index)


        // 이전의 인덱스 들의 체크이미지 visible
        for (i in 0 until index){ // ex) index == 4 경우 0,1,2,3 차례로 호출
            modelList[i].isCompleted = true // 운동 완료 설정
        }
        // 이후의 인덱스 들의 체크이미지 invisible
        for (i in index until modelList.size){ // ex) index == 2 경우 2,3,4 차례로 호출
            modelList[i].isCompleted = false
        }
        changeDataSet(modelList) // 데이터 변경 적용


        isRunning = true
        modelList[index].readyIsRunning = true

        readyProgress(index) // 초기화 작업 진행 후, getReady 실행
    }
    /**
     *  각 Item의 Progress Bar 의 secondary progress 을 진행.( = '준비중' 의미)
     */
    private fun readyProgress(index: Int){
        val readyToExercise = scope.launch {
            readyTimerTask = kotlin.concurrent.timer(period = 10) {
                time++ // 계속 변경됨

                // 5.0 초 가 되는 순간, timerTask 중단 하고 Exercies progress 재생
                if(time == modelList[index].readyProgressMaxValue){
                    readyTimerTask?.cancel() // readyProgress 종료
                    runOnUiThread {
                        // 데이터 초기화
                        time = 0
                        modelList[index].readyProgressValue = 0
                        modelList[index].readyIsRunning = false
                        // Todo 해당 포지션만 변경
                        changeItemData(modelList) // 데이터 변경 적용
                        // 운동 프로그래스 초기화 함수로 이동
                        initExerciseProgress(index)
                    }
                } else {
                    runOnUiThread {
                        modelList[index].readyProgressValue += 1
                        // Todo 해당 포지션만 변경
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
    private fun initExerciseProgress(index: Int){
        modelList[index].exerciseIsRunning = true
        previousTime = modelList[index].exerciseTotalTime
        currentLeftTime = modelList[index].exerciseTimeValue

        exerciseProgress(index) // 초기화 작업 진행 후, exerciseProgress 실행
    }
    /**
     *  각 Item의 Progress Bar 의 progress 을 진행.( = '운동중' 의미)
     */
    private fun exerciseProgress(index: Int){
        val playExercise = scope.launch {
            countDownTimerTask = object : CountDownTimer(currentLeftTime!!, 10) { // 0.01초 마다
                override fun onTick(millisUntilFinished: Long) {
                    currentLeftTime = millisUntilFinished // 0.01초 마다 환산된 값
                    modelList[index].exerciseTimeValue = currentLeftTime!! // 카운트 다운을 위해 exerciseTimeValue 값 을 현재남은시간으로 설정

                    valueConversionByZone(previousTime!!)   // 이전시간의 할당 값
                    valueConversionByZone(currentLeftTime!!)// 현재시간의 할당 값

                    // 현재 측정값 - 이전 측정값 (현재 측정값 >= 이전측정값 , 항상 양수)
                    gap = valueConversionByZone(currentLeftTime!!) - valueConversionByZone(previousTime!!)
                    if(gap < 0){ // 음수 일경우 (0-8, 1-9 형태)
                        gap += 10 // 차이만큼 더한다 (gap 생성)
                    }
                    // 다음 값 비교를위해 이전시간값에 현재남은시간값을 할당
                    previousTime = currentLeftTime

                    runOnUiThread {
                        modelList[index].exerciseProgressValue += gap
                        // Todo 해당 포지션만 변경
                        changeItemData(modelList) // 데이터 변경 적용
                    }
                }// onTick 끝

                // 타이머 종료시에만 작동함
                override fun onFinish() {
                    Log.e("finish = ","확인")
                    // 카운트 다운 타이머 중지
                    countDownTimerTask?.cancel()
                    runOnUiThread {
                        // 데이터 초기화
                        modelList[index].exerciseProgressValue = 0
                        modelList[index].exerciseTimeValue = modelList[index].exerciseTotalTime
                        modelList[index].exerciseIsRunning = false
                        modelList[index].isCompleted = true // 운동 완료 설정
                        // 데이터 변경 적용
                        // Todo 해당 포지션만 변경
                        changeItemData(modelList)
                        // 다음 운동 실행
                        callExercise(index + 1)
                    }
                }
            }.start()
        }
        playExercise.isActive
    }

    // 현재 포지션만 변경
    private fun changeItemData(arraylist : ArrayList<ExplainExerciseListModel>){
        explainExerciseListAdapter.submitList(arraylist)
        explainExerciseListAdapter.notifyItemChanged(currentIndex) // 데이터 변경 적용
    }
    private fun changeDataSet(arraylist : ArrayList<ExplainExerciseListModel>){
        explainExerciseListAdapter.submitList(arraylist)
        explainExerciseListAdapter.notifyDataSetChanged() // 데이터 변경 적용
    }

    /** 운동 콜 함수. */
    private fun callExercise(index :Int){
        return when (index) {
            0 -> {
                currentIndex = 0
                initReadyProgress(0) // 10초
            }
            1 -> {
                currentIndex = 1
                initReadyProgress(1)
            }
            2 -> {
                currentIndex = 2
                initReadyProgress(2) // 13초
            }
            3 -> {
                currentIndex = 3
                initReadyProgress(3) // 50초
            }
            4 -> {
                currentIndex = 4
                initReadyProgress(4)
            }
            else -> {
                currentIndex = index
                initReadyProgress(index)
            }
        }
    }
    /**
     * 파라미터 = 남은 Millisecond(:Long)를 Int 자료형으로 변경하고,
     * 숫자 마지막 2자리 추출 후, 해당 2자기 수가 어느 구간에 포함되어 있는지에 따라서
     * assignedValue 을 return 한다
     */
    private fun valueConversionByZone(leftMillis: Long): Int {
        val paramValue : Int = leftMillis.toInt()
        val twoDigits = paramValue % 100
        val assignedValue :Int? // 할당된 값


        when (twoDigits) {
            in 81..90 -> { // 81~90 사이에 있다
                assignedValue = 1
            }
            in 71..80 -> {
                assignedValue = 2
            }
            in 61..70 -> {
                assignedValue = 3
            }
            in 51..60 -> {
                assignedValue = 4
            }
            in 41..50 -> {
                assignedValue = 5
            }
            in 31..40 -> {
                assignedValue = 6
            }
            in 21..30 -> {
                assignedValue = 7
            }
            in 11..20 -> {
                assignedValue = 8
            }
            in 1..10 -> {
                assignedValue = 9
            }
            else -> { // 0 or 99~91
                assignedValue = 0
            }
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
        Log.e(TAG, "ExplainExerciseActivity - onItemClicked() called / position: $position")

        // 초기화
        if(modelList[currentIndex].readyIsRunning){         // 준비 프로그래스바 작동 중인 경우
            readyTimerTask?.cancel() // readyProgress 종료
            runOnUiThread {
                // 데이터 초기화
                time = 0
                modelList[currentIndex].readyProgressValue = 0
                modelList[currentIndex].readyIsRunning = false

            }
        }
        else if(modelList[currentIndex].exerciseIsRunning){ // 운동 프로그래스바 작동 중인 경우
            countDownTimerTask?.cancel()   // 운동 카운트다운 타이머 중지
            runOnUiThread {
                // 데이터 초기화
                modelList[currentIndex].exerciseProgressValue = 0
                modelList[currentIndex].exerciseTimeValue = modelList[currentIndex].exerciseTotalTime
                modelList[currentIndex].exerciseIsRunning = false
                // 데이터 변경 적용
            }
        }
        // Todo 해당 포지션만 변경
        changeItemData(modelList) // 데이터 변경 적용

        // 선택된 것 실행
        callExercise(position)
    }


}

