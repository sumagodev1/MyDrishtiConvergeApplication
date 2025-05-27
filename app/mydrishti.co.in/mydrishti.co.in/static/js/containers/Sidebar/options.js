import dashboard from "@assets/images/dashboard.png";
import reports from "@assets/images/reports.png";
import settings from "@assets/images/settings.png";
import user from "@assets/images/user.png";
import logout from "@assets/images/logout.png";

const options = [{
        key: "dashboard",
        label: "sidebar.dashboard",
        displayName: "sidebar.dashboard",
        leftIcon: dashboard,
        leftPosition: 24,
    },
    {
        key: "report",
        label: "sidebar.report",
        displayName: "sidebar.report.realtime",
        leftIcon: reports,
        leftPosition: 38,
    },
    {
        key: "settings",
        label: "sidebar.settings",
        displayName: "sidebar.settings",
        leftIcon: settings,
        leftPosition: 30,
    },
    {
        key: "profile",
        label: "sidebar.users",
        displayName: "sidebar.users",
        leftIcon: user,
        leftPosition: 24,
    },
    {
        key: "logout",
        label: "sidebar.logout",
        displayName: "sidebar.logout",
        leftIcon: logout,
        leftPosition: 36,
    },
];
export default options;