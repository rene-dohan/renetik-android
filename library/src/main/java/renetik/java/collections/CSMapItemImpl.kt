package renetik.java.collections

class CSMapItemImpl<K, V>(private val map: Map<K, V>, private val key: K) : CSMapItem<K, V?> {

    override fun key(): K {
        return key
    }

    override fun value(): V? {
        return map[key]
    }
}