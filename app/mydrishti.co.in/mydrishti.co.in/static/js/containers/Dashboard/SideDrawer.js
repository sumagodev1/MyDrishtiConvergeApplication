import React, {
    useEffect,
    useState
} from "react";
import {
    useSelector,
    useDispatch
} from "react-redux";
import {
    Drawer,
    Button,
    Radio,
    Divider,
    Space,
    Checkbox,
    Collapse,
} from "antd";
import axios from "axios";
import {
    variables
} from "@assets/styles/variables";
import {
    isEmpty,
    first,
    uniqBy
} from "lodash";
import {
    API_BASE_URL
} from "@config/site.config";
import {
    CHART_OPTIONS
} from "@config/constants";
import addWidgetActions from "@redux/addWidgets/actions";
import useWindowSize from "@lib/hooks/useWindowSize";
import reportsActions from "@redux/reports/actions";
import Loader from "@components/utility/loader";
import IntlMessages from "@components/utility/intlMessages";
import {
    loadState,
    loadStringState
} from "@lib/helpers/localStorage";
import {
    getPastDate,
    getCurrentDate
} from "@lib/helpers/utility";
import {
    dynamicSort
} from "@lib/helpers/utility";
import SideDrawerWrapper from "./SideDrawer.styles";

const {
    getUserDevices
} = addWidgetActions;
const {
    getUserParameters,
    getReportsSettings
} = reportsActions;
const {
    Panel
} = Collapse;

export default function SideDrawer({
    visible,
    onClose,
    addWidget,
    editWidgetSettings,
    widgetDetailsSettings,
}) {
    const {
        width
    } = useWindowSize();
    const {
        deviceList,
        isLoading
    } = useSelector((state) => state.AddWidgets);
    const {
        userParameterList
    } = useSelector((state) => state.Reports);
    const dispatch = useDispatch();
    const reportsSettingsStorage = loadState("reportsSettingsStorage");
    const [selectedChart, setSelectedChart] = useState("");
    const [selectedDevice, setSelectedDevice] = useState("");
    const [deviceAndParameterList, setDeviceAndParameterList] = useState([]);
    const [selectedDeviceAndParameterList, setselectedDeviceAndParameterList] =
    useState([]);
    const [selectedParameter, setSelectedParameter] = useState("");
    const [selectedParameterList, setSelectedParameterList] = useState([]);
    const [selectedDashboardUserDeviceList, setSelectedDashboardUserDeviceList] =
    useState([]);
    const [selectedReportUserDeviceList, setSelectedReportUserDeviceList] =
    useState([]);
    const [selectedReportUserParameterList, setSelectedReportUserParameterList] =
    useState([]);

    const [selectedUserDeviceObject, setSelectedUserDeviceObject] = useState({});
    const [selectedParameterObject, setSelectedParameterObject] = useState({});
    const [graphParameterList, setGraphParameterList] = useState([]);

    const [selectedReportUserDeviceListObj, setSelectedReportUserDeviceListObj] =
    useState([]);
    const [
        selectedReportUserParameterListObj,
        setSelectedReportUserParameterListObj,
    ] = useState([]);

    const [associatedParameterList, setAssociatedParameterList] = useState([]);
    const [isEditWidget, setIsEditWidget] = useState(false);
    const [allReportDeviceSelected, setAllReportsDeviceSelected] =
    useState(false);
    const [allReportParametersSelected, setAllReportParametersSelected] =
    useState(false);

    const {
        currentModule
    } = useSelector((state) => state.App);
    const moduleTitle = loadState("currentModule");
    const displayTitle = moduleTitle ? moduleTitle : currentModule;
    const currentUser = loadState("currentUser");
    const accessToken = loadStringState("accessToken");
    const isGaugeChart = selectedChart === "gauge_chart";
    const isMetricChart = selectedChart === "metric_chart";
    const isCalChart = selectedChart === "cal_chart";
    const noDeviceSelected = selectedDevice === "";
    const isReport =
        displayTitle === "sidebar.report.realtime" ||
        displayTitle === "sidebar.report.historical";

    const disableUpdateTiles =
        (noDeviceSelected || selectedParameter === "") &&
        (noDeviceSelected || isEmpty(selectedParameterList)) &&
        (isEmpty(selectedDashboardUserDeviceList) ||
            isEmpty(selectedParameterList));
    const disableUpdateData =
        isEmpty(selectedReportUserDeviceList) ||
        isEmpty(selectedReportUserParameterList);
    const disableDashboard =
        disableUpdateTiles && isEmpty(selectedDeviceAndParameterList);
    const isDisabled = isReport ? disableUpdateData : disableDashboard;
    const updateColor = isDisabled ?
        variables.DISABLE_COLOR :
        variables.PRIMARY_COLOR;

    const resetData = () => {
        setSelectedDevice("");
        setSelectedParameter("");
        setSelectedParameterList([]);
        setSelectedUserDeviceObject({});
        setSelectedParameterObject([]);
        setGraphParameterList([]);
        setSelectedDashboardUserDeviceList([]);
        setAssociatedParameterList([]);
        setselectedDeviceAndParameterList([]);
        setIsEditWidget(false);
    };

    useEffect(() => {
        resetData();
        if (!isEmpty(editWidgetSettings)) {
            const {
                chartName
            } = first(editWidgetSettings);
            fetchDeviceParameterList(chartName);
        }
    }, [editWidgetSettings]);

    useEffect(() => {
        if (!isEmpty(editWidgetSettings)) {
            const {
                chartName,
                selectedDeviceAndParameterList,
                parameter,
                device
            } =
            first(editWidgetSettings);
            const result = !isEmpty(deviceAndParameterList) &&
                deviceAndParameterList.filter(
                    (s) => s.deviceEntity.iotDeviceMapId === device.iotDeviceMapId
                );
            setAssociatedParameterList(first(result) ? .parameterEntityList);
            setSelectedChart(chartName);
            setselectedDeviceAndParameterList(selectedDeviceAndParameterList);
            setSelectedParameterObject(parameter);
            if (chartName === "metric_chart" || chartName === "cal_chart") {
                const parameterIds = [];
                parameter.map((s) => {
                    parameterIds.push(s.parameterId);
                });
                setSelectedParameterList(parameterIds);
                setGraphParameterList(parameter);
            }
            setSelectedUserDeviceObject(device);
            setSelectedDevice(device.iotDeviceMapId);
            setSelectedParameter(parameter.parameterId);
            setIsEditWidget(true);
        }
    }, [deviceAndParameterList, editWidgetSettings]);

    useEffect(() => {
        resetData();
        if (isReport && reportsSettingsStorage) {
            const {
                device,
                parameter,
                selectedReportUserDeviceList,
                selectedReportUserParameterList,
                allReportDeviceSelected,
                allReportParametersSelected,
            } = reportsSettingsStorage;
            setSelectedReportUserDeviceList(selectedReportUserDeviceList);
            setSelectedReportUserParameterList(selectedReportUserParameterList);
            setSelectedReportUserDeviceListObj(device);
            setSelectedReportUserParameterListObj(parameter);
            setAllReportsDeviceSelected(allReportDeviceSelected);
            setAllReportParametersSelected(allReportParametersSelected);
        }
    }, [moduleTitle]);

    useEffect(() => {
        if (visible) {
            const url = `${API_BASE_URL}user/${currentUser?.userEmailId}/device`;
            const header = {
                headers: {
                    Authorization: "Bearer " + accessToken,
                    "Content-Type": "application/json",
                },
            };
            dispatch(getUserDevices(url, header));
        }
    }, [accessToken, currentUser ? .userEmailId, dispatch, visible]);

    useEffect(() => {
        if (visible) {
            const parameterUrl = `${API_BASE_URL}user/${currentUser?.userEmailId}/parameter`;
            const header = {
                headers: {
                    Authorization: "Bearer " + accessToken,
                    "Content-Type": "application/json",
                },
            };
            dispatch(getUserParameters(parameterUrl, header));
        }
    }, [dispatch, visible, isReport, currentUser ? .userEmailId, accessToken]);

    const fetchDeviceParameterList = (chartType) => {
        const header = {
            headers: {
                Authorization: "Bearer " + accessToken,
                "Content-Type": "application/json",
            },
        };
        const body = {
            userEmailId: currentUser ? .userEmailId,
            type: chartType.replace(/_/g, "-"),
        };
        const url = `${API_BASE_URL}user/device-parameter`;
        axios.post(url, body, header).then((res) => {
            if (res.data) {
                const {
                    deviceParameter
                } = res.data;
                setDeviceAndParameterList(deviceParameter);
            }
        });
    };

    const renderDeviceParameterAccordian = () => ( <
        div className = "deviceParamAccordianContainer" > {
            deviceAndParameterList.map((device, idx) => ( <
                Collapse accordion key = {
                    device.deviceEntity.deviceDisplayName
                }
                style = {
                    {
                        marginBottom: "2%"
                    }
                }
                defaultActiveKey = {
                    isEditWidget && device.deviceEntity.deviceDisplayName
                } >
                <
                Panel header = {
                    device.deviceEntity.deviceDisplayName
                }
                key = {
                    isEditWidget ? device.deviceEntity.deviceDisplayName : idx
                } >
                {
                    Array.isArray(device.parameterEntityList) ? (
                        device.parameterEntityList
                        .sort(dynamicSort("parameterDisplayName"))
                        .map((parameter, idx) => ( <
                            Checkbox checked = {!isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId &&
                                        s.selectedParameter === parameter.parameterId
                                    )
                                )
                            }
                            key = {
                                idx
                            }
                            className = "deviceParamCheckbox"
                            disabled = {
                                selectedDeviceAndParameterList.length === 2 &&
                                isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedParameter === parameter.parameterId &&
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId
                                    )
                                )
                            }
                            onChange = {
                                (e) => {
                                    if (e.target.checked) {
                                        const result = {
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        };
                                        setselectedDeviceAndParameterList((arr) => [
                                            ...arr,
                                            result,
                                        ]);
                                    } else {
                                        const result = [{
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        }, ];
                                        const myArray = selectedDeviceAndParameterList.filter(
                                            (ar) =>
                                            !result.find(
                                                (rm) =>
                                                rm.selectedDevice === ar.selectedDevice &&
                                                ar.selectedParameter === rm.selectedParameter
                                            )
                                        );
                                        setselectedDeviceAndParameterList(myArray);
                                    }
                                }
                            } >
                            {
                                parameter.parameterDisplayName
                            } <
                            /Checkbox>
                        ))) : ( <
                        div style = {
                            {
                                padding: "1rem",
                                color: "gray"
                            }
                        } >
                        No parameters available. <
                        /div>
                    )
                } <
                /Panel> <
                /Collapse>
            ))
        } <
        /div>
    );

    const renderDeviceParameterBarChartAccordian = () => ( <
        div className = "deviceParamAccordianContainer" > {
            deviceAndParameterList.map((device, idx) => ( <
                Collapse accordion key = {
                    device.deviceEntity.deviceDisplayName
                }
                style = {
                    {
                        marginBottom: "2%"
                    }
                }
                defaultActiveKey = {
                    isEditWidget && device.deviceEntity.deviceDisplayName
                } >
                <
                Panel header = {
                    device.deviceEntity.deviceDisplayName
                }
                key = {
                    isEditWidget ? device.deviceEntity.deviceDisplayName : idx
                } >
                {
                    Array.isArray(device.parameterEntityList) ? (
                        device.parameterEntityList
                        .sort(dynamicSort("parameterDisplayName"))
                        .map((parameter, idx) => ( <
                            Checkbox checked = {!isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId &&
                                        s.selectedParameter === parameter.parameterId
                                    )
                                )
                            }
                            key = {
                                idx
                            }
                            className = "deviceParamCheckbox"
                            disabled = {
                                selectedDeviceAndParameterList.length === 1 &&
                                isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedParameter === parameter.parameterId &&
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId
                                    )
                                )
                            }
                            onChange = {
                                (e) => {
                                    if (e.target.checked) {
                                        const result = {
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        };
                                        setselectedDeviceAndParameterList((arr) => [
                                            ...arr,
                                            result,
                                        ]);
                                    } else {
                                        const result = [{
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        }, ];
                                        const myArray = selectedDeviceAndParameterList.filter(
                                            (ar) =>
                                            !result.find(
                                                (rm) =>
                                                rm.selectedDevice === ar.selectedDevice &&
                                                ar.selectedParameter === rm.selectedParameter
                                            )
                                        );
                                        setselectedDeviceAndParameterList(myArray);
                                    }
                                }
                            } >
                            {
                                parameter.parameterDisplayName
                            } <
                            /Checkbox>
                        ))
                    ) : ( <
                        div style = {
                            {
                                padding: "1rem",
                                color: "gray"
                            }
                        } >
                        No parameters available. <
                        /div>
                    )
                } <
                /Panel> <
                /Collapse>
            ))
        } <
        /div>
    );

    const renderDeviceParameterHourlyBarChartAccordian = () => ( <
        div className = "deviceParamAccordianContainer" > {
            deviceAndParameterList.map((device, idx) => ( <
                Collapse accordion key = {
                    device.deviceEntity.deviceDisplayName
                }
                style = {
                    {
                        marginBottom: "2%"
                    }
                }
                defaultActiveKey = {
                    isEditWidget && device.deviceEntity.deviceDisplayName
                } >
                <
                Panel header = {
                    device.deviceEntity.deviceDisplayName
                }
                key = {
                    isEditWidget ? device.deviceEntity.deviceDisplayName : idx
                } >
                {
                    Array.isArray(device.parameterEntityList) ? (
                        device.parameterEntityList
                        .sort(dynamicSort("parameterDisplayName"))
                        .map((parameter, idx) => ( <
                            Checkbox checked = {!isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId &&
                                        s.selectedParameter === parameter.parameterId
                                    )
                                )
                            }
                            key = {
                                idx
                            }
                            className = "deviceParamCheckbox"
                            disabled = {
                                selectedDeviceAndParameterList.length === 1 &&
                                isEmpty(
                                    selectedDeviceAndParameterList.filter(
                                        (s) =>
                                        s.selectedParameter === parameter.parameterId &&
                                        s.selectedDevice ===
                                        device.deviceEntity.iotDeviceMapId
                                    )
                                )
                            }
                            onChange = {
                                (e) => {
                                    if (e.target.checked) {
                                        const result = {
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        };
                                        setselectedDeviceAndParameterList((arr) => [
                                            ...arr,
                                            result,
                                        ]);
                                    } else {
                                        const result = [{
                                            selectedDevice: device.deviceEntity.iotDeviceMapId,
                                            selectedParameter: parameter.parameterId,
                                        }, ];
                                        const myArray = selectedDeviceAndParameterList.filter(
                                            (ar) =>
                                            !result.find(
                                                (rm) =>
                                                rm.selectedDevice === ar.selectedDevice &&
                                                ar.selectedParameter === rm.selectedParameter
                                            )
                                        );
                                        setselectedDeviceAndParameterList(myArray);
                                    }
                                }
                            } >
                            {
                                parameter.parameterDisplayName
                            } <
                            /Checkbox>
                        ))
                    ) : ( <
                        div style = {
                            {
                                padding: "1rem",
                                color: "gray"
                            }
                        } >
                        No parameters available. <
                        /div>
                    )
                } <
                /Panel> <
                /Collapse>
            ))
        } <
        /div>
    );


    const updateDashboardSettings = () => {
        const min = 1;
        const rand = min + Math.random();
        const parametersList = [];
        deviceAndParameterList.forEach((s) => {
            if (Array.isArray(s.parameterEntityList)) {
                s.parameterEntityList
                    .sort(dynamicSort("parameterDisplayName"))
                    .forEach((sy) => {
                        parametersList.push(sy);
                    });
            }
        });

        const widgetData = {
            id: `item_${rand}_${selectedChart}`,
            device: selectedUserDeviceObject,
            parameter: isGaugeChart ? selectedParameterObject : graphParameterList,
            chartName: selectedChart,
            startDate: getPastDate(6).format("YYYY-MM-DD"),
            endDate: getCurrentDate().format("YYYY-MM-DD"),
            selectedDeviceAndParameterList: selectedDeviceAndParameterList,
            calculatedCalValue: [],
            calculatedResult: [],
            customChartName: "",
            displayCalModal: selectedChart === "cal_chart" ? true : false,
        };
        if (!isEditWidget) {
            addWidget(widgetData);
        } else {
            const filterWidget = widgetDetailsSettings.filter(
                (s) => s.id === first(editWidgetSettings).id
            );
            const widget = first(filterWidget);
            widget.selectedDeviceAndParameterList = selectedDeviceAndParameterList;
            widget.parameter = isGaugeChart ?
                selectedParameterObject :
                graphParameterList;
            widget.device = selectedUserDeviceObject;
            widget.displayCalModal = selectedChart === "cal_chart" ? true : false;
            addWidget(widget);
        }
        localStorage.setItem("deviceList", JSON.stringify(deviceList));
        localStorage.setItem(
            "parametersList",
            JSON.stringify(uniqBy(parametersList, "parameterId"))
        );
        resetData();
        setSelectedChart("");
    };

    const updateReportSettings = () => {
        const min = 1;
        const rand = min + Math.random();
        const data = {
            id: `item_${rand}_reports_settings`,
            device: selectedReportUserDeviceListObj,
            parameter: selectedReportUserParameterListObj,
            allReportDeviceSelected: allReportDeviceSelected,
            allReportParametersSelected: allReportParametersSelected,
            selectedReportUserDeviceList: selectedReportUserDeviceList,
            selectedReportUserParameterList: selectedReportUserParameterList,
            startDate: getPastDate(6).format("YYYY-MM-DD"),
            endDate: getCurrentDate().format("YYYY-MM-DD"),
        };
        dispatch(getReportsSettings(data));
        onClose();
    };

    const displayGaugeParameterList = () =>
        isGaugeChart &&
        associatedParameterList && ( <
            Radio.Group value = {
                selectedParameter
            } >
            <
            Space direction = "vertical" > {
                associatedParameterList
                .sort(dynamicSort("parameterDisplayName"))
                .map((parameter, idx) => ( <
                    Radio value = {
                        parameter.parameterId
                    }
                    key = {
                        idx
                    }
                    disabled = {
                        noDeviceSelected
                    }
                    onChange = {
                        (e) => {
                            setSelectedParameter(e.target.value);
                            setSelectedParameterObject(parameter);
                        }
                    } >
                    {
                        parameter.parameterDisplayName
                    } <
                    /Radio>
                ))
            } <
            /Space> <
            /Radio.Group>
        );

    const isParameterIncludes = (parameter) => {
        return selectedParameterList.includes(parameter.parameterId);
    };

    const isCalOrMetric = () => {
        return isMetricChart || isCalChart;
    };

    const displayChartsDeviceParameterList = () =>
        !isEmpty(selectedChart) &&
        associatedParameterList &&
        associatedParameterList
        .sort(dynamicSort("parameterDisplayName"))
        .map((parameter, idx) => ( <
            div key = {
                idx
            } >
            <
            Checkbox checked = {
                isParameterIncludes(parameter)
            }
            key = {
                idx
            }
            disabled = {
                (!isCalOrMetric() && isEmpty(selectedDashboardUserDeviceList)) ||
                (isCalOrMetric() && noDeviceSelected) ||
                ((selectedChart === "cal_chart") &&
                    !isParameterIncludes(parameter) &&
                    selectedParameterList.length === 5) ||
                ((selectedChart === "metric_chart") &&
                    !isParameterIncludes(parameter) &&
                    selectedParameterList.length === 8)
            }
            onChange = {
                (e) => {
                    if (e.target.checked) {
                        setSelectedParameterList((arr) => [
                            ...arr,
                            parameter.parameterId,
                        ]);
                        const newParams = [...graphParameterList, parameter];
                        setGraphParameterList(newParams);
                    } else {
                        let remove = selectedParameterList.indexOf(
                            parameter.parameterId
                        );
                        setSelectedParameterList(
                            selectedParameterList.filter((_, i) => i !== remove)
                        );
                        const updateParams = graphParameterList.filter(
                            (x) => x.parameterId !== parameter ? .parameterId
                        );
                        setGraphParameterList(updateParams);
                    }
                }
            } >
            {
                parameter.parameterDisplayName
            } <
            /Checkbox> <
            br / >
            <
            /div>
        ));

    const updateButton = (onClick, buttonText) => ( <
        Button type = "primary"
        className = "updateButton"
        style = {
            {
                background: updateColor,
                borderColor: isDisabled ? "#d9d9d9" : updateColor,
                color: isDisabled ? "rgba(0,0,0,.25)" : "white",
            }
        }
        disabled = {
            isDisabled
        }
        onClick = {
            onClick
        } >
        <
        IntlMessages id = {
            buttonText
        }
        /> <
        /Button>
    );

    const displayDeviceListRadio = () => {
        if (
            selectedChart === "gauge_chart" ||
            selectedChart === "metric_chart" ||
            selectedChart === "cal_chart"
        ) {
            return ( <
                Radio.Group value = {
                    selectedDevice
                } >
                <
                Space direction = "vertical" > {
                    deviceAndParameterList.map((device, idx) => {
                        const {
                            deviceEntity
                        } = device;
                        return ( <
                            Radio value = {
                                deviceEntity.iotDeviceMapId
                            }
                            key = {
                                idx
                            }
                            onChange = {
                                (e) => {
                                    setSelectedParameter("");
                                    setSelectedParameterList([]);
                                    setSelectedDevice(e.target.value);
                                    setSelectedUserDeviceObject(deviceEntity);
                                    const result = !isEmpty(deviceAndParameterList) &&
                                        deviceAndParameterList.filter(
                                            (s) => s.deviceEntity.iotDeviceMapId === e.target.value
                                        );
                                    setAssociatedParameterList(
                                        first(result) ? .parameterEntityList
                                    );
                                }
                            } >
                            {
                                deviceEntity.deviceDisplayName
                            } <
                            /Radio>
                        );
                    })
                } <
                /Space> <
                /Radio.Group>
            );
        }
    };

    const renderGaugeChartDeviceParameters = () => {
        return ( <
            >
            <
            div className = "userDeviceList" > {
                displayDeviceListRadio()
            } < /div> <
            div className = "deviceParameterList" > {
                displayGaugeParameterList()
            } < /div> <
            />
        );
    };

    const renderDeviceAndParameters = () => {
        return ( <
            >
            <
            div className = "userDeviceList" > {
                displayDeviceListRadio()
            } < /div> <
            div className = "deviceParameterList" > {
                displayChartsDeviceParameterList()
            } <
            /div> <
            />
        );
    };

    const displayChartsDeviceParametersOptions = () => {
        switch (selectedChart) {
            case "bar_chart":
                return renderDeviceParameterBarChartAccordian();
            case "hourly_bar_chart":
                return renderDeviceParameterHourlyBarChartAccordian();
            case "gauge_chart":
                return renderGaugeChartDeviceParameters();
            case "line_chart":
                return renderDeviceParameterAccordian();
            case "metric_chart":
                return renderDeviceAndParameters();
            case "cal_chart":
                return renderDeviceAndParameters();
            default:
                return <div / > ;
        }
    };

    const renderChartsContent = () => ( <
        >
        <
        Radio.Group defaultValue = {
            selectedChart
        }
        value = {
            selectedChart
        }
        buttonStyle = "solid"
        checked = {
            selectedChart
        }
        className = "chartOptions"
        onChange = {
            (e) => {
                const value = e.target.value;
                setSelectedChart(value);
                fetchDeviceParameterList(value);
                resetData();
            }
        } >
        {
            CHART_OPTIONS.map((chart, idx) => ( <
                Radio.Button value = {
                    chart.value
                }
                key = {
                    idx
                }
                disabled = {!isEmpty(editWidgetSettings) && selectedChart !== chart.value
                } >
                <
                img alt = "chart"
                src = {
                    selectedChart === chart.value ? chart.selectedIcon : chart.icon
                }
                /> <
                div className = "chartOptionsTitle" >
                <
                IntlMessages id = {
                    `sidedrawer.${chart.label}`
                }
                /> <
                /div> <
                /Radio.Button>
            ))
        } <
        /Radio.Group> <
        Divider plain / > {
            selectedChart && displayChartsDeviceParametersOptions()
        } <
        />
    );

    useEffect(() => {
        if (
            isEmpty(selectedReportUserDeviceList) &&
            !isEmpty(selectedReportUserParameterList)
        ) {
            setSelectedReportUserParameterList([]);
        }
    }, [selectedReportUserDeviceList, selectedReportUserParameterList]);

    const renderReportContent = () => ( <
        div style = {
            {
                marginTop: "5%"
            }
        } >
        <
        Divider plain / >
        <
        div className = "userDeviceList" >
        <
        div className = "headings" > Select Devices < /div> {
            !isLoading && ( <
                Checkbox checked = {
                    allReportDeviceSelected
                }
                onChange = {
                    (e) => {
                        let arr = [];
                        deviceList.map((s) => {
                            arr.push(s.iotDeviceMapId);
                        });
                        if (e.target.checked) {
                            setAllReportsDeviceSelected(e.target.checked);
                            setSelectedReportUserDeviceList(arr);
                            setSelectedReportUserDeviceListObj(deviceList);
                        } else {
                            setAllReportsDeviceSelected(e.target.checked);
                            setSelectedReportUserDeviceList([]);
                            setSelectedReportUserDeviceListObj([]);
                            setAllReportParametersSelected(false);
                        }
                    }
                } >
                Select All <
                /Checkbox>
            )
        }

        {
            deviceList.map((device, idx) => ( <
                div key = {
                    idx
                } >
                <
                Checkbox checked = {
                    selectedReportUserDeviceList.includes(
                        device.iotDeviceMapId
                    )
                }
                key = {
                    idx
                }
                onChange = {
                    (e) => {
                        if (e.target.checked) {
                            setSelectedReportUserDeviceList((arr) => [
                                ...arr,
                                device.iotDeviceMapId,
                            ]);
                            const newParams = [
                                ...selectedReportUserDeviceListObj,
                                device,
                            ];
                            setSelectedReportUserDeviceListObj(newParams);
                            setAllReportsDeviceSelected(false);
                        } else {
                            let remove = selectedReportUserDeviceList.indexOf(
                                device.iotDeviceMapId
                            );
                            setSelectedReportUserDeviceList(
                                selectedReportUserDeviceList.filter((_, i) => i !== remove)
                            );
                            const updateParams = selectedReportUserDeviceListObj.filter(
                                (x) => x.iotDeviceMapId !== device ? .iotDeviceMapId
                            );
                            setSelectedReportUserDeviceListObj(updateParams);
                            setAllReportsDeviceSelected(false);
                        }
                    }
                } >
                {
                    device.deviceDisplayName
                } <
                /Checkbox> <
                br / >
                <
                /div>
            ))
        } <
        /div> <
        div className = "deviceParameterList" >
        <
        div className = "headings" > Select Parameters < /div> {
            !isLoading && ( <
                Checkbox checked = {
                    allReportParametersSelected
                }
                disabled = {
                    isEmpty(selectedReportUserDeviceList)
                }
                onChange = {
                    (e) => {
                        let arr = [];
                        userParameterList.map((s) => {
                            arr.push(s.parameterId);
                        });
                        if (e.target.checked) {
                            setAllReportParametersSelected(e.target.checked);
                            setSelectedReportUserParameterList(arr);
                            setSelectedReportUserParameterListObj(userParameterList);
                        } else {
                            setAllReportParametersSelected(e.target.checked);
                            setSelectedReportUserParameterList([]);
                            setSelectedReportUserParameterListObj([]);
                        }
                    }
                } >
                Select All <
                /Checkbox>
            )
        }

        {
            userParameterList.map((parameter, idx) => ( <
                div key = {
                    idx
                } >
                <
                Checkbox checked = {
                    selectedReportUserParameterList.includes(
                        parameter.parameterId
                    )
                }
                key = {
                    idx
                }
                disabled = {
                    isEmpty(selectedReportUserDeviceList)
                }
                onChange = {
                    (e) => {
                        if (e.target.checked) {
                            setSelectedReportUserParameterList((arr) => [
                                ...arr,
                                parameter.parameterId,
                            ]);
                            const newParams = [
                                ...selectedReportUserParameterListObj,
                                parameter,
                            ];
                            setSelectedReportUserParameterListObj(newParams);
                            setAllReportParametersSelected(false);
                        } else {
                            let remove = selectedReportUserParameterList.indexOf(
                                parameter.parameterId
                            );
                            setSelectedReportUserParameterList(
                                selectedReportUserParameterList.filter(
                                    (_, i) => i !== remove
                                )
                            );
                            const updateParams =
                                selectedReportUserParameterListObj.filter(
                                    (x) => x.parameterId !== parameter ? .parameterId
                                );
                            setSelectedReportUserParameterListObj(updateParams);
                            setAllReportParametersSelected(false);
                        }
                    }
                } >
                {
                    parameter.parameterDisplayName
                } <
                /Checkbox> <
                br / >
                <
                /div>
            ))
        } <
        /div> <
        /div>
    );

    const getView = () => {
        let newView = "MobileView";
        if (width > 1220) {
            newView = "DesktopView";
        } else if (width > 767) {
            newView = "TabView";
        }
        return newView;
    }

    const drewerWidth = getView() === "MobileView" ? 400 : 805;

    return ( <
        Drawer width = {
            drewerWidth
        }
        onClose = {
            () => {
                if (!isReport) {
                    resetData();
                    setSelectedChart("");
                }
                onClose();
            }
        }
        visible = {
            visible
        }
        bodyStyle = {
            {
                paddingBottom: 80
            }
        }
        footer = { <
            div
            style = {
                {
                    textAlign: "center",
                    borderTop: "transparent",
                }
            } >
            {
                isReport ?
                updateButton(updateReportSettings, "sidedrawer.updateData") :
                    updateButton(updateDashboardSettings, "sidedrawer.updateTiles")
            } <
            /div>
        } >
        <
        SideDrawerWrapper > {
            " "
        } {
            isReport ? renderReportContent() : renderChartsContent()
        } <
        /SideDrawerWrapper>

        {
            isLoading && < Loader / >
        } <
        /Drawer>
    );
}