package com.arcao.geocaching4locus.base.selection

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.util.valueIterator
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.CopyOnWriteArraySet

class SelectionTracker<T : Parcelable>(
    private val adapter: SelectionAdapter<T>,
    private val stateKey: String = DEFAULT_STATE_KEY
) {
    private val selectedPositionMap = SparseArray<T>()
    private val selectionChangeListeners = CopyOnWriteArraySet<OnSelectionChangeListener>()

    val selectedValues: List<T>
        get() {
            return mutableListOf<T>().apply {
                for (value in selectedPositionMap.valueIterator()) {
                    add(value)
                }
            }
        }

    init {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                checkSelection()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                checkSelection()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                checkSelection()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                checkSelection()
            }
        })
    }

    fun onClick(position: Int, value: T) {
        if (selectedPositionMap.containsKey(position)) {
            selectedPositionMap.remove(position)
        } else {
            selectedPositionMap.put(position, value)
        }

        selectionChangeListeners.forEach { it(position, 1) }
    }

    fun isSelected(position: Int) = selectedPositionMap.containsKey(position)

    fun addSelectionChangeListener(listener: OnSelectionChangeListener) {
        selectionChangeListeners.add(listener)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putSparseParcelableArray(stateKey, selectedPositionMap)
    }

    fun onRestoreInstanceState(inState: Bundle?) {
        if (inState == null) return

        selectedPositionMap.clear()
        inState.getSparseParcelableArray<T>(stateKey)?.forEach { position, value ->
            selectedPositionMap[position] = value
        }
    }

    fun selectAll() {
        for (i in 0 until adapter.itemCount) {
            selectedPositionMap.append(i, adapter.getItem(i))
        }
        selectionChangeListeners.forEach { it(0, adapter.itemCount) }
    }

    fun selectNone() {
        selectedPositionMap.clear()
        selectionChangeListeners.forEach { it(0, adapter.itemCount) }
    }

    private fun checkSelection() {
        val oldSelectPositionMap = selectedPositionMap.clone()
        selectedPositionMap.clear()

        for (value in oldSelectPositionMap.valueIterator()) {
            val position = adapter.findPosition(value)
            if (position > RecyclerView.NO_POSITION) {
                selectedPositionMap.put(position, value)
            }
        }
    }

    companion object {
        private const val DEFAULT_STATE_KEY = "selection_tracker_state"
    }
}

typealias OnSelectionChangeListener = (startPosition: Int, count: Int) -> Unit

interface SelectionAdapter<T> {
    val itemCount: Int
    fun registerAdapterDataObserver(adapterDataObserver: RecyclerView.AdapterDataObserver)
    fun findPosition(value: T): Int
    fun getItem(position: Int): T?
}
