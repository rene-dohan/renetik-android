package renetik.java.lang

import com.google.android.gms.common.internal.Objects.equal
import renetik.java.collections.CSList

fun <T : CSId> CSValues<T>.findById(id: String): T? {
    for (hasId in values) if (equal(hasId.id(), id)) return hasId
    return null
}

fun <T : CSId> CSValues<T>.findById(hasId: CSId): T? {
    return findById(hasId.id())
}

fun <T : CSId> CSValues<T>.findIndexById(listData: CSValues<T>, hasId: CSId): Int {
    return findIndexById(hasId.id())
}

fun <T : CSId> CSValues<T>.findIndexById(id: String): Int {
    for (index in 0 until values.size)
        if (equal(values[index].id(), id))
            return index
    return -1
}

interface CSValues<T> {
    val values: CSList<T>
}
