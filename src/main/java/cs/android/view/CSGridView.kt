package cs.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.makeMeasureSpec
import android.widget.GridView
import cs.java.lang.CSLang.NO
import java.lang.Integer.MAX_VALUE

class CSGridView : GridView {

    var autoResizeHeight = NO;

    private val attrs: AttributeSet?

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.attrs = attrs
    }

    constructor(context: Context) : super(context) {
        this.attrs = null
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle) {
        this.attrs = attrs
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (autoResizeHeight) {
            super.onMeasure(widthMeasureSpec, makeMeasureSpec(MAX_VALUE shr 2, AT_MOST))
        } else super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}