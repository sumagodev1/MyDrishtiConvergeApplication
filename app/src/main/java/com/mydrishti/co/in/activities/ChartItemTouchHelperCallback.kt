package com.mydrishti.co.`in`.activities

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mydrishti.co.`in`.activities.adapters.ChartDashboardAdapter
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModel

/**
 * ItemTouchHelper.Callback implementation for handling drag-and-drop reordering of chart widgets
 */
class ChartItemTouchHelperCallback(
    private val viewModel: ChartViewModel,
    private val adapter: ChartDashboardAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = 0 // No swiping
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        
        // Update adapter with new positions
        adapter.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used as we don't support swiping
    }
    
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
    
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }
    
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        // When drag is complete, update positions in the database
        val updatedCharts = adapter.getChartConfigs()
        viewModel.updateChartPositions(updatedCharts)
    }
}