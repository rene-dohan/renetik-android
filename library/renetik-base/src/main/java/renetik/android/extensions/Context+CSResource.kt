package renetik.android.extensions

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import renetik.android.java.common.*
import renetik.android.java.extensions.collections.list
import renetik.android.java.extensions.isEmpty
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

@ColorInt
fun Context.resourceColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.resourceBytes(id: Int) = catchAllWarn {
    val stream = resources.openRawResource(id)
    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(4 * 1024)
    tryAndFinally({
        var read: Int
        do {
            read = stream.read(buffer, 0, buffer.size)
            if (read == -1) break
            outputStream.write(buffer, 0, read)
        } while (true)
        outputStream.toByteArray()
    }) { stream.close() }
}

fun Context.openInputStream(uri: Uri) = catchErrorReturnNull<InputStream, FileNotFoundException> {
    return contentResolver.openInputStream(uri)
}

fun Context.resourceDimension(id: Int) = resources.getDimension(id).toInt()

fun Context.resourceStrings(id: Int) = catchWarnReturnNull<List<String>, NotFoundException> {
    list(*resources.getStringArray(id))
}

fun Context.resourceInts(id: Int) = catchError<NotFoundException> {
    list(resources.getIntArray(id).asList())
}

val Context.displayMetrics get():DisplayMetrics = resources.displayMetrics

fun Context.toDpF(pixel: Int) = pixel / (displayMetrics.densityDpi / 160f)
fun Context.toDp(pixel: Int) = toDpF(pixel).toInt()

fun Context.toPixelF(dp: Float) = (dp * (displayMetrics.densityDpi / 160f))
fun Context.toPixelF(dp: Int) = toPixelF(dp.toFloat())
fun Context.toPixel(dp: Float) = toPixelF(dp).toInt()
fun Context.toPixel(dp: Int) = toPixelF(dp.toFloat()).toInt()

private fun Context.resolveAttribute(attribute: Int) =
    TypedValue().apply { theme.resolveAttribute(attribute, this, true) }

@ColorInt
fun Context.colorFromAttribute(attribute: Int) = resolveAttribute(attribute).data.apply {
    if (isEmpty) throw NotFoundException()
}

fun Context.dimensionFromAttribute(attribute: Int): Int {
    val attributes = obtainStyledAttributes(intArrayOf(attribute))
    val dimension = attributes.getDimensionPixelSize(0, 0)
    attributes.recycle()
    return dimension
}

fun Context.stringFromAttribute(attribute: Int): String {
    val string = resolveAttribute(attribute).string
    val name = string as? CSName  //TODO fix this
    return name?.name ?: string?.toString() ?: ""
}

fun Context.stringFromAttribute2(attribute: Int) = stringFromAttribute(intArrayOf(attribute), 0)

fun Context.stringFromAttribute(styleable: IntArray, styleableAttribute: Int): String {
    val attributes = obtainStyledAttributes(styleable)
    val string = attributes.getString(styleableAttribute)
    attributes.recycle()
    val name = string as? CSName  //TODO fix this
    return name?.name ?: string?.toString() ?: ""
}

fun Context.resourceFromAttribute(attribute: Int) = resolveAttribute(attribute).resourceId.apply {
    if (isEmpty) throw NotFoundException()
}

fun Context.openAsset(path: String): InputStream {
    return assets.open(path)
}

//fun Context.assetsReadText(path: String) = resources.assets.open(path).readText()

fun Context.assetsReadText(path: String): String {
    return openAsset(path).bufferedReader().use { it.readText() }
}

val Context.isPortrait get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
val Context.isLandscape get() = !isPortrait

