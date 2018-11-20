package renetik.android.sample.model

import renetik.android.model.CSApplication
import renetik.android.lang.AndroidLogger
import renetik.android.sample.BuildConfig.DEBUG

val model by lazy { SampleModel() }

class SampleApplication : CSApplication() {
    override val name: String by lazy { "Renetik Library Example" }
    override val isDebugBuild = DEBUG
    override val logger by lazy { AndroidLogger() }
}