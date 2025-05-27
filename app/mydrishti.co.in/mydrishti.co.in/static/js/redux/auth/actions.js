const actions = {
    CHECK_AUTHORIZATION: "CHECK_AUTHORIZATION",
    LOGIN_REQUEST: "LOGIN_REQUEST",
    LOGIN_SUCCESS: "LOGIN_SUCCESS",
    LOGIN_ERROR: "LOGIN_ERROR",
    LOGOUT: "LOGOUT",

    checkAuthorization: () => ({
        type: actions.CHECK_AUTHORIZATION
    }),
    login: (url, body) => ({
        type: actions.LOGIN_REQUEST,
        payload: {
            url,
            body
        },
    }),
    logout: (url, body, accessToken, history) => ({
        type: actions.LOGOUT,
        payload: {
            url,
            body,
            accessToken,
            history
        },
    }),
};
export default actions;