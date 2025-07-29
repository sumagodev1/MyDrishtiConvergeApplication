package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.adapters.ChartDashboardAdapter
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.utils.NetworkUtils
import com.mydrishti.co.`in`.activities.utils.SessionManager
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModel
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModelFactory
import com.mydrishti.co.`in`.databinding.ActivityMainBinding
import com.mydrishti.co.`in`.activities.utils.ChartStateManager
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chartAdapter: ChartDashboardAdapter
    private lateinit var chartViewModel: ChartViewModel
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var emptyStateAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEmptyStateAnimation()
        setupToolbarAndDrawer()
        setupViewModel()  // Initialize ViewModel first
        setupRecyclerView() // Now RecyclerView can safely use chartViewModel
        setupAddChartButton()
        setupSwipeRefresh()
        setupDragGestureDetection()
        
        // Handle orientation changes by setting configuration change flags
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setupEmptyStateAnimation() {
        // Initialize the Lottie animation
        emptyStateAnimation = binding.contentMain.emptyStateLayout.findViewById(R.id.empty_dashboard_animation)
        emptyStateAnimation.setAnimation(R.raw.empty_dashboard_animation)
        emptyStateAnimation.repeatCount = -1 // Use -1 for infinite looping
        emptyStateAnimation.playAnimation()
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupViewModel() {
        // Get the chart repository
        val chartRepository = (application as? MyDrishtiApplication)?.chartRepository
            ?: throw IllegalStateException("ChartRepository not available")

        // Create the view model
        chartViewModel = ViewModelProvider(
            this,
            ChartViewModelFactory(chartRepository)
        )[ChartViewModel::class.java]

        // Observe chart configurations
        chartViewModel.getAllChartConfigs().observe(this) { charts ->
            // Update adapter with new chart configs
            chartAdapter.updateChartConfigs(charts)

            // Update empty state visibility and animation
            if (charts.isEmpty()) {
                binding.contentMain.emptyStateLayout.visibility = View.VISIBLE
                if (!emptyStateAnimation.isAnimating) {
                    emptyStateAnimation.playAnimation()
                }
            } else {
                binding.contentMain.emptyStateLayout.visibility = View.GONE
                if (emptyStateAnimation.isAnimating) {
                    emptyStateAnimation.pauseAnimation()
                }
            }
                
            // Ensure SwipeRefreshLayout is not refreshing when data is loaded
            binding.contentMain.swipeRefreshLayout.isRefreshing = false
        }
        
        // Observe chart data updates
        chartViewModel.chartDataUpdates.observe(this) { chartDataList ->
            // Update adapter with new chart data
            chartAdapter.updateChartData(chartDataList)
            
            // Ensure SwipeRefreshLayout is not refreshing when data is updated
            binding.contentMain.swipeRefreshLayout.isRefreshing = false
        }

        // Observe loading state
        chartViewModel.isLoading.observe(this) { isLoading ->
            binding.contentMain.swipeRefreshLayout.isRefreshing = isLoading
            
            // Add a safety timeout to stop the refreshing state
            if (isLoading) {
                binding.contentMain.swipeRefreshLayout.postDelayed({
                    binding.contentMain.swipeRefreshLayout.isRefreshing = false
                }, 8000) // Force stop refreshing after 8 seconds
            }
        }

        // Observe error events
        chartViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                chartViewModel.clearError()
                
                // Always ensure refresh is stopped on error
                binding.contentMain.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Initial data load
        chartViewModel.refreshAllChartData()
    }

    override fun onResume() {
        super.onResume()
        // Don't automatically refresh data when returning to activity
        // This prevents constant refreshing when the activity resumes
        // Users can manually refresh by pulling down the SwipeRefreshLayout
        
        // Comment out the line below to avoid constant refreshes
        // chartViewModel.refreshAllChartData()
    }

    private fun setupRecyclerView() {
        chartAdapter = ChartDashboardAdapter(
            this,
            mutableListOf(),
            onChartConfigClickListener = { chartConfig ->
                // Handle click on chart (e.g., show detail view)
                showChartDetailDialog(chartConfig)
            },
            onChartRefreshRequestListener = { chartId ->
                // Refresh data for a specific chart
                // Check if this is a gauge chart
                chartViewModel.getAllChartConfigs().value?.find { it.id == chartId.split("_")[0] }?.let { config ->
                    if (config.chartType == ChartType.GAUGE) {
                        // Use special gauge chart refreshing to avoid the 1.1 value issue
                        println("Using special gauge chart refresh for chart: ${config.title}")
                        chartViewModel.ensureGaugeChartDataFresh(chartId)
                    } else {
                        // Regular refresh for other chart types
                        chartViewModel.refreshChartData(chartId)
                    }
                } ?: run {
                    // Fallback if config not found
                    chartViewModel.refreshChartData(chartId)
                }
            },
            // Pass the edit chart handler directly
            onEditChartListener = { chartConfig ->
                editChart(chartConfig)
            },
            // Pass the delete chart handler directly
            onDeleteChartListener = { chartConfig ->
                deleteChart(chartConfig)
            }
        )

        binding.contentMain.chartsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chartAdapter
            
            // Support horizontal scrolling for wider content (metric charts with multiple parameters)
            isNestedScrollingEnabled = true
            overScrollMode = View.OVER_SCROLL_ALWAYS
            
            // Add item decoration for spacing between charts
            addItemDecoration(
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)
            )

            // Add drag-and-drop support for reordering charts
            val itemTouchHelper = ItemTouchHelper(
                ChartItemTouchHelperCallback(
                    chartViewModel, 
                    chartAdapter, 
                    binding.contentMain.swipeRefreshLayout
                )
            )
            itemTouchHelper.attachToRecyclerView(this)
            
            // Improve drag detection with long press
            setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    // Temporarily disable swipe refresh during potential drag operations
                    binding.contentMain.swipeRefreshLayout.isEnabled = false
                } else if (event.action == android.view.MotionEvent.ACTION_UP || 
                           event.action == android.view.MotionEvent.ACTION_CANCEL) {
                    // Re-enable swipe refresh when touch is released
                    binding.contentMain.swipeRefreshLayout.isEnabled = true
                }
                false  // Don't consume the event, let it propagate
            }
        }
    }

    private fun setupAddChartButton() {
        binding.contentMain.fabAddChart.setOnClickListener {
            showChartTypeSelectionDialog()
        }
    }

    private fun setupSwipeRefresh() {
        // Make it less sensitive to prevent interference with drag operations
        binding.contentMain.swipeRefreshLayout.setDistanceToTriggerSync(100)
        
        binding.contentMain.swipeRefreshLayout.setOnRefreshListener {
            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection. Please connect to the internet.", Toast.LENGTH_LONG).show()
                binding.contentMain.swipeRefreshLayout.isRefreshing = false
                return@setOnRefreshListener
            }
            // Clear saved month/date selections to force charts to load latest data
            ChartStateManager.clearAllSelections()
            // Refresh all chart data
            chartViewModel.refreshAllChartData()
        }
    }

    private fun setupDragGestureDetection() {
        // Custom touch listener to detect vertical drags on RecyclerView items
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // If vertical scrolling distance is significant and greater than horizontal,
                // this might be a drag operation on a chart item
                if (abs(distanceY) > 20 && abs(distanceY) > abs(distanceX) * 1.5) {
                    // If moving vertically more than horizontally, this could be a drag
                    // Find if we're touching a chart item
                    binding.contentMain.chartsRecyclerView.findChildViewUnder(e1?.x ?: 0f, e1?.y ?: 0f)?.let {
                        // We're over a chart item, so temporarily disable pull-to-refresh
                        binding.contentMain.swipeRefreshLayout.isEnabled = false
                        return true
                    }
                }
                return false
            }
        })

        // Apply the detector to the RecyclerView
        binding.contentMain.chartsRecyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                // Re-enable pull-to-refresh when touch is released
                binding.contentMain.swipeRefreshLayout.isEnabled = true
            }
            
            // Let the gesture detector process the event
            gestureDetector.onTouchEvent(event)
            
            // Don't consume the event
            false
        }
    }

    private fun showChartTypeSelectionDialog() {
        ChartTypeSelectionDialog(this) { chartType ->
            // Navigate to site selection
            navigateToSiteSelection(chartType)
        }.show()
    }

    private fun navigateToSiteSelection(chartType: ChartType) {
        val intent = Intent(this, SiteSelectionActivity::class.java).apply {
            putExtra(SiteSelectionActivity.EXTRA_CHART_TYPE, chartType.name)
        }
        startActivity(intent)
    }

    private fun showChartDetailDialog(chartConfig: ChartConfig) {
        // Show a dialog with detailed view of the chart
        // You can expand this with your specific requirements
        Toast.makeText(this, "Showing detail for: ${chartConfig.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showChartOptionsMenu(chartConfig: ChartConfig, position: Int) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Chart Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editChart(chartConfig)
                    1 -> deleteChart(chartConfig)
                }
            }
            .show()
    }

    private fun editChart(chartConfig: ChartConfig) {
        // Debug info - print chart ID information
        println("EDIT CHART DEBUG - Chart ID (original): ${chartConfig.id}")
        println("EDIT CHART DEBUG - Chart ID (toLongOrNull): ${chartConfig.id.toLongOrNull()}")
        println("EDIT CHART DEBUG - Chart deviceId: ${chartConfig.deviceId}")
        println("EDIT CHART DEBUG - Chart title: ${chartConfig.title}")
        
        // Based on chart type, navigate to appropriate edit screen
        when (chartConfig.chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> {
                // Navigate to bar chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId.toIntOrNull() ?: -1)
                    putExtra(ChartParametersActivity.EXTRA_SITE_NAME, chartConfig.deviceName)
                    
                    // Use the original string ID directly instead of converting to Long
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
                    println("EDIT CHART DEBUG - Added chart ID to intent: ${chartConfig.id}")
                }
                startActivity(intent)
            }
            ChartType.GAUGE -> {
                // Navigate to gauge chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId.toIntOrNull() ?: -1)
                    putExtra(ChartParametersActivity.EXTRA_SITE_NAME, chartConfig.deviceName)
                    
                    // Use the original string ID directly
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
                    println("EDIT CHART DEBUG - Added chart ID to intent: ${chartConfig.id}")
                }
                startActivity(intent)
            }
            ChartType.METRIC -> {
                // Navigate to metric chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId.toIntOrNull() ?: -1)
                    putExtra(ChartParametersActivity.EXTRA_SITE_NAME, chartConfig.deviceName)
                    
                    // Use the original string ID directly
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
                    println("EDIT CHART DEBUG - Added chart ID to intent: ${chartConfig.id}")
                }
                startActivity(intent)
            }
        }
    }

    private fun deleteChart(chartConfig: ChartConfig) {
        AlertDialog.Builder(this)
            .setTitle("Delete Chart")
            .setMessage("Are you sure you want to delete this chart?")
            .setPositiveButton("Delete") { _, _ ->
                chartViewModel.deleteChart(chartConfig,
                    onSuccess = {
                        // Create centered success snackbar
                        val snackbar = Snackbar.make(binding.contentMain.chartsRecyclerView, "Chart deleted successfully", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                // When undoing deletion, ensure we use the base chart ID
                                val baseChartId = chartConfig.id.split("_")[0]
                                val chartToRestore = chartConfig.copy(id = baseChartId)
                                println("MainActivity: Restoring chart with base ID: $baseChartId (original: ${chartConfig.id})")
                                
                                // Clear cached state to prevent TextView/chart data mismatch
                                ChartStateManager.clearChartState(baseChartId)
                                println("MainActivity: Cleared cached state for restored chart $baseChartId")
                                
                                // Insert the chart and refresh UI
                                chartViewModel.insertChart(chartToRestore)
                                
                                // Refresh the chart data to ensure UI updates immediately
                                chartViewModel.refreshAllChartData()
                                
                                // Force refresh the adapter UI components after a short delay
                                // This ensures month selectors and other UI elements are properly reinitialized
                                binding.contentMain.swipeRefreshLayout.postDelayed({
                                    chartAdapter.notifyDataSetChanged()
                                    println("MainActivity: Forced adapter refresh after undo to update UI components")
                                }, 300)
                                
                                // Show a brief confirmation that the chart was restored
                                binding.contentMain.swipeRefreshLayout.postDelayed({
                                    Snackbar.make(binding.contentMain.chartsRecyclerView, "Chart restored successfully", Snackbar.LENGTH_SHORT).show()
                                }, 800) // Increased delay to ensure the chart and UI refresh complete first
                            }

                        // Center the snackbar
                        val view = snackbar.view
                        val params = view.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                        params.gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM
                        params.setMargins(50, 0, 50, 100) // Add some margin from edges and bottom
                        view.layoutParams = params

                        snackbar.show()
                    },
                    onError = { errorMessage ->
                        // Show error snackbar
                        val errorSnackbar = Snackbar.make(binding.contentMain.chartsRecyclerView, errorMessage, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getColor(android.R.color.holo_red_dark))

                        // Center the error snackbar
                        val errorView = errorSnackbar.view
                        val errorParams = errorView.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                        errorParams.gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM
                        errorParams.setMargins(50, 0, 50, 100)
                        errorView.layoutParams = errorParams

                        errorSnackbar.show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                // Confirm and logout
                showLogoutConfirmationDialog()
            }
        }

        // Close the drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Clear login credentials but keep chart data
                val authManager = SessionManager.getInstance()
                authManager.logout()

                // Navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        // Only clear chart state if application is really finishing, not just changing configuration
        if (isFinishing) {
            // Clear chart selections when exiting app
            ChartStateManager.clearAllSelections()
            println("ROTATION: Cleared all chart selections on app exit")
        }
        super.onDestroy()
    }
    
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Notify adapter about configuration change to update layouts
        if (::chartAdapter.isInitialized) {
            println("Configuration changed, notifying adapter to update layouts")
            chartAdapter.onConfigurationChanged()
        }
    }
    

    // Add this function to launch the gauge showcase
    private fun launchGaugeShowcase() {
        val intent = Intent(this, GaugeShowcaseActivity::class.java)
        startActivity(intent)
    }
}