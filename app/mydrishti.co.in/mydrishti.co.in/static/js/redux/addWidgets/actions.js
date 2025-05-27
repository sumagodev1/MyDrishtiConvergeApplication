const actions = {
    GET_USERS_DEVICE_REQUEST: "GET_USERS_DEVICE_REQUEST",
    GET_USERS_DEVICE_SUCCESS: "GET_USERS_DEVICE_SUCCESS",
    GET_USERS_DEVICE_ERROR: "GET_USERS_DEVICE_ERROR",

    GET_DEVICE_PARAMETER_REQUEST: "GET_DEVICE_PARAMETER_REQUEST",
    GET_DEVICE_PARAMETER_SUCCESS: "GET_DEVICE_PARAMETER_SUCCESS",
    GET_DEVICE_PARAMETER_ERROR: "GET_DEVICE_PARAMETER_ERROR",

    GET_GRAPH_DATA_REQUEST: "GET_GRAPH_DATA_REQUEST",
    GET_GRAPH_DATA_SUCCESS: "GET_GRAPH_DATA_SUCCESS",
    GET_GRAPH_DATA_ERROR: "GET_GRAPH_DATA_ERROR",

    UPDATE_SELECTED_PARAMS: "UPDATE_SELECTED_PARAMS",
    UPDATE_SELECTED_PARAMS_SUCCESS: "UPDATE_SELECTED_PARAMS_SUCCESS",
    UPDATE_SELECTED_PARAMS_ERROR: "UPDATE_SELECTED_PARAMS_ERROR",

    REMOVE_WIDGET_REQUEST: "REMOVE_WIDGET_REQUEST",
    REMOVE_WIDGET_SUCCESS: "REMOVE_WIDGET_SUCCESS",
    REMOVE_WIDGET_ERROR: "REMOVE_WIDGET_ERROR",

    getUserDevices: (url, header) => ({
        type: actions.GET_USERS_DEVICE_REQUEST,
        payload: {
            url,
            header
        },
    }),
    getDeviceParameters: (url, header) => ({
        type: actions.GET_DEVICE_PARAMETER_REQUEST,
        payload: {
            url,
            header
        },
    }),
    getGraphData: (url, body, header) => ({
        type: actions.GET_GRAPH_DATA_REQUEST,
        payload: {
            url,
            body,
            header
        },
    }),
    updateSelectedParameters: (params) => ({
        type: actions.UPDATE_SELECTED_PARAMS,
        payload: {
            params
        },
    }),
    removeWidget: (params) => ({
        type: actions.REMOVE_WIDGET_REQUEST,
        payload: {
            params
        },
    }),
};
export default actions;