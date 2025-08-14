package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.adapters.SiteAdapter
import com.mydrishti.co.`in`.activities.api.ApiClient
import com.mydrishti.co.`in`.activities.dialogs.LoadingDialog
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.utils.NetworkUtils
import com.mydrishti.co.`in`.activities.utils.SessionManager
import com.mydrishti.co.`in`.activities.utils.StatusBarManager
import com.mydrishti.co.`in`.activities.utils.CrashReportingManager
import com.mydrishti.co.`in`.activities.viewmodels.SiteViewModel
import com.mydrishti.co.`in`.activities.viewmodels.SiteViewModelFactory
import com.mydrishti.co.`in`.databinding.ActivitySiteSelectionBinding

class SiteSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySiteSelectionBinding
    private lateinit var siteViewModel: SiteViewModel
    private lateinit var siteAdapter: SiteAdapter
    private lateinit var loadingDialog: LoadingDialog
    private var chartType: ChartType? = null

    companion object {
        const val EXTRA_CHART_TYPE = "extra_chart_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure status bar with app's primary dark color
        StatusBarManager.configureStatusBar(this, isLightStatusBar = false, useTransparentStatusBar = false, customColor = "#388E3C")
        
        binding = ActivitySiteSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.select_site)

        // Get chart type from intent
        val chartTypeName = intent.getStringExtra(EXTRA_CHART_TYPE)
        if (chartTypeName.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid chart type", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        chartType = ChartType.valueOf(chartTypeName)

        // Initialize loading dialog
        loadingDialog = LoadingDialog(this)

        setupRecyclerView()
        setupViewModel()
    }

    private fun setupRecyclerView() {
        siteAdapter = SiteAdapter { device ->
            // Navigate to parameter selection when a device is clicked
            navigateToParameterSelection(device)
        }

        binding.recyclerViewSites.apply {
            layoutManager = LinearLayoutManager(this@SiteSelectionActivity)
            adapter = siteAdapter
            addItemDecoration(
                DividerItemDecoration(this@SiteSelectionActivity, DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun setupViewModel() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please connect to the internet.", Toast.LENGTH_LONG).show()
            return
        }
        val apiService = ApiClient.getApiService()
        // Get the SessionManager instance
        val sessionManager = SessionManager.getInstance(this)
        // Pass both apiService and sessionManager to the factory
        val factory = SiteViewModelFactory(apiService, sessionManager)
        siteViewModel = ViewModelProvider(this, factory)[SiteViewModel::class.java]

        // Observe devices data
        siteViewModel.sites.observe(this) { devices ->
            binding.emptyStateLayout.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
            siteAdapter.updateSites(devices)
        }

        // Observe loading state
        siteViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                loadingDialog.show()
            } else {
                loadingDialog.dismiss()
            }
        }

        // Observe error events
        siteViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                siteViewModel.clearError()
            }
        }

        // Load sites based on chart type
        when (chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> {
                siteViewModel.loadBarChartSites()
            }

            ChartType.GAUGE -> {
                siteViewModel.loadGaugeChartSites()
            }

            ChartType.METRIC -> {
                siteViewModel.loadMetricChartSites()
            }

            else -> {
                Toast.makeText(this, "Unsupported chart type", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun navigateToParameterSelection(device: com.mydrishti.co.`in`.activities.models.Device) {
        val intent = Intent(this, ChartParametersActivity::class.java).apply {
            putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartType?.name)
            putExtra(ChartParametersActivity.EXTRA_SITE_ID, device.iotDeviceMapId)
            putExtra(ChartParametersActivity.EXTRA_SITE_NAME, device.deviceDisplayName)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return CrashReportingManager.safeExecute(
            operation = {
                if (item.itemId == android.R.id.home) {
                    onBackPressed()
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(
                    "SiteSelectionActivity",
                    "Error handling options item selection",
                    exception
                )
            },
            defaultValue = super.onOptionsItemSelected(item)
        ) ?: super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return CrashReportingManager.safeExecute(
            operation = {
                onBackPressed()
                true
            },
            onError = { exception ->
                CrashReportingManager.logError(
                    "SiteSelectionActivity",
                    "Error handling navigation up",
                    exception
                )
            },
            defaultValue = super.onSupportNavigateUp()
        ) ?: super.onSupportNavigateUp()
    }
}