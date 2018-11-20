package renetik.android.view.pager

import android.view.View
import androidx.viewpager.widget.ViewPager
import renetik.java.extensions.isEmpty
import renetik.android.extensions.view.visible
import renetik.java.collections.CSList
import renetik.java.collections.list
import renetik.android.lang.doLater
import renetik.android.view.adapter.CSOnPageSelected
import renetik.android.view.base.CSViewController
import renetik.java.lang.CSLang

class CSPagerController<PageType>(parent: CSViewController<*>, pagerId: Int)
    : CSViewController<ViewPager>(parent, pagerId)
        where PageType : CSViewController<*>, PageType : CSPagerPage {

    val controllers = list<PageType>()
    var currentIndex = 0
    private var emptyView: View? = null

    constructor(parent: CSViewController<*>, pagerId: Int, pages: CSList<PageType>)
            : this(parent, pagerId) {
        controllers.putAll(pages)
    }

    fun emptyView(view: View) = apply { emptyView = view.visible(controllers.isEmpty) }

    fun reload(pages: CSList<PageType>) = apply {
        val currentIndex = view.currentItem
        for (page in controllers) page.onDeinitialize()
        controllers.reload(pages)
        updatePageVisibility(if (pages.size > currentIndex) currentIndex else 0)
        for (page in pages) page.initialize(state)
        view.setCurrentItem(currentIndex, CSLang.YES)
        updateView()
    }

    override fun onCreate() {
        super.onCreate()
        CSOnPagerPageChange(this)
                .onDragged { index -> controllers[index].showingInContainer(CSLang.YES) }
                .onReleased { index -> if (currentIndex != index) controllers[index].showingInContainer(CSLang.NO) }
        view.addOnPageChangeListener(
                CSOnPageSelected { index -> doLater(100) { updatePageVisibility(index) } })
        updateView()
    }

    private fun updateView() {
        view.adapter = CSPagerAdapter(controllers)
        view.visible(controllers.hasItems)
        emptyView?.visible(controllers.isEmpty())
    }

    private fun updatePageVisibility(newIndex: Int) {
        if (currentIndex == newIndex) return
        currentIndex = newIndex
        for (index in 0 until controllers.size)
            controllers[index].showingInContainer(if (index == currentIndex) CSLang.YES else CSLang.NO)
    }

    fun currentController() = controllers.at(currentIndex)

    fun setCurrentIndex(index: Int) = apply { view.currentItem = index }
}
