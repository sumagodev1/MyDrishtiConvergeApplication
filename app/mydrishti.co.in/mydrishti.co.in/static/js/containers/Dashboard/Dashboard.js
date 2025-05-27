import React, {
    useEffect,
    useState
} from "react";
import {
    useSelector,
    useDispatch
} from "react-redux";
import {
    Layout,
    Modal
} from "antd";
import {
    isEmpty,
    remove
} from "lodash";
import useWindowSize from "@lib/hooks/useWindowSize";
import {
    variables
} from "@assets/styles/variables";
import {
    loadState
} from "@lib/helpers/localStorage";
import addWidgetActions from "@redux/addWidgets/actions";
import appActions from "@redux/app/actions";
import Sidebar from "../Sidebar/Sidebar";
import Topbar from "../Topbar/Topbar";
import DashboardRoutes from "./DashboardRoutes";
import SideDrawer from "./SideDrawer";

import {
    DashboardContainer,
    DashboardGlobalStyles
} from "./Dashboard.styles";
const {
    warning
} = Modal;

const {
    Content
} = Layout;
const {
    toggleAll
} = appActions;
const styles = {
    layout: {
        flexDirection: "row"
    },
    content: {
        padding: "95px 0 0",
        flexShrink: "0",
        background: variables.SECONDARY_COLOR,
        position: "relative",
        overflowX: "auto",
    },
};
const {
    updateSelectedParameters
} = addWidgetActions;

export default function Dashboard() {
    const dispatch = useDispatch();
    const [visible, setVisible] = useState(false);
    const appHeight = useSelector((state) => state.App.height);
    const {
        width,
        height
    } = useWindowSize();
    const widgetDetails = loadState("widgetSettings");
    const [editWidgetSettings, setEditWidgetSettings] = useState([]);


    const showDrawer = () => {
        setVisible(true);
    };

    const onClose = () => {
        setVisible(false);
        setEditWidgetSettings([]);
    };

    useEffect(() => {
        dispatch(toggleAll(width, height));
    }, [width, height, dispatch]);

    const showConfirm = () => {
        warning({
            title: "Gauge Chart already exists",
            content: "Gauge Chart with same device and parameter is already exists.",
        });
    };

    const addWidget = (selectedData) => {
        if (widgetDetails) {
            const isWidgetPresent = widgetDetails.filter(
                (s) => s.id === selectedData.id
            );
            if (isEmpty(isWidgetPresent)) {
                const existingDeviceParameters = widgetDetails;
                const newDeviceParameters = [...existingDeviceParameters, selectedData];
                dispatch(updateSelectedParameters(newDeviceParameters));
                onClose();
            } else {
                const widgets = remove(widgetDetails, function(n) {
                    return n.id !== selectedData.id;
                });
                const newDeviceParameters = [...widgets, selectedData];
                dispatch(updateSelectedParameters(newDeviceParameters));
                onClose();
            }
        } else {
            dispatch(updateSelectedParameters([selectedData]));
            onClose();
        }
    };

    const editWidgetDetails = (id) => {
        if (id) {
            const result = widgetDetails.filter((s) => s.id === id);
            showDrawer();
            setEditWidgetSettings(result);
        }
    };


    return ( <
        DashboardContainer >
        <
        DashboardGlobalStyles / >
        <
        Layout style = {
            {
                height: height
            }
        } >
        <
        Topbar showDrawer = {
            showDrawer
        }
        /> <
        Layout style = {
            styles.layout
        } >
        <
        Sidebar / >
        <
        Layout className = "isoContentMainLayout"
        style = {
            {
                height: appHeight,
            }
        } >
        <
        Content className = "isomorphicContent"
        style = {
            styles.content
        } >
        <
        DashboardRoutes editWidgetDetails = {
            editWidgetDetails
        }
        /> <
        SideDrawer visible = {
            visible
        }
        onClose = {
            onClose
        }
        addWidget = {
            addWidget
        }
        editWidgetSettings = {
            editWidgetSettings
        }
        widgetDetailsSettings = {
            widgetDetails
        }
        /> <
        /Content> <
        /Layout> <
        /Layout> <
        /Layout> <
        /DashboardContainer>
    );
}