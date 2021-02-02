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
    var exerciseTime: String? = null, // 해당 운동 시간

    // 프로그래스바
    var readyIsRunning: Boolean = false,
    var exerciseIsRunning: Boolean = false,

    // 프로그래스 바 Max 값
    var readyProgressMaxValue: Int = 0,     // 1000 = 10초
    var exerciseProgressMaxValue: Int = 0,  // 4000 = 40초

    // 프로그래스바 진행 값
    var readyProgressValue: Int = 0,
    var exerciseProgressValue: Int = 0,

)
