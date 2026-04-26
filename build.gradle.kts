// 프로젝트 루트 빌드 스크립트
// Spring 비유: 부모 POM처럼 모든 모듈에 적용 가능한 플러그인을 선언만 (apply false)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
