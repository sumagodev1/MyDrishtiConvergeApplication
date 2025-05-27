import actions from "./actions";
import Immutable from "seamless-immutable";

const initialState = Immutable({
    isLoading: false,
    errorMessage: {},
    successMessage: "",
    userParameterList: [],
    actionType: "",
    reportsSettings: [],
});

export default function reportsReducer(state = initialState, action) {
    switch (action.type) {
        case actions.GET_USER_PARAMETER_REQUEST:
            return Immutable.merge(state, {
                isLoading: true,
            });
        case actions.GET_USER_PARAMETER_SUCCESS:
            return Immutable.merge(state, {
                failure: false,
                isLoading: false,
                userParameterList: action.userParameterList,
            });
        case actions.GET_USER_PARAMETER_ERROR:
            return Immutable.merge(state, {
                failure: true,
                isLoading: false,
                errorMessage: action.error,
            });

        case actions.GET_REPORT_SETTINGS_REQUEST:
            return Immutable.merge(state, {
                isLoading: true,
                actionType: action.type,
            });
        case actions.GET_REPORT_SETTINGS_SUCCESS:
            return Immutable.merge(state, {
                isLoading: false,
                reportsSettings: action.reportsSettings,
            });
        case actions.GET_METRIC_DATA_SUCCESS:
            return Immutable.merge(state, {
                isLoading: false,
                metricChartData: action.payload,
            });
        case actions.GET_REPORT_SETTINGS_ERROR:
            return Immutable.merge(state, {
                isLoading: false,
                errorMessage: action.error,
            });
        default:
            return state;
    }
}