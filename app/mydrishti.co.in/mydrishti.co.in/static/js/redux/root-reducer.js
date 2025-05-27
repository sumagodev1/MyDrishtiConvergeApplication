import {
    combineReducers
} from "redux";
import App from "@redux/app/reducer";
import Auth from "@redux/auth/reducer";
import Reports from "@redux/reports/reducer";
import AddWidgets from "@redux/addWidgets/reducer";
import Box from "@redux/box/reducer";
import ThemeSwitcher from "@redux/themeSwitcher/reducer";
import LanguageSwitcher from "@redux/languageSwitcher/reducer";
import drawer from "@redux/drawer/reducer";
import modal from "@redux/modal/reducer";
import settingsReducer from "@redux/settings/reducer"

export default combineReducers({
    Auth,
    App,
    AddWidgets,
    Reports,
    ThemeSwitcher,
    LanguageSwitcher,
    Box,
    modal,
    drawer,
    settingsReducer

});