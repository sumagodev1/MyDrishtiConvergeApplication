import {
    all
} from "redux-saga/effects";
import authSagas from "@redux/auth/saga";
import reportsSagas from "@redux/reports/saga";
import addWidgets from "@redux/addWidgets/saga";

export default function* rootSaga() {
    yield all([authSagas(), addWidgets(), reportsSagas()]);
}