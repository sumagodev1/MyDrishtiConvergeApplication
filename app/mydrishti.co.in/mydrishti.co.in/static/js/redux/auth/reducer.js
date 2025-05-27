import actions from "./actions";
import Immutable from "seamless-immutable";

const initialState = Immutable({
    loginFailure: false,
    isFetching: false,
    errorMessage: "",
    successMessage: "",
    accessToken: "",
    currentUser: {},
    actionType: "",
});

export default function authReducer(state = initialState, action) {
    switch (action.type) {
        case actions.LOGIN_REQUEST:
            return Immutable.merge(state, {
                isFetching: true,
            });
        case actions.LOGIN_SUCCESS:
            return Immutable.merge(state, {
                loginFailure: false,
                isFetching: false,
                errorMessage: "",
                actionType: action.type,
                accessToken: action.accessToken,
                currentUser: action.currentUser,
            });
        case actions.LOGIN_ERROR:
            return Immutable.merge(state, {
                loginFailure: true,
                isFetching: false,
                actionType: action.type,
                errorMessage: action.errorMessage,
            });
        case actions.LOGOUT:
            return initialState;
        default:
            return state;
    }
}