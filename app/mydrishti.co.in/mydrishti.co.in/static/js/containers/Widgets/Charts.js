import {
    useCallback,
    useEffect,
    useRef,
    useState
} from "react";
import {
    useSelector,
    useDispatch
} from "react-redux";
import {
    isArray,
    isEmpty,
    uniq,
    values,
    first,
    floor,
    flatten,
    uniqBy,
    remove,
    compact,
    round
} from "lodash";
import {
    evaluate
} from "mathjs";
import moment from "moment-timezone";
// import "moment-timezone";
import axios from "axios";
import HighCharts from "highcharts";
import {
    Row,
    Col,
    Menu,
    Dropdown,
    DatePicker,
    Space,
    Modal,
    Button,
    Input
} from "antd";
import {
    PlusOutlined,
    DownOutlined,
    MinusOutlined
} from "@ant-design/icons";
import {
    Card,
    CardContent
} from "@material-ui/core";
import ReactSpeedometer from "react-d3-speedometer";
import reportsActions from "@redux/reports/actions";
import useWindowSize from "@lib/hooks/useWindowSize";
import LayoutWrapper from "@components/utility/layoutWrapper";
import basicStyle from "@assets/styles/constants";
import {
    API_BASE_URL
} from "@config/site.config";
import Loader from "@components/utility/loader";
import addWidgetActions from "@redux/addWidgets/actions";
import {
    getPastDate,
    getCurrentDate
} from "@lib/helpers/utility";
import {
    loadStringState,
    loadState
} from "@lib/helpers/localStorage";
import Highchart from "./Highchart";
import {
    variables
} from "../../assets/styles/variables";
import ChartsWrapper from "./Charts.styles";
import ToastMaster from "../../components/FormUI/ToastMaster";
import {
    useHistory
} from "react-router";

const {
    setMetricChartData,
    getUserParameters
} = reportsActions;
const {
    updateSelectedParameters
} = addWidgetActions;

const {
    RangePicker
} = DatePicker;
const {
    MonthPicker
} = DatePicker;

function isNumeric(str) {
    const updatedStr = str.toString();
    if (typeof updatedStr != "string") return false; // we only process strings!
    return (!isNaN(updatedStr) && // use type coercion to parse the _entirety_ of the string (`parseFloat` alone does not do this)...
        !isNaN(parseFloat(updatedStr))
    ); // ...and ensure strings of whitespace fail
}

const correctNumberString = (num) => {
    const updatedNumber = num.split(".");
    if (updatedNumber.length > 1) {
        const finalNumber = `${updatedNumber[0]}.${updatedNumber[1]}`;
        return finalNumber;
    }
    return num;
};

function useInterval(callback, delay) {
    const savedCallback = useRef();

    // Remember the latest callback.
    useEffect(() => {
        savedCallback.current = callback;
    }, [callback]);

    // Set up the interval.
    useEffect(() => {
        function tick() {
            savedCallback.current();
        }
        if (delay !== null) {
            let id = setInterval(tick, delay);
            return () => clearInterval(id);
        }
    }, [delay]);
}

export default function Charts({
    widgetData,
    onRemove,
    editWidgetDetails
}) {
    const {
        rowStyle,
        colStyle
    } = basicStyle;
    const dispatch = useDispatch();
    const {
        width
    } = useWindowSize();
    const {
        accessToken
    } = useSelector((state) => state.Auth);
    const widgetDetails = loadState("widgetSettings");
    const storedToken = loadStringState("accessToken");
    const settingState = useSelector((state) => state.settingsReducer);

    const refreshDuration = loadState("refreshDuration") || settingState.refreshDuration;

    const refreshTimeInSeconds1 = 30 * 60;
    const refreshTimeInSeconds2 = refreshDuration * 60;

    const currentUser = loadState("currentUser");
    const parametersList = loadState("parametersList");
    const deviceList = loadState("deviceList");
    const token = accessToken ? accessToken : storedToken;
    const {
        device,
        parameter,
        chartName,
        id,
        selectedDeviceAndParameterList,
        displayCalModal,
        calculatedCalValue,
        calculatedResult,
        customChartName,
    } = widgetData;

    // const startDate = getPastDate(6).format("YYYY-MM-DD");
    // const endDate = getCurrentDate().format("YYYY-MM-DD");
    const userTimeZone = moment.tz.guess();

    // const endDate = getCurrentDate().format("YYYY-MM-DD"); // End date is the current date
    // const startDate = getCurrentDate().subtract(1, "days").format("YYYY-MM-DD");// Subtract 1 day to get the start date (past 24 hours)

    const startDate = moment().tz(userTimeZone).startOf("day").utc().format(); // Convert to UTC
    const endDate = moment().tz(userTimeZone).startOf("day").add(1, "days").utc().format(); // Next day 00:00 UTC

    const monthStartDate = moment.tz(userTimeZone).startOf("month").utc().format();
    const monthEndDate = moment.tz(userTimeZone).endOf("month").subtract(1, "day").utc().format();



    const segmentColors = ["#f87357", "gold", "#8af857", "gold", "#f87357"];
    const {
        iotDeviceMapId,
        deviceDisplayName
    } = device;
    const {
        parameterId,
        parameterDisplayName
    } = parameter;

    const [gaugeData, setGaugeData] = useState({
        minValue: 0,
        maxValue: 0,
        lowLowValue: 0,
        lowValue: 0,
        highValue: 0,
        highHighValue: 0,
        value: 0,
        parameterValue: 0,
        timestamp: "",
        dataLoaded: false,
        errorText: "",
        parameterId: "",
    });

    const [metricData, setMetricData] = useState([]);
    const [calData, setCalData] = useState([]);
    const [calCalculationData, setCalCalculationData] = useState([]);
    const [calDataLoaded, setCalDataLoaded] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [metricDataLoaded, setMetricDataLoaded] = useState(false);
    const [showInLegend, setShowInLeagends] = useState(true);
    const [showGaugeValue, setShowGaugeValue] = useState(false);
    const [lineChartData, setLineChartData] = useState([]);
    const [lineChartDataLoaded, setLineChartDataLoaded] = useState(false);
    const [barChartData, setBarChartData] = useState([]);
    const [hourlyBarChartData, setHourlyBarChartData] = useState([]);

    const [barChartDataLoaded, setBarChartDataLoaded] = useState(false);
    const [hourlyBarChartDataLoaded, setHourlyBarChartDataLoaded] = useState(false);

    const [deviceParameterBoundary, setDeviceParameterBoundary] = useState([]);
    const [inputList, setInputList] = useState([]);
    const [updatedValue, setUpdatedValue] = useState("");
    const [customChartNameOption, setCustomChartNameOption] = useState("");
    const [toastMessage, setToastMessage] = useState("");
    const [dateRange, setDateRange] = useState([null, null]);
    const [lastUpdatedTimestamp, setLastUpdatedTimestamp] = useState(moment().unix());
    const [barChartDates, setBarChartDates] = useState({
        monthStartDate,
        monthEndDate
    });

    const [hourlyBarChartDates, setHourlyBarChartDates] = useState({
        startDate,
        endDate
    });
    const now = moment().unix()
    const CHART_NAMES = {
        GAUGE_CHART: "gauge_chart",
        METRIC_CHART: "metric_chart",
        LINE_CHART: "line_chart",
        BAR_CHART: "bar_chart",
        HOURLY_BAR_CHART: "hourly_bar_chart",
        CAL_CHART: "cal_chart",
    }
    let startTimeStamps = {}
    Object.keys(CHART_NAMES).forEach((chart) => {
        startTimeStamps[CHART_NAMES[chart]] = now
    })
    const [lastUpdatedTimestampChart, setLastUpdatedTimestampChart] = useState(startTimeStamps);

    const getLastUpdatedTimestampChart = (chart) => {
        return moment(lastUpdatedTimestampChart[chart] * 1000).format("MM/DD/YYYY, h:mm:ss A")
    }


    const [lineChartDates, setLineChartDates] = useState({
        startDate,
        endDate
    })

    const history = useHistory();

    const url = `${API_BASE_URL}metric-chart`;

    const getSelectedDeviceDetails = () => {
        const selectedDeviceSeries = [];
        if (!isEmpty(selectedDeviceAndParameterList)) {
            selectedDeviceAndParameterList.map((res) => {
                const result = {
                    iotDeviceMapId: res.selectedDevice,
                    parameterIdList: [],
                };
                selectedDeviceSeries.push(result);
            });

            selectedDeviceAndParameterList.map((s) => {
                selectedDeviceSeries.map((y) => {
                    if (s.selectedDevice === y.iotDeviceMapId) {
                        y.parameterIdList.push(s.selectedParameter);
                    }
                });
            });
        }
        return uniqBy(selectedDeviceSeries, "iotDeviceMapId");
    };

    const getGrapthArrayDataList = (itemList, isDevice) => {
        let arr = [];
        isArray(itemList) &&
            // eslint-disable-next-line array-callback-return
            itemList.map((item, _i) => {
                arr.push(isDevice ? item.iotDeviceMapId : item.parameterId);
            });
        return arr;
    };

    const getDateSeries = (items, isBarChart) => {
        let arr = [];
        if (!items.errorText && !isEmpty(items)) {
            items.forEach((data) => {
                // Convert to local time using moment and format
                const localTimestamp = moment.utc(data.timestamp).local().format("DD-MMM");
                arr.push(localTimestamp);
            });
        }
        return uniq(arr);
    };

    const getDeviceName = (iotDeviceMapId) => {
        return (!isEmpty(deviceList) && first(deviceList.filter((s) => s.iotDeviceMapId === iotDeviceMapId)) ? .deviceDisplayName);
    };

    const getParameterName = (parameterId, iotDeviceMapId) => {
        return `${getDeviceName(iotDeviceMapId)} - ${first(parametersList.filter((s) => s.parameterId === parameterId))?.parameterDisplayName
			}`;
    };

    const chartConfiguration = (type) => {
        return {
            chart: {
                height: 300,
                type: type,
                zoomType: "x",
            },
            title: {
                text: "",
            },
            credits: {
                enabled: false,
            },
            responsive: {
                rules: [{
                    condition: {
                        maxWidth: 500,
                    },
                    chartOptions: {
                        legend: {
                            enabled: false,
                        },
                    },
                }, ],
            },
        };
    };

    const fetchGaugeData = () => {
        if (token) {
            const url = `${API_BASE_URL}gauge-chart`;
            const body = {
                iotDeviceMapId: iotDeviceMapId,
                parameterId: parameterId,
            };
            const header = {
                headers: {
                    Authorization: "Bearer " + token,
                    "Content-Type": "application/json",
                },
            };
            axios
                .post(url, body, header)
                .then((res) => {
                    if (res) {
                        const data = res.data;
                        if (isEmpty(data)) {
                            setGaugeData({
                                errorText: "No Records Found."
                            });
                        } else {
                            setGaugeData({
                                minValue: data.minValue * data.multiplier,
                                lowValue: data.lowValue * data.multiplier,
                                lowLowValue: data.lowLowValue * data.multiplier,
                                highValue: data.highValue * data.multiplier,
                                highHighValue: data.highHighValue * data.multiplier,
                                maxValue: data.maxValue * data.multiplier,
                                value: data.value,
                                timestamp: data.timestamp,
                                dataLoaded: true,
                                errorText: "",
                                parameterId: parameterId,
                            });
                            setLastUpdatedTimestamp(moment().unix());
                            setTimeout(() => {
                                setLastUpdatedTimestampChart(oldTimestamps => {
                                    oldTimestamps[CHART_NAMES.GAUGE_CHART] = moment(data.timestamp).unix();
                                    return { ...oldTimestamps
                                    }
                                });
                            }, 0);
                        }
                    }
                })
                .catch((err) => {
                    if (err.response ? .status) {
                        setGaugeData({
                            errorText: "Unable to fetch Data"
                        });
                    }
                });
        }
    };

    const fetchMetricData = () => {
        if (token) {
            const url = `${API_BASE_URL}metric-chart`;
            const body = {
                iotDeviceMapId: iotDeviceMapId,
                parameterIdList: getGrapthArrayDataList(parameter, false),
            };
            const header = {
                headers: {
                    Authorization: "Bearer " + token,
                    "Content-Type": "application/json",
                },
            };
            axios
                .post(url, body, header)
                .then((res) => {
                    if (res.data) {
                        const {
                            graphData,
                            deviceParameterBoundary
                        } = res.data;
                        setMetricData(graphData);
                        setDeviceParameterBoundary(values(deviceParameterBoundary));
                        setMetricDataLoaded(true);
                        setLastUpdatedTimestamp(moment().unix());
                        let date = graphData.length ? graphData[0].timestamp : moment()
                        setTimeout(() => {
                            setLastUpdatedTimestampChart(oldTimestamps => {
                                oldTimestamps[CHART_NAMES.METRIC_CHART] = moment(date).unix();
                                return { ...oldTimestamps
                                }
                            });
                        }, 0);
                    }
                })
                .catch((err) => {
                    if (err.response ? .status) {
                        setMetricData({
                            errorText: "Unable to fetch Data"
                        });
                        setMetricDataLoaded(false);
                    }
                });
        }
    };

    const fetchCalData = async () => {
        try {
            if (!token) {
                return;
            }

            const url = `${API_BASE_URL}metric-chart`;
            const body = {
                iotDeviceMapId: iotDeviceMapId,
                parameterIdList: getGrapthArrayDataList(parameter, false),
            };
            console.log("Request Payload Size: " + JSON.stringify(body).length + " bytes");
            const header = {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            };

            const res = await axios.post(url, body, header);

            if (res.data) {
                const {
                    graphData,
                    deviceParameterBoundary
                } = res.data;

                if (isEmpty(calculatedCalValue)) {
                    setCalData(graphData);
                } else {
                    calculatedResult.forEach((item) => {
                        const {
                            iotDeviceMapId,
                            parameterId,
                            value
                        } = item;
                        const graphpItem = graphData.find(
                            (x) => x.iotDeviceMapId === iotDeviceMapId && x.parameterId === parameterId
                        );

                        if (graphpItem && value !== graphpItem.value) {
                            // console.log(`Value Changed from ${value} to ${graphpItem.value} in ${customChartName}`);
                            item.value = graphpItem.value;
                        }
                    });

                    setCalData(calculatedCalValue);
                    setInputList(calculatedResult);
                    handleCalChartCalculation();
                }

                setCalCalculationData(graphData);
                setDeviceParameterBoundary(values(deviceParameterBoundary));
                setCalDataLoaded(true);
                setIsModalVisible(displayCalModal);
                const timestampFromApi = graphData.length ? graphData[0].timestamp : moment();
                setLastUpdatedTimestamp(moment().unix());
                setLastUpdatedTimestampChart((oldTimestamps) => {
                    oldTimestamps[CHART_NAMES.CAL_CHART] = moment(timestampFromApi).unix();
                    return { ...oldTimestamps
                    };
                });

                dispatch(setMetricChartData(graphData));
            }
        } catch (err) {
            if (err.response ? .status) {
                setCalData({
                    errorText: "Unable to fetch Data"
                });
                setCalDataLoaded(false);
            }
        }
    };

    const fetchLineChartData = (startDate, endDate, showLegend = true) => {
        if (token) {
            const url = `${API_BASE_URL}line-chart`;
            const body = {
                dateRange: {
                    startDate: startDate,
                    endDate: endDate,
                },
                deviceDetails: getSelectedDeviceDetails(),
            };
            const header = {
                headers: {
                    Authorization: "Bearer " + token,
                    "Content-Type": "application/json",
                },
            };
            axios
                .post(url, body, header)
                .then((res) => {
                    if (res.data) {
                        const {
                            graphData
                        } = res.data;

                        const dateTimeSeries = uniq(graphData.map((s) => s.timestamp));

                        const dataSeries = [];
                        getSelectedDeviceDetails().map((item, i) => {
                            flatten(item.parameterIdList).map((par) => {
                                const result = {
                                    name: getParameterName(par, item.iotDeviceMapId),
                                    showInLegend: showLegend,
                                    data: [],
                                };

                                const getLocalTimeStamp = (dateTimeString) => {
                                    const ts = moment(dateTimeString).unix();
                                    const localTimeStamp = moment(ts * 1000).format();
                                    return localTimeStamp;
                                };

                                dateTimeSeries.map((timestamp) => {
                                    const updatedTimeStamp = getLocalTimeStamp(timestamp);

                                    const record = graphData.filter((s) => {
                                        s.timestamp = getLocalTimeStamp(s.timestamp);

                                        return s.parameterId === par && s.timestamp === updatedTimeStamp;
                                    });
                                    const sample = isEmpty(record) ? null : floor(first(record).value, 2);
                                    const time = moment(updatedTimeStamp);

                                    result.data.push([time.valueOf(), sample]);
                                });

                                const updatedResult = result.data.map((item, i) => {
                                    let d = moment(item[0]).add(5.50, "hours").format();
                                    const newDate = moment(d).format();
                                    const newTs = moment(newDate).unix() * 1000;
                                    item[0] = newTs;
                                    return item;
                                });
                                dataSeries.push(result);
                            });
                        });

                        const dataSeriesResult = dataSeries.map((s, i) => {
                            return {
                                name: s.name,
                                type: "spline",
                                yAxis: i,
                                data: s.data,
                                showInLegend: showLegend,
                            };
                        });
                        const yAxisSeries = dataSeriesResult.map((s, i) => {
                            return {
                                labels: {
                                    style: {
                                        color: HighCharts.getOptions() //.colors[i],
                                    },
                                },
                                title: {
                                    text: s.name,
                                    style: {
                                        color: HighCharts.getOptions() //.colors[i],
                                    },
                                },
                                opposite: i % 2 === 0 ? true : false,
                            };
                        });

                        const result = {
                            ...chartConfiguration("line"),
                            xAxis: {
                                type: "datetime",
                                minRange: 1,
                                labels: {
                                    formatter: function() {
                                        return HighCharts.dateFormat("%Y-%m-%d %H:%M:%S", this.value);
                                    },
                                },
                            },
                            tooltip: {
                                shared: true,
                                formatter: function() {
                                    const timeFormatted = HighCharts.dateFormat("%Y/%m/%d %H:%M:%S %p", new Date(this.x));
                                    let tooltipContent = `<b>Timestamp</b>: ${timeFormatted}<br/>`;

                                    this.points.forEach((point) => {
                                        tooltipContent += `<b>${point.series.name}</b>: ${point.y}<br/>`;
                                    });

                                    return tooltipContent;
                                },
                            },
                            time: {
                                useUTC: false,
                            },
                            yAxis: yAxisSeries,
                            series: dataSeriesResult,
                        };
                        setLineChartData(result);
                        setLineChartDataLoaded(true);
                        setLastUpdatedTimestamp(moment().unix());
                        setLastUpdatedTimestampChart(oldTimestamps => {
                            oldTimestamps[CHART_NAMES.LINE_CHART] = moment().unix();
                            return { ...oldTimestamps
                            }
                        });
                    }
                })
                .catch((err) => {
                    if (err.response ? .data ? .statusText === "statusFalse") {
                        setLineChartData({
                            errorText: "Unable to fetch Data"
                        });
                        setLineChartDataLoaded(false);
                    }
                });
        }
    };

    const fetchBarChartData = (monthStartDate, monthEndDate, showLegend) => {
        if (token) {
            const url = `${API_BASE_URL}bar-chart`;
            const body = {
                dateRange: {
                    startDate: monthStartDate,
                    endDate: monthEndDate,
                },
                deviceDetails: getSelectedDeviceDetails(),
            };
            const header = {
                headers: {
                    Authorization: "Bearer " + token,
                    "Content-Type": "application/json",
                },
            };
            axios
                .post(url, body, header)
                .then((res) => {
                    if (res.data) {
                        const {
                            graphData
                        } = res.data;
                        const dateSeries = getDateSeries(graphData, true);
                        const dataSeries = [];
                        getSelectedDeviceDetails().map((item) => {
                            flatten(item.parameterIdList).map((par) => {
                                const result = {
                                    name: getParameterName(par, item.iotDeviceMapId),
                                    data: [],
                                };

                                dateSeries.forEach((timestamp) => {
                                    const record = graphData.filter((s) => {
                                        const localS = moment.utc(s.timestamp).local().format("DD-MMM");
                                        return s.parameterId === par && localS === timestamp;
                                    });
                                    const sample = isEmpty(record) ? null : floor(first(record).value, 2);
                                    result.data.push(sample);
                                });
                                dataSeries.push(result);
                            });
                        });

                        const dataSeriesResult = dataSeries.flatMap((s, i) => {
                            const barSeries = {
                                name: s.name,
                                type: "column",
                                yAxis: i,
                                data: s.data,
                                showInLegend: showLegend,
                            };

                            const validValues = s.data.filter((v) => v !== null && !isNaN(v));
                            const avg = validValues.length ?
                                validValues.reduce((a, b) => a + b, 0) / validValues.length :
                                0;

                            const avgLine = {
                                name: `${s.name} Average`,
                                type: "line",
                                yAxis: i,
                                data: Array(s.data.length).fill(floor(avg, 2)),
                                color: HighCharts.getOptions().colors[i],
                                dashStyle: "ShortDash",
                                marker: {
                                    enabled: false
                                },
                                enableMouseTracking: true,
                                showInLegend: true,
                            };

                            return [barSeries, avgLine];
                        });

                        const yAxisSeries = dataSeriesResult.map((s, i) => {
                            return {
                                labels: {
                                    style: {
                                        color: HighCharts.getOptions().colors[i],
                                    },
                                },
                                title: {
                                    text: "",
                                },
                                opposite: i % 2 === 0 ? true : false,
                            };
                        });

                        const result = {
                            ...chartConfiguration("column"),
                            xAxis: {
                                categories: dateSeries,
                                crosshair: true,
                            },
                            yAxis: yAxisSeries,
                            series: dataSeriesResult,
                            tooltip: {
                                shared: true,
                                crosshairs: true,
                                formatter: function() {
                                    let s = `<b>${this.x}</b><br/>`;
                                    const grouped = {};

                                    this.points.forEach((point) => {
                                        const isAvg = point.series.name.endsWith(" Average");
                                        const baseName = isAvg ?
                                            point.series.name.replace(" Average", "") :
                                            point.series.name;

                                        if (!grouped[baseName]) {
                                            grouped[baseName] = {};
                                        }
                                        if (isAvg) {
                                            grouped[baseName].avg = point;
                                        } else {
                                            grouped[baseName].value = point;
                                        }
                                    });

                                    Object.entries(grouped).forEach(([param, points]) => {
                                        if (points.value) {
                                            s += `<span style="color:${points.value.color}">\u25CF</span> ${param}: <b>${points.value.y}</b><br/>`;
                                        }
                                        if (points.avg) {
                                            s += `<span style="color:${points.avg.color}">\u25A0</span> ${param} Avg: <b>${points.avg.y}</b><br/>`;
                                        }
                                    });

                                    return s;
                                },
                            },

                        };
                        setBarChartData(result);
                        setBarChartDataLoaded(true);
                        setLastUpdatedTimestamp(moment().unix());
                        setLastUpdatedTimestampChart(oldTimestamps => {
                            oldTimestamps[CHART_NAMES.BAR_CHART] = moment().unix();
                            return { ...oldTimestamps
                            }
                        });
                    }
                })
                .catch((err) => {
                    if (err.response ? .data ? .statusText === "statusFalse") {
                        setBarChartData({
                            errorText: "Unable to fetch Data"
                        });
                        setBarChartDataLoaded(false);
                    }
                });
        }
    };


    const fetchHourlyBarChartData = (startDate, endDate, showLegend) => {
        if (token) {
            const url = `${API_BASE_URL}hourly-bar-chart`;
            const body = {
                dateRange: {
                    startDate: startDate,
                    endDate: endDate,
                },
                deviceDetails: getSelectedDeviceDetails(),
            };
            const header = {
                headers: {
                    Authorization: "Bearer " + token,
                    "Content-Type": "application/json",
                },
            };
            axios
                .post(url, body, header)
                .then((res) => {
                    if (res.data) {
                        const {
                            graphData
                        } = res.data;
                        const uniqueTimestamps = [
                            ...new Set(graphData.map((item) => item.timestamp)),
                        ].sort((a, b) => new Date(a) - new Date(b));

                        // Convert timestamps to local time strings
                        const dateSeries = uniqueTimestamps.map((ts) =>
                            moment(ts).format("HH:mm")
                        );

                        const tooltipDateMap = {};
                        uniqueTimestamps.forEach((ts) => {
                            const timeOnly = moment(ts).format("HH:mm");
                            const fullDate = moment(ts).format("YYYY-MM-DD HH:mm:ss");
                            tooltipDateMap[timeOnly] = fullDate;
                        });
                        const dataSeries = [];

                        getSelectedDeviceDetails().map((item) => {
                            flatten(item.parameterIdList).map((par) => {
                                const result = {
                                    name: getParameterName(par, item.iotDeviceMapId),
                                    data: [],
                                };
                                uniqueTimestamps.map((timestamp) => {
                                    const record = graphData.find(
                                        (s) => s.parameterId === par && s.timestamp === timestamp
                                    );
                                    const sample = record ? floor(record.value, 2) : null;
                                    result.data.push(sample);
                                });
                                dataSeries.push(result);
                            });
                        });

                        // Combine bar and line (trend) series using flatMap
                        const dataSeriesResult = dataSeries.flatMap((s, i) => {
                            const barSeries = {
                                name: s.name,
                                type: "column",
                                yAxis: i,
                                data: s.data,
                                showInLegend: showLegend,
                            };

                            const validValues = s.data.filter((v) => v !== null && !isNaN(v));
                            const avg = validValues.length ? (validValues.reduce((a, b) => a + b, 0) / validValues.length) : 0;
                            const avgLine = {
                                name: `${s.name} Average`,
                                type: "line", // flat line
                                yAxis: i,
                                data: Array(s.data.length).fill(floor(avg, 2)), // flat value across all points
                                color: HighCharts.getOptions().colors[i],
                                dashStyle: "ShortDash",
                                marker: {
                                    enabled: false
                                },
                                enableMouseTracking: true,
                                showInLegend: true,
                            };

                            return [barSeries, avgLine];
                        });

                        // yAxis based only on original data series count
                        const yAxisSeries = dataSeries.map((s, i) => {
                            return {
                                labels: {
                                    style: {
                                        color: HighCharts.getOptions().colors[i],
                                    },
                                },
                                title: {
                                    text: "",
                                    // style: {
                                    // 	color: HighCharts.getOptions().colors[i],
                                    // },
                                },
                                opposite: i % 2 === 0 ? true : false,
                            };
                        });

                        const result = {
                            ...chartConfiguration("column"),
                            xAxis: {
                                categories: dateSeries,
                                crosshair: true,
                            },
                            yAxis: yAxisSeries,
                            series: dataSeriesResult,
                            tooltip: {
                                shared: true,
                                crosshairs: true,
                                formatter: function() {
                                    const timeOnly = this.x;
                                    const fullDateTime = tooltipDateMap[timeOnly] || timeOnly;
                                    let s = `<b>${fullDateTime}</b><br/>`;
                                    this.points.forEach((point) => {
                                        s += `${point.series.name}: <b>${point.y}</b><br/>`;
                                    });
                                    return s;
                                },
                            },
                        };
                        setHourlyBarChartData(result);
                        setHourlyBarChartDataLoaded(true);
                        setLastUpdatedTimestamp(moment().unix());
                        setLastUpdatedTimestampChart((oldTimestamps) => {
                            oldTimestamps[CHART_NAMES.BAR_CHART] = moment().unix();
                            return { ...oldTimestamps
                            };
                        });
                    }
                })
                .catch((err) => {
                    if (err.response ? .data ? .statusText === "statusFalse") {
                        setHourlyBarChartData({
                            errorText: "Unable to fetch Data"
                        });
                        setHourlyBarChartDataLoaded(false);
                    }
                });
        }
    };

    const fetchDataByChartName = () => {
        switch (chartName) {
            case "gauge_chart":
                return fetchGaugeData();
            case "metric_chart":
                return fetchMetricData();
            case "line_chart":
                return fetchLineChartData(lineChartDates ? .startDate || startDate, lineChartDates ? .endDate || endDate);
            case "bar_chart":
                return fetchBarChartData(barChartDates ? .monthStartDate || monthStartDate, barChartDates ? .monthEndDate || monthEndDate);
            case "hourly_bar_chart":
                return fetchHourlyBarChartData(hourlyBarChartDates ? .startDate || startDate, hourlyBarChartDates ? .endDate || endDate);
            case "cal_chart":
                return fetchCalData();
            default:
        }
    };

    const getValue = () => {
        return gaugeData.parameterValue ? gaugeData.parameterValue : gaugeData.value;
    };

    const getUserPerameters = useCallback(() => {
        if (!token) {
            return;
        }
        const parameterUrl = `${API_BASE_URL}user/${currentUser?.userEmailId}/parameter`;
        const header = {
            headers: {
                Authorization: "Bearer " + token,
                "Content-Type": "application/json",
            },
        };
        dispatch(getUserParameters(parameterUrl, header, history));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token]);

    const menu = ( <
        Menu >
        <
        Menu.Item key = "1"
        onClick = {
            () => {
                editWidgetDetails(id);
            }
        } >
        Edit <
        /Menu.Item> <
        Menu.Item key = "2"
        onClick = {
            () => onRemove(id)
        } >
        Delete <
        /Menu.Item> {
            (chartName === "gauge_chart") && ( //chartName === "line_chart" || chartName === "bar_chart" || */*/*/ for future developement \*\*\*
                <
                Menu.Item key = "3"
                onClick = {
                    () => {
                        if (chartName === "gauge_chart") {
                            setShowGaugeValue(!showGaugeValue);
                        }
                        // else { 
                        // setShowInLeagends(!showInLegend);
                        // const refreshTimeInSeconds = chartName === "line_chart" ? refreshTimeInSeconds1 : refreshTimeInSeconds2;
                        // chartName === "line_chart"
                        // ? setInterval(() => {
                        // fetchLineChartData(lineChartDates?.startDate || startDate, lineChartDates?.endDate || endDate, showInLegend ? false : true);
                        // }, refreshTimeInSeconds)
                        // : setInterval(() => {
                        // fetchBarChartData(barChartDates?.startDate || startDate, barChartDates?.endDate || endDate, showInLegend ? false : true);
                        // }, refreshTimeInSeconds);
                        // }
                    }
                } >
                {
                    showInLegend && showGaugeValue ? "Hide Values" : "Show Values"
                } <
                /Menu.Item>
            )
        } <
        /Menu>
    );

    const renderChartMenu = () => ( <
        div style = {
            {
                float: "right"
            }
        }
        className = "chartMenu" >
        <
        Dropdown.Button overlay = {
            menu
        }
        className = "chartMenuOptions" / >
        <
        /div>
    );

    const renderHeader = (title, chartName) => ( <
        div className = "container-fluid headerContainer" >
        <
        div className = "row align-items-center" >
        <
        div className = "col-sm-4 headerTitle" > {
            title
        } < /div>

        <
        div className = "col-sm-7 d-flex justify-content-end align-items-center" >
        <
        span style = {
            {
                marginRight: '10px'
            }
        } > Select Month: < /span> <
        MonthPicker placeholder = "Select Month"
        onChange = {
            (value) => {
                if (!value) return;

                const userTimeZone = moment.tz.guess();
                const startLocal = moment.tz(value.startOf("month").format("YYYY-MM-DD"), userTimeZone);
                const endLocal = moment.tz(value.endOf("month").format("YYYY-MM-DD"), userTimeZone);

                const startDate = startLocal.startOf("day").utc().format();
                const endDate = endLocal.endOf("day").subtract(1, "day").utc().format();

                if (chartName === "bar_chart") {
                    fetchBarChartData(startDate, endDate);
                    setBarChartDates({
                        startDate,
                        endDate
                    });
                }

                setDateRange([startLocal, endLocal]);
            }
        }
        /> <
        /div>

        <
        div className = "col-sm-1 d-flex justify-content-end align-items-center"
        style = {
            {
                top: "-10px"
            }
        } > {
            renderChartMenu()
        } <
        /div> <
        /div> <
        /div>
    );

    const [selectedDate, setSelectedDate] = useState(null);
    const handleDateChange = (date) => {
        if (!date) {
            setSelectedDate(null);
            fetchLineChartData(null, null);
            return;
        }

        const userTimeZone = moment.tz.guess();

        const newSelectedDate = moment.tz(date.format("YYYY-MM-DD"), userTimeZone)
            .startOf("day")
            .utc()
            .format();

        setSelectedDate(newSelectedDate);
        fetchLineChartData(newSelectedDate, newSelectedDate);
    };

    const renderLineChartHeader = (title) => ( <
        div className = "container-fluid headerContainer" >
        <
        div className = "row" >
        <
        div className = "col-sm-4 headerTitle" > {
            title
        } < /div> <
        div className = "col-sm-7 alignCenter"
        style = {
            {
                display: 'flex',
                justifyContent: 'space-between'
            }
        } >
        <
        div style = {
            {
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'flex-end',
                width: '100%'
            }
        } >
        <
        span style = {
            {
                marginRight: '10px'
            }
        } > Select Date: < /span> <
        DatePicker value = {
            selectedDate ? moment(selectedDate, 'YYYY-MM-DD') : null
        }
        onChange = {
            handleDateChange
        }
        /> <
        /div> <
        /div> <
        div className = "col-sm-1"
        style = {
            {
                top: "-10px"
            }
        } > {
            renderChartMenu("line_chart")
        } <
        /div> <
        /div> <
        /div>
    );


    const [selectedHourlyBarChartDate, setSelectedHourlyBarChartDate] = useState(moment());
    const handleHourlyBarChartDateChange = (date) => {
        if (!date) {
            setSelectedHourlyBarChartDate(null);
            fetchHourlyBarChartData(null, null);
            return;
        }

        setSelectedHourlyBarChartDate(date);

        const userTimeZone = moment.tz.guess();

        const newSelectedDate = moment.tz(date.format("YYYY-MM-DD"), userTimeZone)
            .startOf("day")
            .utc()
            .format();

        setSelectedHourlyBarChartDate(newSelectedDate);
        fetchHourlyBarChartData(newSelectedDate, newSelectedDate);
    };

    const renderHourlyBarChartHeader = (title) => ( <
        div className = "container-fluid headerContainer" >
        <
        div className = "row align-items-center" >
        <
        div className = "col-sm-4 headerTitle" > {
            title
        } < /div>

        <
        div className = "col-sm-7 d-flex justify-content-end align-items-center" >
        <
        span style = {
            {
                marginRight: '10px'
            }
        } > Select Date: < /span> <
        DatePicker value = {
            selectedDate ? moment(selectedDate, 'YYYY-MM-DD') : null
        }
        onChange = {
            handleHourlyBarChartDateChange
        }
        /> <
        /div>

        <
        div className = "col-sm-1 d-flex justify-content-end align-items-center"
        style = {
            {
                top: "-10px"
            }
        } > {
            renderChartMenu("hourly_bar_chart")
        } <
        /div> <
        /div> <
        /div>
    );

    const renderGaugeChart = () => {
        if (gaugeData.errorText) {
            return <div className = "gaugeChartError" > {
                gaugeData.errorText
            } < /div>;
        }

        if (gaugeData.dataLoaded) {
            const uomName = !isEmpty(parametersList) &&
                first(parametersList.filter((p) => p.parameterId === gaugeData ? .parameterId)) ? .uomDisplayName;

            return ( <
                >
                <
                ReactSpeedometer forceRender = {
                    true
                }
                ringWidth = {
                    40
                }
                height = {
                    150
                }
                width = {
                    240
                }
                minValue = {
                    gaugeData.minValue
                }
                maxValue = {
                    gaugeData.maxValue
                }
                paddingVertical = {
                    0
                }
                maxSegmentLabels = {!showGaugeValue ? 0 : null
                }
                customSegmentStops = {
                    [
                        gaugeData.minValue,
                        gaugeData.lowLowValue,
                        gaugeData.lowValue,
                        gaugeData.highValue,
                        gaugeData.highHighValue,
                        gaugeData.maxValue,
                    ]
                }
                segmentColors = {
                    segmentColors
                }
                value = {
                    getValue()
                }
                /> <
                div className = "alignCenter" > {
                    uomName || " "
                } < /div> <
                div className = "alignCenter"
                style = {
                    {
                        marginTop: 5
                    }
                } > {
                    `Last Update ${moment(lastUpdatedTimestampChart[CHART_NAMES.GAUGE_CHART] * 1000).format("MM/DD/YYYY, h:mm:ss A")}`
                } { /* {`Last Update ${moment(lastUpdatedTimestampChart * 1000).format("MM/DD/YYYY, h:mm:ss A")}`} */ } <
                /div> <
                />
            );
        } else {
            return <Loader / > ;
        }
    };

    const getMetricColors = (parameterId, value) => {
        let metricColor = "";
        deviceParameterBoundary.map((s) => {
            const obj = s[parameterId];
            const res = !isEmpty(obj) &&
                Object.values(obj).map((val, ind) => {
                    if (val >= value) {
                        return ind;
                    } else {
                        return 0;
                    }
                });
            const result = first(compact(res));
            metricColor = segmentColors[result - 1];
        });
        return metricColor;
    };

    const renderMetricChart = () => {
        if (metricData && metricData[0] ? .errorText) {
            return <div className = "calMetricChartNoData" > Unable to fetch data < /div>;
        } else if (metricDataLoaded && metricData.length === 0) {
            return <div className = "calMetricChartNoData" > No Data found < /div>;
        }

        if (metricDataLoaded) {
            return ( <
                div className = "row calMetricChartContainer" > {
                    metricData.length > 0 &&
                    metricData.map((metric, i) => {
                        const selectedParameter = parameter.find((val) => val.parameterId === metric.parameterId);
                        return ( <
                            div className = "col-sm-3"
                            key = {
                                i
                            }
                            style = {
                                {
                                    margin: "10px auto",
                                    padding: "10px",
                                    border: "2px solid #ddd",
                                    borderRadius: "15px",
                                    boxShadow: "2px 2px 10px rgba(0, 0, 0, 0.1)",
                                    backgroundColor: "#fff",
                                    textAlign: "center",
                                }
                            } >
                            <
                            div className = "alignCenter" > {
                                getParameterName(metric.parameterId, metric.iotDeviceMapId)
                            } <
                            /div> <
                            div style = {
                                {
                                    fontSize: 30,
                                    color: getMetricColors(metric.parameterId, metric.value),
                                }
                            } >
                            {
                                metric.value
                            } <
                            /div> <
                            div className = "alignCenter" > {
                                selectedParameter ? .uomDisplayName === " " ? " " : selectedParameter ? .uomDisplayName
                            } <
                            /div> <
                            div className = "alignCenter" > {
                                `${getLastUpdatedTimestampChart(CHART_NAMES.METRIC_CHART)}`
                            } { /* {`Last Update ${moment(lastUpdatedTimestamp * 1000).format("MM/DD/YYYY, h:mm:ss A")}`} */ } <
                            /div> <
                            /div>
                        );
                    })
                } <
                /div>
            );
        } else {
            return <Loader / > ;
        }
    };

    const renderCalChart = () => {
        if (calculatedCalValue ? .value !== undefined && calculatedCalValue ? .value !== null) {
            return ( <
                div className = "row calMetricChartContainer" >
                <
                div className = "col-sm-12 text-center"
                style = {
                    {
                        margin: "0 auto"
                    }
                } >
                <
                div className = "alignCenter" > {
                    customChartName || ""
                } <
                /div> {
                    calculatedCalValue ? .value !== undefined && ( <
                        div style = {
                            {
                                fontSize: 40
                            }
                        } > {
                            round(calculatedCalValue.value, 3)
                        } <
                        /div>
                    )
                } <
                div className = "alignCenter" > {
                    `Last Update ${getLastUpdatedTimestampChart(CHART_NAMES.CAL_CHART)}`
                } <
                /div> <
                /div> <
                /div>
            );
        } else {
            return (!isModalVisible && ( <
                div className = "text-center" >
                <
                Loader / >
                <
                /div>
            ));
        }
    };

    const renderHighChartContent = (data, dataLoaded) => {
        if (data && data.errorText) {
            return <div className = "highChartNoData" > Unable to fetch data < /div>;
        } else if (dataLoaded && data.length === 0) {
            return <div className = "highChartNoData" > No Data found < /div>;
        }

        if (dataLoaded) {
            return <Highchart chartOptions = {
                data
            }
            />;
        } else {
            return ( <
                div className = "highChartNoData" >
                <
                Loader / >
                <
                /div>
            );
        }
    };

    const displayCharts = () => {
        switch (chartName) {
            case "bar_chart":
                return ( <
                    > {
                        renderHeader("Daily Bar Chart", chartName)
                    } {
                        renderHighChartContent(barChartData, barChartDataLoaded)
                    } <
                    />
                );
            case "hourly_bar_chart":
                return ( <
                    > {
                        renderHourlyBarChartHeader("Hourly Bar Chart", chartName)
                    } {
                        renderHighChartContent(hourlyBarChartData, hourlyBarChartDataLoaded)
                    } <
                    />
                );
            case "gauge_chart":
                return ( <
                    div className = "width100" >
                    <
                    div className = "deviceDisplayNameHeder" > {
                        `${deviceDisplayName} - ${parameterDisplayName}`
                    } < /div> {
                        renderChartMenu()
                    } {
                        renderGaugeChart()
                    } <
                    /div>
                );
            case "line_chart":
                return ( <
                    > {
                        renderLineChartHeader("Line Chart", chartName)
                    } {
                        renderHighChartContent(lineChartData, lineChartDataLoaded)
                    } <
                    />
                );
            case "metric_chart":
                return ( <
                    div className = "width100" >
                    <
                    div className = "deviceDisplayNameHeder" > {
                        deviceDisplayName
                    } < /div> {
                        renderChartMenu()
                    } {
                        renderMetricChart()
                    } <
                    /div>
                );
            case "cal_chart":
                return ( <
                    div className = "width100" >
                    <
                    div className = "deviceDisplayNameHeder" > {
                        deviceDisplayName
                    } < /div> {
                        renderChartMenu()
                    } {
                        renderCalChart()
                    } <
                    /div>
                    // (!displayCalModal || !isEmpty(calculatedCalValue)) && (
                    // )
                );
            default:
                return <div / > ;
        }
    };

    const getChartWidth = () => {
        let chartWidth = 230;
        if (chartName === "gauge_chart" || chartName === "cal_chart") {
            chartWidth = 260;
        } else if (chartName === "metric_chart") {
            chartWidth = 550;
        } else if (chartName === "bar_chart" || chartName === "line_chart" || chartName === "hourly_bar_chart") {
            chartWidth = 1320;
        }
        return chartWidth;
    };

    const addWidget = (selectedData) => {
        console.log("addWidget function called with data:", selectedData);
        if (widgetDetails) {
            const isWidgetPresent = widgetDetails.filter((s) => s.id === selectedData.id);
            if (isEmpty(isWidgetPresent)) {
                const existingDeviceParameters = widgetDetails;
                const newDeviceParameters = [...existingDeviceParameters, selectedData];
                console.log("New Parameters:", newDeviceParameters);
                dispatch(updateSelectedParameters(newDeviceParameters));
            } else {
                const widgets = remove(widgetDetails, function(n) {
                    return n.id !== selectedData.id;
                });
                const newDeviceParameters = [...widgets, selectedData];
                console.log("New Parameters:", newDeviceParameters);
                dispatch(updateSelectedParameters(newDeviceParameters));
            }
        } else {
            console.log("New Parameters:", [selectedData]);
            dispatch(updateSelectedParameters([selectedData]));
        }
    };

    const handleAddClick = (parameterName, obj) => {
        if (!isNumeric(obj ? .value || obj ? .constantValue || "0")) {
            return setToastMessage("Calculations can't be performed on this parameter");
        }

        setInputList([
            ...inputList,
            {
                parameterName: parameterName,
                iotDeviceMapId: obj.iotDeviceMapId || "",
                parameterId: obj.parameterId || "",
                timestamp: obj.timestamp || "",
                value: obj.value || "",
                selectedOperator: "",
                constantValue: 0,
            },
        ]);
    };

    const handleRemoveClick = (index) => {
        const updatedList = inputList.filter((item, i) => i !== index);
        setInputList(updatedList);
    };

    const handleOk = () => {
        const calculatedRes = { ...first(inputList)
        };
        calculatedRes.value = updatedValue;
        calculatedRes.timestamp = moment().format("MM/DD/YYYY, h:mm:ss A");

        // Check if the calculation result is valid
        if (calculatedRes.value !== undefined && calculatedRes.value !== null) {
            setCalData([calculatedRes]);
            setIsModalVisible(false);

            const filterWidget = widgetDetails.filter((s) => s.id === id);

            if (!isEmpty(filterWidget)) {
                const widget = { ...first(filterWidget)
                };
                widget.displayCalModal = false;
                widget.calculatedCalValue = calculatedRes;
                widget.calculatedResult = inputList;
                widget.customChartName = customChartNameOption;

                // Add or update the widget based on whether it already exists
                addWidget(widget);
            }
        } else {
            // Reset the state or handle the case when the calculation result is empty
            console.error("Calculation result is empty.");
            setIsModalVisible(false);
        }
    };

    const operatorOptions = [{
            name: "Add",
            value: "+",
        },
        {
            name: "Subtract",
            value: "-",
        },
        {
            name: "Divide",
            value: "/",
        },
        {
            name: "Multiply",
            value: "*",
        },
    ];

    const calculate = (a) => (str) => (b) => {
        a = Number(a);
        b = Number(b);
        switch (str) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "/":
                return a / b;
            case "*":
                return a * b;
            default:
                return 0;
        }
    };

    const handleMenuClick = (e, index) => {
        const currentOperator = e.key;
        if (currentOperator !== inputList[index] ? .selectedOperator) {
            const updatedList = inputList.map((item, i) => {
                if (i === index) {
                    item.selectedOperator = currentOperator;
                    return item;
                }
                return item;
            });
            setInputList(updatedList);
        }
    };

    /**
     * Handling calChart calculation and updating total to display if it is changed
     *
     */
    const handleCalChartCalculation = () => {
        let finalCalculatedValue = 0;
        inputList.forEach((data, index) => {
            const {
                constantValue,
                value,
                parameterId
            } = data;
            const currentValue = parameterId ? value : constantValue;
            if (index === 0) {
                finalCalculatedValue = currentValue;
            } else {
                const {
                    selectedOperator: prevIndexOperator
                } = inputList[index - 1];
                if (currentValue > 0 && prevIndexOperator)
                    finalCalculatedValue = calculate(finalCalculatedValue)(prevIndexOperator)(currentValue);
            }
        });
        if (calculatedCalValue ? .value && calculatedCalValue ? .value !== finalCalculatedValue) {
            calculatedCalValue.timestamp = moment().format("MM/DD/YYYY, h:mm:ss A");
        }
        calculatedCalValue.value = finalCalculatedValue;
        setLastUpdatedTimestamp(moment().unix());
        setUpdatedValue(evaluate(correctNumberString(finalCalculatedValue.toString())));
    };

    const handleInputChange = (e, index) => {
        const list = [...inputList];
        const constantValue = e.target.value;
        list[index].constantValue = constantValue;
        setInputList(list);
    };

    const operatorMenu = (i) => ( <
        Menu onClick = {
            (e) => handleMenuClick(e, i)
        } > {
            operatorOptions.map((s, i) => ( <
                Menu.Item key = {
                    s.value
                } > {
                    s.name
                } < /Menu.Item>
            ))
        } <
        /Menu>
    );

    const getSelectedOperator = (selectedOperator) => {
        const result = operatorOptions.filter((s) => s.value === selectedOperator);
        return first(result) ? .name;
    };

    const displayOperators = () =>
        !isEmpty(calData) &&
        inputList.map((x, i) => {
            return ( <
                div key = {
                    i
                }
                style = {
                    {
                        marginBottom: 5
                    }
                } > {
                    x.parameterName === "Constant" ? ( <
                        div style = {
                            {
                                textAlign: "left",
                                marginBottom: 5
                            }
                        } >
                        <
                        div className = "constantFieldContainer"
                        style = {
                            {
                                float: "left",
                                marginTop: 2,
                                marginBottom: 2,
                                minWidth: 200,
                            }
                        } >
                        <
                        Input className = "constantField"
                        type = "number"
                        style = {
                            {
                                width: 170,
                                borderRadius: 5
                            }
                        }
                        placeholder = "Enter Value"
                        value = {
                            inputList[i] ? .constantValue
                        }
                        onChange = {
                            (e) => handleInputChange(e, i)
                        }
                        /> <
                        /div> {
                            i !== inputList.length - 1 && ( <
                                Dropdown overlay = {
                                    operatorMenu(i)
                                } >
                                <
                                Button style = {
                                    {
                                        width: 170
                                    }
                                } >
                                <
                                span style = {
                                    {
                                        float: "left"
                                    }
                                } > {
                                    getSelectedOperator(inputList[i].selectedOperator) || "Select Operator"
                                } <
                                /span>{" "} <
                                DownOutlined style = {
                                    {
                                        float: "right",
                                        marginTop: 5
                                    }
                                }
                                /> <
                                /Button> <
                                /Dropdown>
                            )
                        } <
                        MinusOutlined style = {
                            {
                                marginLeft: 5,
                                background: "red",
                                color: "white",
                                padding: 3,
                                fontSize: 13,
                                borderRadius: 7,
                                marginTop: 8,
                            }
                        }
                        onClick = {
                            () => handleRemoveClick(i)
                        }
                        /> <
                        /div>
                    ) : ( <
                        >
                        <
                        div style = {
                            {
                                textAlign: "left",
                                marginBottom: 5
                            }
                        } >
                        <
                        div className = "selectedParameterName"
                        style = {
                            {
                                float: "left",
                                marginTop: 2,
                                minWidth: 200
                            }
                        } > { /* {x.parameterName + " " + x.value} */ } {
                            x.parameterName
                        } <
                        /div> <
                        MinusOutlined style = {
                            {
                                marginLeft: 5,
                                background: "red",
                                color: "white",
                                padding: 3,
                                fontSize: 13,
                                borderRadius: 7,
                            }
                        }
                        onClick = {
                            () => handleRemoveClick(i)
                        }
                        /> <
                        /div> {
                            i !== inputList.length - 1 && ( <
                                Dropdown overlay = {
                                    operatorMenu(i)
                                } >
                                <
                                Button style = {
                                    {
                                        width: 170
                                    }
                                } >
                                <
                                span style = {
                                    {
                                        float: "left"
                                    }
                                } > {
                                    getSelectedOperator(inputList[i].selectedOperator) || "Select Operator"
                                } <
                                /span>{" "} <
                                DownOutlined style = {
                                    {
                                        float: "right",
                                        marginTop: 5
                                    }
                                }
                                /> <
                                /Button> <
                                /Dropdown>
                            )
                        } <
                        />
                    )
                } <
                /div>
            );
        });


    const renderCalParameters = () => {
        return (!isEmpty(calCalculationData) &&
            calCalculationData.map((s, i) => {
                const parameterName = getParameterName(s.parameterId, s.iotDeviceMapId);
                return ( <
                    div key = {
                        i
                    } >
                    <
                    div style = {
                        {
                            marginBottom: 5
                        }
                    } >
                    <
                    div >
                    <
                    div style = {
                        {
                            float: "left",
                            marginTop: 2,
                            minWidth: 200
                        }
                    } > {
                        parameterName
                    } < /div> <
                    Button style = {
                        {
                            background: "transparent",
                            border: "none",
                            boxShadow: "none",
                        }
                    }
                    onClick = {
                        () => handleAddClick(parameterName, s)
                    } >
                    <
                    PlusOutlined style = {
                        {
                            marginLeft: 5,
                            background: variables.PRIMARY_COLOR,
                            color: "white",
                            padding: "3px",
                            fontSize: "13px",
                            borderRadius: "7px",
                        }
                    }
                    /> <
                    /Button> <
                    /div> <
                    div style = {
                        {
                            color: variables.PRIMARY_COLOR,
                            fontWeight: "bold",
                            fontSize: 12,
                            clear: "both",
                        }
                    } >
                    {
                        s.value
                    } <
                    /div> <
                    /div> <
                    /div>
                );
            })
        );
    };

    const getView = () => {
        let newView = "MobileView";
        if (width > 1220) {
            newView = "DesktopView";
        } else if (width > 767) {
            newView = "TabView";
        }
        return newView;
    };

    useEffect(() => {
        getUserPerameters();
        fetchDataByChartName();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        // eslint-disable-next-line
        if (displayCalModal) {
            // Calling API data when edit is done from the user side
            if (!isEmpty(calCalculationData)) {
                fetchDataByChartName();
            }
            setIsModalVisible(true); // Set modal to be always visible when displayCalModal is true
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [displayCalModal]);

    const shouldRefresh = !displayCalModal && chartName !== "bar_chart" && chartName !== "hourly_bar_chart";

    useInterval(() => {
        if (shouldRefresh) {
            fetchDataByChartName();
        }
    }, (chartName === "line_chart" ? refreshTimeInSeconds1 : refreshTimeInSeconds2) * 1000);

    useEffect(() => {
        setCustomChartNameOption(customChartName);
    }, [customChartName]);

    useEffect(() => {
        if (chartName === "cal_chart") {
            handleCalChartCalculation();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [inputList]);

    const colWidth = getView() === "MobileView" ? 24 : 12;

    return ( <
        ChartsWrapper >
        <
        LayoutWrapper >
        <
        Row style = {
            rowStyle
        }
        gutter = {
            0
        }
        justify = "start" >
        <
        Col style = {
            colStyle
        } >
        <
        Card style = {
            {
                minHeight: 240,
                width: getChartWidth(),
                maxWidth: getChartWidth(),
            }
        } >
        <
        CardContent > {
            displayCharts()
        } < /CardContent> <
        /Card> <
        /Col> <
        /Row> <
        Modal title = "Apply Calculation"
        visible = {
            isModalVisible
        }
        closable = {
            true
        }
        width = {
            650
        }
        maskClosable = {
            false
        }

        cancelButtonProps = {
            {
                style: {
                    display: "none"
                }
            }
        }
        okButtonProps = {
            {
                disabled: isEmpty(inputList) || first(inputList).selectedOperator === "" || customChartNameOption === "",
            }
        }
        okText = "Apply Calculation"
        onOk = {
            handleOk
        }
        onCancel = {
            () => {
                setIsModalVisible(false);
            }
        } >
        <
        Row style = {
            {
                marginBottom: 20
            }
        } >
        <
        Col >
        <
        div style = {
            {
                float: "left",
                marginTop: 5,
                marginRight: 10
            }
        } > Chart Name: < /div> <
        /Col> <
        Col >
        <
        Input className = "CHART_NAMES"
        style = {
            {
                width: 430,
                borderRadius: 5
            }
        }
        placeholder = "Enter Chart Name"
        value = {
            customChartNameOption
        }
        onChange = {
            (e) => setCustomChartNameOption(e.target.value)
        }
        /> <
        /Col> <
        /Row> <
        Row >
        <
        Col span = {
            colWidth
        } > {
            renderCalParameters()
        } <
        div >
        <
        div style = {
            {
                float: "left",
                marginTop: 5,
                minWidth: 200
            }
        } > Constant < /div> <
        Button style = {
            {
                background: "transparent",
                border: "none",
                boxShadow: "none",
            }
        } >
        <
        PlusOutlined onClick = {
            () => handleAddClick("Constant", {})
        }
        style = {
            {
                marginLeft: 5,
                background: variables.PRIMARY_COLOR,
                color: "white",
                padding: "3px",
                fontSize: "13px",
                borderRadius: "7px",
            }
        }
        /> <
        /Button> <
        /div> <
        /Col> <
        Col span = {
            colWidth
        }
        className = "selectedParameterContainer" > {
            displayOperators()
        } {
            inputList.length > 1 && ( <
                div style = {
                    {
                        marginTop: "10%"
                    }
                } > {
                    `Calculated Value: ${round(updatedValue, 3)}`
                } < /div>
            )
        } <
        /Col> <
        /Row> <
        /Modal> <
        /LayoutWrapper> <
        ToastMaster open = {
            Boolean(toastMessage)
        }
        severity = "error"
        message = {
            toastMessage
        }
        onClose = {
            () => setToastMessage("")
        }
        /> <
        /ChartsWrapper>
    );
}