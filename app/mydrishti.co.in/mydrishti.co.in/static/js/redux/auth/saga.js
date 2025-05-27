import {
    all,
    takeEvery,
    put,
    fork,
    call
} from "redux-saga/effects";
import {
    getToken,
    clearToken
} from "@lib/helpers/utility";
import actions from "./actions";
import ApiSauce from "@services/apiSauce";

function postRequest(payload) {
    return ApiSauce.post(payload.url, payload.body, {});
}

export function* loginRequest() {
    yield takeEvery("LOGIN_REQUEST", function*({
        payload
    }) {
        try {
            const response = yield call(postRequest, payload);
            const {
                accessToken,
                user
            } = response;
            if (accessToken) {
                yield put({
                    type: actions.LOGIN_SUCCESS,
                    accessToken: accessToken,
                    currentUser: user,
                });
            } else {
                yield put({
                    type: actions.LOGIN_ERROR,
                    errorMessage: response.data ? .statusText,
                });
            }
        } catch (error) {
            yield put({
                type: actions.LOGIN_ERROR,
                errorMessage: "Something went wrong. Please try after some time.",
            });
        }
    });
}

export function* loginSuccess() {
    yield takeEvery(actions.LOGIN_SUCCESS, function*(payload) {
        yield localStorage.setItem("accessToken", payload.accessToken);
        yield localStorage.setItem(
            "currentUser",
            JSON.stringify(payload.currentUser)
        );
    });
}

export function* loginError() {
    yield takeEvery(actions.LOGIN_ERROR, function*() {});
}

export function* logout() {
    yield takeEvery(actions.LOGOUT, function*({
        payload
    }) {
        // const response = yield call(logoutSession, payload);
        const message = "You have successfully logout";
        if (message) {
            yield clearToken();
            payload.history.push("/login");
        }
    });
}

export function* checkAuthorization() {
    yield takeEvery(actions.CHECK_AUTHORIZATION, function*() {
        const token = getToken().get("accessToken");
        if (token) {
            yield put({
                type: actions.LOGIN_SUCCESS,
                token,
                profile: "Profile",
            });
        }
    });
}

export default function* rootSaga() {
    yield all([
        fork(checkAuthorization),
        fork(loginRequest),
        fork(loginSuccess),
        fork(loginError),
        fork(logout),
    ]);
}