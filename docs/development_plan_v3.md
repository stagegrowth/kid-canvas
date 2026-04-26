# 색칠 놀이 앱 개발 플랜 v3

> **변경점**: 콘텐츠 빌드 파이프라인 추가. 앱과 빌드 도구를 분리.
> **사용 범위**: 자녀·가족 내부용 (외부 배포 안 함)
> **입력 형태**: 이미 제작된 흑백 외곽선 도안 (PDF / PNG)

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 앱 이름 | 색칠 놀이 (가칭) |
| 대상 사용자 | 한국 나이 5세 자녀 |
| 사용 범위 | 가족 내부용 (외부 배포 X) |
| 안드로이드 앱 | Kotlin + Jetpack Compose, 자유 드로잉 |
| 콘텐츠 빌드 도구 | Python 스크립트 (PDF/PNG → 외곽선 PNG + 메타) |
| 초기 콘텐츠 | 흑백 티니핑 도안 (인터넷에서 수집한 PDF/PNG) |

## 2. 전체 시스템 구성

```
coloring-project/
 ├─ app/                    ← 안드로이드 앱 (Kotlin + Compose)
 │   └─ src/main/
 │       ├─ java/...        (앱 코드)
 │       └─ assets/
 │           ├─ outlines/   ← 콘텐츠 빌드 도구가 채움
 │           ├─ thumbs/     ← 콘텐츠 빌드 도구가 채움
 │           └─ content.json ← 콘텐츠 빌드 도구가 갱신
 │
 ├─ tools/
 │   └─ content-builder/    ← 콘텐츠 빌드 도구 (Python)
 │       ├─ add_character.py
 │       ├─ add_pdf.py
 │       ├─ requirements.txt
 │       └─ README.md
 │
 ├─ inputs/                 ← 작업자가 PDF/PNG 원본을 두는 폴더
 │   └─ (gitignore 가능)
 │
 └─ README.md               ← 전체 프로젝트 안내
```

이 두 영역이 만나는 단 하나의 인터페이스: **`app/src/main/assets/` 폴더의 산출물 형식**

## 3. 콘텐츠 빌드 도구

### 3.1 입력 시나리오

| 입력 | 처리 흐름 |
|---|---|
| 흑백 외곽선 PNG 1장 | 노이즈 제거 → 1000x1000 정규화 → 썸네일 → JSON 추가 |
| PDF (1~여러 페이지가 한 캐릭터씩) | 페이지를 PNG로 추출 → 페이지마다 위 처리 반복 |
| 흑백 외곽선이지만 컬러 채널이 섞임 | 그레이스케일 변환 → 임계값 이진화 → 흰 배경 + 검은 선만 남김 |

### 3.2 처리 파이프라인 (이미 흑백 도안 가정)

1. **로드**: PNG는 그대로, PDF는 페이지 단위로 PNG 추출 (200~300 DPI)
2. **그레이스케일화**: RGBA → L 모드 (안전망)
3. **이진화**: 임계값 200 정도. 회색 노이즈를 흰색으로, 진한 선만 검정으로
4. **여백 정리(트림)**: 캐릭터 외곽 빈 공간 제거 후 다시 중앙 배치
5. **정규화**: 1000x1000 흰색 캔버스에 80% 크기로 중앙 배치
6. **썸네일 생성**: 200x200으로 LANCZOS 축소
7. **저장**: `app/src/main/assets/outlines/<id>.png`, `app/src/main/assets/thumbs/<id>.png`
8. **JSON 갱신**: `app/src/main/assets/content.json`의 해당 카테고리 targets 배열에 항목 추가/갱신

### 3.3 명령어 인터페이스

```bash
# 단일 PNG 추가
python tools/content-builder/add_character.py \
  --image inputs/hachu.png \
  --category-id hachu_friends \
  --category-name "하츄핑 친구들" \
  --category-color "#FF5A8C" \
  --target-id hachu_main \
  --target-name "하츄핑"

# PDF에서 여러 캐릭터 일괄 추가 (대화형 모드)
python tools/content-builder/add_pdf.py \
  --pdf inputs/티니핑-색칠공부-도안.pdf \
  --category-id hachu_friends

# → 페이지마다 "이 캐릭터 ID와 이름을 입력하세요" 묻고 처리
```

### 3.4 의존성 (Python)

```txt
# tools/content-builder/requirements.txt
Pillow==10.4.0
pypdfium2==4.30.0
```

매우 가벼움. OpenCV·NumPy 같은 무거운 라이브러리 없음.

### 3.5 검증 포인트

- 결과 PNG는 흰 배경 + 검은 외곽선만 남아야 함 (반투명 X)
- 1000x1000 정확히 (앱이 가정함)
- 썸네일은 외곽선이 식별 가능한 정도 (너무 얇아지지 않게)
- content.json은 유효한 JSON으로 유지 (실패 시 백업 자동 복구)

## 4. 안드로이드 앱 (v2 그대로 유지)

기본 설계는 v2의 자유 드로잉 그대로. 변경점은 다음 두 가지뿐:

### 4.1 외곽선 표시 방식 미세 조정

콘텐츠 빌드 도구가 출력하는 PNG는 **흰 배경 + 검은 선**입니다. 사용자가 그린 색이 흰 배경에 가려지지 않도록:
- 외곽선 PNG를 표시할 때 흰 배경을 투명하게 처리 (Canvas 합성 또는 BlendMode)
- 또는 빌드 도구에서 흰 배경 → 알파 0으로 변환해 RGBA PNG로 저장 (권장)

빌드 도구 쪽에서 처리하는 게 단순합니다. **결과 PNG는 RGBA**로, 흰 픽셀을 알파 0으로 만들어 검은 선만 남깁니다. 앱에서는 그냥 위에 얹기만 하면 됨.

### 4.2 사용자 그림 ↔ 외곽선 z-order

```
바닥 (z=0): 흰색 배경
중간 (z=1): 사용자 드로잉 레이어 (Compose Canvas)
위   (z=2): 외곽선 PNG (검은 선만, 흰 부분은 투명) ← 항상 사용자 그림 위에 표시
```

이렇게 해야 사용자가 외곽선 위로 그려도 검은 선이 가려지지 않고 그대로 보임.

### 4.3 기타는 v2 그대로

- 자유 드로잉, Stroke 리스트 정규화 좌표 저장
- 12색 + 24색 추가 시트
- 굵기 3단계, 도구 붓/지우개
- Undo + Reset
- 자동 저장 + 복원
- 카테고리 → 캐릭터 → 색칠 동선

## 5. 마일스톤 (앱 + 빌드 도구)

| 단계 | 영역 | 기간 | 산출물 |
|---|---|---|---|
| **M1** | 앱 | 0.5일 | 프로젝트 셋업, 빌드 통과 |
| **M2** | 앱 | 1일 | 데이터 계층 (Room, Repository, content.json 로더) |
| **M3** | 앱 | 1.5일 | 드로잉 캔버스 PoC (외곽선 위에 손가락 드로잉) |
| **M4** | 앱 | 1일 | 색·굵기·도구·Undo·Reset |
| **M5** | 앱 | 1일 | 자동 저장·복원 |
| **B1** | 도구 | 0.5일 | content-builder 기본 골격, 단일 PNG 처리 |
| **B2** | 도구 | 0.5일 | PDF 입력 + 대화형 일괄 추가 |
| **B3** | 도구 | 0.5일 | content.json 갱신, 카테고리 자동 생성, 백업/롤백 |
| **C1** | 콘텐츠 | 0.5일 | 빌드 도구로 티니핑 5~10개 1차 등록 |
| **M6** | 앱 | 1일 | 카테고리/그림 선택 화면 (실제 콘텐츠로 동작 확인) |
| **M7** | 앱 | 0.5일 | 홈 + 네비게이션 통합 |
| **M8** | 앱 | 1일 | 폴리싱 (효과음, 애니메이션, README) |
| **C2** | 콘텐츠 | 별도 | 추가 캐릭터 일괄 등록 (필요한 만큼) |

총 **약 9 작업일** (콘텐츠 등록 작업 별도, 한 캐릭터당 1~2분).

### 권장 순서
1. M1 ~ M5: 자유 드로잉이 동작하는 단일 화면까지 (가장 핵심)
2. B1 ~ B3: 빌드 도구 완성
3. C1: 빌드 도구로 콘텐츠 5~10개 넣기
4. M6 ~ M8: 실제 콘텐츠로 전체 동선 완성
5. C2: 자녀 반응 보면서 콘텐츠 추가

## 6. 사용 시나리오 (전체 흐름)

### 시나리오 A: 새 PDF 도안집을 받음
```bash
# 1. inputs/ 폴더에 PDF 둠
cp ~/Downloads/티니핑-색칠공부-도안.pdf inputs/

# 2. 일괄 추가 (대화형)
python tools/content-builder/add_pdf.py \
  --pdf inputs/티니핑-색칠공부-도안.pdf \
  --category-id hachu_friends \
  --category-name "하츄핑 친구들" \
  --category-color "#FF5A8C"
# → 도구가 페이지마다 "이 페이지 캐릭터 ID와 이름?" 묻고 자동 처리

# 3. Android Studio에서 앱 재빌드 → 자녀 폰/태블릿에 설치
```

### 시나리오 B: 단일 PNG 추가
```bash
python tools/content-builder/add_character.py \
  --image inputs/hachu_outline.png \
  --category-id hachu_friends \
  --target-id hachu_main \
  --target-name "하츄핑"
# → 자동으로 정규화, 썸네일, JSON 갱신
```

### 시나리오 C: 카테고리 새로 만들기
```bash
# 빌드 도구가 --category-id가 content.json에 없으면 자동으로 새 카테고리 생성
python tools/content-builder/add_character.py \
  --image inputs/sasha.png \
  --category-id new_kingdom \
  --category-name "사샤핑 왕국" \
  --category-color "#B362FF" \
  --target-id sasha_main \
  --target-name "사샤핑"
```

## 7. 데이터 형식 명세 (앱 ↔ 빌드 도구 계약)

### 7.1 외곽선 PNG
- 1000 × 1000 픽셀
- RGBA (PNG-32)
- 흰 영역 → 알파 0 (투명)
- 검은 선 → 알파 255, RGB 검정 (또는 어두운 회색)
- 캐릭터는 캔버스 80% 크기로 중앙 배치, 가장자리 10% 여백

### 7.2 썸네일 PNG
- 200 × 200 픽셀
- RGBA, 같은 규칙

### 7.3 content.json
```json
{
  "version": 1,
  "generatedAt": 1714128000000,
  "categories": [
    {
      "id": "hachu_friends",
      "name": "하츄핑 친구들",
      "themeColor": "#FF5A8C",
      "thumbnail": "thumbs/cat_hachu_friends.png",
      "targets": [
        {
          "id": "hachu_main",
          "name": "하츄핑",
          "outline": "outlines/hachu_main.png",
          "thumbnail": "thumbs/hachu_main.png",
          "addedAt": 1714128000000
        }
      ]
    }
  ]
}
```

### 7.4 ID 규칙
- 영문 소문자, 숫자, 언더스코어만 (`hachu_main`, `cha_cha`)
- 카테고리 ID는 카테고리 단위, 캐릭터 ID는 전체 유일
- 빌드 도구가 ID 충돌 시 경고 + 사용자 확인 받음

## 8. 빌드 도구 구현 디테일

### 8.1 흰 배경 → 알파 처리

```python
def make_outline_transparent(img: Image.Image, threshold: int = 230) -> Image.Image:
    """흰 픽셀을 투명으로, 검은 픽셀은 그대로 유지"""
    img = img.convert("RGBA")
    data = img.getdata()
    new_data = []
    for r, g, b, a in data:
        # 밝은 픽셀이면 투명 처리
        if r > threshold and g > threshold and b > threshold:
            new_data.append((255, 255, 255, 0))
        else:
            # 어두운 픽셀은 검정 + 불투명
            darkness = max(0, 255 - max(r, g, b))
            new_data.append((0, 0, 0, min(255, darkness * 2)))
    img.putdata(new_data)
    return img
```

### 8.2 트림 + 정규화

```python
def normalize_to_canvas(img: Image.Image, canvas_size: int = 1000, margin_ratio: float = 0.10) -> Image.Image:
    """이미지의 콘텐츠 영역을 추출해 정사각 캔버스 중앙에 배치"""
    bbox = img.getbbox()  # 비투명 영역
    if bbox is None:
        return Image.new("RGBA", (canvas_size, canvas_size), (255, 255, 255, 0))
    cropped = img.crop(bbox)
    w, h = cropped.size
    target = int(canvas_size * (1 - margin_ratio * 2))
    scale = min(target / w, target / h)
    new_size = (int(w * scale), int(h * scale))
    resized = cropped.resize(new_size, Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (255, 255, 255, 0))
    paste_x = (canvas_size - new_size[0]) // 2
    paste_y = (canvas_size - new_size[1]) // 2
    canvas.paste(resized, (paste_x, paste_y), resized)
    return canvas
```

### 8.3 PDF 페이지 추출

```python
import pypdfium2 as pdfium

def extract_pdf_pages(pdf_path: str, dpi: int = 250) -> list[Image.Image]:
    pdf = pdfium.PdfDocument(pdf_path)
    pages = []
    for page in pdf:
        bitmap = page.render(scale=dpi / 72)
        pages.append(bitmap.to_pil())
    return pages
```

### 8.4 content.json 안전 갱신

- 갱신 전 `content.json.bak` 백업
- 새 JSON으로 덮어쓰되 검증 실패 시 백업에서 복구
- 멀티 사용자 가정 안 해도 됨 (혼자 작업)

## 9. 위험 요소 및 완화

| 위험 | 영향 | 완화 |
|---|---|---|
| 외곽선이 너무 얇아 캔버스에서 안 보임 | 중간 | 빌드 도구에서 너무 얇은 선 굵기 1~2px 보정 옵션. 필요 시 `--stroke-boost` 플래그 |
| PDF 페이지에 여러 캐릭터가 함께 있음 | 중간 | 빌드 도구에서 `--split` 옵션으로 페이지를 N등분해 각각 처리. 또는 별도 이미지 편집 후 PNG로 입력 |
| 흑백이지만 회색 음영이 있음 | 낮음 | 임계값으로 이진화하면 보존 가능. 너무 강한 음영은 보호자가 사전 정리 |
| 썸네일에서 외곽선이 흐릿함 | 낮음 | 썸네일 생성 시 미세 외곽선 굵기 보강 (얇은 선이 축소되며 사라지는 문제 방지) |

## 10. 산출물 체크리스트

**앱**
- [ ] 빌드 가능한 Android Studio 프로젝트
- [ ] 4개 화면 (홈/카테고리/그림 선택/드로잉)
- [ ] 자유 드로잉 + Undo + Reset
- [ ] 자동 저장 + 재시작 시 복원
- [ ] content.json + assets 폴더로 콘텐츠 동적 로드

**콘텐츠 빌드 도구**
- [ ] `add_character.py` (단일 이미지)
- [ ] `add_pdf.py` (PDF 일괄)
- [ ] `requirements.txt`
- [ ] 도구 README.md
- [ ] content.json 백업/롤백
- [ ] ID 충돌 검사

**문서**
- [ ] 프로젝트 루트 README.md
- [ ] 도구 사용 예시 5개 이상
- [ ] 자녀 폰/태블릿에 설치하는 방법
