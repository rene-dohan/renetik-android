package renetik.android.view

import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.airbnb.paris.extensions.style
import renetik.android.R
import renetik.android.application
import renetik.android.java.collections.CSList
import renetik.android.java.collections.list
import renetik.android.lang.CSLang.*
import renetik.android.viewbase.CSViewController
import renetik.android.viewbase.menu.CSOnMenuItem

open class CSNavigationController(activity: AppCompatActivity)
    : CSViewController<FrameLayout>(activity) {

    open var controllers: CSList<CSViewController<*>> = list()

    init {
//        view = FrameLayout(ContextThemeWrapper(this.context(), R.style.CSNavigationContainer))
        view = FrameLayout(context()).apply { style(R.style.CSNavigationContainer) }
    }

    fun <T : View> push(controller: CSViewController<T>): CSViewController<T> {
        if (controllers.hasItems) controllers.last()?.setShowingInContainer(NO)
        controllers.put(controller)
        controller.view.startAnimation(loadAnimation(context(), R.anim.abc_slide_in_top))
        add(controller)
        controller.setShowingInContainer(YES)
        controller.initialize(state)
        updateBackButton()
        updateTitleButton()
        invalidateOptionsMenu()
        hideKeyboard()
        return controller
    }

    fun pop() {
        val controller = controllers.removeLast()
        controller.view.startAnimation(loadAnimation(context(), R.anim.abc_slide_out_top))
        controller.setShowingInContainer(NO)
        controller.onDeinitialize(state)
        removeView(controller)
        controller.onDestroy()
        controllers.last()?.setShowingInContainer(YES)
        updateBackButton()
        updateTitleButton()
        hideKeyboard()
    }

    fun <T : View> pushAsLast(controller: CSViewController<T>): CSViewController<T> {
        val lastController = controllers.removeLast()
        lastController.view.startAnimation(loadAnimation(context(), R.anim.abc_fade_out))
        lastController.setShowingInContainer(NO)
        lastController.onDeinitialize(state)
        removeView(lastController)
        lastController.onDestroy()

        controllers.put(controller)
        controller.view.startAnimation(loadAnimation(context(), R.anim.abc_fade_in))
        add(controller)
        controller.setShowingInContainer(YES)
        controller.initialize(state)
        updateBackButton()
        updateTitleButton()
        invalidateOptionsMenu()
        hideKeyboard()
        return controller
    }

    private fun updateTitleButton() {
        val title = (controllers.last() as? CSNavigationItem)?.navigationTitle()
        if (set(title)) actionBar.title = title
        else actionBar.title = application.name
    }

    private fun updateBackButton() {
        val isBackButtonVisible =
                (controllers.last() as? CSNavigationItem)?.isNavigationBackButtonVisible() ?: true
        if (controllers.count() > 1 && isBackButtonVisible) showBackButton()
        else hideBackButton()
    }

    private fun showBackButton() = actionBar.setDisplayHomeAsUpEnabled(YES)

    private fun hideBackButton() = actionBar.setDisplayHomeAsUpEnabled(NO)

    override fun onGoBack(): Boolean {
        if (controllers.count() > 1) {
            pop()
            return NO
        }
        return YES
    }

    override fun onOptionsItemSelected(event: CSOnMenuItem) {
        super.onOptionsItemSelected(event)
        if (event.consume(android.R.id.home)) goBack()
    }
}

interface CSNavigationItem {
    fun isNavigationBackButtonVisible(): Boolean {
        return YES
    }

    fun navigationTitle(): String? {
        return null
    }
}