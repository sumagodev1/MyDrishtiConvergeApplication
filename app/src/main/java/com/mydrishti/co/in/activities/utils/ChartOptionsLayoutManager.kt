package com.mydrishti.co.`in`.activities.utils

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

/**
 * Utility class for managing chart options alignment and layout
 * Fixes chart options synchronization and alignment issues
 */
object ChartOptionsLayoutManager {

    private const val TAG = "ChartOptionsLayoutManager"

    /**
     * Align chart option views properly within a container
     * @param container The container holding the chart options
     * @param optionViews List of option views to align
     * @param orientation Current screen orientation
     */
    fun alignChartOptions(
        container: ViewGroup,
        optionViews: List<View>,
        orientation: Int = Configuration.ORIENTATION_PORTRAIT
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                when (container) {
                    is ConstraintLayout -> alignInConstraintLayout(container, optionViews, orientation)
                    is LinearLayout -> alignInLinearLayout(container, optionViews, orientation)
                    else -> alignInGenericLayout(container, optionViews, orientation)
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error aligning chart options", exception)
            }
        )
    }

    /**
     * Align options in a ConstraintLayout
     */
    private fun alignInConstraintLayout(
        container: ConstraintLayout,
        optionViews: List<View>,
        orientation: Int
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                val constraintSet = ConstraintSet()
                constraintSet.clone(container)

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // Landscape: arrange horizontally with equal spacing
                    alignHorizontallyInConstraintLayout(constraintSet, optionViews, container)
                } else {
                    // Portrait: arrange vertically or in a compact horizontal layout
                    alignVerticallyInConstraintLayout(constraintSet, optionViews, container)
                }

                constraintSet.applyTo(container)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error aligning in ConstraintLayout", exception)
            }
        )
    }

    /**
     * Align options horizontally in ConstraintLayout
     */
    private fun alignHorizontallyInConstraintLayout(
        constraintSet: ConstraintSet,
        optionViews: List<View>,
        container: ConstraintLayout
    ) {
        if (optionViews.isEmpty()) return

        val spacing = container.context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4

        optionViews.forEachIndexed { index, view ->
            when (index) {
                0 -> {
                    // First view: align to start of parent
                    constraintSet.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, spacing)
                    constraintSet.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, spacing)
                    constraintSet.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, spacing)
                }
                optionViews.size - 1 -> {
                    // Last view: align to end of parent
                    constraintSet.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, spacing)
                    constraintSet.connect(view.id, ConstraintSet.START, optionViews[index - 1].id, ConstraintSet.END, spacing)
                    constraintSet.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, spacing)
                    constraintSet.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, spacing)
                }
                else -> {
                    // Middle views: chain between previous and next
                    constraintSet.connect(view.id, ConstraintSet.START, optionViews[index - 1].id, ConstraintSet.END, spacing)
                    constraintSet.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, spacing)
                    constraintSet.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, spacing)
                }
            }
        }

        // Create horizontal chain for equal distribution
        val viewIds = optionViews.map { it.id }.toIntArray()
        constraintSet.createHorizontalChain(
            ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
            viewIds, null, ConstraintSet.CHAIN_SPREAD
        )
    }

    /**
     * Align options vertically in ConstraintLayout
     */
    private fun alignVerticallyInConstraintLayout(
        constraintSet: ConstraintSet,
        optionViews: List<View>,
        container: ConstraintLayout
    ) {
        if (optionViews.isEmpty()) return

        val spacing = container.context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6

        optionViews.forEachIndexed { index, view ->
            when (index) {
                0 -> {
                    // First view: align to top of parent
                    constraintSet.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, spacing)
                    constraintSet.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, spacing)
                    constraintSet.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, spacing)
                }
                optionViews.size - 1 -> {
                    // Last view: align to bottom of parent
                    constraintSet.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, spacing)
                    constraintSet.connect(view.id, ConstraintSet.TOP, optionViews[index - 1].id, ConstraintSet.BOTTOM, spacing)
                    constraintSet.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, spacing)
                    constraintSet.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, spacing)
                }
                else -> {
                    // Middle views: chain between previous and next
                    constraintSet.connect(view.id, ConstraintSet.TOP, optionViews[index - 1].id, ConstraintSet.BOTTOM, spacing)
                    constraintSet.connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, spacing)
                    constraintSet.connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, spacing)
                }
            }
        }
    }

    /**
     * Align options in a LinearLayout
     */
    private fun alignInLinearLayout(
        container: LinearLayout,
        optionViews: List<View>,
        orientation: Int
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                // Set appropriate orientation
                container.orientation = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    LinearLayout.HORIZONTAL
                } else {
                    LinearLayout.VERTICAL
                }

                // Set equal weight for all views
                val weight = 1f / optionViews.size
                val spacing = container.context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4

                optionViews.forEach { view ->
                    val layoutParams = view.layoutParams as? LinearLayout.LayoutParams
                        ?: LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                    layoutParams.weight = weight
                    layoutParams.setMargins(spacing, spacing, spacing, spacing)

                    if (container.orientation == LinearLayout.HORIZONTAL) {
                        layoutParams.width = 0
                        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                    } else {
                        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                        layoutParams.height = 0
                    }

                    view.layoutParams = layoutParams
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error aligning in LinearLayout", exception)
            }
        )
    }

    /**
     * Align options in a generic ViewGroup
     */
    private fun alignInGenericLayout(
        container: ViewGroup,
        optionViews: List<View>,
        orientation: Int
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                // For generic layouts, just ensure proper margins and sizing
                val spacing = container.context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4

                optionViews.forEach { view ->
                    val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                        ?: ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                    layoutParams.setMargins(spacing, spacing, spacing, spacing)
                    view.layoutParams = layoutParams
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error aligning in generic layout", exception)
            }
        )
    }

    /**
     * Synchronize multiple chart option containers
     * @param containers List of containers to synchronize
     * @param context Context for resource access
     */
    fun synchronizeChartOptions(containers: List<ViewGroup>, context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                if (containers.isEmpty()) return@safeExecute

                // Find the maximum height among all containers
                var maxHeight = 0
                containers.forEach { container ->
                    container.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    maxHeight = maxOf(maxHeight, container.measuredHeight)
                }

                // Apply the maximum height to all containers
                containers.forEach { container ->
                    val layoutParams = container.layoutParams
                    layoutParams.height = maxHeight
                    container.layoutParams = layoutParams
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error synchronizing chart options", exception)
            }
        )
    }

    /**
     * Apply responsive design patterns for different screen sizes
     * @param container The container to make responsive
     * @param optionViews List of option views
     * @param context Context for resource access
     */
    fun applyResponsiveDesign(
        container: ViewGroup,
        optionViews: List<View>,
        context: Context
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                val configuration = context.resources.configuration
                val screenWidthDp = configuration.screenWidthDp
                val screenHeightDp = configuration.screenHeightDp
                val orientation = configuration.orientation

                // Determine layout strategy based on screen size
                when {
                    screenWidthDp >= 600 -> {
                        // Tablet or large screen: use horizontal layout with more spacing
                        applyTabletLayout(container, optionViews, context)
                    }
                    orientation == Configuration.ORIENTATION_LANDSCAPE -> {
                        // Phone landscape: compact horizontal layout
                        applyPhoneLandscapeLayout(container, optionViews, context)
                    }
                    else -> {
                        // Phone portrait: vertical or compact layout
                        applyPhonePortraitLayout(container, optionViews, context)
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error applying responsive design", exception)
            }
        )
    }

    /**
     * Apply tablet-specific layout
     */
    private fun applyTabletLayout(container: ViewGroup, optionViews: List<View>, context: Context) {
        val spacing = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2
        
        optionViews.forEach { view ->
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            
            layoutParams.setMargins(spacing, spacing, spacing, spacing)
            view.layoutParams = layoutParams
        }
    }

    /**
     * Apply phone landscape layout
     */
    private fun applyPhoneLandscapeLayout(container: ViewGroup, optionViews: List<View>, context: Context) {
        val spacing = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6
        
        optionViews.forEach { view ->
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            
            layoutParams.setMargins(spacing, spacing, spacing, spacing)
            view.layoutParams = layoutParams
        }
    }

    /**
     * Apply phone portrait layout
     */
    private fun applyPhonePortraitLayout(container: ViewGroup, optionViews: List<View>, context: Context) {
        val spacing = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4
        
        optionViews.forEach { view ->
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                ?: ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            
            layoutParams.setMargins(spacing, spacing, spacing, spacing)
            view.layoutParams = layoutParams
        }
    }

    /**
     * Ensure consistent spacing between chart option elements
     * @param optionViews List of option views to space consistently
     * @param context Context for resource access
     */
    fun ensureConsistentSpacing(optionViews: List<View>, context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                val standardSpacing = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4
                
                optionViews.forEach { view ->
                    val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                    if (layoutParams != null) {
                        layoutParams.setMargins(standardSpacing, standardSpacing, standardSpacing, standardSpacing)
                        view.layoutParams = layoutParams
                    }
                    
                    // Ensure minimum touch target size
                    val minTouchTarget = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
                    if (view.minimumWidth < minTouchTarget) {
                        view.minimumWidth = minTouchTarget
                    }
                    if (view.minimumHeight < minTouchTarget) {
                        view.minimumHeight = minTouchTarget
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error ensuring consistent spacing", exception)
            }
        )
    }
}