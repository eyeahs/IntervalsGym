package com.lighthousepark.intervalsgym.core

internal data class AppTextTranslation(
    val korean: String,
    val english: String,
    val japanese: String,
    val chinese: String,
) {
    fun text(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SYSTEM,
            AppLanguage.KOREAN -> korean
            AppLanguage.ENGLISH -> english
            AppLanguage.JAPANESE -> japanese
            AppLanguage.CHINESE_SIMPLIFIED -> chinese
            AppLanguage.GERMAN,
            AppLanguage.FRENCH,
            AppLanguage.ITALIAN,
            AppLanguage.SPANISH,
            AppLanguage.PORTUGUESE -> localizedEuropeanAppText(korean, language) ?: english
        }
    }
}

private fun translation(
    korean: String,
    english: String,
    japanese: String,
    chinese: String,
): AppTextTranslation {
    return AppTextTranslation(korean, english, japanese, chinese)
}

private val exactAppTextTranslations = listOf(
    translation("Intervals 주간 훈련", "Intervals Weekly Training", "Intervals 週間トレーニング", "Intervals 每周训练"),
    translation("하루 훈련", "Daily training", "1日のトレーニング", "单日训练"),
    translation("주간 훈련", "Weekly training", "週間トレーニング", "每周训练"),
    translation("월간 훈련", "Monthly training", "月間トレーニング", "每月训练"),
    translation("일", "Sun", "日", "日"),
    translation("월", "Mon", "月", "一"),
    translation("화", "Tue", "火", "二"),
    translation("수", "Wed", "水", "三"),
    translation("목", "Thu", "木", "四"),
    translation("금", "Fri", "金", "五"),
    translation("토", "Sat", "土", "六"),
    translation(
        "Intervals.icu 계정으로 로그인하면 훈련 Routine과 결과를 동기화합니다.",
        "Sign in with Intervals.icu to sync workout routines and results.",
        "Intervals.icuでログインすると、トレーニングルーティンと結果を同期できます。",
        "登录 Intervals.icu 以同步训练计划和结果。"
    ),
    translation("Intervals 로그인 중", "Signing in to Intervals", "Intervalsにログイン中", "正在登录 Intervals"),
    translation("Intervals OAuth 로그인", "Sign in with Intervals OAuth", "Intervals OAuthでログイン", "使用 Intervals OAuth 登录"),
    translation("Intervals OAuth 설정 없음", "Intervals OAuth is not configured", "Intervals OAuthが設定されていません", "Intervals OAuth 未配置"),
    translation("로그인 없이 사용", "Continue without signing in", "ログインせずに使用", "不登录直接使用"),
    translation("새로고침", "Refresh", "更新", "刷新"),
    translation("설정", "Settings", "設定", "设置"),
    translation("언어", "Language", "言語", "语言"),
    translation("앱 언어", "App language", "アプリの言語", "应用语言"),
    translation("시스템 기본", "System default", "システムのデフォルト", "跟随系统"),
    translation("오늘로 이동", "Go to today", "今日へ移動", "转到今天"),
    translation("이동", "Go", "移動", "前往"),
    translation("이동 취소", "Cancel move", "移動をキャンセル", "取消移动"),
    translation("뒤로", "Back", "戻る", "返回"),
    translation("닫기", "Close", "閉じる", "关闭"),
    translation("취소", "Cancel", "キャンセル", "取消"),
    translation("확인", "OK", "確認", "确定"),
    translation("완료", "Done", "完了", "完成"),
    translation("추가", "Add", "追加", "添加"),
    translation("변경", "Change", "変更", "更改"),
    translation("수정", "Edit", "編集", "编辑"),
    translation("삭제", "Delete", "削除", "删除"),
    translation("제거", "Remove", "削除", "移除"),
    translation("복구", "Restore", "復元", "恢复"),
    translation("저장", "Save", "保存", "保存"),
    translation("저장 안 함", "Don't save", "保存しない", "不保存"),
    translation("선택", "Select", "選択", "选择"),
    translation("선택됨", "Selected", "選択済み", "已选择"),
    translation("다시 시도", "Try again", "再試行", "重试"),
    translation("다시 검색", "Search again", "再検索", "重新搜索"),
    translation("검색 중", "Searching", "検索中", "正在搜索"),
    translation("대기", "Waiting", "待機", "等待"),
    translation("시작", "Start", "開始", "开始"),
    translation("일시정지", "Pause", "一時停止", "暂停"),
    translation("리셋", "Reset", "リセット", "重置"),
    translation("결과", "Result", "結果", "结果"),
    translation("설명", "Description", "説明", "说明"),
    translation("메모", "Note", "メモ", "备注"),
    translation("그래프", "Graph", "グラフ", "图表"),
    translation("Routine 그래프", "Routine graph", "ルーティングラフ", "计划图表"),
    translation("운동 실행", "Start workout", "ワークアウトを実行", "开始训练"),
    translation("계획 추가", "Add plan", "プランを追加", "添加计划"),
    translation("Routine 관리", "Manage routines", "ルーティン管理", "管理计划"),
    translation("런닝", "Running", "ランニング", "跑步"),
    translation("웨이트", "Strength", "筋力トレーニング", "力量训练"),
    translation("운동 시작", "Start workout", "ワークアウト開始", "开始训练"),
    translation("운동 종료", "End workout", "ワークアウト終了", "结束训练"),
    translation("운동 마치기", "Finish workout", "ワークアウトを終了", "完成训练"),
    translation("운동 중지", "Stop workout", "ワークアウトを停止", "停止训练"),
    translation(
        "현재까지 수행한 러닝 기록을 로컬에 저장할까요?",
        "Save the running activity completed so far locally?",
        "ここまでのランニング記録を端末に保存しますか？",
        "是否将目前完成的跑步记录保存到本地？"
    ),
    translation("운동 완료", "Workout complete", "ワークアウト完了", "训练完成"),
    translation("운동 완료 준비", "Ready to finish", "完了の準備", "准备完成"),
    translation("운동 목록", "Exercise list", "種目一覧", "动作列表"),
    translation("운동 목록으로", "Exercise list", "種目一覧へ", "返回动作列表"),
    translation("운동 수정", "Edit workout", "ワークアウト編集", "编辑训练"),
    translation("운동 변경", "Change exercise", "種目を変更", "更换动作"),
    translation("운동 추가", "Add exercise", "種目を追加", "添加动作"),
    translation("신규 운동 추가", "Add new exercise", "新しい種目を追加", "添加新动作"),
    translation("운동 삭제", "Delete exercise", "種目を削除", "删除动作"),
    translation("운동 저장", "Save exercise", "種目を保存", "保存动作"),
    translation("운동 생성", "Create exercise", "種目を作成", "创建动作"),
    translation("운동 선택", "Select exercise", "種目を選択", "选择动作"),
    translation("운동 검색", "Search exercises", "種目を検索", "搜索动作"),
    translation("운동 이름", "Exercise name", "種目名", "动作名称"),
    translation("운동 상세", "Exercise details", "種目の詳細", "动作详情"),
    translation("웨이트 상세 기록", "Strength workout details", "筋力トレーニング詳細", "力量训练详情"),
    translation("운동 상세 변경", "Exercise detail changes", "種目詳細の変更", "动作详情更改"),
    translation("운동 종류 변경", "Exercise type changes", "種目タイプの変更", "动作类型更改"),
    translation("수행 중 운동", "Current exercise", "実行中の種目", "当前动作"),
    translation("진행 중 운동", "Exercise in progress", "実行中の種目", "进行中的动作"),
    translation("Routine 추가", "Add routine", "ルーティンを追加", "添加计划"),
    translation("Routine 수정", "Edit routine", "ルーティンを編集", "编辑计划"),
    translation("Routine 저장", "Save routine", "ルーティンを保存", "保存计划"),
    translation("Routine 삭제", "Delete routine", "ルーティンを削除", "删除计划"),
    translation("Routine 복제", "Clone routine", "ルーティンを複製", "克隆计划"),
    translation("Routine 업데이트", "Update routine", "ルーティンを更新", "更新计划"),
    translation("Routine 이름", "Routine name", "ルーティン名", "计划名称"),
    translation("새 웨이트 Routine", "New strength routine", "新しい筋力ルーティン", "新力量训练计划"),
    translation("웨이트 Routine", "Strength routine", "筋力ルーティン", "力量训练计划"),
    translation("웨이트 Routine & 기록", "Strength routine & log", "筋力ルーティン＆記録", "力量训练计划和记录"),
    translation("웨이트 Routine 관리", "Manage strength routines", "筋力ルーティン管理", "管理力量训练计划"),
    translation("웨이트 routine 선택", "Select a strength routine", "筋力ルーティンを選択", "选择力量训练计划"),
    translation("러닝 routine 선택", "Select a running routine", "ランニングルーティンを選択", "选择跑步计划"),
    translation("러닝 Routine 관리", "Manage running routines", "ランニングルーティン管理", "管理跑步计划"),
    translation("러닝 Routine 삭제", "Delete running routine", "ランニングルーティンを削除", "删除跑步计划"),
    translation("러닝 Routine 저장", "Save running routine", "ランニングルーティンを保存", "保存跑步计划"),
    translation("웨이트 Routine 로컬에 저장", "Save strength routine locally", "筋力ルーティンを端末に保存", "将力量训练计划保存到本地"),
    translation("웨이트 Routine 로컬에 저장됨", "Strength routine saved locally", "筋力ルーティンを保存しました", "力量训练计划已保存到本地"),
    translation("러닝 Routine 저장", "Save running routine", "ランニングルーティンを保存", "保存跑步计划"),
    translation("로컬 기록 삭제", "Delete local record", "ローカル記録を削除", "删除本地记录"),
    translation("로컬 러닝 기록 그래프", "Local running record graph", "ローカルランニング記録グラフ", "本地跑步记录图表"),
    translation("로컬 상세 기록 매칭", "Matched local details", "ローカル詳細記録と一致", "已匹配本地详细记录"),
    translation("Intervals.icu에서 가져오는 중", "Loading from Intervals.icu", "Intervals.icuから取得中", "正在从 Intervals.icu 获取"),
    translation("Intervals.icu 업로드", "Upload to Intervals.icu", "Intervals.icuへアップロード", "上传到 Intervals.icu"),
    translation("Intervals.icu 업데이트", "Update Intervals.icu", "Intervals.icuを更新", "更新 Intervals.icu"),
    translation("Intervals.icu에 기록 업로드 중...", "Uploading to Intervals.icu...", "Intervals.icuへアップロード中...", "正在上传到 Intervals.icu..."),
    translation("업로드 다시 시도", "Retry upload", "アップロードを再試行", "重试上传"),
    translation("업로드 준비", "Ready to upload", "アップロード準備完了", "准备上传"),
    translation("업로드 중", "Uploading", "アップロード中", "正在上传"),
    translation("업로드됨", "Uploaded", "アップロード済み", "已上传"),
    translation("미동기화", "Not synced", "未同期", "未同步"),
    translation("저장됨", "Saved", "保存済み", "已保存"),
    translation("저장 중", "Saving", "保存中", "正在保存"),
    translation("동기화 실패", "Sync failed", "同期に失敗", "同步失败"),
    translation("세트", "Set", "セット", "组"),
    translation("세트 추가", "Add set", "セットを追加", "添加一组"),
    translation("세트 관리", "Manage sets", "セット管理", "管理组"),
    translation("세트 묶기", "Group sets", "セットをまとめる", "组合组"),
    translation("세트 완료", "Set complete", "セット完了", "组完成"),
    translation("세트 방식 선택", "Select set format", "セット方式を選択", "选择组模式"),
    translation("완료 체크", "Mark complete", "完了にする", "标记完成"),
    translation("완료 취소", "Undo completion", "完了を取り消す", "取消完成"),
    translation("완료된 세트", "Completed set", "完了したセット", "已完成组"),
    translation("완료됨", "Completed", "完了", "已完成"),
    translation("미완료", "Incomplete", "未完了", "未完成"),
    translation("슈퍼세트", "Superset", "スーパーセット", "超级组"),
    translation("슈퍼 세트", "Superset", "スーパーセット", "超级组"),
    translation("페어 세트", "Paired set", "ペアセット", "配对组"),
    translation("선택 묶기", "Group selected", "選択項目をまとめる", "组合所选项"),
    translation("묶기 해제", "Ungroup", "グループ解除", "取消组合"),
    translation("순서", "Order", "順序", "顺序"),
    translation("좌우 방식", "Side mode", "左右方式", "左右方式"),
    translation("측정 방식", "Measurement", "測定方式", "测量方式"),
    translation("횟수", "Reps", "回数", "次数"),
    translation("시간", "Time", "時間", "时间"),
    translation("기구", "Equipment", "器具", "器械"),
    translation("기구 직접 입력", "Enter equipment", "器具を直接入力", "输入器械"),
    translation("세부 타입", "Variation", "詳細タイプ", "详细类型"),
    translation("타입 변경", "Change type", "タイプを変更", "更改类型"),
    translation("양쪽", "Both sides", "両側", "双侧"),
    translation("한쪽", "One side", "片側", "单侧"),
    translation("각", "Each", "各", "每侧"),
    translation("좌", "L", "左", "左"),
    translation("우", "R", "右", "右"),
    translation("결과", "Result", "結果", "结果"),
    translation("휴식", "Rest", "休憩", "休息"),
    translation("휴식 중단", "Stop rest", "休憩を終了", "结束休息"),
    translation("다음 세트 시간", "Next set time", "次のセット時間", "下一组时间"),
    translation("남은 시간", "Time remaining", "残り時間", "剩余时间"),
    translation("수행 시간", "Workout time", "実行時間", "训练时间"),
    translation("페이스", "Pace", "ペース", "配速"),
    translation("속도", "Speed", "速度", "速度"),
    translation("경사도", "Incline", "傾斜", "坡度"),
    translation("현재 Block", "Current block", "現在のブロック", "当前区块"),
    translation("Block 남은 시간", "Block time remaining", "ブロック残り時間", "区块剩余时间"),
    translation("Block 건너뛰기", "Skip block", "ブロックをスキップ", "跳过区块"),
    translation("이전\nBlock", "Previous\nblock", "前の\nブロック", "上一个\n区块"),
    translation("Warmup 중", "Warming up", "ウォームアップ中", "热身中"),
    translation("Warmup 종료", "End warmup", "ウォームアップ終了", "结束热身"),
    translation("준비가 끝나면 첫 번째 Block을 시작하세요.", "Start the first block when you're ready.", "準備ができたら最初のブロックを開始してください。", "准备好后开始第一个区块。"),
    translation("Running Workout 완료", "Running workout complete", "ランニング完了", "跑步训练完成"),
    translation("심박계", "Heart-rate monitor", "心拍計", "心率计"),
    translation("심박계 연결", "Connect heart-rate monitor", "心拍計を接続", "连接心率计"),
    translation("심박계 연결 대기", "Waiting for heart-rate monitor", "心拍計の接続待ち", "等待心率计连接"),
    translation("연결된 심박계", "Connected heart-rate monitor", "接続済みの心拍計", "已连接的心率计"),
    translation("심박계를 검색 중입니다.", "Searching for heart-rate monitors.", "心拍計を検索中です。", "正在搜索心率计。"),
    translation("검색된 심박계가 없습니다.", "No heart-rate monitors found.", "心拍計が見つかりません。", "未找到心率计。"),
    translation("연결", "Connect", "接続", "连接"),
    translation("연결 중", "Connecting", "接続中", "正在连接"),
    translation("연결 해제", "Disconnect", "接続解除", "断开连接"),
    translation("심박 그래프", "Heart-rate graph", "心拍グラフ", "心率图表"),
    translation("최근 5분", "Last 5 minutes", "直近5分", "最近5分钟"),
    translation("최근 수행 History", "Recent workout history", "最近の実行履歴", "最近训练记录"),
    translation("같은 운동, 기구, 타입 기준", "Same exercise, equipment, and variation", "同じ種目・器具・タイプ", "相同动作、器械和类型"),
    translation("장소 선택", "Select location", "場所を選択", "选择地点"),
    translation("장소 미지정", "No location", "場所未指定", "未指定地点"),
    translation("새 장소 추가", "Add new location", "新しい場所を追加", "添加新地点"),
    translation("장소 이름", "Location name", "場所名", "地点名称"),
    translation("직접 입력", "Custom", "直接入力", "自定义"),
    translation("기본", "Default", "基本", "默认"),
    translation("사용자 추가", "Custom", "ユーザー追加", "自定义"),
    translation("Workout 이름", "Workout name", "ワークアウト名", "训练名称"),
    translation("목표 무게 kg", "Target weight (kg)", "目標重量 kg", "目标重量 kg"),
    translation("휴식초", "Rest (sec)", "休憩（秒）", "休息（秒）"),
    translation("바로 운동 시작", "Start workout now", "すぐにワークアウト開始", "立即开始训练"),
    translation("Routine에 추가", "Add to routine", "ルーティンに追加", "添加到计划"),
    translation("Garmin 기록 병합", "Merge Garmin activity", "Garmin記録を統合", "合并 Garmin 记录"),
    translation("병합", "Merge", "統合", "合并"),
    translation(
        "Garmin 경로와 활동 데이터는 유지하고 IntervalsGym 블록 수행 정보를 추가합니다. 앱 심박 기록이 있으면 앱 심박을 사용합니다. 시작과 종료 시각은 앱 기록에 맞추며, 앱이 자동 업로드한 중복 기록이 있으면 병합 후 삭제합니다.",
        "Keep the Garmin route and activity data, then add IntervalsGym block data. App heart-rate data is used when available. Start and end times match the app record, and any duplicate uploaded by the app is deleted after merging.",
        "Garminのルートとアクティビティデータを保持し、IntervalsGymのブロック実行情報を追加します。アプリに心拍記録がある場合はそれを使用します。開始・終了時刻はアプリの記録に合わせ、アプリが自動アップロードした重複記録は統合後に削除します。",
        "保留 Garmin 路线和活动数据，并添加 IntervalsGym 区块数据。如有应用心率记录则优先使用。开始和结束时间以应用记录为准，应用自动上传的重复记录将在合并后删除。"
    ),
    translation("독도 400m 가상 트랙", "Dokdo 400 m virtual track", "独島400mバーチャルトラック", "独岛 400 米虚拟跑道"),
    translation("진행률", "Progress", "進捗", "进度"),
    translation("Total(예상)", "Total (estimated)", "合計（推定）", "总计（预计）"),
    translation("메뉴 열기", "Open menu", "メニューを開く", "打开菜单"),
    translation("메뉴 닫기", "Close menu", "メニューを閉じる", "关闭菜单"),
    translation("드래그해서 순서 변경", "Drag to reorder", "ドラッグして並べ替え", "拖动排序"),
    translation("길게 눌러 순서 변경", "Long press to reorder", "長押しして並べ替え", "长按排序"),
    translation("API반영중", "Syncing", "API反映中", "正在同步"),
    translation("변경사항 저장", "Save changes", "変更を保存", "保存更改"),
    translation("Routine 수정 내용을 저장할까요?", "Save the routine changes?", "ルーティンの変更を保存しますか？", "是否保存计划更改？"),
    translation("저장된 history가 없습니다.", "No saved history.", "保存された履歴はありません。", "没有已保存的记录。"),
    translation("저장된 웨이트 Routine이 없습니다.", "No saved strength routines.", "保存された筋力ルーティンはありません。", "没有已保存的力量训练计划。"),
    translation("저장할 웨이트 Routine이 없습니다.", "No strength routine to save.", "保存する筋力ルーティンがありません。", "没有可保存的力量训练计划。"),
    translation("선택된 웨이트 Routine이 없습니다.", "No strength routine selected.", "筋力ルーティンが選択されていません。", "未选择力量训练计划。"),
    translation("선택된 항목이 없습니다.", "Nothing selected.", "項目が選択されていません。", "未选择任何项目。"),
    translation("수행할 세트가 없습니다.", "No sets to perform.", "実行するセットがありません。", "没有可执行的组。"),
    translation(
        "수행할 웨이트 Routine이 없습니다. 우측 상단 관리에서 Routine을 추가하세요.",
        "No strength routines are available. Add one from Manage in the top right.",
        "実行できる筋力ルーティンがありません。右上の管理から追加してください。",
        "没有可执行的力量训练计划。请从右上角的管理中添加。"
    ),
    translation(
        "운동 중 변경된 routine 항목이 없습니다.",
        "No routine items were changed during the workout.",
        "ワークアウト中に変更されたルーティン項目はありません。",
        "训练过程中没有更改计划项目。"
    ),
    translation(
        "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다.",
        "Sign in to update Intervals.icu.",
        "Intervals.icuの更新にはログインが必要です。",
        "登录后可更新 Intervals.icu。"
    ),
    translation("운동을 추가해 Routine을 구성하세요.", "Add exercises to build the routine.", "種目を追加してルーティンを作成してください。", "添加动作来创建训练计划。"),
    translation("운동을 선택하고 Routine에 추가하세요.", "Select an exercise and add it to the routine.", "種目を選択してルーティンに追加してください。", "选择动作并添加到计划。"),
    translation("예: 무릎 각도 확인", "e.g. Check knee angle", "例：膝の角度を確認", "例如：检查膝盖角度"),
    translation("예: 회사 근처 헬스장", "e.g. Gym near work", "例：会社近くのジム", "例如：公司附近的健身房"),
    translation("예: 케이블", "e.g. Cable", "例：ケーブル", "例如：绳索"),
    translation("예: 케이블 풀오버", "e.g. Cable pullover", "例：ケーブルプルオーバー", "例如：绳索直臂下压")
).associateBy(AppTextTranslation::korean)

private val exerciseAndEquipmentTranslations = listOf(
    translation("데드리프트", "Deadlift", "デッドリフト", "硬拉"),
    translation("벤치프레스", "Bench press", "ベンチプレス", "卧推"),
    translation("체스트 프레스", "Chest press", "チェストプレス", "推胸"),
    translation("스쿼트", "Squat", "スクワット", "深蹲"),
    translation("핵스쿼트", "Hack squat", "ハックスクワット", "哈克深蹲"),
    translation("오버헤드 프레스", "Overhead press", "オーバーヘッドプレス", "过顶推举"),
    translation("오버헤드 익스텐션", "Overhead extension", "オーバーヘッドエクステンション", "过顶臂屈伸"),
    translation("로우", "Row", "ロウ", "划船"),
    translation("풀업", "Pull-up", "プルアップ", "引体向上"),
    translation("랫풀다운", "Lat pulldown", "ラットプルダウン", "高位下拉"),
    translation("런지", "Lunge", "ランジ", "弓步"),
    translation("힙 쓰러스트", "Hip thrust", "ヒップスラスト", "臀推"),
    translation("레그프레스", "Leg press", "レッグプレス", "腿举"),
    translation("레그 익스텐션", "Leg extension", "レッグエクステンション", "腿屈伸"),
    translation("레그 컬", "Leg curl", "レッグカール", "腿弯举"),
    translation("카프 레이즈", "Calf raise", "カーフレイズ", "提踵"),
    translation("플라이", "Fly", "フライ", "飞鸟"),
    translation("딥스", "Dips", "ディップス", "双杠臂屈伸"),
    translation("푸쉬업", "Push-up", "プッシュアップ", "俯卧撑"),
    translation("숄더 레이즈", "Shoulder raise", "ショルダーレイズ", "肩部平举"),
    translation("리어 델트 플라이", "Rear delt fly", "リアデルトフライ", "反向飞鸟"),
    translation("페이스 풀", "Face pull", "フェイスプル", "面拉"),
    translation("바이셉스 컬", "Biceps curl", "バイセプスカール", "二头弯举"),
    translation("트라이셉스 익스텐션", "Triceps extension", "トライセプスエクステンション", "三头臂屈伸"),
    translation("클린", "Clean", "クリーン", "翻举"),
    translation("스내치", "Snatch", "スナッチ", "抓举"),
    translation("케틀벨 스윙", "Kettlebell swing", "ケトルベルスイング", "壶铃摆动"),
    translation("파머스 캐리", "Farmer's carry", "ファーマーズキャリー", "农夫行走"),
    translation("플랭크", "Plank", "プランク", "平板支撑"),
    translation("크런치", "Crunch", "クランチ", "卷腹"),
    translation("우드찹", "Woodchop", "ウッドチョップ", "伐木式"),
    translation("바벨", "Barbell", "バーベル", "杠铃"),
    translation("덤벨", "Dumbbell", "ダンベル", "哑铃"),
    translation("케틀벨", "Kettlebell", "ケトルベル", "壶铃"),
    translation("스미스", "Smith machine", "スミスマシン", "史密斯机"),
    translation("트랩바", "Trap bar", "トラップバー", "六角杠"),
    translation("케이블", "Cable", "ケーブル", "绳索"),
    translation("머신", "Machine", "マシン", "器械"),
    translation("맨몸", "Bodyweight", "自重", "自重"),
    translation("밴드", "Band", "バンド", "弹力带"),
    translation("중량", "Weighted", "加重", "负重"),
    translation("하체", "Lower body", "下半身", "下肢"),
    translation("가슴", "Chest", "胸", "胸部"),
    translation("등", "Back", "背中", "背部"),
    translation("어깨", "Shoulders", "肩", "肩部"),
    translation("코어", "Core", "体幹", "核心"),
    translation("둔근", "Glutes", "臀筋", "臀部"),
    translation("햄스트링", "Hamstrings", "ハムストリング", "腘绳肌"),
    translation("대퇴사두", "Quadriceps", "大腿四頭筋", "股四头肌"),
    translation("종아리", "Calves", "ふくらはぎ", "小腿"),
    translation("후면어깨", "Rear delts", "リアデルト", "三角肌后束"),
    translation("이두", "Biceps", "上腕二頭筋", "肱二头肌"),
    translation("삼두", "Triceps", "上腕三頭筋", "肱三头肌"),
    translation("전신", "Full body", "全身", "全身"),
    translation("기본", "Default", "基本", "默认"),
    translation("불가리안 스플릿", "Bulgarian split", "ブルガリアンスプリット", "保加利亚分腿"),
    translation("싱글레그", "Single-leg", "シングルレッグ", "单腿"),
    translation("싱글암", "Single-arm", "シングルアーム", "单臂"),
    translation("원암", "One-arm", "ワンアーム", "单臂"),
    translation("사이드", "Side", "サイド", "侧向"),
    translation("코펜하겐", "Copenhagen", "コペンハーゲン", "哥本哈根"),
    translation("EZ바", "EZ bar", "EZバー", "EZ 杠"),
    translation("랜드마인", "Landmine", "ランドマイン", "地雷架"),
    translation("어시스트 머신", "Assisted machine", "アシストマシン", "助力器械"),
    translation("중량벨트", "Dip belt", "ディップベルト", "负重腰带"),
    translation("중량조끼", "Weight vest", "ウェイトベスト", "负重背心"),
    translation("팩 덱 머신", "Pec deck", "ペックデック", "蝴蝶机"),
    translation("버터플라이 머신", "Butterfly machine", "バタフライマシン", "蝴蝶机"),
    translation("짐볼", "Exercise ball", "バランスボール", "健身球"),
    translation("메디신볼", "Medicine ball", "メディシンボール", "药球"),
    translation("캐리 핸들", "Carry handles", "キャリーハンドル", "农夫行走把手"),
    translation("하체/후면사슬", "Lower body / posterior chain", "下半身・後面連鎖", "下肢/后侧链"),
    translation("전신/파워", "Full body / power", "全身・パワー", "全身/爆发力"),
    translation("가슴/삼두", "Chest / triceps", "胸・上腕三頭筋", "胸部/肱三头肌"),
    translation("후면사슬", "Posterior chain", "後面連鎖", "后侧链"),
    translation("백 스쿼트", "Back squat", "バックスクワット", "后蹲"),
    translation("프론트 스쿼트", "Front squat", "フロントスクワット", "前蹲"),
    translation("고블릿", "Goblet", "ゴブレット", "高脚杯"),
    translation("박스", "Box", "ボックス", "箱式"),
    translation("루마니안", "Romanian", "ルーマニアン", "罗马尼亚式"),
    translation("스모", "Sumo", "スモウ", "相扑式"),
    translation("스티프레그", "Stiff-leg", "スティッフレッグ", "直腿"),
    translation("블록 풀", "Block pull", "ブロックプル", "垫高硬拉"),
    translation("플랫", "Flat", "フラット", "平板"),
    translation("인클라인", "Incline", "インクライン", "上斜"),
    translation("디클라인", "Decline", "デクライン", "下斜"),
    translation("클로즈그립", "Close grip", "クローズグリップ", "窄握"),
    translation("와이드그립", "Wide grip", "ワイドグリップ", "宽握"),
    translation("템포", "Tempo", "テンポ", "节奏"),
    translation("시티드", "Seated", "シーテッド", "坐姿"),
    translation("플레이트 로드", "Plate-loaded", "プレートロード", "杠铃片加载"),
    translation("리버스", "Reverse", "リバース", "反向"),
    translation("스탠딩", "Standing", "スタンディング", "站姿"),
    translation("푸시 프레스", "Push press", "プッシュプレス", "借力推举"),
    translation("아놀드", "Arnold", "アーノルド", "阿诺德"),
    translation("벤트오버", "Bent-over", "ベントオーバー", "俯身"),
    translation("펜들레이", "Pendlay", "ペンドレイ", "彭德莱"),
    translation("체스트 서포티드", "Chest-supported", "チェストサポート", "胸托"),
    translation("티바", "T-bar", "Tバー", "T 杠"),
    translation("친업", "Chin-up", "チンアップ", "反握引体"),
    translation("뉴트럴그립", "Neutral grip", "ニュートラルグリップ", "对握"),
    translation("언더그립", "Underhand grip", "アンダーグリップ", "反握"),
    translation("스트레이트암", "Straight-arm", "ストレートアーム", "直臂"),
    translation("워킹", "Walking", "ウォーキング", "行走式"),
    translation("포워드", "Forward", "フォワード", "前向"),
    translation("회전", "Rotational", "ローテーション", "旋转"),
    translation("글루트 브릿지", "Glute bridge", "グルートブリッジ", "臀桥"),
    translation("밴드 어브덕션", "Band abduction", "バンドアブダクション", "弹力带外展"),
    translation("하이 풋", "High foot", "ハイフット", "高脚位"),
    translation("로우 풋", "Low foot", "ローフット", "低脚位"),
    translation("와이드", "Wide", "ワイド", "宽距"),
    translation("피크 수축", "Peak contraction", "ピーク収縮", "顶峰收缩"),
    translation("라잉", "Lying", "ライイング", "俯卧"),
    translation("레그프레스", "Leg press", "レッグプレス", "腿举"),
    translation("하이투로우", "High-to-low", "ハイ・トゥ・ロー", "高到低"),
    translation("로우투하이", "Low-to-high", "ロー・トゥ・ハイ", "低到高"),
    translation("가슴 중심", "Chest focus", "胸中心", "侧重胸部"),
    translation("삼두 중심", "Triceps focus", "上腕三頭筋中心", "侧重肱三头肌"),
    translation("벤치 딥", "Bench dip", "ベンチディップ", "凳上臂屈伸"),
    translation("링 딥", "Ring dip", "リングディップ", "吊环臂屈伸"),
    translation("다이아몬드", "Diamond", "ダイヤモンド", "钻石式"),
    translation("아처", "Archer", "アーチャー", "弓箭手式"),
    translation("프론트", "Front", "フロント", "前平举"),
    translation("리어델트", "Rear delt", "リアデルト", "三角肌后束"),
    translation("Y 레이즈", "Y raise", "Yレイズ", "Y 字平举"),
    translation("린어웨이", "Lean-away", "リーンアウェイ", "侧倾"),
    translation("리버스 펙덱", "Reverse pec deck", "リバースペックデック", "反向蝴蝶机"),
    translation("인버티드", "Inverted", "インバーテッド", "反向"),
    translation("로프", "Rope", "ロープ", "绳索"),
    translation("하이풀", "High pull", "ハイプル", "高拉"),
    translation("외회전", "External rotation", "外旋", "外旋"),
    translation("해머", "Hammer", "ハンマー", "锤式"),
    translation("프리처", "Preacher", "プリーチャー", "牧师凳"),
    translation("컨센트레이션", "Concentration", "コンセントレーション", "集中弯举"),
    translation("오버헤드", "Overhead", "オーバーヘッド", "过顶"),
    translation("스컬크러셔", "Skull crusher", "スカルクラッシャー", "仰卧臂屈伸"),
    translation("푸시다운", "Pushdown", "プッシュダウン", "下压"),
    translation("킥백", "Kickback", "キックバック", "俯身臂屈伸"),
    translation("파워 클린", "Power clean", "パワークリーン", "高翻"),
    translation("행 클린", "Hang clean", "ハングクリーン", "悬垂翻举"),
    translation("머슬 클린", "Muscle clean", "マッスルクリーン", "肌肉翻举"),
    translation("클린 풀", "Clean pull", "クリーンプル", "翻举拉"),
    translation("파워 스내치", "Power snatch", "パワースナッチ", "高抓"),
    translation("행 스내치", "Hang snatch", "ハングスナッチ", "悬垂抓举"),
    translation("머슬 스내치", "Muscle snatch", "マッスルスナッチ", "肌肉抓举"),
    translation("스내치 풀", "Snatch pull", "スナッチプル", "抓举拉"),
    translation("러시안", "Russian", "ロシアン", "俄式"),
    translation("아메리칸", "American", "アメリカン", "美式"),
    translation("핸드투핸드", "Hand-to-hand", "ハンド・トゥ・ハンド", "换手"),
    translation("양손", "Two-hand", "両手", "双手"),
    translation("슈트케이스", "Suitcase", "スーツケース", "手提箱式"),
    translation("랙 캐리", "Rack carry", "ラックキャリー", "架式行走"),
    translation("오버헤드 캐리", "Overhead carry", "オーバーヘッドキャリー", "过顶行走"),
    translation("RKC", "RKC", "RKC", "RKC"),
    translation("숄더탭", "Shoulder tap", "ショルダータップ", "触肩"),
    translation("바이시클", "Bicycle", "バイシクル", "自行车式"),
    translation("데드버그", "Dead bug", "デッドバグ", "死虫式"),
    translation("수평", "Horizontal", "水平", "水平"),
    translation("하프니링", "Half-kneeling", "ハーフニーリング", "半跪姿")
).associateBy(AppTextTranslation::korean)

private val appTextSegmentTranslations = listOf(
    translation("운동 완료 준비", "Ready to finish", "完了の準備", "准备完成"),
    translation("운동 종류 변경", "Exercise type changes", "種目タイプの変更", "动作类型更改"),
    translation("운동 상세 변경", "Exercise detail changes", "種目詳細の変更", "动作详情更改"),
    translation("실제 휴식", "Actual rest", "実際の休憩", "实际休息"),
    translation("운동 시간", "Workout time", "ワークアウト時間", "训练时间"),
    translation("남은 시간", "Time remaining", "残り時間", "剩余时间"),
    translation("예상 Load", "Estimated load", "推定Load", "预计负荷"),
    translation("볼륨", "Volume", "ボリューム", "训练量"),
    translation("세트 완료", "sets complete", "セット完了", "组完成"),
    translation("휴식", "Rest", "休憩", "休息"),
    translation("장소", "Location", "場所", "地点"),
    translation("운동", "exercise", "種目", "动作"),
    translation("종목", "exercise", "種目", "动作"),
    translation("세트", "sets", "セット", "组"),
    translation("남음", "remaining", "残り", "剩余"),
    translation("증가", "increase", "増加", "增加"),
    translation("감소", "decrease", "減少", "减少"),
    translation("회", "reps", "回", "次"),
    translation("초", " sec", "秒", "秒"),
    translation("분", " min", "分", "分钟"),
    translation("시간", " hr", "時間", "小时"),
    translation("개", "", "", "个")
)

internal fun localizeAppText(
    text: String,
    language: AppLanguage,
): String {
    val effectiveLanguage = if (language == AppLanguage.SYSTEM) {
        AppLanguage.fromLanguageTag(null)
    } else {
        language
    }
    if (effectiveLanguage == AppLanguage.KOREAN || text.isBlank()) return text

    exactAppTextTranslations[text]?.let { return it.text(effectiveLanguage) }
    exerciseAndEquipmentTranslations[text]?.let { return it.text(effectiveLanguage) }

    var localized = localizeKoreanCountText(
        text = localizeKoreanWeekdayText(
            text = localizeKoreanDateText(text, effectiveLanguage),
            language = effectiveLanguage
        ),
        language = effectiveLanguage
    )
    val reusableExactTranslations = exactAppTextTranslations.values
        .filter { it.korean.length >= 2 }
    val replacements = (
        appTextSegmentTranslations +
            reusableExactTranslations +
            exerciseAndEquipmentTranslations.values
        )
        .distinctBy(AppTextTranslation::korean)
        .sortedByDescending { it.korean.length }
    replacements.forEach { item ->
        localized = localized.replace(item.korean, item.text(effectiveLanguage))
    }
    return localized
}

private fun localizeKoreanCountText(
    text: String,
    language: AppLanguage,
): String {
    val units = listOf(
        Triple(Regex("""(\d+(?:/\d+)?)세트"""), "sets", "セット"),
        Triple(Regex("""(\d+(?:\.\d+)?)회"""), "reps", "回"),
        Triple(Regex("""(\d+(?:\.\d+)?)초"""), "sec", "秒"),
        Triple(Regex("""(\d+(?:\.\d+)?)분"""), "min", "分"),
        Triple(Regex("""(\d+(?:\.\d+)?)시간"""), "hr", "時間")
    )
    return units.fold(text) { current, (pattern, englishUnit, japaneseUnit) ->
        pattern.replace(current) { match ->
            val count = match.groupValues[1]
            when (language) {
                AppLanguage.ENGLISH -> "$count $englishUnit"
                AppLanguage.JAPANESE -> "$count$japaneseUnit"
                AppLanguage.CHINESE_SIMPLIFIED -> when (englishUnit) {
                    "sets" -> "${count}组"
                    "reps" -> "${count}次"
                    "sec" -> "${count}秒"
                    "min" -> "${count}分钟"
                    else -> "${count}小时"
                }
                AppLanguage.GERMAN -> "$count ${
                    when (englishUnit) {
                        "sets" -> "Sätze"
                        "reps" -> "Wdh."
                        "sec" -> "Sek."
                        "min" -> "Min."
                        else -> "Std."
                    }
                }"
                AppLanguage.FRENCH -> "$count ${
                    when (englishUnit) {
                        "sets" -> "séries"
                        "reps" -> "rép."
                        "sec" -> "s"
                        "min" -> "min"
                        else -> "h"
                    }
                }"
                AppLanguage.ITALIAN -> "$count ${
                    when (englishUnit) {
                        "sets" -> "serie"
                        "reps" -> "rip."
                        "sec" -> "sec"
                        "min" -> "min"
                        else -> "h"
                    }
                }"
                AppLanguage.SPANISH -> "$count ${
                    when (englishUnit) {
                        "sets" -> "series"
                        "reps" -> "rep."
                        "sec" -> "s"
                        "min" -> "min"
                        else -> "h"
                    }
                }"
                AppLanguage.PORTUGUESE -> "$count ${
                    when (englishUnit) {
                        "sets" -> "séries"
                        "reps" -> "rep."
                        "sec" -> "s"
                        "min" -> "min"
                        else -> "h"
                    }
                }"
                AppLanguage.SYSTEM,
                AppLanguage.KOREAN -> match.value
            }
        }
    }
}

private fun localizeKoreanWeekdayText(
    text: String,
    language: AppLanguage,
): String {
    val labels = mapOf(
        "일" to Triple("Sun", "日", "日"),
        "월" to Triple("Mon", "月", "一"),
        "화" to Triple("Tue", "火", "二"),
        "수" to Triple("Wed", "水", "三"),
        "목" to Triple("Thu", "木", "四"),
        "금" to Triple("Fri", "金", "五"),
        "토" to Triple("Sat", "土", "六")
    )
    return Regex("""(?<![가-힣])(일|월|화|수|목|금|토)(?![가-힣])""").replace(text) { match ->
        val translated = labels.getValue(match.value)
        when (language) {
            AppLanguage.ENGLISH -> translated.first
            AppLanguage.JAPANESE -> translated.second
            AppLanguage.CHINESE_SIMPLIFIED -> translated.third
            AppLanguage.GERMAN -> germanWeekdayLabel(match.value)
            AppLanguage.FRENCH -> frenchWeekdayLabel(match.value)
            AppLanguage.ITALIAN -> italianWeekdayLabel(match.value)
            AppLanguage.SPANISH -> spanishWeekdayLabel(match.value)
            AppLanguage.PORTUGUESE -> portugueseWeekdayLabel(match.value)
            AppLanguage.SYSTEM,
            AppLanguage.KOREAN -> match.value
        }
    }
}

private fun localizeKoreanDateText(
    text: String,
    language: AppLanguage,
): String {
    val yearMonthPattern = Regex("""(\d{4})년\s+(\d{1,2})월""")
    val monthDayPattern = Regex("""(\d{1,2})월\s+(\d{1,2})일""")
    val localizedYearMonth = yearMonthPattern.replace(text) { match ->
        val year = match.groupValues[1]
        val month = match.groupValues[2]
        when (language) {
            AppLanguage.ENGLISH -> "$month/$year"
            AppLanguage.JAPANESE -> "${year}年${month}月"
            AppLanguage.CHINESE_SIMPLIFIED -> "${year}年${month}月"
            AppLanguage.GERMAN,
            AppLanguage.FRENCH,
            AppLanguage.ITALIAN,
            AppLanguage.SPANISH,
            AppLanguage.PORTUGUESE -> "$month/$year"
            AppLanguage.SYSTEM,
            AppLanguage.KOREAN -> match.value
        }
    }
    return monthDayPattern.replace(localizedYearMonth) { match ->
        val month = match.groupValues[1]
        val day = match.groupValues[2]
        when (language) {
            AppLanguage.ENGLISH -> "$month/$day"
            AppLanguage.JAPANESE -> "${month}月${day}日"
            AppLanguage.CHINESE_SIMPLIFIED -> "${month}月${day}日"
            AppLanguage.GERMAN -> "$day.$month."
            AppLanguage.FRENCH,
            AppLanguage.ITALIAN,
            AppLanguage.SPANISH,
            AppLanguage.PORTUGUESE -> "$day/$month"
            AppLanguage.SYSTEM,
            AppLanguage.KOREAN -> match.value
        }
    }
}

private fun germanWeekdayLabel(korean: String): String {
    return mapOf(
        "일" to "So.",
        "월" to "Mo.",
        "화" to "Di.",
        "수" to "Mi.",
        "목" to "Do.",
        "금" to "Fr.",
        "토" to "Sa."
    ).getValue(korean)
}

private fun frenchWeekdayLabel(korean: String): String {
    return mapOf(
        "일" to "dim.",
        "월" to "lun.",
        "화" to "mar.",
        "수" to "mer.",
        "목" to "jeu.",
        "금" to "ven.",
        "토" to "sam."
    ).getValue(korean)
}

private fun italianWeekdayLabel(korean: String): String {
    return mapOf(
        "일" to "dom",
        "월" to "lun",
        "화" to "mar",
        "수" to "mer",
        "목" to "gio",
        "금" to "ven",
        "토" to "sab"
    ).getValue(korean)
}

private fun spanishWeekdayLabel(korean: String): String {
    return mapOf(
        "일" to "dom",
        "월" to "lun",
        "화" to "mar",
        "수" to "mié",
        "목" to "jue",
        "금" to "vie",
        "토" to "sáb"
    ).getValue(korean)
}

private fun portugueseWeekdayLabel(korean: String): String {
    return mapOf(
        "일" to "dom",
        "월" to "seg",
        "화" to "ter",
        "수" to "qua",
        "목" to "qui",
        "금" to "sex",
        "토" to "sáb"
    ).getValue(korean)
}
