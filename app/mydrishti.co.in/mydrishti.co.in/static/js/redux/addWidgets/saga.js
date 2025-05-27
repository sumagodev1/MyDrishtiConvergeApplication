import {
    all,
    takeEvery,
    fork,
    call,
    put
} from "redux-saga/effects";
import actions from "./actions";
import ApiSauce from "@services/apiSauce";

function getRequest(payload) {
    const {
        url,
        header
    } = payload;
    return ApiSauce.getWithCustomHeader(url, header);
}

function postRequest(payload) {
    const {
        url,
        body,
        header
    } = payload;
    return ApiSauce.post(url, body, header);
}

export function* getUserDevices() {
    yield takeEvery("GET_USERS_DEVICE_REQUEST", function*({
        payload
    }) {
        const response = yield call(getRequest, payload);
        if (response.success) {
            yield put({
                type: actions.GET_USERS_DEVICE_SUCCESS,
                deviceList: response.deviceList,
            });
        } else {
            yield put({
                type: actions.GET_USERS_DEVICE_ERROR
            });
        }
    });
}

export function* getDeviceParameters() {
    yield takeEvery("GET_DEVICE_PARAMETER_REQUEST", function*({
        payload
    }) {
        const response = yield call(getRequest, payload);
        if (response.success) {
            yield put({
                type: actions.GET_DEVICE_PARAMETER_SUCCESS,
                deviceParameterList: response.deviceParameterList,
            });
        } else {
            yield put({
                type: actions.GET_DEVICE_PARAMETER_ERROR
            });
        }
    });
}

export function* getGraphData() {
    yield takeEvery("GET_GRAPH_DATA_REQUEST", function*({
        payload
    }) {
        const response = yield call(postRequest, payload);
        if (response.graphData) {
            yield put({
                type: actions.GET_GRAPH_DATA_SUCCESS,
                graphData: response.graphData,
            });
        } else {
            yield put({
                type: actions.GET_GRAPH_DATA_ERROR
            });
        }
    });
}

export function* updateSelectedParameters() {
    yield takeEvery("UPDATE_SELECTED_PARAMS", function*({
        payload
    }) {
        if (payload.params) {
            yield put({
                type: actions.UPDATE_SELECTED_PARAMS_SUCCESS,
                displayWidget: payload.params,
            });
            localStorage.setItem("widgetSettings", JSON.stringify(payload.params));
        } else {
            yield put({
                type: actions.UPDATE_SELECTED_PARAMS_ERROR
            });
        }
    });
}

export function* removeWidget() {
    yield takeEvery("REMOVE_WIDGET_REQUEST", function*({
        payload
    }) {
        if (payload.params) {
            localStorage.setItem("widgetSettings", JSON.stringify(payload.params));
            yield put({
                type: actions.REMOVE_WIDGET_SUCCESS,
                displayWidget: payload.params,
            });
        } else {
            yield put({
                type: actions.REMOVE_WIDGET_ERROR
            });
        }
    });
}

export default function* rootSaga() {
    yield all([
        fork(getUserDevices),
        fork(getDeviceParameters),
        fork(updateSelectedParameters),
        fork(getGraphData),
        fork(removeWidget),
    ]);
}