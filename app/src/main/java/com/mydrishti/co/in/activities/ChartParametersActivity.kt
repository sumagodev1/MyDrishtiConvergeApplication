package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.api.ApiClient
import com.mydrishti.co.`in`.activities.dao.ChartDao
import com.mydrishti.co.`in`.activities.database.AppDatabase
import com.mydrishti.co.`in`.activities.dialogs.LoadingDialog
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.activities.repositories.ChartRepository
import com.mydrishti.co.`in`.activities.viewmodels.ChartParameterViewModelFactory
import com.mydrishti.co.`in`.activities.viewmodels.ChartParametersViewModel
import com.mydrishti.co.`in`.activities.viewmodels.SiteViewModelFactory
import com.mydrishti.co.`in`.activities.utils.SessionManager
import com.mydrishti.co.`in`.databinding.ActivityChartParametersBinding
import java.util.*
import com.mydrishti.co.`in`.activities.models.DateRange


class ChartParametersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartParametersBinding
    private lateinit var viewModel: ChartParametersViewModel
    private lateinit var loadingDialog: LoadingDialog

    private var chartType: ChartType? = null
    private var siteId: Long = -1
    private var siteName: String = ""
    private var chartId: String = "" // Changed to String type for chart ID

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
            siteId = extras.getInt(EXTRA_SITE_ID, -1).toLong()
            siteName = extras.getString(EXTRA_SITE_NAME, "")
            
            // Extract chart ID for editing - now as String
            chartId = extras.getString(EXTRA_CHART_ID, "")
            println("Extracted chart ID from intent: $chartId")
        }
        
        // Validate parameters
        if (chartType == null || siteId == -1L || siteName.isEmpty()) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Set title based on chart type and edit mode
        val actionTitle = if (chartId.isEmpty()) {
            when (chartType) {
                ChartType.BAR_DAILY -> getString(R.string.setup_daily_bar_chart)
                ChartType.BAR_HOURLY -> getString(R.string.setup_hourly_bar_chart)
                ChartType.GAUGE -> getString(R.string.setup_gauge_chart)
                ChartType.METRIC -> getString(R.string.setup_metric_chart)
                else -> getString(R.string.setup_chart)
            }
        } else {
            getString(R.string.edit_chart)
        }
        supportActionBar?.title = actionTitle

        // Initialize loading dialog
        loadingDialog = LoadingDialog(this)

        setupViewModel()

        setupUI()

    }

    private fun setupViewModel() {
        val apiService = ApiClient.getApiService()
        val chartDao=AppDatabase.getDatabase(this@ChartParametersActivity).chartDao()
        val parameterDao = AppDatabase.getDatabase(this@ChartParametersActivity).parameterDao()
        val sessionManager = SessionManager.getInstance(this)
        val chartRepository = ChartRepository(
            chartDao,
            parameterDao,
            apiService,
            sessionManager
        )
        val factory = ChartParameterViewModelFactory(chartRepository, sessionManager)

        viewModel = ViewModelProvider(this,factory)[ChartParametersViewModel::class.java]

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
                    if (chartId.isEmpty()) R.string.chart_added else R.string.chart_updated,
                    Toast.LENGTH_SHORT
                ).show()

                // Get the actual chart ID to use for refreshing
                val actualChartId = if (chartId.isEmpty()) {
                    // For new charts, we need to query the latest chart ID
                    // This is just a workaround - in a real app you'd return the new ID from saveChart
                    viewModel.getLatestChartId()
                } else {
                    chartId
                }

                // Refresh the chart data to ensure updated values
                viewModel.refreshChartData(actualChartId)

                // Navigate directly to MainActivity instead of just finishing
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        // If editing existing chart, load its data
        if (chartId.isNotEmpty()) {
            println("Loading chart config for editing with ID: $chartId")
            viewModel.loadChartConfig(chartId)
        } else {
            println("Creating new chart (no chart ID provided)")
        }

        // Load available parameters for this site and chart type
        viewModel.loadAvailableParameters(chartType!!, siteId)
    }

    private fun setupUI() {
        // Display site name
        binding.tvSiteName.text = siteName
        
        // If we're editing a chart, update the title accordingly
        if (chartId.isNotEmpty()) {
            supportActionBar?.title = "Edit Chart"
            supportActionBar?.subtitle = "ID: $chartId"
            println("Setting up UI for editing chart with ID: $chartId")
        }

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
                println("ChartParametersActivity: Received chart config update: $chartConfig")
                println("ChartParametersActivity: Chart title to populate: ${chartConfig.title}")
                populateFormWithChartData(chartConfig)
            } else {
                println("ChartParametersActivity: Received NULL chart config")
            }
        }

        // Observe available parameters
        viewModel.availableParameters.observe(this) { parameters ->
            if (parameters.isEmpty()) {
                binding.tvNoParameters.visibility = View.VISIBLE
                binding.tvNoParameters.text = getString(R.string.no_parameters_available)
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
        
        // Bar chart specific setup - no date range picker needed
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
        // Display parameters differently based on chart type
        when (chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY, ChartType.GAUGE -> {
                // For these chart types, we only need one parameter selection
                binding.parameterRadioGroup.visibility = View.VISIBLE
                binding.parameterCheckboxContainer.visibility = View.GONE

                // Clear existing radio buttons first
                binding.parameterRadioGroup.removeAllViews()

                // Add a radio button for each parameter
                parameters.forEach { parameter ->
                    val radioButton = android.widget.RadioButton(this)
                    radioButton.id = parameter.id.toInt()
                    radioButton.text = "${parameter.displayName} (${parameter.uomDisplayName})"
                    binding.parameterRadioGroup.addView(radioButton)

                    // Log for debugging
                    println("Added parameter radio button: ${parameter.displayName} with ID ${parameter.id}")
                }

                // Select the first parameter by default
                if (parameters.isNotEmpty()) {
                    binding.parameterRadioGroup.check(parameters.first().id.toInt())
                }
            }

            ChartType.METRIC -> {
                // For metric charts, allow multiple parameter selection
                binding.parameterRadioGroup.visibility = View.GONE
                binding.parameterCheckboxContainer.visibility = View.VISIBLE

                // Clear existing checkboxes first
                binding.parameterCheckboxContainer.removeAllViews()

                // Add a checkbox for each parameter
                parameters.forEach { parameter ->
                    val checkbox = android.widget.CheckBox(this)
                    checkbox.id = parameter.id.toInt()
                    checkbox.text = "${parameter.displayName} (${parameter.uomDisplayName})"
                    binding.parameterCheckboxContainer.addView(checkbox)

                    // Log for debugging
                    println("Added parameter checkbox: ${parameter.displayName} with ID ${parameter.id}")
                }
            }

            else -> {
                // No selection needed for other chart types
                binding.parameterRadioGroup.visibility = View.GONE
                binding.parameterCheckboxContainer.visibility = View.GONE
            }
        }
    }

    private fun populateFormWithChartData(chartConfig: ChartConfig) {
        // Populate the form fields with data from the existing chart
        println("Populating form with chart data: $chartConfig")
        println("Chart title: ${chartConfig.title}")
        
        // Make sure the EditText is properly populated with the title
        binding.etChartTitle.setText(chartConfig.title)
        
        // Add additional debug logging to verify the EditText was populated
        println("EditText value after setting: ${binding.etChartTitle.text}")
        
        // Apply the title immediately so it's visible
        binding.etChartTitle.post {
            binding.etChartTitle.setText(chartConfig.title)
            binding.etChartTitle.text?.let { editable ->
                binding.etChartTitle.setSelection(editable.length)
            }
            println("Re-applied title in post() callback: ${binding.etChartTitle.text}")
        }

        // Populate parameter selection based on chart type
        when (chartConfig.chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY, ChartType.GAUGE -> {
                // For these chart types, select the parameter in the radio group
                if (chartConfig.parameterIds.isNotEmpty()) {
                    val parameterId = chartConfig.parameterIds.first()
                    binding.parameterRadioGroup.post {
                        binding.parameterRadioGroup.check(parameterId)
                        println("Selected radio button with ID: $parameterId")
                    }
                }
            }
            ChartType.METRIC -> {
                // For metric charts, check all parameter checkboxes that match the IDs
                binding.parameterCheckboxContainer.post {
                    for (i in 0 until binding.parameterCheckboxContainer.childCount) {
                        val checkbox = binding.parameterCheckboxContainer.getChildAt(i) as? android.widget.CheckBox
                        if (checkbox != null) {
                            val checkboxId = checkbox.id
                            checkbox.isChecked = chartConfig.parameterIds.contains(checkboxId)
                            if (checkbox.isChecked) {
                                println("Checked checkbox with ID: $checkboxId")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveChart() {
        val title = binding.etChartTitle.text.toString().trim()

        if (title.isEmpty()) {
            binding.etChartTitle.error = getString(R.string.error_chart_title_required)
            return
        }

        // Collect parameter IDs based on chart type
        val parameterIds = when (chartType) {
            ChartType.BAR_DAILY, ChartType.BAR_HOURLY -> collectBarChartParameters()
            ChartType.GAUGE -> collectGaugeChartParameters()
            ChartType.METRIC -> collectMetricChartParameters()
            else -> listOf()
        }

        // Add debug logging
        println("Saving chart with parameter IDs: $parameterIds")

        // Create chart config
        val chartConfig = ChartConfig(
            id = if (chartId.isEmpty()) UUID.randomUUID().toString() else chartId,
            chartType = chartType!!,
            deviceId = siteId.toString(),
            deviceName = siteName, // Instead of siteName, use it as deviceName
            title = title,
            parameterIds = parameterIds,
            // We're not setting customDateRange here as it's null by default
            // Users will set date ranges when interacting with charts
            customDateRange = null
        )

        // Log the chart config for debugging
        println("Saving chart config: $chartConfig")

        // Save or update chart
        if (chartId.isEmpty()) {
            println("Creating new chart with generated ID: ${chartConfig.id}")
            viewModel.saveChart(chartConfig)
        } else {
            println("Updating existing chart with ID: $chartId")
            viewModel.updateChart(chartConfig)
        }
    }

    private fun collectBarChartParameters(): List<Int> {
        // Collect selected parameter for bar chart
        val selectedParameterId = getSelectedParameterId()
        if (selectedParameterId <= 0) {
            return listOf()
        }

        // Return with the parameter ID which is crucial for data fetching
        return listOf(selectedParameterId)
    }

    private fun collectGaugeChartParameters(): List<Int> {
        // Collect selected parameter for gauge chart
        val selectedParameterId = getSelectedParameterId()
        if (selectedParameterId <= 0) {
            return listOf()
        }

        // Return with the parameter ID which is crucial for data fetching
        return listOf(selectedParameterId)
    }

    private fun collectMetricChartParameters(): List<Int> {
        // For metric charts, we can select multiple parameters
        val selectedParameterIds = mutableListOf<Int>()

        // Collect all selected checkboxes
        for (i in 0 until binding.parameterCheckboxContainer.childCount) {
            val checkbox = binding.parameterCheckboxContainer.getChildAt(i) as? android.widget.CheckBox
            if (checkbox != null && checkbox.isChecked) {
                selectedParameterIds.add(checkbox.id)
            }
        }

        // If no parameters selected, use the default
        if (selectedParameterIds.isEmpty()) {
            val defaultId = getSelectedParameterId()
            selectedParameterIds.add(defaultId)
            println("No parameters selected for metric chart, using default ID: $defaultId")
        }

        println("Selected parameter IDs for metric chart: $selectedParameterIds")

        return selectedParameterIds
    }

    // Helper method to get the selected parameter ID
    private fun getSelectedParameterId(): Int {
        // For bar charts and gauge charts, get the selected radio button
        if (chartType == ChartType.BAR_DAILY || chartType == ChartType.BAR_HOURLY || chartType == ChartType.GAUGE) {
            val selectedId = binding.parameterRadioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                println("Selected parameter ID: $selectedId")
                return selectedId
            }
        }

        // For metric charts, get the first checked checkbox
        else if (chartType == ChartType.METRIC) {
            for (i in 0 until binding.parameterCheckboxContainer.childCount) {
                val checkbox = binding.parameterCheckboxContainer.getChildAt(i) as? android.widget.CheckBox
                if (checkbox != null && checkbox.isChecked) {
                    println("Selected parameter ID: ${checkbox.id}")
                    return checkbox.id
                }
            }
        }

        // If nothing is selected, return a default parameter ID from the API
        // We prefer Energy (184) for most charts, but Power (182) for gauge charts
        val defaultId = if (chartType == ChartType.GAUGE) 182 else 184
        println("No parameter selected, using default ID: $defaultId")
        return defaultId
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