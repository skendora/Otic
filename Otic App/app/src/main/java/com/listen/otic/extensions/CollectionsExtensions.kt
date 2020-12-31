package com.listen.otic.extensions

fun <T> List<T>?.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (this == null) {
        return emptyList()
    }
    return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}

fun <T> List<T>.equalsBy(other: List<T>, by: (left: T, right: T) -> Boolean): Boolean {
    if (this.size != other.size) {
        return false
    }
    for ((index, item) in withIndex()) {
        val otherItem = other[index]
        val itemsEqual = by(item, otherItem)
        if (!itemsEqual) {
            return false
        }
    }
    return true
}
