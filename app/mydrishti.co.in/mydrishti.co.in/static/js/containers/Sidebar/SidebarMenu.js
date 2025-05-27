import React from "react";
import {
    Link,
    useHistory,
    useRouteMatch
} from "react-router-dom";
import {
    loadState
} from "@lib/helpers/localStorage";
import {
    useSelector,
    useDispatch
} from "react-redux";
import Menu from "@components/uielements/menu";
import IntlMessages from "@components/utility/intlMessages";
import authAction from "@redux/auth/actions";
import appActions from "@redux/app/actions";

const {
    logout
} = authAction;
const {
    updateModuleTitle,
    changeCurrent
} = appActions;

const stripTrailingSlash = (str) => {
    if (str.substr(-1) === "/") {
        return str.substr(0, str.length - 1);
    }
    return str;
};

export default React.memo(function SidebarMenu({
    singleOption,
    submenuStyle,
    submenuColor,
    ...rest
}) {
    let match = useRouteMatch();
    const user = useSelector((state) => state.Auth.currentUser);
    const storedUser = loadState("currentUser");
    const currentUser = user ? .userId ? user : storedUser;
    const dispatch = useDispatch();
    const history = useHistory();

    const logOut = () => {
        const payload = {
            email: currentUser ? .email
        };
        dispatch(logout("logout_url", payload, currentUser ? .accessToken, history));
        dispatch(changeCurrent(["dashboard"]));
        dispatch(updateModuleTitle("sidebar.dashboard"));
        localStorage.setItem("currentModule", JSON.stringify("sidebar.dashboard"));
    };

    const {
        key,
        label,
        displayName,
        leftIcon,
        leftPosition
    } = singleOption;
    const url = stripTrailingSlash(match.url);

    return ( <
        Menu.Item key = {
            key
        } { ...rest
        }
        inlineindent = {
            leftPosition
        } > {
            key === "logout" ? ( <
                div className = "isoMenuHolder"
                style = {
                    submenuColor
                }
                onClick = {
                    logOut
                } >
                <
                img alt = "leftIcon"
                src = {
                    leftIcon
                }
                /> <
                div className = "nav-text" >
                <
                IntlMessages id = {
                    label
                }
                /> <
                /div> <
                /div>
            ) : ( <
                Link to = {
                    key === "dashboard" ? `${url}` : `${url}/${key}`
                }
                onClick = {
                    () => {
                        dispatch(updateModuleTitle(displayName));
                        localStorage.setItem("currentModule", JSON.stringify(displayName));
                    }
                } >
                <
                div className = "isoMenuHolder"
                style = {
                    submenuColor
                } >
                <
                img alt = "leftIcon"
                src = {
                    leftIcon
                }
                /> <
                div className = "nav-text" >
                <
                IntlMessages id = {
                    label
                }
                /> <
                /div> <
                /div> <
                /Link>
            )
        } <
        /Menu.Item>
    );
});