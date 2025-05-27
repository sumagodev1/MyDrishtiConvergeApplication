import React from "react";
import {
    useSelector,
    useDispatch
} from "react-redux";
import {
    first
} from "lodash";
import {
    Modal
} from "antd";
import LayoutWrapper from "@components/utility/layoutWrapper";
import addWidgetActions from "@redux/addWidgets/actions";
import {
    loadState
} from "@lib/helpers/localStorage";
import WidgetDetails from "./WidgetDetails";

const {
    removeWidget,
    updateSelectedParameters
} = addWidgetActions;
const {
    confirm
} = Modal;

export default function Widgets({
    editWidgetDetails
}) {
    const dispatch = useDispatch();
    const widgetDetails = loadState("widgetSettings");
    const {
        displayWidget
    } = useSelector((state) => state.AddWidgets);
    const widgetDetailsData = first(displayWidget) ? .chartName ? displayWidget :
        widgetDetails;
    const onWidgetRemove = (widgetId) => {
        const results = widgetDetailsData.filter((item) => item.id !== widgetId);
        confirm({
            title: "Are you sure you want to delete?",
            okText: "Yes",
            okType: "danger",
            cancelText: "No",
            onOk() {
                dispatch(removeWidget(results));
            },
            onCancel() {
                console.log("Cancel");
            },
        });
    };

    const updateParameter = (result) => {
        dispatch(updateSelectedParameters(result.widgetDetailsData));
    };

    return ( <
        LayoutWrapper > {
            widgetDetailsData && ( <
                WidgetDetails widgetDetailsData = {
                    widgetDetailsData
                }
                onRemove = {
                    onWidgetRemove
                }
                editWidgetDetails = {
                    editWidgetDetails
                }
                updateParameter = {
                    updateParameter
                }
                />
            )
        } <
        /LayoutWrapper>
    );
}