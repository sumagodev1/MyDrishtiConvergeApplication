import React, {
    lazy,
    Suspense
} from "react";
import {
    Route,
    Redirect,
    BrowserRouter as Router,
    Switch,
} from "react-router-dom";
import {
    useSelector
} from "react-redux";
import ErrorBoundary from "./ErrorBoundary";
import {
    PUBLIC_ROUTE
} from "./route.constants";
import Loader from "@components/utility/loader";

const Dashboard = lazy(() =>
    import ("./containers/Dashboard/Dashboard"));

const publicRoutes = [{
        path: PUBLIC_ROUTE.LANDING,
        exact: true,
        component: lazy(() =>
            import ("@containers/Auth/SignIn/SignIn")),
    },
    {
        path: PUBLIC_ROUTE.PAGE_404,
        component: lazy(() =>
            import ("@containers/Auth/404/404")),
    },
    {
        path: PUBLIC_ROUTE.PAGE_500,
        component: lazy(() =>
            import ("@containers/Auth/500/500")),
    },
    {
        path: PUBLIC_ROUTE.SIGN_IN,
        component: lazy(() =>
            import ("@containers/Auth/SignIn/SignIn")),
    },
    {
        path: PUBLIC_ROUTE.FORGOT_PASSWORD,
        component: lazy(() =>
            import ("@containers/Auth/ForgotPassword/ForgotPassword")),
    },
];

function PrivateRoute({
    children,
    ...rest
}) {
    const isLoggedIn = useSelector((state) => state.Auth.currentUser);
    return ( <
        Route { ...rest
        }
        render = {
            ({
                location
            }) =>
            isLoggedIn ? (
                children
            ) : ( <
                Redirect to = {
                    {
                        pathname: "/login",
                        state: {
                            from: location
                        },
                    }
                }
                />
            )
        }
        />
    );
}

export default function Routes() {
    return ( <
        ErrorBoundary >
        <
        Suspense fallback = { < Loader / >
        } >
        <
        Router >
        <
        Switch > {
            publicRoutes.map((route, index) => ( <
                Route key = {
                    index
                }
                path = {
                    route.path
                }
                exact = {
                    route.exact
                } >
                <
                route.component / >
                <
                /Route>
            ))
        } <
        PrivateRoute path = "/dashboard" >
        <
        Dashboard / >
        <
        /PrivateRoute> <
        /Switch> <
        /Router> <
        /Suspense> <
        /ErrorBoundary>
    );
}