package com.mydrishti.co.`in`.activities

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mydrishti.co.`in`.activities.adapters.ChartDashboardAdapter
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModel

/**
 * Helper class to handle drag and drop reordering of charts
 */
class ChartItemTouchHelperCallback(
    private val viewModel: ChartViewModel,
    private val adapter: ChartDashboardAdapter
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Get current positions
        val fromPosition = source.adapterPosition
        val toPosition = target.adapterPosition

        // Move item in adapter (visual update)
        adapter.moveChart(fromPosition, toPosition)

        // Update positions in database
        viewModel.updateChartPositions(adapter.getCharts())
        
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used as swiping is disabled
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        // After drag completes, ensure positions are updated
        viewModel.updateChartPositions(adapter.getCharts())
    }
}