import actions from "./actions";

const initialState = {
    failure: false,
    isLoading: false,
    errorMessage: {},
    successMessage: "",
    deviceParameterList: [],
    graphData: [],
    deviceList: [],
    displayWidget: [],
};

export default function userReducer(state = initialState, action) {
    switch (action.type) {
        case actions.GET_USERS_DEVICE_REQUEST:
            return { ...state,
                isLoading: true
            };
        case actions.GET_USERS_DEVICE_SUCCESS:
            return {
                ...state,
                isLoading: false,
                errorMessage: "",
                deviceList: action.deviceList,
            };
        case actions.GET_USERS_DEVICE_ERROR:
            return {
                ...state,
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            };

        case actions.GET_DEVICE_PARAMETER_REQUEST:
            return { ...state,
                isLoading: true
            };
        case actions.GET_DEVICE_PARAMETER_SUCCESS:
            return {
                ...state,
                failure: false,
                isLoading: false,
                deviceParameterList: action.deviceParameterList,
            };
        case actions.GET_DEVICE_PARAMETER_ERROR:
            return {
                ...state,
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            };

        case actions.GET_GRAPH_DATA_REQUEST:
            return { ...state,
                isLoading: true
            };
        case actions.GET_GRAPH_DATA_SUCCESS:
            return {
                ...state,
                failure: false,
                isLoading: false,
                graphData: action.graphData,
            };
        case actions.GET_GRAPH_DATA_ERROR:
            return {
                ...state,
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            };

        case actions.UPDATE_SELECTED_PARAMS:
            return { ...state,
                isLoading: true
            };
        case actions.UPDATE_SELECTED_PARAMS_SUCCESS:
            return {
                ...state,
                failure: false,
                isLoading: false,
                displayWidget: action.displayWidget,
            };
        case actions.UPDATE_SELECTED_PARAMS_ERROR:
            return {
                ...state,
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            };

        case actions.REMOVE_WIDGET_REQUEST:
            return { ...state,
                isLoading: true
            };
        case actions.REMOVE_WIDGET_SUCCESS:
            return {
                ...state,
                failure: false,
                isLoading: false,
                displayWidget: action.displayWidget,
            };
        case actions.REMOVE_WIDGET_ERROR:
            return {
                ...state,
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            };
        default:
            return state;
    }
}