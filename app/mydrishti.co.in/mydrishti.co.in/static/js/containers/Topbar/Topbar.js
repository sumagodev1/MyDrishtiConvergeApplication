import React from "react";
import {
    useSelector,
    useDispatch
} from "react-redux";
import {
    Layout,
    Button
} from "antd";
import IntlMessages from "@components/utility/intlMessages";
import TopbarWrapper from "./Topbar.styles";
import appActions from "@redux/app/actions";
import dashboard from "@assets/images/dashboard.png";
import {
    loadState
} from "@lib/helpers/localStorage";

const {
    Header
} = Layout;
const {
    toggleCollapsed
} = appActions;

export default function Topbar(props) {
    const customizedTheme = useSelector(
        (state) => state.ThemeSwitcher.topbarTheme
    );
    const {
        collapsed,
        openDrawer
    } = useSelector((state) => state.App);
    const {
        currentModule
    } = useSelector((state) => state.App);
    const moduleTitle = loadState("currentModule");
    const displayTitle = moduleTitle ? moduleTitle : currentModule;
    const dispatch = useDispatch();
    const handleToggle = React.useCallback(
        () => dispatch(toggleCollapsed()), [dispatch]
    );
    const isCollapsed = collapsed && !openDrawer;
    const isReport =
        displayTitle === "sidebar.report.realtime" ||
        displayTitle === "sidebar.report.historical";

    const styling = {
        background: customizedTheme.backgroundColor,
        position: "fixed",
        width: "100%",
        height: 70,
    };

    const collapseStyle = {
        position: "unset",
        right: "0",
    };

    const expandStyle = {
        position: "relative",
        right: "90px",
    };

    return ( <
        TopbarWrapper >
        <
        Header style = {
            styling
        }
        className = {
            isCollapsed ? "isomorphicTopbar collapsed" : "isomorphicTopbar"
        } >
        <
        div className = "isoLeft"
        style = {
            isCollapsed ? collapseStyle : expandStyle
        } >
        <
        button className = {
            isCollapsed ? "triggerBtn menuCollapsed" : "triggerBtn menuOpen"
        }
        style = {
            {
                color: customizedTheme.textColor
            }
        }
        onClick = {
            handleToggle
        }
        /> <
        /div> <
        div className = "isoCenter" >
        <
        IntlMessages id = {
            displayTitle
        }
        /> <
        /div>

        {
            displayTitle === "sidebar.dashboard" || isReport ? ( <
                div className = "isoRight"
                onClick = {
                    props.showDrawer
                } >
                <
                Button type = "primary"
                className = "addTiles"
                style = {
                    {
                        width: isReport ? 136 : 120
                    }
                }
                onClick = {
                    props.showDrawer
                } >
                <
                img alt = "logo"
                src = {
                    dashboard
                }
                />{" "} {
                    isReport ? ( <
                        IntlMessages id = "topbar.selectDevice" / >
                    ) : ( <
                        IntlMessages id = "topbar.addTiles" / >
                    )
                } <
                /Button> <
                /div>
            ) : ( <
                div className = "isoRight" / >
            )
        } <
        /Header> <
        /TopbarWrapper>
    );
}