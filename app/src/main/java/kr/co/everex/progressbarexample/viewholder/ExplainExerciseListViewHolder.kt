package kr.co.everex.progressbarexample.viewholder

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.everex.progressbarexample.R
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.info.App
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import kotlinx.android.synthetic.main.item_explain_exercies.view.*
import java.time.temporal.TemporalAmount
import java.util.*


/**
 * 2021-01-25
 * 운동 커리큘럼 리스트 리사이클러뷰 뷰홀더
 */
class ExplainExerciseListViewHolder (itemView: View,
                                    recyclerviewInterface: MyRecyclerviewInterface // 인터페이스 상속
):
                                    RecyclerView.ViewHolder(itemView),
                                    View.OnClickListener
{
    val TAG: String = "로그"

    private val exerciseImageView = itemView.exercise_img
    private val exerciseNameTextView = itemView.exercise_name_txt
    private val exerciseTotalTimeTextView = itemView.exercise_total_time

    // 프로그래스 바
    private val exerciseProgressBar = itemView.progressBar_exercise_item

    // 리사이클러뷰 인터페이스 상속
    private var myRecyclerviewInterface : MyRecyclerviewInterface? = null


    // 기본 생성자
    init {
        Log.d(TAG, "ExplainExerciseListViewHolder - init() called")

        itemView.setOnClickListener(this)
        this.myRecyclerviewInterface = recyclerviewInterface
    }

    // adpater 에서 받은 Model 을 가지고, 데이터와 뷰를 묶는다.
    fun bind(exerciseModel: ExplainExerciseListModel){
        Log.d(TAG, "ExplainExerciseListViewHolder - bind() called")

        // TextView & Text data 를 묶는다.
        exerciseNameTextView.text = exerciseModel.exerciseName

        // ImageView & Image data 를 묶는다 .
        Glide
            .with(App.getApplicationContext())
            .load(exerciseModel.exerciseImage)
            //.centerCrop()
            .placeholder(R.drawable.loading)
            .into(exerciseImageView)
        /**--------------------------------------------------**/



        // 준비 프로그래스 바 진행
        if(exerciseModel.readyIsRunning){
            updateCountDownText(exerciseModel.exerciseTotalTime)
            exerciseProgressBar.max = exerciseModel.readyProgressMaxValue
            exerciseProgressBar.secondaryProgress = exerciseModel.readyProgressValue
        }
        // 운동 프로그래스 바 진행
        if(exerciseModel.exerciseIsRunning){

            updateCountDownText(exerciseModel.exerciseTotalTime)

            exerciseProgressBar.max = exerciseModel.exerciseProgressMaxValue
            exerciseProgressBar.progress = exerciseModel.exerciseProgressValue
        }


    }

    override fun onClick(p0: View?) {
        Log.d(TAG, "ExplainExerciseListViewHolder - onClick() called")

        this.myRecyclerviewInterface?.onItemClicked(adapterPosition)
    }

    // 시간 Text 업데이트
    private fun updateCountDownText(timeLeftInMillis:Long) {
        val minutes = (timeLeftInMillis / 1000).toInt() / 60
        val seconds = (timeLeftInMillis / 1000).toInt() % 60
        val timeLeftFormatted: String =
            java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        exerciseTotalTimeTextView.text = timeLeftFormatted
    }
}