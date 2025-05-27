import React from "react";
import {
    Link
} from "react-router-dom";
import {
    config
} from "@config/site.config";
import appAction from "@redux/app/actions";
import {
    useDispatch
} from "react-redux";
import logo from "@assets/images/logo.png";

const {
    changeCurrent,
    updateModuleTitle
} = appAction;

// eslint-disable-next-line import/no-anonymous-default-export
export default ({
    collapsed
}) => {
    const dispatch = useDispatch();

    const resetDashboard = () => {
        dispatch(changeCurrent(["dashboard"]));
        dispatch(updateModuleTitle("sidebar.dashboard"));
        localStorage.setItem("currentModule", JSON.stringify("sidebar.dashboard"));
    };

    return ( <
        div className = "isoLogoWrapper" > {
            collapsed ? ( <
                div >
                <
                h3 >
                <
                Link to = "/dashboard" >
                <
                i className = {
                    config.siteIcon
                }
                /> <
                /Link> <
                /h3> <
                /div>
            ) : ( <
                div className = "logoContent" >
                <
                Link to = "/dashboard"
                onClick = {
                    resetDashboard
                } >
                <
                img alt = "logo"
                src = {
                    logo
                }
                /> <
                /Link> <
                /div>
            )
        } <
        /div>
    );
};