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
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.adapters.ChartDashboardAdapter
import com.mydrishti.co.`in`.activities.api.ApiClient
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.dao.ParameterDao
import com.mydrishti.co.`in`.activities.database.AppDatabase
import com.mydrishti.co.`in`.activities.dialogs.ChartTypeSelectionDialog
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import com.mydrishti.co.`in`.activities.utils.NetworkUtils
import com.mydrishti.co.`in`.activities.utils.SessionManager
import com.mydrishti.co.`in`.activities.viewmodels.ChartViewModel
import com.mydrishti.co.`in`.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chartAdapter: ChartDashboardAdapter
    private lateinit var chartViewModel: ChartViewModel
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupViewModel()  // Initialize ViewModel first
        setupRecyclerView() // Now RecyclerView can safely use chartViewModel
        setupAddChartButton()
        setupSwipeRefresh()
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
        val apiService = ApiClient.getApiService()
        val chartDao = AppDatabase.getDatabase(this).chartDao()
        val parameterDao = AppDatabase.getDatabase(this).parameterDao()
        val sessionManager = SessionManager.getInstance(this)

        val chartRepository = ChartRepository(
            chartDao,
            parameterDao,
            apiService,
            sessionManager
        )
        chartViewModel = ViewModelProvider(
            this,
            ChartViewModel.Factory(chartRepository)
        )[ChartViewModel::class.java]

        // Observe chart configurations
        chartViewModel.getAllChartConfigs().observe(this) { charts ->
            // Update adapter with new data
            chartAdapter.updateCharts(charts)

            // Update empty state visibility
            binding.contentMain.emptyStateLayout.visibility =
                if (charts.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe loading state
        chartViewModel.isLoading.observe(this) { isLoading ->
            binding.contentMain.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observe error events
        chartViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                chartViewModel.clearError()
            }
        }

        // Refresh all chart data when app starts
        chartViewModel.refreshAllChartData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh chart data when returning to this activity
        chartViewModel.refreshAllChartData()
    }

    private fun setupRecyclerView() {
        chartAdapter = ChartDashboardAdapter(
            this,
            mutableListOf(),
            onChartConfigClickListener = { chartConfig ->
                // Handle click on chart (e.g., show detail view)
                showChartDetailDialog(chartConfig)
            },
            onChartConfigLongClickListener = { chartConfig, position ->
                // Handle long press (e.g., show edit/delete options)
                showChartOptionsMenu(chartConfig, position)
                true
            }
        )

        binding.contentMain.chartsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chartAdapter
            // Add item decoration for spacing between charts
            addItemDecoration(
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)
            )

            // Add drag-and-drop support for reordering charts
            val itemTouchHelper = ItemTouchHelper(ChartItemTouchHelperCallback(chartViewModel, chartAdapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun setupAddChartButton() {
        binding.contentMain.fabAddChart.setOnClickListener {
            showChartTypeSelectionDialog()
        }
    }

    private fun setupSwipeRefresh() {
        binding.contentMain.swipeRefreshLayout.setOnRefreshListener {
            // Refresh all chart data
            chartViewModel.refreshAllChartData()
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
        // Based on chart type, navigate to appropriate edit screen
        when (chartConfig.chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> {
                // Navigate to bar chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId)
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
                }
                startActivity(intent)
            }
            ChartType.GAUGE -> {
                // Navigate to gauge chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId)
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
                }
                startActivity(intent)
            }
            ChartType.METRIC -> {
                // Navigate to metric chart edit activity
                val intent = Intent(this, ChartParametersActivity::class.java).apply {
                    putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartConfig.chartType.name)
                    putExtra(ChartParametersActivity.EXTRA_SITE_ID, chartConfig.deviceId)
                    putExtra(ChartParametersActivity.EXTRA_CHART_ID, chartConfig.id)
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
                chartViewModel.deleteChart(chartConfig)
                Snackbar.make(binding.root, "Chart deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        chartViewModel.insertChart(chartConfig)
                    }
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
            }
            // Add more navigation items here as needed
        }
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
}