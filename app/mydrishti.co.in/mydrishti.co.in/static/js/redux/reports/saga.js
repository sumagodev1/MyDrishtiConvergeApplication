import {
    all,
    takeEvery,
    fork,
    call,
    put
} from "redux-saga/effects";
import actions from "./actions";
import ApiSauce from "@services/apiSauce";
import {
    CodeSandboxCircleFilled
} from "@ant-design/icons";

function getRequest(payload) {
    const {
        url,
        header
    } = payload;

    return ApiSauce.getWithCustomHeader(url, header);
}

export function* getUserParameters() {
    yield takeEvery("GET_USER_PARAMETER_REQUEST", function*({
        payload
    }) {
        const response = yield call(getRequest, payload);
        if (response.success) {
            yield put({
                type: actions.GET_USER_PARAMETER_SUCCESS,
                userParameterList: response.userParameterList,
            });
        } else {
            yield put({
                type: actions.GET_USER_PARAMETER_ERROR
            });
            payload.history.push("/");
        }
    });
}

export function* getReportsSettings() {
    yield takeEvery("GET_REPORT_SETTINGS_REQUEST", function*({
        payload
    }) {
        if (payload.params) {
            yield put({
                type: actions.GET_REPORT_SETTINGS_SUCCESS,
                reportsSettings: payload.params,
            });
            localStorage.setItem(
                "reportsSettingsStorage",
                JSON.stringify(payload.params)
            );
        } else {
            yield put({
                type: actions.GET_REPORT_SETTINGS_ERROR
            });
        }
    });
}
export function* setMetricChartData() {

    yield takeEvery("GET_METRIC_DATA_REQUEST", function*({
        payload
    }) {
        const response = yield call(getRequest, payload);

        if (response) {
            yield put({
                type: "GET_METRIC_DATA_SUCCESS",
                getMetricsdata: response.graphData,
            });
        } else {
            yield put({
                type: actions.GET_METRIC_DATA_ERROR
            });
        }
    });
}

export default function* rootSaga() {
    yield all([
        fork(getReportsSettings),
        fork(getUserParameters),
        fork(setMetricChartData),
    ]);
}