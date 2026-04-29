package com.stagegrowth.kidcanvas.data.asset

import android.content.Context
import android.util.Log
import com.stagegrowth.kidcanvas.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * assets/content.json 을 읽어 도메인 모델 List<Category> 로 변환한다.
 * 파일이 없거나 파싱 실패 시 빈 리스트 반환 — 앱이 죽지 않게 함.
 *
 * Spring 비유: ApplicationContext 의 ResourceLoader 로 classpath 내부 JSON 읽는 것과 비슷.
 */
@Singleton
class AssetContentLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun loadCategories(): List<Category> {
        return try {
            val text = context.assets.open(CONTENT_JSON_PATH)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            if (text.isBlank()) return emptyList()
            val root = json.decodeFromString(ContentRoot.serializer(), text)
            root.categories.map { it.toDomain() }
        } catch (e: Exception) {
            Log.w(TAG, "content.json 로드 실패 — 빈 리스트로 폴백", e)
            emptyList()
        }
    }

    companion object {
        private const val TAG = "AssetContentLoader"
        private const val CONTENT_JSON_PATH = "content.json"
    }
}
