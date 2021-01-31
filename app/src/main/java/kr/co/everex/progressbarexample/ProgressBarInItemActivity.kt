package kr.co.everex.progressbarexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.adapter.ExplainExerciseListAdapter
import kr.co.everex.progressbarexample.databinding.ActivityProgressBarInItemBinding
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import java.util.*
import kotlin.properties.Delegates

class ProgressBarInItemActivity : AppCompatActivity(), MyRecyclerviewInterface {
    private lateinit var binding: ActivityProgressBarInItemBinding
    val TAG: String = "로그"


    // 리사이클러뷰 데이터 리스트
    var modelList = ArrayList<ExplainExerciseListModel>()
    private lateinit var explainExerciseListAdapter: ExplainExerciseListAdapter



    // 스톱워치 변수
    private var time = 0
    private var isRunning = false
    private var timerTask: Timer? = null


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
        var milli: Int by Delegates.observable(0, { props, old, new ->
            // 데이터 변경 시에 실행되는 코드
            timeDataProgressValue += 1
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBarInItemBinding.inflate(layoutInflater)
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


        /**
         * 리사이클러뷰 세팅  ------------------------------------------------------------
         */
        for (i in 1..5){
            val imageUri = numberImageWhen(i).toString()
            Log.e(TAG, "imageUri = $imageUri")

            val explainExerciseListModel = ExplainExerciseListModel(
                exerciseName = "Exercise $i",
                exerciseImage = imageUri
            )
            this.modelList.add(explainExerciseListModel)
        }


        // 어답터 인스턴스 생성
        explainExerciseListAdapter = ExplainExerciseListAdapter(this)
        explainExerciseListAdapter.submitList(this.modelList)
        // 리사이클러뷰 설정
        binding.RecyclerViewPlayExerciseList.apply {
            // 리사이클러뷰 방향 등 설정
            layoutManager = LinearLayoutManager(this@ProgressBarInItemActivity, LinearLayoutManager.VERTICAL, false)
            // 어답터 장착
            adapter = explainExerciseListAdapter
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
//                 binding.progressBarHorizonLine.max = 1000 // 1000 칸으로
                timeData.timeDataProgressValue = 0
                progressBooleanType = false
            }
            /**
             * sec = 15초 가 되는 순간
             * Progress = 0 초기화
             * timerTask 중지
             */
            else if (timeData.sec == 10){
                timeData.timeDataProgressValue = 0
                progressBooleanType = true
                timerTask?.cancel() // 테스크 종료
            }

            // 지켜보고 있다가 데이터가 변하면 바로 적용
            runOnUiThread {
                binding.secText.text = "${timeData.sec}"
                binding.milliText.text = "${timeData.milli}"

                // Todo true
                if(timeData.sec <= 4){ // progressbar secondaryProgress 진행
                    binding.progressBarHorizonLine.secondaryProgress =
                        timeData.timeDataProgressValue
                    // Todo false
                }else { // progressbar Main Progress 진행
                    binding.progressBarHorizonLine.progress =
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



    /**
     * 리사이클러뷰 리스트 기능
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

    override fun onItemClicked(position: Int) {
        Log.d(TAG, "ExplainExerciseActivity - onItemClicked() called / position: $position")
//
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