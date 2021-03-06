package renetik.android.view.extensions

import android.animation.Animator
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewPropertyAnimator
import renetik.android.base.CSApplicationObject.application
import renetik.android.view.adapter.CSAnimatorAdapter

val shortAnimationDuration =
    application.resources.getInteger(android.R.integer.config_shortAnimTime)

val mediumAnimationDuration =
    application.resources.getInteger(android.R.integer.config_mediumAnimTime)

fun <T : View> T.fadeIn(duration: Int = mediumAnimationDuration): ViewPropertyAnimator? {
    if (isVisible) return null
    visibility = VISIBLE
    alpha = 0f
    return animate().alpha(1f).setDuration(duration.toLong()).setListener(null)
}

fun <T : View> T.fadeOut(
    duration: Int = shortAnimationDuration, onDone: (() -> Unit)? = null): ViewPropertyAnimator? {
    when {
        isGone -> {
            onDone?.invoke()
            return null
        }
        alpha == 0f -> {
            hide()
            return null
        }
        else -> {
            isClickable = false
            return animate().alpha(0f).setDuration(duration.toLong())
                .setListener(object : CSAnimatorAdapter() {
                    override fun onAnimationEnd(animator: Animator?) {
                        isClickable = true
                        visibility = GONE
                        onDone?.invoke()
                    }
                })
        }
    }
}

fun <T : View> T.fadeVisible(fadeVisible: Boolean) = if (fadeVisible) fadeIn() else fadeOut()

fun <T : View> T.fadeGone(fadeGone: Boolean) = if (fadeGone) fadeOut() else fadeIn()

var <T : View> T.isFadeVisible
    get() = visibility == VISIBLE
    set(value) {
        fadeVisible(value)
    }

