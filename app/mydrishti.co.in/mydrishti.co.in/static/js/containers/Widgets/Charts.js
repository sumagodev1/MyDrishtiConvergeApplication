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

    // Update segment colors to match document - Red, Gold, Green, Gold, Red
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
        let metricColor = "#000000"; // Default to black
        deviceParameterBoundary.map((s) => {
            const obj = s[parameterId];
            if (!isEmpty(obj)) {
                // Get threshold values
                const minValue = obj.minValue || 0;
                const lowLowValue = obj.lowLowValue || 0;
                const lowValue = obj.lowValue || 0;
                const highValue = obj.highValue || 0;
                const highHighValue = obj.highHighValue || 0;
                const maxValue = obj.maxValue || 0;
                
                // Apply colors according to document rules
                if ((value >= minValue && value <= lowLowValue) || (value >= highHighValue && value <= maxValue)) {
                    // Red: If value between lowLowValue and minValue OR between highHighValue and maxValue
                    metricColor = "#f87357";
                } else if ((value >= lowLowValue && value <= lowValue) || (value >= highValue && value <= highHighValue)) {
                    // Gold: If value between lowValue and lowLowValue OR between highValue and highHighValue
                    metricColor = "gold";
                } else if (value > lowValue && value < highValue) {
                    // Green: If value strictly higher than lowValue and strictly lower than highValue
                    metricColor = "#8af857";
                } else {
                    // Default/Black: If value exceeds the maxValue or minValue
                    metricColor = "#000000";
                }
            }
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