package com.mydrishti.co.`in`.activities

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mydrishti.co.`in`.activities.adapters.ChartDashboardAdapter
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModel

/**
 * Helper class to handle drag and drop reordering of charts
 */
class ChartItemTouchHelperCallback(
    private val viewModel: ChartViewModel,
    private val adapter: ChartDashboardAdapter,
    private val swipeRefreshLayout: SwipeRefreshLayout? = null
) : ItemTouchHelper.Callback() {

    private var isDragging = false
    private var pendingDragUpdate = false
    private var currentTargetPos = RecyclerView.NO_POSITION
    
    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
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
        
        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
            return false
        }
        
        // Track current position
        currentTargetPos = toPosition
        
        // Move item in adapter (visual update)
        adapter.moveChart(fromPosition, toPosition)
        
        // Mark that we need to update positions in DB when drag ends
        pendingDragUpdate = true
        
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used as swiping is disabled
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        isDragging = false
        
        // After drag completes, ensure positions are updated in DB if needed
        if (pendingDragUpdate) {
            pendingDragUpdate = false
            viewModel.updateChartPositions(adapter.getCharts())
            
            // Use post() to safely call notifyDataSetChanged after the current layout pass
            recyclerView.post {
                // Force a full notify of data changes to ensure proper layout
                adapter.notifyDataSetChanged()
            }
        }
        
        // Reset tracking variables
        currentTargetPos = RecyclerView.NO_POSITION
        
        // Re-enable the SwipeRefreshLayout when drag is finished
        swipeRefreshLayout?.isEnabled = true
    }
    
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isDragging = true
            
            // When item is being dragged, disable the SwipeRefreshLayout
            swipeRefreshLayout?.isEnabled = false
        }
    }
    
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // Add elevation to dragged item to show it above other items
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive) {
            viewHolder.itemView.elevation = 20f
        } else {
            viewHolder.itemView.elevation = 0f
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}