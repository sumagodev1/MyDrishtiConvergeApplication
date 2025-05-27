const actions = {
    GET_USER_PARAMETER_REQUEST: "GET_USER_PARAMETER_REQUEST",
    GET_USER_PARAMETER_SUCCESS: "GET_USER_PARAMETER_SUCCESS",
    GET_USER_PARAMETER_ERROR: "GET_USER_PARAMETER_ERROR",

    GET_METRIC_DATA_REQUEST: "GET_METRIC_DATA_REQUEST",
    GET_METRIC_DATA_SUCCESS: "GET_METRIC_DATA_SUCCESS",
    GET_METRIC_DATA_ERROR: "GET_METRIC_DATA_ERROR",

    GET_REPORT_SETTINGS_REQUEST: "GET_REPORT_SETTINGS_REQUEST",
    GET_REPORT_SETTINGS_SUCCESS: "GET_REPORT_SETTINGS_SUCCESS",
    GET_REPORT_SETTINGS_ERROR: "GET_REPORT_SETTINGS_ERROR",

    getUserParameters: (url, header, history) => ({
        type: actions.GET_USER_PARAMETER_REQUEST,
        payload: {
            url,
            header,
            history
        },
    }),
    getReportsSettings: (params) => ({
        type: actions.GET_REPORT_SETTINGS_REQUEST,
        payload: {
            params
        },
    }),

    setMetricChartData: (data) => {
        return {
            type: actions.GET_METRIC_DATA_SUCCESS,
            payload: data,
        };
    },
};
export default actions;