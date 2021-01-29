package kr.co.everex.progressbarexample.model

/**
 * 커리큘럼 운동 목록 데이터 클래스
 * 2021-01-25
 */
data class ExplainExerciseListModel(
    // 이미지
    var exerciseImage: String? = null,
    // 텍스트
    var exerciseName: String? = null,
    var exerciseTime: String? = null,
)
