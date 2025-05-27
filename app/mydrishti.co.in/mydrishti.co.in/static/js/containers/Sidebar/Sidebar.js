import {
    useDispatch,
    useSelector
} from "react-redux";
import {
    Layout
} from "antd";
import options from "./options";
import Scrollbars from "@components/utility/customScrollBar";
import Menu from "@components/uielements/menu";
import appActions from "@redux/app/actions";
import Logo from "@components/utility/logo";
import {
    variables
} from "@assets/styles/variables";
import SidebarWrapper from "./Sidebar.styles";
import SidebarMenu from "./SidebarMenu";

const {
    Sider,
    Footer
} = Layout;

const styles = {
    footer: {
        background: variables.PRIMARY_COLOR,
        textAlign: "center",
        position: "absolute",
        bottom: "0",
        padding: "21.5px",
        color: variables.WHITE_COLOR,
        fontSize: "x-small",
    },
};

const {
    changeOpenKeys,
    changeCurrent,
    toggleCollapsed
} = appActions;

export default function Sidebar() {
    const dispatch = useDispatch();
    const {
        view,
        openKeys,
        collapsed,
        openDrawer,
        current,
        height
    } =
    useSelector((state) => state.App);
    const customizedTheme = useSelector(
        (state) => state.ThemeSwitcher.sidebarTheme
    );

    function handleClick(e) {
        dispatch(changeCurrent([e.key]));
        if (view === "MobileView") {
            setTimeout(() => {
                dispatch(toggleCollapsed());
                // dispatch(toggleOpenDrawer());
            }, 100);

            // clearTimeout(timer);
        }
    }

    function onOpenChange(newOpenKeys) {
        const latestOpenKey = newOpenKeys.find(
            (key) => !(openKeys.indexOf(key) > -1)
        );
        const latestCloseKey = openKeys.find(
            (key) => !(newOpenKeys.indexOf(key) > -1)
        );
        let nextOpenKeys = [];
        if (latestOpenKey) {
            nextOpenKeys = getAncestorKeys(latestOpenKey).concat(latestOpenKey);
        }
        if (latestCloseKey) {
            nextOpenKeys = getAncestorKeys(latestCloseKey);
        }
        dispatch(changeOpenKeys(nextOpenKeys));
    }
    const getAncestorKeys = (key) => {
        const map = {
            sub3: ["sub2"],
        };
        return map[key] || [];
    };

    const isCollapsed = collapsed && !openDrawer;
    const mode = isCollapsed === true ? "vertical" : "inline";

    const styling = {
        backgroundColor: variables.PRIMARY_COLOR,
    };
    const submenuStyle = {
        backgroundColor: "rgba(0,0,0,0.3)",
        color: customizedTheme.textColor,
    };
    const submenuColor = {
        color: "#fff",
    };
    return ( <
        SidebarWrapper >
        <
        Sider trigger = {
            null
        }
        collapsible = {
            true
        }
        collapsed = {
            isCollapsed
        }
        width = {
            160
        }
        className = "isomorphicSidebar"
        style = {
            styling
        } >
        <
        Logo collapsed = {
            isCollapsed
        }
        /> <
        Scrollbars style = {
            {
                height: height - 70
            }
        } >
        <
        Menu onClick = {
            handleClick
        }
        theme = "dark"
        className = "isoDashboardMenu"
        mode = {
            mode
        }
        openKeys = {
            isCollapsed ? [] : openKeys
        }
        selectedKeys = {
            current
        }
        onOpenChange = {
            onOpenChange
        } >
        {
            options.map((singleOption) => ( <
                SidebarMenu key = {
                    singleOption.key
                }
                submenuStyle = {
                    submenuStyle
                }
                submenuColor = {
                    submenuColor
                }
                singleOption = {
                    singleOption
                }
                />
            ))
        } <
        /Menu> <
        Footer style = {
            styles.footer
        } >
        <
        div > info @casglobals.com < /div> <
        div > Copyright© 2021 < /div> <
        div > All Rights Reserved < /div> <
        /Footer> <
        /Scrollbars> <
        /Sider> <
        /SidebarWrapper>
    );
}