import React, {
    lazy,
    Suspense
} from "react";
import {
    Route,
    useRouteMatch,
    Switch
} from "react-router-dom";
import {
    isEmpty
} from "lodash";
import Loader from "@components/utility/loader";

const routes = [{
        path: "",
        component: lazy(() =>
            import ("@containers/Widgets/Widgets")),
        exact: true,
    },
    {
        path: "report",
        component: lazy(() =>
            import ("@containers/Report/Report")),
    },
    {
        path: "profile",
        component: lazy(() =>
            import ("@containers/Profile/Profile")),
    },
    {
        path: "settings",
        component: lazy(() =>
            import ("@containers/Settings/Settings")),
    },
];

export default function AppRouter(props) {
    const {
        url
    } = useRouteMatch();
    return ( <
        Suspense fallback = { < Loader / >
        } >
        <
        Switch > {
            routes.map((route, idx) => ( <
                Route exact = {
                    route.exact
                }
                key = {
                    idx
                }
                path = {
                    `${url}/${route.path}`
                } > {
                    isEmpty(route.path) ? ( <
                        route.component editWidgetDetails = {
                            props.editWidgetDetails
                        }
                        />
                    ) : ( <
                        route.component / >
                    )
                } <
                /Route>
            ))
        } <
        /Switch> <
        /Suspense>
    );
}