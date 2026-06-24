# IntervalsGym 데이터 삭제 안내

시행일: 2026년 6월 24일

IntervalsGym은 별도의 자체 서버를 운영하지 않습니다. 앱 데이터는 주로 사용자의 Android 기기 안에 저장되며, 사용자가 Intervals.icu 연동을 선택한 경우에만 Intervals.icu 서비스로 운동 계획 및 운동 기록이 전송될 수 있습니다.

## 기기 안에 저장된 데이터 삭제

다음 방법으로 IntervalsGym이 기기에 저장한 데이터를 삭제할 수 있습니다.

1. 앱 안에서 Intervals 로그아웃을 실행하면 저장된 Intervals.icu API Key가 삭제됩니다.
2. Android 설정 > 앱 > IntervalsGym > 저장공간에서 앱 데이터를 삭제하면 로컬 운동 계획, 로컬 운동 기록, 진행 중 운동 상태, Intervals.icu 캐시가 삭제됩니다.
3. IntervalsGym 앱을 제거하면 Android가 앱 저장소에 보관하던 로컬 데이터가 삭제됩니다.

## Intervals.icu에 업로드된 데이터 삭제

사용자가 Intervals.icu 업로드 기능을 사용한 경우, 운동 계획 또는 운동 결과가 Intervals.icu 계정에 저장될 수 있습니다.

Intervals.icu에 이미 업로드된 데이터는 Intervals.icu 웹사이트 또는 앱에서 직접 삭제할 수 있습니다. IntervalsGym은 Intervals.icu가 보관하는 계정 데이터의 삭제 권한이나 보관 기간을 직접 관리하지 않습니다.

## 삭제되는 데이터

기기 내 앱 데이터를 삭제하면 다음 데이터가 삭제됩니다.

- 저장된 Intervals.icu API Key
- 웨이트 트레이닝 plan
- 러닝 plan
- 로컬 웨이트 및 러닝 운동 기록
- 진행 중 운동 및 타이머 상태
- Intervals.icu에서 내려받은 일정 캐시

## 보관되는 데이터

IntervalsGym은 자체 서버에 사용자 데이터를 보관하지 않습니다. 따라서 앱 개발자가 별도로 보관하는 서버 데이터는 없습니다.

다만 다음 데이터는 IntervalsGym 외부에서 보관될 수 있습니다.

- 사용자가 Intervals.icu에 업로드한 운동 계획 또는 운동 결과
- Android 시스템 백업 설정에 따라 사용자의 Google 계정 백업에 포함된 앱 데이터

이 데이터의 삭제 또는 보관 기간은 각 서비스의 설정 및 정책에 따릅니다.

## 삭제 요청 또는 문의

앱의 데이터 처리 또는 삭제 방법에 대한 문의는 GitHub Issues로 접수할 수 있습니다.

https://github.com/eyeahs/IntervalsGym/issues
