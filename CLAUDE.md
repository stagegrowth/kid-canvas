# Kid Canvas — Claude Code 세션 컨텍스트

이 파일은 Claude Code 세션 시작 시 자동 로드되어 프로젝트 컨텍스트를 유지합니다.
사용자에게 응답할 때는 항상 한국어를 사용하세요.

## Project Overview
- 한국 나이 5살 자녀를 위한 색칠 놀이 안드로이드 앱
- 가족 내부용 (외부 배포·플레이스토어 등록 안 함)
- 콘텐츠는 흑백 외곽선 도안 PDF/PNG (티니핑 등 자녀가 좋아하는 캐릭터)

## User Background
- Java + Spring + Tomcat 기반 백엔드 개발자
- Android·Kotlin은 처음 (Jetpack Compose, Hilt, Room, Coroutines 등 모두 새로움)
- 처음 보는 안드로이드 개념은 **Spring 비유로 한 줄씩만** 짧게 설명
  - 예: "Hilt는 Spring DI랑 같은 역할. `@Inject`가 `@Autowired`랑 비슷"
  - 비유는 헷갈릴 만한 부분에서만. 길게 설명하지 말 것
- Python은 익숙 (Django/Flask 정도 경험)

## Development Environment
- 호스트: Windows + WSL2 Ubuntu
- **프로젝트 위치 (Windows 디스크에 위치, WSL과 Windows가 같은 폴더 공유)**:
  - WSL 시점: `/mnt/c/Users/sgkim/workspace/kid-canvas`
  - Windows 시점: `C:\Users\sgkim\workspace\kid-canvas`
  - 이전 위치(`/home/segon/workspace/kid-canvas`)는 백업으로 `.OLD` 접미사를 붙여 보존됨 (며칠 후 안정 확인 시 삭제 결정)
  - WSL ext4 경로에 두면 9P 프로토콜의 파일 락 미지원 때문에 Gradle Sync가 IOException으로 실패하여 Windows 디스크로 옮김
- **WSL의 Claude Code에서 진행**: 코드 편집, `git`, 콘텐츠 빌드 스크립트(Python), 간단한 `./gradlew` 검증(`--version`, `tasks`, `compileDebugKotlin`)
- **Windows의 Android Studio에서 진행**: 실제 빌드, 기기 설치, 에뮬레이터 실행, SDK 매니저, 라이선스 동의
- Android SDK는 Windows에만 설치. WSL에는 별도 SDK 두지 않음
- WSL의 `adb`는 빠른 기기 인식 점검용 보조 도구. 실제 기기 설치는 Windows의 `adb` 사용
- 양쪽 adb daemon이 5037 포트 충돌 시 한쪽에서 `adb kill-server` 후 다른 쪽만 사용

## Tech Stack

### Android 앱 (`app/`)
- 언어: Kotlin
- UI: Jetpack Compose + Material3
- DI: Hilt
- 로컬 DB: Room
- 비동기: Coroutines + Flow
- 직렬화: kotlinx.serialization
- 이미지: Coil
- 네비게이션: Navigation Compose
- 빌드: Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)
- SDK: minSdk 26, targetSdk 34, compileSdk 34
- 아키텍처: MVVM + Repository

### 콘텐츠 빌드 도구 (`tools/content-builder/`)
- 언어: Python 3.10+
- 라이브러리: Pillow, pypdfium2 (가벼운 것만, OpenCV·NumPy 사용 금지)
- 가상환경(venv) 사용 가정

## Folder Structure

```
kid-canvas/   (= /mnt/c/Users/sgkim/workspace/kid-canvas, 문서상의 coloring-project에 해당)
├─ app/                              안드로이드 앱
│  └─ src/main/
│     ├─ java/com/stagegrowth/kidcanvas/
│     │  ├─ data/
│     │  │  ├─ local/                Room: Entity, Dao, Database, Converter
│     │  │  ├─ asset/                content.json 로더
│     │  │  └─ repository/           ColoringRepository
│     │  ├─ domain/
│     │  │  ├─ model/                Category, ColoringTarget, Stroke, DrawingState
│     │  │  └─ usecase/
│     │  ├─ ui/
│     │  │  ├─ home/
│     │  │  ├─ category/
│     │  │  ├─ picker/
│     │  │  ├─ drawing/              ★ 핵심 화면
│     │  │  ├─ component/
│     │  │  └─ theme/
│     │  ├─ di/                      Hilt 모듈
│     │  ├─ KidCanvasApplication.kt
│     │  └─ MainActivity.kt
│     └─ assets/
│        ├─ outlines/                ← 빌드 도구가 채움 (1000×1000 RGBA PNG)
│        ├─ thumbs/                  ← 빌드 도구가 채움 (200×200 RGBA PNG)
│        └─ content.json             ← 빌드 도구가 갱신
├─ tools/content-builder/            Python 빌드 도구 (별도 작업)
├─ inputs/                           콘텐츠 원본 PDF/PNG (gitignored)
└─ docs/                             기획 문서
```

## App ↔ Builder 계약 (변경 금지)
- 외곽선 PNG: 1000 × 1000, RGBA, 흰 영역 알파 0, 검은 선 RGB 검정 + 알파 255, 캐릭터 80% 중앙 배치
- 썸네일 PNG: 200 × 200, RGBA, 같은 규칙
- `content.json`: `version`, `generatedAt`, `categories[].targets[]` 구조 (자세한 스키마는 `docs/development_plan_v3.md` §7 참조)
- ID는 영문 소문자/숫자/언더스코어만, 캐릭터 ID는 전체 유일

## Coding Rules
- **빌드되는 코드만 작성**. 컴파일 에러 없게. 부분 코드("이 줄을 추가하세요") 금지, 풀 코드로 보여주기
- `build.gradle.kts` 변경 시 명시적으로 전체 코드를 보여주기
- 라이브러리 버전은 안정 stable 버전만 사용
- 한국어 주석을 핵심 로직에 적절히 (모든 줄에 달지 말 것)
- 처음 보는 안드로이드 개념은 Spring 비유 한 줄로 짧게 설명
- 패키지명: `com.stagegrowth.kidcanvas` (영문 소문자만)
- **5살 UX 원칙**:
  - 모든 터치 타깃 ≥ 56dp
  - 글자보다 색·아이콘·이미지로 정보 전달
  - 화면당 다음 행동이 1~2가지로 명확
  - 위험 행동(전체 초기화 등)은 반드시 확인 다이얼로그
  - 즉각적 시각·청각 피드백
  - 외곽선 밖으로 그림이 빠져도 OK (재미가 우선)
- **외부 배포 코드 금지**: 서명 키, ProGuard/R8 난독화, 분석 SDK(Firebase Analytics 등), 스토어 메타데이터 추가 X
- 좌표는 반드시 **정규화 좌표 (0~1)**로 저장 (화면 크기 달라도 복원되도록)

## Workflow
- **마일스톤 단위로 진행**:
  - 앱: M1(셋업) → M2(데이터 계층) → M3(드로잉 캔버스 PoC) → M4(색·굵기·도구·Undo·Reset) → M5(자동 저장·복원) → M6(카테고리/그림 선택) → M7(홈 + 네비게이션) → M8(폴리싱)
  - 빌드 도구: B1(골격 + 단일 PNG) → B2(PDF + 일괄) → B3(견고성 + remove + batch)
  - 콘텐츠: C1(초기 5~10개 등록) → C2(추가 등록)
- 권장 순서: M1~M5 → B1~B3 → C1 → M6~M8 → C2
- 각 마일스톤의 상세 요구사항은 `docs/claude_prompt_v3.md`에 정의됨
- 마일스톤 시작 시: 짧은 계획 보여주고 시작
- 마일스톤 완료 시: 실행/검증 방법 1~3줄 가이드 + git commit + push

## WSL + Android 운영 가이드

### ⚠️ 절대 하지 말 것
- `chmod -R 777` (NTFS ACL 영구 손상, Android Studio Open 차단)
- `.idea/` 폴더 삭제 (IDE Trust 정보 손실)

### ✅ 권한 변경 시 표준 권한
- 파일: `chmod 644`
- 디렉토리: `chmod 755`
- 실행 스크립트(`gradlew` 등): `chmod 755`

### 🔧 알려진 한계 및 우회
1. WSL ext4(`/home/...`) 경로의 Android 프로젝트:
   - Android Studio Open 시 NTFS ACL 권한 매핑 이슈 발생 가능
   - Gradle Sync 시 9P 프로토콜이 파일 락 미지원 → `IOException`
2. **우회**: 안드로이드 프로젝트는 Windows 디스크(`/mnt/c/Users/<id>/...`)에 두는 것이 표준
3. 다른 종류 프로젝트(Spring, Python, 일반 Linux 도구)는 WSL ext4에 두어도 OK

### 📋 Claude Code 워크플로우 (변화 없음)
- `cd /mnt/c/Users/sgkim/workspace/kid-canvas`
- `claude`
- 모든 명령(`git`, `gradle`, 파일 편집)은 그대로
- 단지 진입 경로만 다름

### 🔍 트러블슈팅
- **폴더 Open 권한 에러**:
  1. 빈 폴더로 격리 테스트 (시스템 vs 폴더 문제 분리)
  2. 폴더 문제면 새 폴더에 fresh 복사 + `.git`만 `git clone`
- **Gradle Sync `IOException`**:
  - 9P 한계, Windows 디스크 이동 필수
- **`git status`에 가짜 변경분 (mode `100644 → 100755` 등)**:
  - `git config core.fileMode false` 적용

## Build Verification (WSL)
WSL에서는 Gradle 래퍼 동작과 Kotlin 컴파일 정도만 검증:
- `./gradlew --version` (wrapper 정상 동작)
- `./gradlew tasks` (프로젝트 인식)
- `./gradlew :app:compileDebugKotlin` (Kotlin 컴파일 통과)

실제 APK 빌드, 기기 설치, 에뮬레이터 실행은 **Windows의 Android Studio**에서 수행.
WSL에 Android SDK가 없어 `assembleDebug` 등 SDK가 필요한 태스크는 실패함 (정상).

## Reference Docs
- [docs/development_plan_v3.md](docs/development_plan_v3.md) — 전체 개발 플랜, 시스템 구성, 데이터 형식 명세
- [docs/claude_prompt_v3.md](docs/claude_prompt_v3.md) — 마일스톤별 상세 프롬프트 (M1~M8, B1~B3)
