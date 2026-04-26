# Claude Code 프롬프트 v3: 앱 + 콘텐츠 빌드 도구

> **사용 범위**: 자녀·가족 내부용
> **콘텐츠 입력**: 흑백 외곽선 도안 PDF/PNG
> **사용자 배경**: Java/Spring 백엔드 개발자, Android 처음

이 문서는 **두 개의 마스터 프롬프트**와 각각의 **마일스톤 프롬프트**로 구성됩니다:

- 🅰️ 안드로이드 앱 (마일스톤 M1~M8)
- 🅱️ 콘텐츠 빌드 도구 (마일스톤 B1~B3)

권장 작업 순서: **M1~M5 → B1~B3 → C1(콘텐츠 등록) → M6~M8 → C2(추가 콘텐츠)**

---

## 🅰️ 안드로이드 앱: 마스터 프롬프트

세션 시작 시 한 번 붙여넣고 "준비 완료" 응답을 받은 뒤 마일스톤 프롬프트를 진행하세요.

```
너는 시니어 안드로이드 개발자야. 한국 나이 5살 자녀를 위한 색칠 놀이 앱을 함께 만들 거야.
나는 Java/Spring/Tomcat 기반 백엔드 개발자고, 안드로이드 앱은 처음 만든다. Kotlin도 처음이야.
앞으로 마일스톤 단위로 작업을 요청할 텐데, 시작 전에 반드시 이 프로젝트의 전체 컨텍스트를 머릿속에 정확히 세워.

## 사용자 배경
- Java + Spring + Tomcat에 익숙한 백엔드 개발자
- Kotlin과 Android는 처음. 처음 보는 개념은 Spring/Java 비유로 짧게 설명해줘
- 예: "Hilt는 Spring DI랑 같은 역할이야. @Inject가 @Autowired랑 비슷"
- 비유는 헷갈릴 만한 부분에서 한 줄씩만. 길게 설명하지 마.

## 사용 범위
- 자녀·가족 내부용. 외부 배포(플레이스토어) 없음
- 디버그 빌드를 자녀의 안드로이드 폰/태블릿에 직접 설치
- 서명 키, ProGuard, 플레이스토어 메타데이터 등 신경 쓸 필요 없음

## 프로젝트 개요
- 앱: 5살 자녀용 색칠 놀이 (콘텐츠는 흑백 티니핑 도안)
- 플랫폼: Android, 최소 API 26, 타깃 API 34
- 스택: Kotlin + Jetpack Compose + Hilt + Room + Coroutines/Flow + kotlinx.serialization + Coil(이미지)
- 아키텍처: MVVM + Repository
- 색칠 방식: ★ 손가락으로 자유롭게 그리기 (자유 드로잉)

## 핵심 기능
1. 카테고리 선택 → 캐릭터 선택 → 자유 드로잉
2. 외곽선 PNG(흰 영역 투명, 검은 선만 보임) 위에 사용자가 손가락으로 자유롭게 그림
3. 도구: 붓 / 지우개
4. 색상: 12색 기본 팔레트 + 추가 24색 시트
5. 굵기: 8dp / 16dp / 28dp 3단계 (슬라이더 아님)
6. Undo (한 획 단위) + 전체 초기화 (확인 다이얼로그)
7. 매 획마다 자동 저장, 앱 재시작 후에도 상태 복원

## 5살 UX 원칙
- 모든 터치 타깃 ≥ 56dp
- 글자보다 색·아이콘·이미지로 정보 전달
- 화면당 다음 행동이 1~2가지로 명확
- 위험 행동(전체 초기화)은 반드시 확인 다이얼로그
- 즉각적 시각·청각 피드백
- 외곽선 밖으로 그림이 빠져도 OK (재미가 우선)

## 폴더 구조 (반드시 이대로)
프로젝트 루트는 coloring-project/ 라고 가정.
coloring-project/
 ├─ app/                              ← 안드로이드 앱 (이 마스터 프롬프트의 작업 영역)
 │   ├─ src/main/java/com/example/coloring/
 │   │   ├─ data/
 │   │   │   ├─ local/      (Room: Entity, Dao, Database, Converter)
 │   │   │   ├─ asset/      (content.json 로더)
 │   │   │   └─ repository/ (ColoringRepository)
 │   │   ├─ domain/
 │   │   │   ├─ model/      (Category, ColoringTarget, Stroke, DrawingState)
 │   │   │   └─ usecase/
 │   │   ├─ ui/
 │   │   │   ├─ home/
 │   │   │   ├─ category/
 │   │   │   ├─ picker/
 │   │   │   ├─ drawing/    (★ 핵심 화면)
 │   │   │   ├─ component/
 │   │   │   └─ theme/
 │   │   ├─ di/             (Hilt 모듈)
 │   │   ├─ ColoringApplication.kt
 │   │   └─ MainActivity.kt
 │   └─ src/main/assets/
 │       ├─ outlines/        ← 콘텐츠 빌드 도구가 채움 (외곽선 PNG, 1000x1000, RGBA)
 │       ├─ thumbs/          ← 콘텐츠 빌드 도구가 채움 (200x200 PNG)
 │       └─ content.json     ← 콘텐츠 빌드 도구가 갱신
 │
 └─ tools/content-builder/   ← Python 빌드 도구 (별도 마스터 프롬프트로 작업)

## 외곽선 PNG 형식 (콘텐츠 빌드 도구가 보장하는 계약)
- 1000 × 1000 픽셀, RGBA
- 흰 영역(원래 배경) → 알파 0 (투명)
- 검은 선 → RGB 검정 + 알파 255
- 캐릭터는 캔버스 80%로 중앙 배치, 가장자리 10% 여백

## 외곽선 ↔ 사용자 그림 z-order
바닥 (z=0): 흰색 배경 (Surface 자체)
중간 (z=1): 사용자 드로잉 레이어 (Compose Canvas)
위   (z=2): 외곽선 PNG (검은 선만 보임, 흰 부분은 투명)
→ 외곽선은 항상 사용자 그림 위에 표시되어 가려지지 않음

## 데이터 모델

data class Category(
    val id: String,
    val name: String,
    val themeColor: Long,           // ARGB
    val thumbnailAsset: String?,    // null 가능
    val targets: List<ColoringTarget>
)

data class ColoringTarget(
    val id: String,
    val categoryId: String,
    val name: String,
    val outlineAssetPath: String,   // "outlines/hachu_main.png"
    val thumbnailPath: String       // "thumbs/hachu_main.png"
)

data class Stroke(
    val points: List<NormalizedPoint>, // 정규화 좌표 0~1
    val color: Long,                    // ARGB
    val widthDp: Float,                 // 8f, 16f, 28f
    val isEraser: Boolean = false
)

data class NormalizedPoint(val x: Float, val y: Float)
// Compose의 Offset은 직렬화 어색해서 별도 클래스로

data class DrawingState(
    val targetId: String,
    val strokes: List<Stroke>,
    val updatedAt: Long
)

## 자유 드로잉 핵심 구현 원칙
1. ★ 좌표는 반드시 정규화(0~1)로 저장. 화면 크기 달라도 그대로 복원.
2. 캔버스는 정사각형 비율 강제 (BoxWithConstraints + 짧은 변 기준).
3. 두 레이어: 사용자 Canvas + 그 위에 외곽선 Image (둘 다 같은 정사각 영역).
4. 손가락 입력: pointerInput { detectDragGestures } + onDragStart/End로 한 획 단위 관리.
5. 부드러운 선: 점들 사이 quadraticBezierTo 보간.
6. 지우개: BlendMode.Clear (해당 layer를 graphicsLayer { compositingStrategy = Offscreen }로 둘러야 동작).
7. 외곽선 PNG는 Coil의 AsyncImage로 assets에서 로드 (또는 BitmapFactory.decodeStream + Image).

## 콘텐츠 확장성
- assets/content.json + assets/outlines/ + assets/thumbs/ 만 보면 됨
- 별도 콘텐츠 빌드 도구가 이 셋을 자동으로 채움 (별도 작업)
- 앱은 그저 정적으로 읽기만. 변환 로직 없음

## 작업 방식 규칙
1. 매 마일스톤 시작 시 짧게 계획 보여주고 시작
2. 코드는 빌드되는 형태로 완성도 있게. 컴파일 에러 없도록
3. build.gradle.kts 변경사항은 명시적으로 보여줘
4. 라이브러리 버전은 안정 stable 버전 사용
5. 처음 보는 안드로이드 개념은 Spring 비유 한 줄로 짧게 설명
6. 한국어 주석을 핵심 로직에 적절히
7. 마일스톤마다 "어떻게 실행/검증하는지" 1~3줄 가이드 포함
8. 5살 UX 원칙을 모든 결정에서 우선시
9. 가족 내부용이라 외부 배포 관련 코드(서명, 난독화, 분석 SDK 등) 추가 금지

이해했으면 "준비 완료"라고만 답하고 첫 마일스톤 요청을 기다려.
```

---

### M1. 프로젝트 셋업

```
M1을 시작하자.

요구사항:
- coloring-project/ 루트 디렉토리 안에 app/ 모듈로 안드로이드 프로젝트 생성
  (또는 그 구조에 맞는 standard Android Studio 프로젝트)
- Kotlin + Jetpack Compose + Material3
- 패키지: com.example.coloring
- minSdk 26, targetSdk 34, compileSdk 34
- Gradle Kotlin DSL (build.gradle.kts)
- Version Catalog 사용 (gradle/libs.versions.toml)
- 의존성: Hilt, Room, Navigation Compose, Coroutines, kotlinx.serialization, Material3, Coil(SVG/PNG)
- ColoringApplication 클래스 (@HiltAndroidApp), AndroidManifest에 등록
- 빈 MainActivity (Scaffold + 환영 텍스트)
- 폴더 구조 (data/domain/ui/di) 미리 만들기 (빈 채로라도)
- assets/ 디렉토리에 outlines/ thumbs/ 빈 폴더 + 빈 content.json 만들기 (빌드 도구가 채울 자리)

내가 받아야 하는 것:
1. project-level build.gradle.kts (전체)
2. app-level build.gradle.kts (전체)
3. settings.gradle.kts (전체)
4. gradle/libs.versions.toml (전체)
5. AndroidManifest.xml
6. ColoringApplication.kt
7. MainActivity.kt (Scaffold + "색칠 놀이" 환영 텍스트)
8. 빈 content.json 템플릿
9. 빌드/실행 가이드 (Android Studio 처음 쓰는 사람용, 3~5단계)

어디 한 줄 추가하라는 식의 부분 코드는 안 됨. 빌드되는 풀 코드.
```

### M2. 데이터 계층

```
M2 진행. 데이터 계층 만들자.

만들어야 할 것:
1. Room
   - DrawingStateEntity (@Entity)
   - DrawingStateDao (@Dao)
   - AppDatabase (@Database)
   - Converters (List<Stroke> ↔ JSON, kotlinx.serialization)

2. content.json 로더
   - assets/content.json 더미 데이터 (카테고리 1개, 캐릭터 0개로 시작 - 빌드 도구가 채움)
   - @Serializable data class CategoryDto, ColoringTargetDto
   - AssetContentLoader: Application Context로 assets/content.json 읽어 List<Category> 반환
   - 파일 없거나 빈 경우 빈 리스트 반환 (앱 죽지 않게)

3. 도메인 모델
   - Category, ColoringTarget, NormalizedPoint, Stroke, DrawingState
   - 모두 @Serializable

4. Repository
   - ColoringRepository 인터페이스
   - ColoringRepositoryImpl (Dao + AssetContentLoader 조합)
   - 메서드:
     · getCategories(): Flow<List<Category>>
     · getCategory(categoryId): Flow<Category?>
     · getDrawingState(targetId): Flow<DrawingState?>
     · saveDrawingState(state): suspend
     · resetDrawing(targetId): suspend
     · hasDrawing(targetId): Flow<Boolean>

5. Hilt 모듈
   - DatabaseModule (@Module @InstallIn(SingletonComponent))
   - RepositoryModule

빌드 통과 + 컴파일 에러 없을 것. 단위 테스트는 생략.
```

### M3. 드로잉 캔버스 PoC ★

```
M3, 가장 중요한 마일스톤이야. 자유 드로잉이 동작하는 단일 화면을 만들자.

요구사항:
1. DrawingScreen (Composable) - 일단 NavGraph 없이 MainActivity에서 직접 띄움
   - BoxWithConstraints로 정사각형 영역 확보 (짧은 변 기준)
   - z=0: 흰 배경
   - z=1: DrawingCanvas (사용자 드로잉)
   - z=2: 외곽선 PNG Image (assets에서 Coil AsyncImage로 로드)
     ※ 외곽선 PNG가 없으면 임시 placeholder (Compose로 큰 원 + 작은 원 두 개로 얼굴 형태)

2. DrawingViewModel (@HiltViewModel)
   - StateFlow<DrawingUiState>
   - DrawingUiState: targetId, outlinePath, strokes, currentStroke, currentColor, currentWidthDp, currentTool
   - PoC 단계에서 currentColor는 빨강 고정, widthDp는 16, tool은 brush
   - 함수: onDragStart(offset, canvasSize), onDrag(offset, canvasSize), onDragEnd(), clearAll()
   - 좌표는 canvasSize로 나눠서 NormalizedPoint(0~1)로 저장

3. DrawingCanvas (Composable, 재사용)
   - props: strokes, currentStroke, canvasSize, onStrokeStart, onStrokeUpdate, onStrokeEnd
   - 각 Stroke를 Path로 변환해 drawPath
   - 점이 3개 이상이면 quadraticBezierTo 보간으로 부드럽게
   - 지우개 처리는 다음 마일스톤에서 (지금은 brush만)
   - pointerInput으로 awaitEachGesture { awaitFirstDown → drag } 패턴 사용

4. PoC 화면 임시 UI
   - 상단에 캐릭터 이름 (placeholder "테스트")
   - 중앙 정사각 캔버스
   - 하단에 "지우기" 버튼만 (clearAll)

5. MainActivity에서 DrawingScreen 직접 띄우기 (NavGraph는 M7에서)

검증 방법:
- 에뮬레이터에서 마우스로 드래그 → 빨간 선이 그려지는지
- 여러 획을 그리고 "지우기" → 다 지워지는지
- 화면 회전 시 정규화 좌표로 비율 유지되는지 (회전 후 동일 그림 비율로 다시 보임)
- 외곽선 placeholder가 사용자 그림 위에 보이는지

이번 마일스톤이 가장 어려우니, 막히면 짧게 일부만 보여주고 추가 질문 받아.
```

### M4. 색·굵기·도구·Undo·Reset

```
M4 진행. DrawingScreen에 도구 모음을 채우자.

추가할 것:
1. 색상 팔레트 (ColorPalette Composable)
   - 12색: #FF5A8C, #FFB84D, #FFE74C, #6BCB77, #4D96FF, #B362FF,
            #FFC0CB, #8B4513, #00CED1, #FFFFFF, #2C2C2A, "더보기(+)"
   - 동그란 칩 56dp, 가로 6 × 세로 2 그리드
   - 선택된 색은 검정 3dp 테두리
   - "더보기" 누르면 ModalBottomSheet에 추가 24색 (적당히 보색·파스텔 다양하게)

2. 도구 토글 (ToolBar Composable)
   - 붓 / 지우개 두 큰 버튼 (가로 나란히, 각 56dp 이상)
   - 선택된 도구는 진한 배경 + 굵은 테두리

3. 굵기 토글 (StrokeWidthPicker Composable)
   - 작은·중간·큰 동그라미 세 버튼
   - 각각 8dp / 16dp / 28dp 미리보기 굵기로 표시

4. 상단 액션 바 (TopActionBar Composable)
   - 뒤로(←) - placeholder
   - 캐릭터 이름 (가운데)
   - Undo(↶) - 마지막 Stroke 제거
   - Reset(⟲) - AlertDialog "정말 다 지울까요? 😢 / 응, 지울래 / 아니" 두 큰 버튼

5. ViewModel 확장
   - undo() : strokes의 마지막 항목 제거 (LinkedList 또는 List drop last)
   - reset() : strokes 비우기 (DB는 다음 마일스톤)
   - changeColor(color), changeWidthDp(dp), changeTool(tool)

6. 지우개 동작
   - DrawingCanvas에서 Stroke의 isEraser=true면 BlendMode.Clear 사용
   - 사용자 레이어에 graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
   - 외곽선 레이어는 별도라 영향 안 받음

7. DrawingScreen 레이아웃
   - 상단: TopActionBar
   - 중앙: 정사각 캔버스
   - 하단: ToolBar → StrokeWidthPicker → ColorPalette (세로 정렬)

5살 UX 원칙: 버튼 ≥ 56dp, Undo는 빠른 연타 가능.
```

### M5. 자동 저장·복원

```
M5.

추가할 것:
1. ViewModel에서 onDragEnd 시점에 Repository.saveDrawingState 호출
   - viewModelScope.launch로 비동기. UI 블로킹 없게.
   - 매 획마다 저장. 5살 사용 패턴(초당 1~2획)에서 충분.

2. DrawingScreen 진입 시 (LaunchedEffect(targetId))
   - Repository.getDrawingState(targetId).first() 로 한 번만 로드해 ViewModel state에 반영
   - 이후는 ViewModel 내부 상태가 source of truth

3. Reset 동작 시 DB도 함께 비우기 (Repository.resetDrawing 호출)

4. ViewModel은 SavedStateHandle로 targetId를 받음 (Navigation arg 전달용 사전 작업)

5. 임시: M3의 PoC에서 targetId를 하드코딩으로 "test_target"으로 시작.
   외곽선 PNG는 나중에 빌드 도구가 채움. 없으면 placeholder 그대로.

검증:
- 그림 그리기 → 앱 강제 종료 → 다시 실행 → 같은 화면 진입 시 그림 그대로
- Reset 누르면 DB도 비워져, 앱 재시작 시 빈 캔버스
- 두 다른 targetId로 진입 시 각자 상태 분리

이 마일스톤은 작업량 적지만 가장 핵심 가치라 꼼꼼히. 검증 시연 가이드 포함.
```

### M6. 카테고리 + 그림 선택 화면

```
M6.

만들 것:
1. CategoryScreen (CategoryViewModel)
   - LazyVerticalGrid(2 columns, 16dp gap)
   - 각 카드: themeColor 배경, 카테고리 썸네일(선택), 이름, 진행 표시
   - 진행 표시: "8개 중 3개 그렸어요" (DB에서 strokes 비어있지 않은 항목 카운트)
   - 코너 16dp, padding 16dp, 각 카드 누르면 PickerScreen으로 이동
   - 카테고리가 0개면 "아직 그림이 없어요" 안내 (빌드 도구로 추가하라는 안내는 어른용 작은 글씨)

2. PickerScreen (PickerViewModel, categoryId 인자)
   - 상단: 카테고리 이름 + 뒤로 버튼
   - LazyVerticalGrid(2 columns, 12dp gap)
   - 각 카드: 외곽선 썸네일(Coil로 assets/thumbs/ 로드), 캐릭터 이름
   - 진행 중인 카드는 우상단에 작은 배지 (예: 🎨)
   - 길게 누르면 AlertDialog "이 그림 다시 시작?" → 해당 targetId 리셋
   - 누르면 DrawingScreen(targetId)로 이동

3. Repository 확장
   - getCategoryProgress(categoryId): Flow<CategoryProgress(total, started)>
   - hasDrawing(targetId)는 이미 있음

4. @Preview 1개씩 (모의 데이터)

빈 카테고리/캐릭터 처리에 신경 써. 빌드 도구가 콘텐츠를 안 넣은 상태에서도 앱이 죽지 않고 안내가 나와야 함.
```

### M7. 홈 + 네비게이션 통합

```
M7. MVP 완성.

만들 것:
1. HomeScreen
   - 화면 70% 차지하는 거대한 "🎨 시작하기" 버튼 (눌러 카테고리로)
   - 작은 갤러리 아이콘 / 설정 아이콘 (둘 다 placeholder, 동작 없음)
   - 배경 파스텔, 큰 폰트, 아이용 분위기

2. NavGraph (NavHostController + NavHost)
   - Routes:
     · home
     · category
     · picker/{categoryId}
     · drawing/{targetId}
   - 시스템 백 자연스럽게
   - 화면 전환 fadeIn(150) + fadeOut(150)

3. MainActivity 정리
   - setContent { ColoringTheme { AppNavGraph() } }
   - DrawingScreen 직접 띄우던 PoC 코드 제거

4. 통합 검증:
   - 홈 → 카테고리 → 그림 선택 → 색칠 → 백 → 백 → 백 자연스럽게
   - 색칠 후 백 → 다시 들어가면 그림 유지
   - 모든 기능(Undo/Reset/도구 변경/색 변경) 정상

5. 회고
   - 부족한 점 짧게 정리
   - 5살 자녀 사용 시 점검 항목 5개 제안
```

### M8. 폴리싱 (선택)

```
M8.

추가:
- 색칠 시 작은 효과음 (SoundPool, res/raw/pop.ogg 더미 파일 추가, 없으면 안내)
- 색 변경 시 살짝 scale 애니메이션 (200ms)
- 가로/세로 회전 시 ViewModel + rememberSaveable로 상태 유지
- 다크 모드는 비활성화 (forceDarkAllowed=false)
- 그림 그릴 때만 화면 켜짐 유지 (FLAG_KEEP_SCREEN_ON, DrawingScreen 진입 시 on/off)
- 프로젝트 루트 README.md:
  * 안드로이드 처음 만지는 사람용 빌드/설치 가이드
  * 자녀 폰/태블릿에 디버그 빌드 설치하는 방법 (USB 디버깅 + 개발자 모드 + adb install 또는 Android Studio Run)
  * 폴더 구조 한눈에
  * 콘텐츠 추가 방법은 별도 도구 README 링크
  * 트러블슈팅 자주 나는 빌드 에러 3개 + 해결법
```

---

## 🅱️ 콘텐츠 빌드 도구: 마스터 프롬프트

별도 세션에서 시작하세요. 앱 작업 세션과 분리하면 컨텍스트가 깨끗합니다.

```
너는 시니어 파이썬 개발자야. 이미 만들어진 안드로이드 색칠 놀이 앱에 콘텐츠를 추가하는 빌드 도구를 만들 거야.

## 사용자 배경
- Java/Spring 백엔드 개발자, Python은 익숙함 (Django/Flask 정도)
- 처음 보는 라이브러리 개념은 짧게 설명

## 사용 범위
- 자녀·가족 내부용. 외부 배포 없음
- 한 사람(부모)이 단독으로 도구를 사용해 콘텐츠 추가

## 도구 목적
이미 만들어진 흑백 외곽선 도안 (PDF / PNG) 을 받아서:
1. 1000x1000 RGBA PNG 외곽선 (흰 영역 투명, 검은 선만)으로 변환
2. 200x200 썸네일 PNG 생성
3. content.json에 캐릭터 항목 추가/갱신
4. 결과를 안드로이드 앱의 assets 폴더에 직접 떨어뜨림

## 위치
프로젝트 루트는 coloring-project/ 가정.
- 입력: coloring-project/inputs/<원본 PDF/PNG>
- 출력 1: coloring-project/app/src/main/assets/outlines/<id>.png
- 출력 2: coloring-project/app/src/main/assets/thumbs/<id>.png
- 출력 3: coloring-project/app/src/main/assets/content.json (갱신)
- 도구 코드: coloring-project/tools/content-builder/

## 출력 PNG 형식 (앱과의 계약, 반드시 준수)
- 외곽선: 1000 × 1000, RGBA (PNG-32)
- 흰 영역 → 알파 0 (투명)
- 검은 선 → RGB 검정 + 알파 255 (선의 진한 정도에 따라 알파 가중)
- 캐릭터는 캔버스 80% 크기로 중앙 배치, 가장자리 10% 여백
- 썸네일: 200 × 200, RGBA, 같은 규칙
- 썸네일 축소 시 외곽선이 사라지지 않게 주의 (필요 시 morphology dilate로 1px 굵기 보강)

## content.json 형식
{
  "version": 1,
  "generatedAt": <epoch_ms>,
  "categories": [
    {
      "id": "<영문 소문자/숫자/언더스코어>",
      "name": "<한글 가능>",
      "themeColor": "#RRGGBB",
      "thumbnail": "thumbs/cat_<id>.png" | null,
      "targets": [
        {
          "id": "<영문 소문자/숫자/언더스코어>",
          "name": "<한글 가능>",
          "outline": "outlines/<id>.png",
          "thumbnail": "thumbs/<id>.png",
          "addedAt": <epoch_ms>
        }
      ]
    }
  ]
}

## 입력 가정
- 입력은 이미 흑백 외곽선 도안 (컬러 → 외곽선 변환은 불필요)
- 다만 회색 노이즈/안티앨리어싱은 있을 수 있음. 임계값 이진화로 정리
- PDF는 1페이지에 1캐릭터인 경우와 여러 캐릭터 섞인 경우 둘 다 가정 (split 옵션 제공)
- PNG는 한 파일이 한 캐릭터

## 의존성
Pillow + pypdfium2 만 사용 (OpenCV·NumPy 등 무거운 거 X).
requirements.txt에 명시. 가상환경(venv) 사용 가정.

## 명령어 인터페이스 (argparse)
1. add_character.py
   - --image <path> (필수)
   - --category-id <id> (필수)
   - --category-name <name> (선택, 카테고리 신규 생성 시 필요)
   - --category-color <#RRGGBB> (선택, 기본 #888888)
   - --target-id <id> (필수)
   - --target-name <name> (필수)
   - --threshold <int> (선택, 기본 200)
   - --stroke-boost (선택, 얇은 선 굵기 1~2px 보강)
   - --force (선택, 동일 ID가 있을 때 덮어쓰기)

2. add_pdf.py
   - --pdf <path> (필수)
   - --category-id <id> (필수)
   - --category-name, --category-color (위와 동일)
   - --interactive (기본, 페이지마다 ID/이름 입력 받음)
   - --auto-id-prefix <prefix> (대화형 안 쓰고 prefix_001, prefix_002... 자동)
   - --pages <range> (예: 1-3,5,7-9, 특정 페이지만)
   - --split <int> (각 페이지를 N분할, 한 페이지에 여러 캐릭터일 때)
   - --dpi <int> (기본 250)

3. list_content.py (콘텐츠 목록 보기)

4. remove_target.py (특정 캐릭터 제거)
   - --target-id <id>
   - 외곽선/썸네일 PNG 삭제 + JSON에서 제거

## 안전성
- content.json 수정 전 항상 백업 (.bak)
- 검증 실패 시 백업에서 복구 + 에러 메시지
- 동일 ID 충돌 시 --force 없으면 에러
- 입력 파일이 없으면 명확한 에러
- 중간 단계 임시 파일은 tempfile로 (남기지 말 것)

## 처리 파이프라인 (이미 흑백 가정)
1. 입력 로드 (PDF는 페이지별 PNG로)
2. RGBA 변환
3. 그레이스케일 → 임계값 이진화
4. 흰 픽셀 → 알파 0 (투명) 변환
5. 검은 픽셀의 알파는 어두움 정도에 비례 (안티앨리어싱 가장자리 부드러움 유지)
6. 비투명 영역 bbox로 트림
7. 1000x1000 캔버스 중앙에 80% 크기로 paste
8. 200x200 썸네일 생성 (LANCZOS 축소 + 필요 시 굵기 보강)
9. 결과 저장
10. content.json 갱신 (백업 → 수정 → 검증)

## 작업 방식 규칙
1. 매 마일스톤 시작 시 짧게 계획 보여주고 시작
2. 코드는 동작 보장. 단위 테스트는 핵심 함수 1~2개에만 (이미지 처리는 시각 검증)
3. 파일별로 보여주고, 마지막에 사용 예시 3~5개 데모
4. 한국어 주석을 핵심 로직에 적절히
5. 외부에서 수동으로 만든 마음대로 PDF로도 어느 정도 동작하도록 견고하게

이해했으면 "준비 완료"라고만 답하고 첫 마일스톤 요청을 기다려.
```

---

### B1. 도구 골격 + 단일 PNG 처리

```
B1을 시작하자.

만들 것:
1. tools/content-builder/ 구조
   - requirements.txt (Pillow, pypdfium2)
   - README.md (사용법, 예시)
   - core/ 패키지
     · image_processing.py (외곽선 변환, 트림, 정규화, 썸네일)
     · content_json.py (JSON 읽기/쓰기/갱신, 백업/복구)
     · paths.py (assets 경로 상수)
   - add_character.py (CLI 진입점)
   - list_content.py (콘텐츠 목록 출력)

2. image_processing.py 함수
   - load_as_rgba(path) -> Image
   - to_outline_rgba(img, threshold=200) -> Image  (흰 → 투명, 검은 → RGB 검정 + 알파)
   - trim_to_content(img) -> Image  (비투명 영역 bbox로 자름)
   - normalize_to_canvas(img, canvas_size=1000, margin_ratio=0.10) -> Image
   - make_thumbnail(img, size=200, stroke_boost=True) -> Image
   - process_pipeline(input_path, threshold, stroke_boost) -> (outline_img, thumb_img)

3. content_json.py 함수
   - load_or_init(path) -> dict
   - save_with_backup(path, data) -> None
   - find_or_create_category(data, cat_id, cat_name, cat_color) -> dict
   - upsert_target(category, target_id, target_name, outline_path, thumb_path, force) -> bool

4. add_character.py
   - argparse로 위 명세 파라미터 받음
   - 핸들링: 입력 검증 → 처리 → 저장 → JSON 갱신 → 결과 요약 출력
   - Exit code: 0 성공, 1 사용자 에러, 2 시스템 에러

5. list_content.py
   - content.json 읽어서 카테고리/캐릭터 트리 형태로 출력
   - 빈 카테고리 표시
   - 진행률 통계는 앱이 DB에 있으니 여기선 카운트만

6. README.md
   - 설치: python -m venv .venv && source .venv/bin/activate && pip install -r requirements.txt
   - 기본 사용 예시 3개
   - 트러블슈팅 (파일 권한, JSON 깨짐, ID 충돌)

검증 방법:
- 테스트용 흑백 PNG 1장으로 add_character.py 실행
- assets/outlines/, assets/thumbs/, content.json이 정상 생성·갱신되는지
- list_content.py로 결과 확인
```

### B2. PDF 입력 + 일괄 처리

```
B2 진행.

추가:
1. core/pdf_utils.py
   - extract_pages(pdf_path, dpi=250, page_range=None) -> list[Image]
     · pypdfium2 사용
     · page_range 파싱 (예: "1-3,5,7-9")
   - split_image(img, n) -> list[Image]  (페이지를 가로로 N분할)

2. add_pdf.py
   - argparse로 위 명세 파라미터
   - 흐름:
     a. PDF 페이지 추출
     b. --split N이면 각 페이지를 N개로 분할
     c. --interactive (기본)면 각 항목마다 콘솔에서 "ID와 이름 입력? skip는 엔터" 받음
     d. --auto-id-prefix면 prefix_001, prefix_002 자동 부여 + 이름은 "캐릭터 N" 식
     e. 각 항목마다 image_processing.process_pipeline 돌려 저장
     f. 마지막에 결과 요약 (성공 N개, 실패 N개)
   - 중간 실패해도 이미 처리된 건 유지 (멱등성 비슷한 패턴)

3. add_character.py와 add_pdf.py가 공유하는 처리 로직은 core/ 안에서 함수로 빼서 재사용

4. README에 PDF 사용 예시 추가
   - 1페이지 1캐릭터 PDF
   - 1페이지 다캐릭터 PDF (--split)
   - 특정 페이지만 (--pages)

검증 방법:
- 샘플 PDF 1개 (1~2페이지)로 add_pdf.py 실행
- 인터랙티브 모드에서 페이지 미리보기를 어떻게 보여줄지: 각 페이지를 임시 파일로 저장하고 경로를 콘솔에 출력하면 사용자가 직접 열어볼 수 있게
- 결과 PNG들이 정확히 1000x1000인지, 알파 처리 됐는지
```

### B3. 견고성 + remove + 자동화 옵션

```
B3 진행. 마지막 마일스톤.

추가:
1. remove_target.py
   - --target-id <id>
   - --keep-files (선택, 파일 안 지우고 JSON에서만 제거)
   - 처리: JSON 백업 → JSON에서 제거 → outlines/<id>.png 삭제 → thumbs/<id>.png 삭제
   - 카테고리가 비게 되면 카테고리도 제거할지 확인 (--prune-empty 플래그)

2. 견고성 강화
   - JSON 검증: jsonschema 또는 수동 검증 (필수 키 존재, 타입 체크)
   - 모든 명령에 --dry-run 옵션 (실제 변경 없이 무엇이 바뀔지 출력만)
   - 명령 실행 시 작업 로그를 tools/content-builder/.log/<timestamp>.log로 자동 저장 (선택)

3. 배치 모드
   - add_pdf.py에 --batch <yaml> 옵션
     · YAML로 페이지별 ID/이름을 미리 정의
     · 비대화형 일괄 처리
   - 예시 YAML:
     ```
     pdf: inputs/티니핑.pdf
     category:
       id: hachu_friends
       name: 하츄핑 친구들
       color: "#FF5A8C"
     items:
       - page: 1
         id: hachu_main
         name: 하츄핑
       - page: 2
         id: malang
         name: 말랑핑
       - page: 3
         id: posil
         name: 포실핑
     ```

4. 최종 README 다듬기
   - 5가지 시나리오 풀 예시:
     A. 단일 PNG 추가
     B. PDF 1페이지 1캐릭터
     C. PDF 다페이지 일괄 (인터랙티브)
     D. PDF 다페이지 일괄 (배치 YAML)
     E. 캐릭터 제거
   - 트러블슈팅 5개 이상

5. 회고
   - 자녀가 자주 추가 요청할 때 가장 빠른 워크플로우 추천
   - 콘텐츠 30개 일괄 추가 시 예상 소요 시간

여기까지 끝나면 도구 완성. 콘텐츠 등록(C1)은 도구 사용일 뿐 코딩 작업 없음.
```

---

## 💡 사용 팁

### 세션 분리 권장
- 앱 작업과 도구 작업은 **다른 세션**으로 진행하세요. 컨텍스트가 깨끗하고 결과 품질이 더 좋습니다.
- 같은 세션에서 둘을 섞으면 "Kotlin 컨텍스트인데 Python 코드가 나오는" 식의 혼선이 생길 수 있습니다.

### 작업 순서 권장
1. 앱 M1~M5 (드로잉 동작하는 단일 화면까지)
2. 도구 B1~B3 (콘텐츠 빌드 도구 완성)
3. 콘텐츠 5~10개 추가 (도구로 등록)
4. 앱 M6~M7 (전체 동선 통합)
5. 자녀에게 보여주고 피드백 (가장 중요!)
6. 앱 M8 (폴리싱) + 콘텐츠 추가

### 빌드 에러 디버깅 프롬프트
```
다음 빌드 에러를 수정해줘. Spring 비유로 짧게 원인 설명하고 수정 코드만 보여줘.
[에러 로그 붙여넣기]
```

### 자녀 폰/태블릿에 설치
가족 내부용이라 디버그 빌드면 충분:
1. 자녀 기기에서 "개발자 옵션" 활성화 (설정 → 휴대전화 정보 → 빌드 번호 7번 탭)
2. 개발자 옵션에서 "USB 디버깅" 켜기
3. PC에 USB 연결 후 Android Studio에서 Run ▶ → 기기 선택
4. 또는 `./gradlew assembleDebug` → 생긴 APK를 기기에 옮겨 설치

설치 후 USB 분리해도 앱은 계속 사용 가능. 새 콘텐츠 추가 시 또 빌드해서 재설치.

---

## 📌 자녀 사용 시 점검 체크리스트 (M7 이후)

자녀에게 처음 보여주기 전 직접 한 번씩 체크:

- [ ] 5살이 한 손으로 들고 사용 가능한가
- [ ] 시작 버튼이 충분히 큰가 (한 번에 누르는가)
- [ ] 색 선택이 직관적인가 (안 헤매는가)
- [ ] 그림이 끊기지 않고 부드럽게 그려지는가
- [ ] 실수했을 때 Undo로 쉽게 복구되는가
- [ ] Reset이 실수로 눌려도 다이얼로그로 막히는가
- [ ] 앱을 끄고 켜도 그림이 그대로인가
- [ ] 카테고리/그림 선택이 5살에게 어렵지 않은가 (글자 못 읽어도 그림으로 인지 가능한가)
- [ ] 외곽선이 너무 흐리거나 너무 진하지 않은가
- [ ] 한 그림에 너무 오래 머물러 지루하지 않은가 (5살 집중 시간 5~10분 고려)
