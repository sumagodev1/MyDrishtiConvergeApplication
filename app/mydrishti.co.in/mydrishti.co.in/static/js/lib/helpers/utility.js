import {
    Map
} from "seamless-immutable";
import options from "@containers/Sidebar/options";
import moment from "moment";

export function clearToken() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("currentUser");
    localStorage.removeItem("selectedUser");
}

export function getToken() {
    try {
        const idToken = localStorage.getItem("accessToken");
        return new Map({
            idToken
        });
    } catch (err) {
        clearToken();
        return new Map();
    }
}

export const isServer = typeof window === "undefined";

export function getDefaultPath() {
    const getParent = (lastRoute) => {
        const parent = [];
        if (!lastRoute) return parent;
        parent.push(lastRoute);
        options.forEach((option) => {
            if (option.children) {
                option.children.forEach((child) => {
                    if (child.key === lastRoute) {
                        parent.push(option.key);
                    }
                });
            }
        });
        return parent;
    };
    if (!isServer && window.location.pathname) {
        const routes = window.location.pathname.split("/");
        if (routes.length > 1) {
            const lastRoute = routes[routes.length - 1];
            return getParent(lastRoute);
        }
    }
    return [];
}

export const getFutureDate = (numberOfDays) => {
    return moment().add(numberOfDays, "day").endOf("day");
};

export const getPastDate = (numberOfDays) => {
    return moment().subtract(numberOfDays, "day").endOf("day");
};

export const getCurrentDate = () => {
    return moment().endOf("day");
};

export const dynamicSort = (property) => {
    var sortOrder = 1;

    if (property[0] === "-") {
        sortOrder = -1;
        property = property.substr(1);
    }

    return function(a, b) {
        if (sortOrder === -1) {
            return b[property].localeCompare(a[property]);
        } else {
            return a[property].localeCompare(b[property]);
        }
    };
};