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
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.models.Site
import com.mydrishti.co.`in`.activities.viewmodels.SiteViewModel
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
        siteAdapter = SiteAdapter { site ->
            // Navigate to parameter selection when a site is clicked
            navigateToParameterSelection(site)
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
        siteViewModel = ViewModelProvider(this)[SiteViewModel::class.java]

        // Observe sites data
        siteViewModel.sites.observe(this) { sites ->
            binding.emptyStateLayout.visibility = if (sites.isEmpty()) View.VISIBLE else View.GONE
            siteAdapter.submitList(sites)
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

    private fun navigateToParameterSelection(site: Site) {
        val intent = Intent(this, ChartParametersActivity::class.java).apply {
            putExtra(ChartParametersActivity.EXTRA_CHART_TYPE, chartType?.name)
            putExtra(ChartParametersActivity.EXTRA_SITE_ID, site.id)
            putExtra(ChartParametersActivity.EXTRA_SITE_NAME, site.name)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}