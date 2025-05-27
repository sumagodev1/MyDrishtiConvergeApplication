import barChart from "@assets/images/icon-bar.png";
import barChartActive from "@assets/images/icon-bar-active.png";
import gaugeChart from "@assets/images/icon-gauge.png";
import gaugeChartActive from "@assets/images/icon-gauge-active.png";
import liveChart from "@assets/images/icon-live.png";
import liveChartActive from "@assets/images/icon-live-active.png";
import metricChart from "@assets/images/icon-metric.png";
import metricChartActive from "@assets/images/icon-metric-active.png";
import calChart from "@assets/images/icon-calc.png";
import calChartActive from "@assets/images/icon-calc-active.png";

export const CHART_OPTIONS = [{
        id: 1,
        value: "bar_chart",
        label: "barChart",
        icon: barChart,
        selectedIcon: barChartActive,
    },
    {
        id: 2,
        value: "hourly_bar_chart",
        label: "hourlybarChart",
        icon: barChart,
        selectedIcon: barChartActive,
    },
    {
        id: 3,
        value: "gauge_chart",
        label: "gaugeChart",
        icon: gaugeChart,
        selectedIcon: gaugeChartActive,
    },
    {
        id: 4,
        value: "line_chart",
        label: "lineChart",
        icon: liveChart,
        selectedIcon: liveChartActive,
    },
    {
        id: 5,
        value: "metric_chart",
        label: "metricChart",
        icon: metricChart,
        selectedIcon: metricChartActive,
    },
    {
        id: 6,
        value: "cal_chart",
        label: "calChart",
        icon: calChart,
        selectedIcon: calChartActive,
    },
];