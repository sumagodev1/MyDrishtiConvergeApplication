import {
    createStore,
    applyMiddleware
} from "redux";
import thunk from "redux-thunk";
import createSagaMiddleware from "redux-saga";
import {
    composeWithDevTools
} from "redux-devtools-extension";
import rootReducer from "./root-reducer";
import rootSaga from "./root-saga";

const sagaMiddleware = createSagaMiddleware();
const middlewares = [thunk, sagaMiddleware];
const bindMiddleware = (middleware) => {
    if (process.env.NODE_ENV !== "production") {
        return composeWithDevTools(applyMiddleware(...middleware));
    }
    return applyMiddleware(...middleware);
};

const store = createStore(rootReducer, bindMiddleware(middlewares));
sagaMiddleware.run(rootSaga);
export {
    store
};