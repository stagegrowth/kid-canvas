package com.stagegrowth.kidcanvas

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt DI 컨테이너 진입점.
 * Spring 비유: @SpringBootApplication 이 붙은 메인 클래스 + ApplicationContext 부트스트랩.
 */
@HiltAndroidApp
class KidCanvasApplication : Application()
