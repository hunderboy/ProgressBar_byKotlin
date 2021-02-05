package kr.co.everex.progressbarexample.model

/**
 * 커리큘럼 운동 목록 데이터 클래스
 * 2021-01-25
 */
data class ExplainExerciseListModel(
    // 운동 이미지
    var exerciseImage: String? = null,
    // 운동 이름
    var exerciseName: String? = null,
    var exerciseTotalTime: Long = 0,    // 해당 운동 시간 ex) 600000 = 600초 = 10분
    var exerciseTimeValue: Long = 0,    // 카운트 다운 되면서 값 변화 됨

    // 준비 or 운동 프로그래스 진행 여부
    var readyIsRunning: Boolean = false,
    var exerciseIsRunning: Boolean = false,

    // progressBar Max(채워져야 할 총 칸 개수) 값 = (고정값)
    var readyProgressMaxValue: Int = 0,     // ex)1000 = 10초
    var exerciseProgressMaxValue: Int = 0,  // ex)4000 = 40초

    // progressBar 진행 값 = (0 ~ progressBar Max 값까지 증가한다. = 변동값)
    var readyProgressValue: Int = 0,    // 준비 값
    var exerciseProgressValue: Int = 0, // 진행 값
)
