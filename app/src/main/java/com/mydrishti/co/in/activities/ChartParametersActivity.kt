package com.mydrishti.co.`in`.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.dialogs.LoadingDialog
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.viewmodels.ChartParametersViewModel
import com.mydrishti.co.`in`.databinding.ActivityChartParametersBinding


class ChartParametersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartParametersBinding
    private lateinit var viewModel: ChartParametersViewModel
    private lateinit var loadingDialog: LoadingDialog

    private var chartType: ChartType? = null
    private var siteId: Long = -1
    private var siteName: String = ""
    private var chartId: Long = -1 // Used for editing existing chart

    companion object {
        const val EXTRA_CHART_TYPE = "extra_chart_type"
        const val EXTRA_SITE_ID = "extra_site_id"
        const val EXTRA_SITE_NAME = "extra_site_name"
        const val EXTRA_CHART_ID = "extra_chart_id" // For editing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartParametersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get parameters from intent
        intent.extras?.let { extras ->
            val chartTypeName = extras.getString(EXTRA_CHART_TYPE)
            chartType = if (chartTypeName != null) ChartType.valueOf(chartTypeName) else null
            siteId = extras.getLong(EXTRA_SITE_ID, -1)
            siteName = extras.getString(EXTRA_SITE_NAME, "")
            chartId = extras.getLong(EXTRA_CHART_ID, -1)
        }

        // Validate parameters
        if (chartType == null || siteId == -1L || siteName.isEmpty()) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set title based on chart type
        supportActionBar?.title = when (chartType) {
            ChartType.BAR_DAILY -> getString(R.string.setup_daily_bar_chart)
            ChartType.BAR_HOURLY -> getString(R.string.setup_hourly_bar_chart)
            ChartType.GAUGE -> getString(R.string.setup_gauge_chart)
            ChartType.METRIC -> getString(R.string.setup_metric_chart)
            else -> getString(R.string.setup_chart)
        }

        // Initialize loading dialog
        loadingDialog = LoadingDialog(this)

        setupViewModel()
        setupUI()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChartParametersViewModel::class.java]

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                loadingDialog.show()
            } else {
                loadingDialog.dismiss()
            }
        }

        // Observe error events
        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe chart save/update result
        viewModel.chartSaveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(
                    this,
                    if (chartId == -1L) R.string.chart_added else R.string.chart_updated,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        // If editing existing chart, load its data
        if (chartId != -1L) {
            viewModel.loadChartConfig(chartId)
        }

        // Load available parameters for this site and chart type
        viewModel.loadAvailableParameters(chartType!!, siteId)
    }

    private fun setupUI() {
        // Display site name
        binding.tvSiteName.text = siteName

        // Set up specific UI for different chart types
        when (chartType) {
            ChartType.BAR_DAILY -> setupBarChartUI(true)
            ChartType.BAR_HOURLY -> setupBarChartUI(false)
            ChartType.GAUGE -> setupGaugeChartUI()
            ChartType.METRIC -> setupMetricChartUI()
            else -> {
                // Unsupported chart type
                Toast.makeText(this, "Unsupported chart type", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Save button click listener
        binding.btnSaveChart.setOnClickListener {
            saveChart()
        }

        // Observe existing chart data for editing
        viewModel.chartConfig.observe(this) { chartConfig ->
            if (chartConfig != null) {
                populateFormWithChartData(chartConfig)
            }
        }

        // Observe available parameters
        viewModel.availableParameters.observe(this) { parameters ->
            if (parameters.isEmpty()) {
                binding.tvNoParameters.visibility = View.VISIBLE
                binding.parametersLayout.visibility = View.GONE
                binding.btnSaveChart.isEnabled = false
            } else {
                binding.tvNoParameters.visibility = View.GONE
                binding.parametersLayout.visibility = View.VISIBLE
                binding.btnSaveChart.isEnabled = true

                // Populate parameter selection UI based on chart type
                populateParameterSelectionUI(parameters)
            }
        }
    }

    private fun setupBarChartUI(isDaily: Boolean) {
        binding.barChartLayout.visibility = View.VISIBLE
        binding.gaugeChartLayout.visibility = View.GONE
        binding.metricChartLayout.visibility = View.GONE

        // Setup date range picker for bar charts
        binding.tvDateRangeTitle.text = if (isDaily) {
            getString(R.string.select_daily_date_range)
        } else {
            getString(R.string.select_hourly_date_range)
        }

        // Set up date range picker click listener
        binding.dateRangePicker.setOnClickListener {
            // Show date range picker dialog
            showDateRangePickerDialog(isDaily)
        }
    }

    private fun setupGaugeChartUI() {
        binding.barChartLayout.visibility = View.GONE
        binding.gaugeChartLayout.visibility = View.VISIBLE
        binding.metricChartLayout.visibility = View.GONE

        // Gauge chart specific setup
    }

    private fun setupMetricChartUI() {
        binding.barChartLayout.visibility = View.GONE
        binding.gaugeChartLayout.visibility = View.GONE
        binding.metricChartLayout.visibility = View.VISIBLE

        // Metric chart specific setup
    }

    private fun populateParameterSelectionUI(parameters: List<ParameterInfo>) {
        // Implement based on chart type and available parameters
        // This would typically involve creating checkboxes, radio buttons,
        // or spinners for parameter selection
    }

    private fun showDateRangePickerDialog(isDaily: Boolean) {
        // Show appropriate date range picker dialog
        // For daily charts, pick date range by days
        // For hourly charts, pick specific hours within a day
    }

    private fun populateFormWithChartData(chartConfig: ChartConfig) {
        // Populate the form fields with data from the existing chart
        binding.etChartTitle.setText(chartConfig.title)

        // Populate other fields based on chart type
        when (chartConfig.chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> {
                // Populate bar chart specific fields
            }
            ChartType.GAUGE -> {
                // Populate gauge chart specific fields
            }
            ChartType.METRIC -> {
                // Populate metric chart specific fields
            }
        }
    }

    private fun saveChart() {
        val title = binding.etChartTitle.text.toString().trim()

        if (title.isEmpty()) {
            binding.etChartTitle.error = getString(R.string.error_chart_title_required)
            return
        }

        // Collect parameters based on chart type
        val parameters = when (chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> collectBarChartParameters()
            ChartType.GAUGE -> collectGaugeChartParameters()
            ChartType.METRIC -> collectMetricChartParameters()
            else -> mapOf()
        }

        // Create chart config
        val chartConfig = ChartConfig(
            id = (if (chartId == -1L) 0 else chartId).toString(),
            chartType = chartType!!,
            siteId = siteId.toString(),
            siteName = siteName,
            title = title,
            parameters = parameters
        )

        // Save or update chart
        if (chartId == -1L) {
            viewModel.saveChart(chartConfig)
        } else {
            viewModel.updateChart(chartConfig)
        }
    }

    private fun collectBarChartParameters(): Map<String, String> {
        // Collect parameters for bar chart
        return mapOf()
    }

    private fun collectGaugeChartParameters(): Map<String, String> {
        // Collect parameters for gauge chart
        return mapOf()
    }

    private fun collectMetricChartParameters(): Map<String, String> {
        // Collect parameters for metric chart
        return mapOf()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    data class ParameterInfo(
        val id: Long,
        val name: String,
        val displayName: String,
        val uomDisplayName: String
    )
}