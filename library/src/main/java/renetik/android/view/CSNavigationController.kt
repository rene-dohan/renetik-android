package renetik.android.view

import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.FrameLayout
import com.airbnb.paris.extensions.style
import renetik.android.R
import renetik.android.model.application
import renetik.java.extensions.notNull
import renetik.java.extensions.primitives.set
import renetik.android.extensions.view.add
import renetik.android.extensions.view.remove
import renetik.java.collections.CSList
import renetik.java.collections.list
import renetik.android.view.base.CSActivity
import renetik.android.view.base.CSViewController
import renetik.android.view.menu.CSOnMenuItem
import renetik.java.lang.CSLang

open class CSNavigationController(activity: CSActivity)
    : CSViewController<FrameLayout>(activity) {

    open var controllers: CSList<CSViewController<*>> = list()

    override fun createView() = FrameLayout(this).apply { style(R.style.CSNavigationContainer) }

    fun <T : View> push(controller: CSViewController<T>): CSViewController<T> {
        if (controllers.hasItems) controllers.last()?.showingInContainer(CSLang.NO)
        controllers.put(controller)
        controller.view.startAnimation(loadAnimation(this, R.anim.abc_slide_in_top))
        view.add(controller)
        controller.showingInContainer(CSLang.YES)
        controller.initialize(state)
        updateBackButton()
        updateTitleButton()
        invalidateOptionsMenu()
        hideKeyboard()
        return controller
    }

    fun pop() {
        controllers.deleteLast().notNull { lastController ->
            lastController.view.startAnimation(loadAnimation(this, R.anim.abc_slide_out_top))
            lastController.showingInContainer(CSLang.NO)
            view.remove(lastController)
            lastController.onDeinitialize()

            controllers.last()?.showingInContainer(CSLang.YES)
            updateBackButton()
            updateTitleButton()
            hideKeyboard()
        }
    }

    fun <T : View> pushAsLast(controller: CSViewController<T>): CSViewController<T> {
        controllers.deleteLast().notNull { lastController ->
            lastController.view.startAnimation(loadAnimation(this, R.anim.abc_fade_out))
            lastController.showingInContainer(CSLang.NO)
            view.remove(lastController)
            lastController.onDeinitialize()
        }

        controllers.put(controller)
        controller.view.startAnimation(loadAnimation(this, R.anim.abc_fade_in))
        view.add(controller)
        controller.showingInContainer(CSLang.YES)
        controller.initialize(state)
        updateBackButton()
        updateTitleButton()
        invalidateOptionsMenu()
        hideKeyboard()
        return controller
    }

    private fun updateTitleButton() {
        val title = (controllers.last() as? CSNavigationItem)?.navigationItemTitle
        if (title.set) actionBar?.title = title
        else actionBar?.title = application.name
    }

    private fun updateBackButton() {
        val isBackButtonVisible =
                (controllers.last() as? CSNavigationItem)?.isNavigationItemBackButton ?: true
        if (controllers.size > 1 && isBackButtonVisible) showBackButton()
        else hideBackButton()
    }

    private fun showBackButton() = actionBar?.setDisplayHomeAsUpEnabled(CSLang.YES)

    private fun hideBackButton() = actionBar?.setDisplayHomeAsUpEnabled(CSLang.NO)

    override fun onGoBack(): Boolean {
        if (controllers.size > 1) {
            pop()
            return CSLang.NO
        }
        return CSLang.YES
    }

    override fun onOptionsItemSelected(onItem: CSOnMenuItem) {
        super.onOptionsItemSelected(onItem)
        if (onItem.consume(android.R.id.home)) goBack()
    }
}

interface CSNavigationItem {
    val isNavigationItemBackButton get() = CSLang.YES
    val navigationItemTitle: String? get() = null
}
