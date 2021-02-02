package kr.co.everex.progressbarexample.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.everex.progressbarexample.R
import kr.co.everex.progressbarexample.`interface`.MyRecyclerviewInterface
import kr.co.everex.progressbarexample.model.ExplainExerciseListModel
import kr.co.everex.progressbarexample.viewholder.ExplainExerciseListViewHolder

class ExplainExerciseListAdapter(
    myRecyclerviewInterface: MyRecyclerviewInterface
):
    RecyclerView.Adapter<ExplainExerciseListViewHolder>()
{
    val TAG: String = "로그"

    private var modelList = ArrayList<ExplainExerciseListModel>()
    private var myRecyclerviewInterface :MyRecyclerviewInterface? = null

    // 생성자
    init {
        this.myRecyclerviewInterface = myRecyclerviewInterface
    }


    // 외부의 ArrayList 와 매칭
    fun submitList(modelList: ArrayList<ExplainExerciseListModel>){
        this.modelList = modelList
    }

    // 뷰홀더가 생성 되었을때
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExplainExerciseListViewHolder {
        // 연결할 레이아웃 설정
        return ExplainExerciseListViewHolder(
            LayoutInflater.from(parent.context).
        inflate(R.layout.item_explain_exercies, parent, false), this.myRecyclerviewInterface!!)
    }

    // 뷰와 뷰홀더가 묶였을때
    override fun onBindViewHolder(holder: ExplainExerciseListViewHolder, position: Int) {
        Log.d(TAG, "ExplainExerciseListAdapter - onBindViewHolder() called / position: $position")
        // bind 할때, Model 데이터를 넘긴다.
        holder.bind(this.modelList[position])
    }

    // 목록의 아이템수
    override fun getItemCount(): Int {
        return this.modelList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


}