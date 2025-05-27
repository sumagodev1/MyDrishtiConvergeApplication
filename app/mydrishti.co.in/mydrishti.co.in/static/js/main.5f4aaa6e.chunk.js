(this["webpackJsonpmydrishti-ui"] = this["webpackJsonpmydrishti-ui"] || []).push([
    [7], {
        109: function(e, t, n) {
            "use strict";
            var r = n(67),
                a = {
                    CHANGE_LANGUAGE: "CHANGE_LANGUAGE",
                    ACTIVATE_LANG_MODAL: "ACTIVATE_LANG_MODAL",
                    switchActivation: function() {
                        return {
                            type: a.ACTIVATE_LANG_MODAL
                        }
                    },
                    changeLanguage: function(e) {
                        return {
                            type: a.CHANGE_LANGUAGE,
                            language: Object(r.b)(e)
                        }
                    }
                };
            t.a = a
        },
        120: function(e, t, n) {
            "use strict";
            n.d(t, "a", (function() {
                return r
            })), n.d(t, "b", (function() {
                return a
            }));
            var r = function(e) {
                    try {
                        var t = localStorage.getItem(e);
                        if (null === t) return;
                        return JSON.parse(t)
                    } catch (n) {
                        return
                    }
                },
                a = function(e) {
                    try {
                        var t = localStorage.getItem(e);
                        if (null === t) return;
                        return t
                    } catch (n) {
                        return
                    }
                }
        },
        134: function(e, t, n) {
            "use strict";
            n(0);
            var r, a = n(71),
                o = n(52),
                i = n(60),
                s = o.d.div(r || (r = Object(a.a)(["\n  width: 100%;\n  height: 100%;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  position: absolute;\n  z-index: 10000000000;\n  top: 0;\n  right: 0;\n\n  @media only screen and (min-width: 768px) and (max-width: 1220px) {\n    width: calc(100% - 80px);\n  }\n\n  @media only screen and (max-width: 767px) {\n    width: 100%;\n  }\n\n  .isoContentLoader {\n    width: 50px;\n    height: 50px;\n    animation: svgSpinner 1.4s linear infinite;\n  }\n\n  .isoContentLoaderCircle {\n    animation: svgSpinnerCircle 1.4s ease-in-out infinite;\n    stroke-dasharray: 80px, 200px;\n    stroke-dashoffset: 0px;\n    stroke: ", ";\n    stroke-linecap: round;\n  }\n\n  @keyframes svgSpinner {\n    100% {\n      transform: rotate(360deg);\n    }\n  }\n  @keyframes svgSpinnerCircle {\n    0% {\n      stroke-dasharray: 1px, 200px;\n      stroke-dashoffset: 0px;\n    }\n    50% {\n      stroke-dasharray: 100px, 200px;\n      stroke-dashoffset: -15px;\n    }\n    100% {\n      stroke-dasharray: 100px, 200px;\n      stroke-dashoffset: -120px;\n    }\n  }\n"])), Object(i.palette)("primary", 0)),
                c = n(12);
            t.a = function(e) {
                return Object(c.jsx)(s, {
                    children: Object(c.jsx)("svg", {
                        className: "isoContentLoader",
                        viewBox: "0 0 50 50",
                        children: Object(c.jsx)("circle", {
                            className: "isoContentLoaderCircle",
                            cx: "25",
                            cy: "25",
                            r: "20",
                            fill: "none",
                            strokeWidth: "3.6"
                        })
                    })
                })
            }
        },
        140: function(e, t, n) {
            "use strict";
            n.d(t, "a", (function() {
                return r
            }));
            var r = "UPDATE_FREQUENCY_TIME"
        },
        15: function(e, t, n) {
            "use strict";
            var r = {
                GET_USERS_DEVICE_REQUEST: "GET_USERS_DEVICE_REQUEST",
                GET_USERS_DEVICE_SUCCESS: "GET_USERS_DEVICE_SUCCESS",
                GET_USERS_DEVICE_ERROR: "GET_USERS_DEVICE_ERROR",
                GET_DEVICE_PARAMETER_REQUEST: "GET_DEVICE_PARAMETER_REQUEST",
                GET_DEVICE_PARAMETER_SUCCESS: "GET_DEVICE_PARAMETER_SUCCESS",
                GET_DEVICE_PARAMETER_ERROR: "GET_DEVICE_PARAMETER_ERROR",
                GET_GRAPH_DATA_REQUEST: "GET_GRAPH_DATA_REQUEST",
                GET_GRAPH_DATA_SUCCESS: "GET_GRAPH_DATA_SUCCESS",
                GET_GRAPH_DATA_ERROR: "GET_GRAPH_DATA_ERROR",
                UPDATE_SELECTED_PARAMS: "UPDATE_SELECTED_PARAMS",
                UPDATE_SELECTED_PARAMS_SUCCESS: "UPDATE_SELECTED_PARAMS_SUCCESS",
                UPDATE_SELECTED_PARAMS_ERROR: "UPDATE_SELECTED_PARAMS_ERROR",
                REMOVE_WIDGET_REQUEST: "REMOVE_WIDGET_REQUEST",
                REMOVE_WIDGET_SUCCESS: "REMOVE_WIDGET_SUCCESS",
                REMOVE_WIDGET_ERROR: "REMOVE_WIDGET_ERROR",
                getUserDevices: function(e, t) {
                    return {
                        type: r.GET_USERS_DEVICE_REQUEST,
                        payload: {
                            url: e,
                            header: t
                        }
                    }
                },
                getDeviceParameters: function(e, t) {
                    return {
                        type: r.GET_DEVICE_PARAMETER_REQUEST,
                        payload: {
                            url: e,
                            header: t
                        }
                    }
                },
                getGraphData: function(e, t, n) {
                    return {
                        type: r.GET_GRAPH_DATA_REQUEST,
                        payload: {
                            url: e,
                            body: t,
                            header: n
                        }
                    }
                },
                updateSelectedParameters: function(e) {
                    return {
                        type: r.UPDATE_SELECTED_PARAMS,
                        payload: {
                            params: e
                        }
                    }
                },
                removeWidget: function(e) {
                    return {
                        type: r.REMOVE_WIDGET_REQUEST,
                        payload: {
                            params: e
                        }
                    }
                }
            };
            t.a = r
        },
        156: function(e, t, n) {
            "use strict";
            t.a = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB8AAAAfCAYAAAAfrhY5AAAABHNCSVQICAgIfAhkiAAAAUFJREFUSEvtV9ERgjAMpROoE4gT6Ai6gRsIG+gEruAG6gZuoCPIBMIE4gT4Xq/ceXwlDT8quevRgySvCe1L6pqmWSRJcsTgUyIllHbOuUurDB8Z5nuMVOIAOneM3MGQk7nQqFWrMZlhATXsx5g/MPjUyJ3gjcbiQ3cF8BvMl3h3jfExgMdkbUi7acPx2Iwi8s6jVlp3OwmCJKORM4Bpl5jAgwOym5QkagCTmLyYwTUhd3W/FzwUFtKjNO0MPkfqT+a0A7yEk2lE6iehsJiO2lcXFv6uZ0TmKnNVC/9dyxUv2C17AY+I2pv8BnggG+nmfZEle4kcwDzzG2X6fQMpXW3Xd9tMpPjABlIrdR/g/0syQ+TaDWcnGVMzEVlSSRK+/oe7GsuytgkteNTYv5EkpJfFCrrbzi11jXcHDGlfUEA3ewMrhhElVRmNogAAAABJRU5ErkJggg=="
        },
        181: function(e) {
            e.exports = JSON.parse('{"sidebar.dashboard":"Dashboard","sidebar.report":"Report","sidebar.report.realtime":"Report - Realtime","sidebar.report.historical":"Reports - Historical","sidebar.settings":"Settings","sidebar.users":"My Profile","sidebar.logout":"Logout","page.title.dashboard":"Dashboard","pages.title.user.profile":"User Profile","pages.title.user.edit":"Edit User","pages.title.user.new":"New User","page.title.settings":"Settings","page.title.report":"Report","languageSwitcher.label":"Change Language","updateLiveTiles.label":"Update live tiles after","topbar.selectDevice":"Select Device","topbar.addTiles":"Add Tiles","sidedrawer.barChart":"Daily Bar Chart","sidedrawer.hourlybarChart":"Hourly Bar Chart","sidedrawer.gaugeChart":"Gauge Chart","sidedrawer.lineChart":"Line Chart","sidedrawer.metricChart":"Metric Chart","sidedrawer.calChart":"Calculation Chart","sidedrawer.updateTiles":"Update Tiles","sidedrawer.updateData":"Update Data","report.info.selectDeviceAndParameter":"Please Select Devices and Parameters to display results.","report.switch.historical":"HISTORICAL","report.switch.viewData":"View Data","settings.saveSettings":"Save Settings","settings.resetToDefault":"Reset To Default"}')
        },
        183: function(e) {
            e.exports = JSON.parse('{"sidebar.dashboard":"\u0921\u0948\u0936\u092c\u094b\u0930\u094d\u0921","sidebar.report":"\u0930\u093f\u092a\u094b\u0930\u094d\u091f","sidebar.report.realtime":"\u0930\u093f\u092a\u094b\u0930\u094d\u091f - \u0930\u0940\u092f\u0932\u091f\u093e\u0907\u092e","sidebar.report.historical":"\u0930\u093f\u092a\u094b\u0930\u094d\u091f - \u0910\u0924\u093f\u0939\u093e\u0938\u093f\u0915","sidebar.settings":"\u0938\u092e\u093e\u092f\u094b\u091c\u0928","sidebar.users":"\u092e\u0947\u0930\u0940 \u092a\u094d\u0930\u094b\u092b\u093e\u0907\u0932","sidebar.logout":"\u0932\u0949\u0917 \u0906\u0909\u091f","page.title.dashboard":"\u0921\u0948\u0936\u092c\u094b\u0930\u094d\u0921","page.title.settings":"\u0938\u092e\u093e\u092f\u094b\u091c\u0928","page.title.report":"\u0930\u093f\u092a\u094b\u0930\u094d\u091f","languageSwitcher.label":"\u092d\u093e\u0937\u093e \u092c\u0926\u0932\u0947\u0902","updateLiveTiles.label":"\u0907\u0938\u0915\u0947 \u092c\u093e\u0926 \u0932\u093e\u0907\u0935 \u091f\u093e\u0907\u0932\u0947\u0902 \u0905\u092a\u0921\u0947\u091f \u0915\u0930\u0947\u0902","topbar.selectDevice":"\u0921\u093f\u0935\u093e\u0907\u0938 \u0915\u093e \u091a\u092f\u0928 \u0915\u0930\u0947\u0902","topbar.addTiles":"\u091f\u093e\u0907\u0932\u0947\u0902 \u091c\u094b\u0921\u093c\u0947\u0902","sidedrawer.barChart":"\u092c\u093e\u0930 \u091a\u093e\u0930\u094d\u091f","sidedrawer.hourlybarChart":"\u0939\u0930 \u0918\u0902\u091f\u0947 \u092c\u093e\u0930 \u091a\u093e\u0930\u094d\u091f","sidedrawer.gaugeChart":"\u0917\u0947\u091c \u091a\u093e\u0930\u094d\u091f","sidedrawer.lineChart":"\u0932\u093e\u0907\u0928 \u091a\u093e\u0930\u094d\u091f","sidedrawer.metricChart":"\u092e\u0940\u091f\u094d\u0930\u093f\u0915 \u091a\u093e\u0930\u094d\u091f","sidedrawer.calChart":"\u0915\u0948\u0932 \u091a\u093e\u0930\u094d\u091f","sidedrawer.updateTiles":"\u091f\u093e\u0907\u0932\u0947\u0902 \u0905\u092a\u0921\u0947\u091f \u0915\u0930\u0947\u0902","sidedrawer.updateData":"\u0921\u0947\u091f\u093e \u0905\u092a\u0921\u0947\u091f \u0915\u0930\u0947\u0902","report.info.selectDeviceAndParameter":"\u0915\u0943\u092a\u092f\u093e \u092a\u0930\u093f\u0923\u093e\u092e \u092a\u094d\u0930\u0926\u0930\u094d\u0936\u093f\u0924 \u0915\u0930\u0928\u0947 \u0915\u0947 \u0932\u093f\u090f \u0921\u093f\u0935\u093e\u0907\u0938 \u0914\u0930 \u092a\u0948\u0930\u093e\u092e\u0940\u091f\u0930 \u091a\u0941\u0928\u0947\u0902\u0964","report.switch.historical":"\u0910\u0924\u093f\u0939\u093e\u0938\u093f\u0915","report.switch.viewData":"\u0921\u0947\u091f\u093e \u0926\u0947\u0916\u0947\u0902","settings.saveSettings":"\u0938\u0947\u091f\u093f\u0902\u0917\u094d\u0938 \u0938\u0947\u0935 \u0915\u0930\u0947\u0902","settings.resetToDefault":"\u0921\u093f\u092b\u093c\u0949\u0932\u094d\u091f \u092a\u0930 \u0930\u0940\u0938\u0947\u091f \u0915\u0930\u0947\u0902"}')
        },
        210: function(e, t, n) {},
        211: function(e, t, n) {},
        32: function(e, t, n) {
            "use strict";
            var r = {
                CHECK_AUTHORIZATION: "CHECK_AUTHORIZATION",
                LOGIN_REQUEST: "LOGIN_REQUEST",
                LOGIN_SUCCESS: "LOGIN_SUCCESS",
                LOGIN_ERROR: "LOGIN_ERROR",
                LOGOUT: "LOGOUT",
                checkAuthorization: function() {
                    return {
                        type: r.CHECK_AUTHORIZATION
                    }
                },
                login: function(e, t) {
                    return {
                        type: r.LOGIN_REQUEST,
                        payload: {
                            url: e,
                            body: t
                        }
                    }
                },
                logout: function(e, t, n, a) {
                    return {
                        type: r.LOGOUT,
                        payload: {
                            url: e,
                            body: t,
                            accessToken: n,
                            history: a
                        }
                    }
                }
            };
            t.a = r
        },
        33: function(e, t, n) {
            "use strict";
            var r = {
                GET_USER_PARAMETER_REQUEST: "GET_USER_PARAMETER_REQUEST",
                GET_USER_PARAMETER_SUCCESS: "GET_USER_PARAMETER_SUCCESS",
                GET_USER_PARAMETER_ERROR: "GET_USER_PARAMETER_ERROR",
                GET_METRIC_DATA_REQUEST: "GET_METRIC_DATA_REQUEST",
                GET_METRIC_DATA_SUCCESS: "GET_METRIC_DATA_SUCCESS",
                GET_METRIC_DATA_ERROR: "GET_METRIC_DATA_ERROR",
                GET_REPORT_SETTINGS_REQUEST: "GET_REPORT_SETTINGS_REQUEST",
                GET_REPORT_SETTINGS_SUCCESS: "GET_REPORT_SETTINGS_SUCCESS",
                GET_REPORT_SETTINGS_ERROR: "GET_REPORT_SETTINGS_ERROR",
                getUserParameters: function(e, t, n) {
                    return {
                        type: r.GET_USER_PARAMETER_REQUEST,
                        payload: {
                            url: e,
                            header: t,
                            history: n
                        }
                    }
                },
                getReportsSettings: function(e) {
                    return {
                        type: r.GET_REPORT_SETTINGS_REQUEST,
                        payload: {
                            params: e
                        }
                    }
                },
                setMetricChartData: function(e) {
                    return {
                        type: r.GET_METRIC_DATA_SUCCESS,
                        payload: e
                    }
                }
            };
            t.a = r
        },
        357: function(e, t, n) {
            "use strict";
            n.r(t);
            var r, a = n(0),
                o = n.n(a),
                i = n(40),
                s = n.n(i),
                c = (n(210), n(211), n(212), n(47)),
                l = n(71),
                u = n(52),
                d = n(60),
                p = Object(u.b)(r || (r = Object(l.a)(["\n  .ant-btn{\n    border-radius: 4px;\n  }\n\n  .header {\n    display: flex;\n    justify-content: space-between;\n    align-items: center;\n  }\n\n  .logo {\n    font-size: 32px;\n    font-weight: 700;\n    color: #fff;\n  }\n\n  .ant-table-thead > tr.ant-table-row-hover:not(.ant-table-expanded-row) > td, .ant-table-tbody > tr.ant-table-row-hover:not(.ant-table-expanded-row) > td, .ant-table-thead > tr:hover:not(.ant-table-expanded-row) > td, .ant-table-tbody > tr:hover:not(.ant-table-expanded-row) > td {\n    background: #f8f8f8!important;\n  }\n\n  .ant-row.ant-form-item {\n    margin-bottom: 5px;\n  }\n\n  .has-success.has-feedback {\n    .ant-select {\n      .ant-select-selection {\n        .ant-select-selection__rendered {\n          .ant-select-selection__placeholder {\n            display: none !important;\n          }\n        }\n      }\n    }\n  }\n\n  /*-----------------------------------------------*/\n  // style for project category menu [ScrumBoard]\n  /*-----------------------------------------------*/\n  .project-category {\n    .ant-select-dropdown-menu {\n      .ant-select-dropdown-menu-item {\n        padding: 8px 12px;\n        color: #000000;\n        font-family: 'Roboto';\n        font-weight: 400;\n      }\n    }\n  }\n\n  /*-----------------------------------------------*/\n  // style for project menu [ScrumBoard]\n  /*-----------------------------------------------*/\n  .ant-dropdown {\n    &.project-menu {\n      width: 280px;\n      top: 133px !important;\n\n      .ant-dropdown-menu {\n        padding: 0;\n        overflow: hidden;\n\n        .ant-dropdown-menu-item {\n          min-height: 54px;\n          line-height: auto;\n          display: flex;\n          align-items: center;\n          padding: 10px 20px;\n\n          &:first-child {\n            padding: 0;\n            border-bottom: 1px solid #f4f6fd;\n\n            &:hover,\n            &:focus {\n              background-color: #ffffff;\n            }\n          }\n\n          &:hover,\n          &:focus {\n            background-color: #F3F5FD;\n          }\n\n          &:last-child {\n            background-color: #E6EAF8;\n          }\n        }\n      }\n    }\n  }\n\n  /*-----------------------------------------------*/\n  // style for popover [ScrumBoard]\n  /*-----------------------------------------------*/\n  .ant-popover {\n    .ant-checkbox-group {\n      display: flex;\n      flex-direction: column;\n      .ant-checkbox-group-item {\n        margin: 5px 0;\n        span {\n          font-size: 14px;\n          color: #788195;\n          text-transform: capitalize;\n        }\n      }\n    }\n  }\n\n  /*-----------------------------------------------*/\n  // style for modal [ScrumBoard]\n  /*-----------------------------------------------*/\n  .ant-modal-wrap {\n    .ant-modal {\n      .ant-modal-content {\n        .ant-modal-body {\n          .render-form-wrapper {\n            padding: 10px;\n            h2 {\n              margin: 0;\n            }\n            form {\n              padding: 15px 0 3px;\n              .field-container {\n                margin-bottom: 26px;\n              }\n            }\n          }\n        }\n      }\n    }\n  }\n\n\n/*-----------------------------------------------*/\n  // style form previous GlobalStyles\n  /*-----------------------------------------------*/\n\n  .ant-table-thead > tr.ant-table-row-hover:not(.ant-table-expanded-row) > td, .ant-table-tbody > tr.ant-table-row-hover:not(.ant-table-expanded-row) > td, .ant-table-thead > tr:hover:not(.ant-table-expanded-row) > td, .ant-table-tbody > tr:hover:not(.ant-table-expanded-row) > td {\n    background: #f8f8f8!important;\n}\n\nfont-family: ", ";\n\nh1,\nh2,\nh3,\nh4,\nh5,\nh6,\na,\np,\nli,\ninput,\ntextarea,\nspan,\ndiv,\nimg,\nsvg {\n  &::selection {\n    background: ", ";\n    color: #fff;\n  }\n}\n\n.ant-row:not(.ant-form-item) {\n  ", ";\n  &:before,\n  &:after {\n    display: none;\n  }\n}\n\n.ant-row > div {\n  padding: 0;\n}\n\n.isoLeftRightComponent {\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  width: 100%;\n}\n\n.isoCenterComponent {\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  width: 100%;\n}\n/********** Add Your Global CSS Here **********/\n\nbody {\n  -webkit-overflow-scrolling: touch;\n}\n\nhtml h1,\nhtml h2,\nhtml h3,\nhtml h4,\nhtml h5,\nhtml h6,\nhtml a,\nhtml p,\nhtml li,\ninput,\ntextarea,\nspan,\ndiv,\nhtml,\nbody,\nhtml a {\n  margin-bottom: 0;\n  font-family: 'Roboto', sans-serif;\n  -webkit-font-smoothing: antialiased;\n  -moz-osx-font-smoothing: grayscale;\n  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.004);\n}\n\nhtml ul {\n  -webkit-padding-start: 0px;\n  list-style: none;\n  margin-bottom: 0;\n}\n\n.scrollbar-track-y,\n.scrollbar-thumb-y {\n  width: 5px !important;\n}\n\n.scrollbar-track-x,\n.scrollbar-thumb-x {\n  height: 5px !important;\n}\n\n.scrollbar-thumb {\n  border-radius: 0 !important;\n}\n\n.scrollbar-track {\n  background: rgba(222, 222, 222, 0.15) !important;\n}\n\n.scrollbar-thumb {\n  border-radius: 0 !important;\n  background: rgba(0, 0, 0, 0.5) !important;\n}\n\n.ant-popover-placement-bottom > .ant-popover-content > .ant-popover-arrow:after,\n.ant-popover-placement-bottomLeft\n  > .ant-popover-content\n  > .ant-popover-arrow:after,\n.ant-popover-placement-bottomRight\n  > .ant-popover-content\n  > .ant-popover-arrow:after,\n.ant-popover-placement-top > .ant-popover-content > .ant-popover-arrow:after,\n.ant-popover-placement-topLeft\n  > .ant-popover-content\n  > .ant-popover-arrow:after,\n.ant-popover-placement-topRight\n  > .ant-popover-content\n  > .ant-popover-arrow:after {\n  left: 0;\n  margin-left: -4px;\n}\n\n/* Instagram Modal */\n\n.ant-modal-wrap.instagram-modal .ant-modal {\n  max-width: 935px;\n  width: 100% !important;\n}\n\n@media only screen and (max-width: 991px) {\n  .ant-modal-wrap.instagram-modal .ant-modal {\n    padding: 0 60px;\n  }\n}\n\n@media only screen and (max-width: 767px) {\n  .ant-modal-wrap.instagram-modal .ant-modal {\n    max-width: 580px;\n  }\n}\n\n.ant-modal-wrap.instagram-modal .ant-modal-content {\n  border-radius: 0;\n}\n\n.ant-modal-wrap.instagram-modal .ant-modal-content button.ant-modal-close {\n  position: fixed;\n  color: #fff;\n}\n\n.ant-modal-wrap.instagram-modal .ant-modal-content button.ant-modal-close i {\n  font-size: 24px;\n}\n\n.ant-modal-wrap.instagram-modal .ant-modal-content .ant-modal-body {\n  padding: 0;\n}\n\n/********** Add Your Global RTL CSS Here **********/\n\n/* Popover */\n\nhtml[dir='rtl'] .ant-popover {\n  text-align: right;\n}\n\n/* Ecommerce Card */\n\nhtml[dir='rtl'] .isoCardInfoForm .ant-input {\n  text-align: right;\n}\n\n/* Modal */\n\nhtml[dir='rtl'] .has-success.has-feedback:after,\nhtml[dir='rtl'] .has-warning.has-feedback:after,\nhtml[dir='rtl'] .has-error.has-feedback:after,\nhtml[dir='rtl'] .is-validating.has-feedback:after {\n  left: 0;\n  right: auto;\n}\n\nhtml[dir='rtl'] .ant-modal-close {\n  right: inherit;\n  left: 0;\n}\n\nhtml[dir='rtl'] .ant-modal-footer {\n  text-align: left;\n}\n\nhtml[dir='rtl'] .ant-modal-footer button + button {\n  margin-left: 0;\n  margin-right: 8px;\n}\n\nhtml[dir='rtl'] .ant-confirm-body .ant-confirm-content {\n  margin-right: 42px;\n}\n\nhtml[dir='rtl'] .ant-btn > .anticon + span,\nhtml[dir='rtl'] .ant-btn > span + .anticon {\n  margin-right: 0.5em;\n}\n\nhtml[dir='rtl'] .ant-btn-loading span {\n  margin-left: 0;\n  margin-right: 0.5em;\n}\n\nhtml[dir='rtl']\n  .ant-btn.ant-btn-loading:not(.ant-btn-circle):not(.ant-btn-circle-outline) {\n  padding-left: 25px;\n  padding-right: 29px;\n}\n\nhtml[dir='rtl']\n  .ant-btn.ant-btn-loading:not(.ant-btn-circle):not(.ant-btn-circle-outline)\n  .anticon {\n  margin-right: -14px;\n  margin-left: 0;\n}\n\n/* Confirm */\n\nhtml[dir='rtl'] .ant-modal.ant-confirm .ant-confirm-body > .anticon {\n  margin-left: 16px;\n  margin-right: 0;\n  float: right;\n}\n\nhtml[dir='rtl'] .ant-modal.ant-confirm .ant-confirm-btns {\n  float: left;\n}\n\nhtml[dir='rtl'] .ant-modal.ant-confirm .ant-confirm-btns button + button {\n  margin-right: 10px;\n  margin-left: 0;\n}\n\n/* Message */\n\nhtml[dir='rtl'] .ant-message .anticon {\n  margin-left: 8px;\n  margin-right: 0;\n}\n\n/* Pop Confirm */\n\nhtml[dir='rtl'] .ant-popover-message-title {\n  padding-right: 20px;\n  padding-left: 0;\n}\n\nhtml[dir='rtl'] .ant-popover-buttons {\n  text-align: left;\n}\n\n/* Notification */\n\nhtml[dir='rtl']\n  .ant-notification-notice-closable\n  .ant-notification-notice-message {\n  padding-left: 24px;\n  padding-right: 0;\n}\n\nhtml[dir='rtl']\n  .ant-notification-notice-with-icon\n  .ant-notification-notice-message,\nhtml[dir='rtl']\n  .ant-notification-notice-with-icon\n  .ant-notification-notice-description {\n  margin-right: 48px;\n}\n\nhtml[dir='rtl'] .ant-notification-notice-close {\n  right: auto;\n  left: 16px;\n}\n\nhtml[dir='rtl'] .ant-notification-notice-with-icon {\n  left: 0;\n}\n\n/* Dropzone */\n\nhtml[dir='rtl'] .dz-hidden-input {\n  display: none;\n}\n"])), Object(d.font)("primary", 0), Object(d.palette)("primary", 0), ""),
                f = n(116),
                E = n(46),
                h = n(179),
                m = n(185),
                b = (n(283), n(5)),
                g = n(45),
                A = n(89),
                T = n(43),
                R = Object(g.d)(),
                S = {
                    collapsed: !(!g.g && window.innerWidth > 1220),
                    view: !g.g && Object(T.b)(window.innerWidth),
                    height: !g.g && window.innerHeight,
                    openDrawer: !1,
                    openKeys: R,
                    current: R,
                    currentModule: A.a[0].label
                };
            var O = n(32),
                C = n(27),
                _ = n.n(C),
                y = _()({
                    loginFailure: !1,
                    isFetching: !1,
                    errorMessage: "",
                    successMessage: "",
                    accessToken: "",
                    currentUser: {},
                    actionType: ""
                });
            var x = n(33),
                w = _()({
                    isLoading: !1,
                    errorMessage: {},
                    successMessage: "",
                    userParameterList: [],
                    actionType: "",
                    reportsSettings: []
                });
            var v = n(15),
                j = {
                    failure: !1,
                    isLoading: !1,
                    errorMessage: {},
                    successMessage: "",
                    deviceParameterList: [],
                    graphData: [],
                    deviceList: [],
                    displayWidget: []
                };
            var D = n(12),
                I = function() {
                    return Object(D.jsx)("button", {
                        children: "Add Me"
                    })
                },
                U = [{
                    uid: "a1",
                    title: "Box-1",
                    content: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    reactComponent: function() {
                        return Object(D.jsx)("h2", {
                            children: " Hello "
                        })
                    }
                }, {
                    uid: "a2",
                    title: "Box-2",
                    content: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    reactComponent: function() {
                        return Object(D.jsx)("input", {})
                    }
                }, {
                    uid: "a3",
                    title: "Box-3",
                    content: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    reactComponent: I
                }, {
                    uid: "a4",
                    title: "Box-4",
                    content: "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    reactComponent: I
                }],
                G = {
                    DELETE_BOX: "DELETE_BOX",
                    SAVE_BOX: "SAVE_BOX",
                    deleteBox: function(e) {
                        return {
                            type: G.DELETE_BOX,
                            uid: e
                        }
                    },
                    saveBox: function(e) {
                        return {
                            type: G.SAVE_BOX,
                            box: e
                        }
                    }
                },
                k = G,
                L = {
                    allBox: (U.map((function(e, t) {
                        var n = {};
                        return n.lg = {
                            x: t % 2 === 0 ? 0 : 48,
                            y: 0,
                            h: 2,
                            w: 48,
                            i: e.uid.toString()
                        }, n.md = {
                            x: t % 2 === 0 ? 0 : 48,
                            y: 0,
                            h: 2,
                            w: 48,
                            i: e.uid.toString()
                        }, n.sm = {
                            x: t % 2 === 0 ? 0 : 48,
                            y: 0,
                            h: 2,
                            w: 48,
                            i: e.uid.toString()
                        }, n.xs = {
                            x: t % 2 === 0 ? 0 : 48,
                            y: 0,
                            h: 2,
                            w: 48,
                            i: e.uid.toString()
                        }, n.xxs = {
                            x: t % 2 === 0 ? 0 : 48,
                            y: 0,
                            h: 2,
                            w: 48,
                            i: e.uid.toString()
                        }, U[t].size = n, null
                    })), U),
                    reload: !1
                };
            var P = n(79),
                M = {
                    palette: {
                        primary: ["#4482FF", "#3A78F5", "#3775F2", "rgba(68, 130, 255, 0.2)", "#4C8AFF", "rgba(68, 130, 255, 0.75)", "#6AA8FF", "#63A1FF", "#3F7DFA", "#3369e7", "#5896FF", "#2b69e6", "#236cfe", "#4d88ff"],
                        secondary: ["#2d3446", "#f1f3f6", "#788195", "#E4E6E9", "#364d79", "#202739", "#f5f6f8", "#e9ebf1", "#F6F8FB", "#E9EBEE", "#1a1a1a"],
                        color: ["#FEAC01", "#42299a", "#F75D81", "#7ED321", "#39435f", "#FFCA28", "#F2BD1B", "#3b5998", "#344e86", "#dd4b39", "#d73925", "#e14615", "#ca3f13", "#e0364c"],
                        warning: ["#ffbf00"],
                        success: ["#00b16a"],
                        error: ["#f64744", "#EC3D3A", "#FF5B58"],
                        grayscale: ["#bababa", "#c1c1c1", "#D8D8D8", "#f1f1f1", "#F3F3F3", "#fafafa", "#F9F9F9", "#fcfcfc", "#eeeeee", "#fbfbfb", "#f5f5f5", "#f7f8f9"],
                        text: ["#323332", "#595959", "#979797", "#797979", "#6a6c6a"],
                        border: ["#e9e9e9", "#d8d8d8", "#ebebeb", "#d3d3d3", "rgba(228, 228, 228, 0.65)"],
                        calendar: ["#905", "#690", "#a67f59", "#07a", "#dd4a68", "#e90"]
                    },
                    fonts: {
                        primary: "Roboto, sans-serif",
                        pre: "Consolas, Liberation Mono, Menlo, Courier, monospace"
                    }
                },
                N = M,
                H = {
                    defaultTheme: N,
                    customTheme: Object(b.a)(Object(b.a)({}, N), {}, {
                        palette: {
                            primary: ["#f00"],
                            secondary: ["#0f0"]
                        }
                    })
                },
                F = {
                    changeThemes: {
                        id: "changeThemes",
                        label: "themeSwitcher",
                        defaultTheme: "defaultTheme",
                        options: [{
                            themeName: "defaultTheme",
                            buttonColor: "#ffffff",
                            textColor: "#323332"
                        }, {
                            themeName: "customTheme",
                            buttonColor: "#ffffff",
                            textColor: "#323332"
                        }]
                    },
                    topbarTheme: {
                        id: "topbarTheme",
                        label: "themeSwitcher.Topbar",
                        defaultTheme: "defaultTheme",
                        options: [{
                            themeName: "defaultTheme",
                            buttonColor: "#ffffff",
                            textColor: "#323332"
                        }, {
                            themeName: "theme1",
                            buttonColor: "#e0364c",
                            backgroundColor: "#e0364c",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme2",
                            buttonColor: "#6534ff",
                            backgroundColor: "#6534ff",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme3",
                            buttonColor: "#4482FF",
                            backgroundColor: "#4482FF",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme4",
                            buttonColor: "#422e62",
                            backgroundColor: "#422e62",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme5",
                            buttonColor: "#22144c",
                            backgroundColor: "#22144c",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme6",
                            buttonColor: "#4670a2",
                            backgroundColor: "#4670a2",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme7",
                            buttonColor: "#494982",
                            backgroundColor: "#494982",
                            textColor: "#ffffff"
                        }]
                    },
                    sidebarTheme: {
                        id: "sidebarTheme",
                        label: "themeSwitcher.Sidebar",
                        defaultTheme: "defaultTheme",
                        options: [{
                            themeName: "defaultTheme",
                            buttonColor: "#323332",
                            backgroundColor: void 0,
                            textColor: "#788195"
                        }, {
                            themeName: "theme1",
                            buttonColor: "#e0364c",
                            backgroundColor: "#e0364c",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme2",
                            buttonColor: "#6534ff",
                            backgroundColor: "#6534ff",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme3",
                            buttonColor: "#4482FF",
                            backgroundColor: "#4482FF",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme4",
                            buttonColor: "#422e62",
                            backgroundColor: "#422e62",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme5",
                            buttonColor: "#22144c",
                            backgroundColor: "#22144c",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme6",
                            buttonColor: "#4670a2",
                            backgroundColor: "#4670a2",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme7",
                            buttonColor: "#494982",
                            backgroundColor: "#494982",
                            textColor: "#ffffff"
                        }]
                    },
                    layoutTheme: {
                        id: "layoutTheme",
                        label: "themeSwitcher.Background",
                        defaultTheme: "defaultTheme",
                        options: [{
                            themeName: "defaultTheme",
                            buttonColor: "#ffffff",
                            backgroundColor: "#F1F3F6",
                            textColor: void 0
                        }, {
                            themeName: "theme1",
                            buttonColor: "#ffffff",
                            backgroundColor: "#ffffff",
                            textColor: "#323232"
                        }, {
                            themeName: "theme2",
                            buttonColor: "#F9F9F9",
                            backgroundColor: "#F9F9F9",
                            textColor: "#ffffff"
                        }, {
                            themeName: "theme3",
                            buttonColor: "#ebebeb",
                            backgroundColor: "#ebebeb",
                            textColor: "#ffffff"
                        }]
                    }
                };

            function V(e, t) {
                var n = {};
                return F[e].options.forEach((function(e) {
                    e.themeName === t && (n = e)
                })), n
            }
            var W = F,
                B = {
                    CHANGE_THEME: "CHANGE_THEME",
                    SWITCH_ACTIVATION: "SWITCH_ACTIVATION",
                    switchActivation: function() {
                        return {
                            type: B.SWITCH_ACTIVATION
                        }
                    },
                    changeTheme: function(e, t) {
                        var n = V(e, t);
                        return "layoutTheme" === e && (document.getElementsByClassName("isomorphicContent")[0].style.backgroundColor = n.backgroundColor), {
                            type: B.CHANGE_THEME,
                            attribute: e,
                            theme: n
                        }
                    }
                },
                Q = B,
                Y = {
                    isActivated: !1,
                    changeThemes: V("changeThemes", W.changeThemes.defaultTheme || "themedefault"),
                    topbarTheme: V("topbarTheme", W.topbarTheme.defaultTheme || "themedefault"),
                    sidebarTheme: V("sidebarTheme", W.sidebarTheme.defaultTheme || "themedefault"),
                    layoutTheme: V("layoutTheme", W.layoutTheme.defaultTheme || "themedefault")
                },
                K = n(67),
                J = n(109),
                z = {
                    isActivated: !1,
                    language: Object(K.b)(K.a.defaultLanguage || "english")
                },
                X = {
                    SHOW_DRAWER: "SHOW_DRAWER",
                    HIDE_DRAWER: "HIDE_DRAWER",
                    openDrawer: function(e) {
                        return {
                            type: X.SHOW_DRAWER,
                            payload: e
                        }
                    },
                    closeDrawer: function() {
                        return {
                            type: X.HIDE_DRAWER
                        }
                    }
                },
                Z = X,
                q = {
                    drawerVisibility: !1,
                    drawerType: "",
                    drawerProps: {}
                };
            var $ = {
                    SHOW_MODAL: "SHOW_MODAL",
                    HIDE_MODAL: "HIDE_MODAL",
                    openModal: function(e) {
                        return {
                            type: $.SHOW_MODAL,
                            payload: e
                        }
                    },
                    closeModal: function() {
                        return {
                            type: $.HIDE_MODAL
                        }
                    }
                },
                ee = $,
                te = {
                    modalVisibility: !1,
                    modalType: "",
                    modalProps: {}
                };
            var ne = n(140),
                re = {
                    frequencyUpdateSeconds: 300,
                    refreshDuration: 5
                },
                ae = Object(E.combineReducers)({
                    Auth: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : y,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case O.a.LOGIN_REQUEST:
                                return _.a.merge(e, {
                                    isFetching: !0
                                });
                            case O.a.LOGIN_SUCCESS:
                                return _.a.merge(e, {
                                    loginFailure: !1,
                                    isFetching: !1,
                                    errorMessage: "",
                                    actionType: t.type,
                                    accessToken: t.accessToken,
                                    currentUser: t.currentUser
                                });
                            case O.a.LOGIN_ERROR:
                                return _.a.merge(e, {
                                    loginFailure: !0,
                                    isFetching: !1,
                                    actionType: t.type,
                                    errorMessage: t.errorMessage
                                });
                            case O.a.LOGOUT:
                                return y;
                            default:
                                return e
                        }
                    },
                    App: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : S,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case T.a.COLLPSE_CHANGE:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    collapsed: !e.collapsed
                                });
                            case T.a.COLLPSE_OPEN_DRAWER:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    openDrawer: !e.openDrawer
                                });
                            case T.a.TOGGLE_ALL:
                                if (e.view !== t.view || t.height !== e.height) {
                                    var n = t.height ? t.height : e.height;
                                    return Object(b.a)(Object(b.a)({}, e), {}, {
                                        collapsed: t.collapsed,
                                        view: t.view,
                                        height: n
                                    })
                                }
                                break;
                            case T.a.CHANGE_OPEN_KEYS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    openKeys: t.openKeys
                                });
                            case T.a.CHANGE_CURRENT:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    current: t.current
                                });
                            case T.a.UPDATE_MODULE_TITLE:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    currentModule: t.module
                                });
                            case T.a.CLEAR_MENU:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    openKeys: [],
                                    current: []
                                });
                            default:
                                return e
                        }
                        return e
                    },
                    AddWidgets: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : j,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case v.a.GET_USERS_DEVICE_REQUEST:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !0
                                });
                            case v.a.GET_USERS_DEVICE_SUCCESS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !1,
                                    errorMessage: "",
                                    deviceList: t.deviceList
                                });
                            case v.a.GET_USERS_DEVICE_ERROR:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            case v.a.GET_DEVICE_PARAMETER_REQUEST:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !0
                                });
                            case v.a.GET_DEVICE_PARAMETER_SUCCESS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !1,
                                    isLoading: !1,
                                    deviceParameterList: t.deviceParameterList
                                });
                            case v.a.GET_DEVICE_PARAMETER_ERROR:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            case v.a.GET_GRAPH_DATA_REQUEST:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !0
                                });
                            case v.a.GET_GRAPH_DATA_SUCCESS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !1,
                                    isLoading: !1,
                                    graphData: t.graphData
                                });
                            case v.a.GET_GRAPH_DATA_ERROR:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            case v.a.UPDATE_SELECTED_PARAMS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !0
                                });
                            case v.a.UPDATE_SELECTED_PARAMS_SUCCESS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !1,
                                    isLoading: !1,
                                    displayWidget: t.displayWidget
                                });
                            case v.a.UPDATE_SELECTED_PARAMS_ERROR:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            case v.a.REMOVE_WIDGET_REQUEST:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isLoading: !0
                                });
                            case v.a.REMOVE_WIDGET_SUCCESS:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !1,
                                    isLoading: !1,
                                    displayWidget: t.displayWidget
                                });
                            case v.a.REMOVE_WIDGET_ERROR:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            default:
                                return e
                        }
                    },
                    Reports: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : w,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case x.a.GET_USER_PARAMETER_REQUEST:
                                return _.a.merge(e, {
                                    isLoading: !0
                                });
                            case x.a.GET_USER_PARAMETER_SUCCESS:
                                return _.a.merge(e, {
                                    failure: !1,
                                    isLoading: !1,
                                    userParameterList: t.userParameterList
                                });
                            case x.a.GET_USER_PARAMETER_ERROR:
                                return _.a.merge(e, {
                                    failure: !0,
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            case x.a.GET_REPORT_SETTINGS_REQUEST:
                                return _.a.merge(e, {
                                    isLoading: !0,
                                    actionType: t.type
                                });
                            case x.a.GET_REPORT_SETTINGS_SUCCESS:
                                return _.a.merge(e, {
                                    isLoading: !1,
                                    reportsSettings: t.reportsSettings
                                });
                            case x.a.GET_METRIC_DATA_SUCCESS:
                                return _.a.merge(e, {
                                    isLoading: !1,
                                    metricChartData: t.payload
                                });
                            case x.a.GET_REPORT_SETTINGS_ERROR:
                                return _.a.merge(e, {
                                    isLoading: !1,
                                    errorMessage: t.error
                                });
                            default:
                                return e
                        }
                    },
                    ThemeSwitcher: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : Y,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case Q.SWITCH_ACTIVATION:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isActivated: !e.isActivated
                                });
                            case Q.CHANGE_THEME:
                                return Object(b.a)(Object(b.a)({}, e), {}, Object(P.a)({}, t.attribute, t.theme));
                            default:
                                return e
                        }
                    },
                    LanguageSwitcher: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : z,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case J.a.ACTIVATE_LANG_MODAL:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    isActivated: !e.isActivated
                                });
                            case J.a.CHANGE_LANGUAGE:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    language: t.language
                                });
                            default:
                                return e
                        }
                    },
                    Box: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : L,
                            t = arguments.length > 1 ? arguments[1] : void 0,
                            n = e.allBox;
                        switch (t.type) {
                            case k.DELETE_BOX:
                                var r = [];
                                return n.map((function(e) {
                                    return e.uid.toString() !== t.uid.toString() && r.push(e), null
                                })), Object(b.a)(Object(b.a)({}, e), {}, {
                                    allBox: r,
                                    reload: !0
                                });
                            case k.SAVE_BOX:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    reload: !1,
                                    allBox: t.box
                                });
                            default:
                                return e
                        }
                    },
                    modal: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : te,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case ee.SHOW_MODAL:
                                return {
                                    modalVisibility: !0,
                                    modalType: t.payload.modalType,
                                    modalProps: t.payload.modalProps
                                };
                            case ee.HIDE_MODAL:
                                return te;
                            default:
                                return e
                        }
                    },
                    drawer: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : q,
                            t = arguments.length > 1 ? arguments[1] : void 0;
                        switch (t.type) {
                            case Z.SHOW_DRAWER:
                                return {
                                    drawerVisibility: !0,
                                    drawerType: t.payload.drawerType,
                                    drawerProps: t.payload.drawerProps
                                };
                            case Z.HIDE_DRAWER:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    drawerVisibility: !1
                                });
                            default:
                                return e
                        }
                    },
                    settingsReducer: function() {
                        var e = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : re,
                            t = arguments.length > 1 ? arguments[1] : void 0,
                            n = t.type,
                            r = t.payload,
                            a = void 0 === r ? re.frequencyUpdateSeconds : r;
                        switch (n) {
                            case ne.a:
                                return Object(b.a)(Object(b.a)({}, e), {}, {
                                    frequencyUpdateSeconds: a
                                });
                            default:
                                return e
                        }
                    }
                }),
                oe = n(6),
                ie = n.n(oe),
                se = n(7),
                ce = n(58),
                le = n(72),
                ue = n(73),
                de = n(180),
                pe = Object(de.create)({}),
                fe = new(function() {
                    function e() {
                        var t = this;
                        Object(le.a)(this, e), this.handlePromise = function(e, n, r) {
                            r.ok && r.data && null === r.originalError ? e(r.data) : r.data && !r.ok ? e({
                                data: r.data,
                                status: r.status
                            }) : 404 !== r.status || r.ok || null === r.originalError ? 401 === r.status && (t.props.history.push("/login"), alert("hello")) : n(r.problem)
                        }
                    }
                    return Object(ue.a)(e, [{
                        key: "post",
                        value: function() {
                            var e = Object(ce.a)(ie.a.mark((function e(t, n, r) {
                                var a, o = this;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return e.next = 2, pe.post(t, n, r);
                                        case 2:
                                            return a = e.sent, e.abrupt("return", new Promise((function(e, t) {
                                                o.handlePromise(e, t, a)
                                            })));
                                        case 4:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                            return function(t, n, r) {
                                return e.apply(this, arguments)
                            }
                        }()
                    }, {
                        key: "postWithToken",
                        value: function() {
                            var e = Object(ce.a)(ie.a.mark((function e(t, n, r) {
                                var a, o, i = this;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return a = {
                                                headers: {
                                                    "Content-Type": "application/json",
                                                    Authorization: "Bearer ".concat(r)
                                                }
                                            }, e.next = 3, pe.post(t, n, a);
                                        case 3:
                                            return o = e.sent, e.abrupt("return", new Promise((function(e, t) {
                                                i.handlePromise(e, t, o)
                                            })));
                                        case 5:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                            return function(t, n, r) {
                                return e.apply(this, arguments)
                            }
                        }()
                    }, {
                        key: "get",
                        value: function() {
                            var e = Object(ce.a)(ie.a.mark((function e(t, n) {
                                var r, a = this;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return pe.setHeaders({
                                                "Content-Type": "application/json"
                                            }), e.next = 3, pe.get(t, n);
                                        case 3:
                                            return r = e.sent, e.abrupt("return", new Promise((function(e, t) {
                                                a.handlePromise(e, t, r)
                                            })));
                                        case 5:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                            return function(t, n) {
                                return e.apply(this, arguments)
                            }
                        }()
                    }, {
                        key: "postWithCustomHeader",
                        value: function() {
                            var e = Object(ce.a)(ie.a.mark((function e(t, n) {
                                var r, a = this;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return e.next = 2, pe.post(t, n);
                                        case 2:
                                            return r = e.sent, e.abrupt("return", new Promise((function(e, t) {
                                                a.handlePromise(e, t, r)
                                            })));
                                        case 4:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                            return function(t, n) {
                                return e.apply(this, arguments)
                            }
                        }()
                    }, {
                        key: "getWithCustomHeader",
                        value: function() {
                            var e = Object(ce.a)(ie.a.mark((function e(t, n) {
                                var r, a = this;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return e.next = 2, pe.get(t, {}, n);
                                        case 2:
                                            return 401 === (r = e.sent).status && (window.location.replace("http://localhost:3000/"), localStorage.removeItem("accessToken"), localStorage.removeItem("persist:root"), localStorage.removeItem("currentUser"), localStorage.removeItem("currentModule"), localStorage.removeItem("widgetSettings"), localStorage.removeItem("parametersList"), localStorage.removeItem("deviceList")), e.abrupt("return", new Promise((function(e, t) {
                                                a.handlePromise(e, t, r)
                                            })));
                                        case 5:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                            return function(t, n) {
                                return e.apply(this, arguments)
                            }
                        }()
                    }]), e
                }()),
                Ee = ie.a.mark(Re),
                he = ie.a.mark(Se),
                me = ie.a.mark(Oe),
                be = ie.a.mark(Ce),
                ge = ie.a.mark(_e),
                Ae = ie.a.mark(ye);

            function Te(e) {
                return fe.post(e.url, e.body, {})
            }

            function Re() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("LOGIN_REQUEST", ie.a.mark((function e(t) {
                                var n, r, a, o, i;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.prev = 1, e.next = 4, Object(se.b)(Te, n);
                                        case 4:
                                            if (r = e.sent, a = r.accessToken, o = r.user, !a) {
                                                e.next = 11;
                                                break
                                            }
                                            return e.next = 9, Object(se.d)({
                                                type: O.a.LOGIN_SUCCESS,
                                                accessToken: a,
                                                currentUser: o
                                            });
                                        case 9:
                                            e.next = 13;
                                            break;
                                        case 11:
                                            return e.next = 13, Object(se.d)({
                                                type: O.a.LOGIN_ERROR,
                                                errorMessage: null === (i = r.data) || void 0 === i ? void 0 : i.statusText
                                            });
                                        case 13:
                                            e.next = 19;
                                            break;
                                        case 15:
                                            return e.prev = 15, e.t0 = e.catch(1), e.next = 19, Object(se.d)({
                                                type: O.a.LOGIN_ERROR,
                                                errorMessage: "Something went wrong. Please try after some time."
                                            });
                                        case 19:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e, null, [
                                    [1, 15]
                                ])
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Ee)
            }

            function Se() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)(O.a.LOGIN_SUCCESS, ie.a.mark((function e(t) {
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return e.next = 2, localStorage.setItem("accessToken", t.accessToken);
                                        case 2:
                                            return e.next = 4, localStorage.setItem("currentUser", JSON.stringify(t.currentUser));
                                        case 4:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), he)
            }

            function Oe() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)(O.a.LOGIN_ERROR, ie.a.mark((function e() {
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), me)
            }

            function Ce() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)(O.a.LOGOUT, ie.a.mark((function e(t) {
                                var n;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, "You have successfully logout", e.next = 5, Object(g.a)();
                                        case 5:
                                            n.history.push("/login");
                                        case 6:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), be)
            }

            function _e() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)(O.a.CHECK_AUTHORIZATION, ie.a.mark((function e() {
                                var t;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            if (!(t = Object(g.f)().get("accessToken"))) {
                                                e.next = 4;
                                                break
                                            }
                                            return e.next = 4, Object(se.d)({
                                                type: O.a.LOGIN_SUCCESS,
                                                token: t,
                                                profile: "Profile"
                                            });
                                        case 4:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), ge)
            }

            function ye() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.a)([Object(se.c)(_e), Object(se.c)(Re), Object(se.c)(Se), Object(se.c)(Oe), Object(se.c)(Ce)]);
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Ae)
            }
            var xe = ie.a.mark(Ie),
                we = ie.a.mark(Ue),
                ve = ie.a.mark(Ge),
                je = ie.a.mark(ke);

            function De(e) {
                var t = e.url,
                    n = e.header;
                return fe.getWithCustomHeader(t, n)
            }

            function Ie() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_USER_PARAMETER_REQUEST", ie.a.mark((function e(t) {
                                var n, r;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.next = 3, Object(se.b)(De, n);
                                        case 3:
                                            if (!(r = e.sent).success) {
                                                e.next = 9;
                                                break
                                            }
                                            return e.next = 7, Object(se.d)({
                                                type: x.a.GET_USER_PARAMETER_SUCCESS,
                                                userParameterList: r.userParameterList
                                            });
                                        case 7:
                                            e.next = 12;
                                            break;
                                        case 9:
                                            return e.next = 11, Object(se.d)({
                                                type: x.a.GET_USER_PARAMETER_ERROR
                                            });
                                        case 11:
                                            n.history.push("/");
                                        case 12:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), xe)
            }

            function Ue() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_REPORT_SETTINGS_REQUEST", ie.a.mark((function e(t) {
                                var n;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            if (!(n = t.payload).params) {
                                                e.next = 7;
                                                break
                                            }
                                            return e.next = 4, Object(se.d)({
                                                type: x.a.GET_REPORT_SETTINGS_SUCCESS,
                                                reportsSettings: n.params
                                            });
                                        case 4:
                                            localStorage.setItem("reportsSettingsStorage", JSON.stringify(n.params)), e.next = 9;
                                            break;
                                        case 7:
                                            return e.next = 9, Object(se.d)({
                                                type: x.a.GET_REPORT_SETTINGS_ERROR
                                            });
                                        case 9:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), we)
            }

            function Ge() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_METRIC_DATA_REQUEST", ie.a.mark((function e(t) {
                                var n, r;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.next = 3, Object(se.b)(De, n);
                                        case 3:
                                            if (!(r = e.sent)) {
                                                e.next = 9;
                                                break
                                            }
                                            return e.next = 7, Object(se.d)({
                                                type: "GET_METRIC_DATA_SUCCESS",
                                                getMetricsdata: r.graphData
                                            });
                                        case 7:
                                            e.next = 11;
                                            break;
                                        case 9:
                                            return e.next = 11, Object(se.d)({
                                                type: x.a.GET_METRIC_DATA_ERROR
                                            });
                                        case 11:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), ve)
            }

            function ke() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.a)([Object(se.c)(Ue), Object(se.c)(Ie), Object(se.c)(Ge)]);
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), je)
            }
            var Le = ie.a.mark(Be),
                Pe = ie.a.mark(Qe),
                Me = ie.a.mark(Ye),
                Ne = ie.a.mark(Ke),
                He = ie.a.mark(Je),
                Fe = ie.a.mark(ze);

            function Ve(e) {
                var t = e.url,
                    n = e.header;
                return fe.getWithCustomHeader(t, n)
            }

            function We(e) {
                var t = e.url,
                    n = e.body,
                    r = e.header;
                return fe.post(t, n, r)
            }

            function Be() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_USERS_DEVICE_REQUEST", ie.a.mark((function e(t) {
                                var n, r;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.next = 3, Object(se.b)(Ve, n);
                                        case 3:
                                            if (!(r = e.sent).success) {
                                                e.next = 9;
                                                break
                                            }
                                            return e.next = 7, Object(se.d)({
                                                type: v.a.GET_USERS_DEVICE_SUCCESS,
                                                deviceList: r.deviceList
                                            });
                                        case 7:
                                            e.next = 11;
                                            break;
                                        case 9:
                                            return e.next = 11, Object(se.d)({
                                                type: v.a.GET_USERS_DEVICE_ERROR
                                            });
                                        case 11:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Le)
            }

            function Qe() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_DEVICE_PARAMETER_REQUEST", ie.a.mark((function e(t) {
                                var n, r;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.next = 3, Object(se.b)(Ve, n);
                                        case 3:
                                            if (!(r = e.sent).success) {
                                                e.next = 9;
                                                break
                                            }
                                            return e.next = 7, Object(se.d)({
                                                type: v.a.GET_DEVICE_PARAMETER_SUCCESS,
                                                deviceParameterList: r.deviceParameterList
                                            });
                                        case 7:
                                            e.next = 11;
                                            break;
                                        case 9:
                                            return e.next = 11, Object(se.d)({
                                                type: v.a.GET_DEVICE_PARAMETER_ERROR
                                            });
                                        case 11:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Pe)
            }

            function Ye() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("GET_GRAPH_DATA_REQUEST", ie.a.mark((function e(t) {
                                var n, r;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            return n = t.payload, e.next = 3, Object(se.b)(We, n);
                                        case 3:
                                            if (!(r = e.sent).graphData) {
                                                e.next = 9;
                                                break
                                            }
                                            return e.next = 7, Object(se.d)({
                                                type: v.a.GET_GRAPH_DATA_SUCCESS,
                                                graphData: r.graphData
                                            });
                                        case 7:
                                            e.next = 11;
                                            break;
                                        case 9:
                                            return e.next = 11, Object(se.d)({
                                                type: v.a.GET_GRAPH_DATA_ERROR
                                            });
                                        case 11:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Me)
            }

            function Ke() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("UPDATE_SELECTED_PARAMS", ie.a.mark((function e(t) {
                                var n;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            if (!(n = t.payload).params) {
                                                e.next = 7;
                                                break
                                            }
                                            return e.next = 4, Object(se.d)({
                                                type: v.a.UPDATE_SELECTED_PARAMS_SUCCESS,
                                                displayWidget: n.params
                                            });
                                        case 4:
                                            localStorage.setItem("widgetSettings", JSON.stringify(n.params)), e.next = 9;
                                            break;
                                        case 7:
                                            return e.next = 9, Object(se.d)({
                                                type: v.a.UPDATE_SELECTED_PARAMS_ERROR
                                            });
                                        case 9:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Ne)
            }

            function Je() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.e)("REMOVE_WIDGET_REQUEST", ie.a.mark((function e(t) {
                                var n;
                                return ie.a.wrap((function(e) {
                                    for (;;) switch (e.prev = e.next) {
                                        case 0:
                                            if (!(n = t.payload).params) {
                                                e.next = 7;
                                                break
                                            }
                                            return localStorage.setItem("widgetSettings", JSON.stringify(n.params)), e.next = 5, Object(se.d)({
                                                type: v.a.REMOVE_WIDGET_SUCCESS,
                                                displayWidget: n.params
                                            });
                                        case 5:
                                            e.next = 9;
                                            break;
                                        case 7:
                                            return e.next = 9, Object(se.d)({
                                                type: v.a.REMOVE_WIDGET_ERROR
                                            });
                                        case 9:
                                        case "end":
                                            return e.stop()
                                    }
                                }), e)
                            })));
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), He)
            }

            function ze() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.a)([Object(se.c)(Be), Object(se.c)(Qe), Object(se.c)(Ke), Object(se.c)(Ye), Object(se.c)(Je)]);
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Fe)
            }
            var Xe = ie.a.mark(Ze);

            function Ze() {
                return ie.a.wrap((function(e) {
                    for (;;) switch (e.prev = e.next) {
                        case 0:
                            return e.next = 2, Object(se.a)([ye(), ze(), ke()]);
                        case 2:
                        case "end":
                            return e.stop()
                    }
                }), Xe)
            }
            var qe, $e = Object(m.a)(),
                et = [h.a, $e],
                tt = Object(E.createStore)(ae, (qe = et, E.applyMiddleware.apply(void 0, Object(f.a)(qe))));
            $e.run(Ze);
            var nt = n(133),
                rt = n(17),
                at = n(124),
                ot = n(121),
                it = n(123),
                st = function(e) {
                    Object(ot.a)(n, e);
                    var t = Object(it.a)(n);

                    function n(e) {
                        var r;
                        return Object(le.a)(this, n), (r = t.call(this, e)).state = {
                            hasError: !1
                        }, r
                    }
                    return Object(ue.a)(n, [{
                        key: "render",
                        value: function() {
                            return this.state.hasError ? Object(D.jsx)("p", {
                                children: "Loading failed! Please reload."
                            }) : this.props.children
                        }
                    }], [{
                        key: "getDerivedStateFromError",
                        value: function(e) {
                            return {
                                hasError: !0
                            }
                        }
                    }]), n
                }(o.a.Component),
                ct = "/",
                lt = "/login",
                ut = "/forgot-password",
                dt = "/404",
                pt = "/500",
                ft = n(134),
                Et = ["children"],
                ht = Object(a.lazy)((function() {
                    return Promise.all([n.e(11), n.e(14)]).then(n.bind(null, 869))
                })),
                mt = [{
                    path: ct,
                    exact: !0,
                    component: Object(a.lazy)((function() {
                        return Promise.all([n.e(0), n.e(1), n.e(2), n.e(4)]).then(n.bind(null, 878))
                    }))
                }, {
                    path: dt,
                    component: Object(a.lazy)((function() {
                        return n.e(17).then(n.bind(null, 879))
                    }))
                }, {
                    path: pt,
                    component: Object(a.lazy)((function() {
                        return n.e(18).then(n.bind(null, 880))
                    }))
                }, {
                    path: lt,
                    component: Object(a.lazy)((function() {
                        return Promise.all([n.e(0), n.e(1), n.e(2), n.e(4)]).then(n.bind(null, 878))
                    }))
                }, {
                    path: ut,
                    component: Object(a.lazy)((function() {
                        return Promise.all([n.e(0), n.e(1), n.e(2), n.e(13)]).then(n.bind(null, 881))
                    }))
                }];

            function bt(e) {
                var t = e.children,
                    n = Object(nt.a)(e, Et),
                    r = Object(c.c)((function(e) {
                        return e.Auth.currentUser
                    }));
                return Object(D.jsx)(rt.b, Object(b.a)(Object(b.a)({}, n), {}, {
                    render: function(e) {
                        var n = e.location;
                        return r ? t : Object(D.jsx)(rt.a, {
                            to: {
                                pathname: "/login",
                                state: {
                                    from: n
                                }
                            }
                        })
                    }
                }))
            }

            function gt() {
                return Object(D.jsx)(st, {
                    children: Object(D.jsx)(a.Suspense, {
                        fallback: Object(D.jsx)(ft.a, {}),
                        children: Object(D.jsx)(at.a, {
                            children: Object(D.jsxs)(rt.d, {
                                children: [mt.map((function(e, t) {
                                    return Object(D.jsx)(rt.b, {
                                        path: e.path,
                                        exact: e.exact,
                                        children: Object(D.jsx)(e.component, {})
                                    }, t)
                                })), Object(D.jsx)(bt, {
                                    path: "/dashboard",
                                    children: Object(D.jsx)(ht, {})
                                })]
                            })
                        })
                    })
                })
            }
            n(349);
            var At = n(51),
                Tt = n(363),
                Rt = n(182),
                St = n.n(Rt),
                Ot = n(181),
                Ct = {
                    messages: Object(b.a)({}, Ot),
                    antd: St.a,
                    locale: "en-US"
                },
                _t = n(183),
                yt = {
                    en: Ct,
                    hi: {
                        messages: Object(b.a)({}, _t),
                        antd: null,
                        locale: "hi-Hindi"
                    }
                };

            function xt(e) {
                var t = e.children,
                    n = Object(c.c)((function(e) {
                        return e.LanguageSwitcher.language
                    })).locale,
                    r = Object(c.c)((function(e) {
                        return e.ThemeSwitcher.changeThemes
                    })).themeName,
                    a = yt[n];
                return Object(D.jsx)(At.a, {
                    locale: a.antd,
                    children: Object(D.jsx)(Tt.a, {
                        locale: a.locale,
                        messages: a.messages,
                        children: Object(D.jsx)(u.a, {
                            theme: H[r],
                            children: t
                        })
                    })
                })
            }
            var wt = function() {
                    return Object(D.jsx)(c.a, {
                        store: tt,
                        children: Object(D.jsxs)(xt, {
                            children: [Object(D.jsx)(p, {}), Object(D.jsx)(gt, {})]
                        })
                    })
                },
                vt = function(e) {
                    e && e instanceof Function && n.e(20).then(n.bind(null, 866)).then((function(t) {
                        var n = t.getCLS,
                            r = t.getFID,
                            a = t.getFCP,
                            o = t.getLCP,
                            i = t.getTTFB;
                        n(e), r(e), a(e), o(e), i(e)
                    }))
                };
            Boolean("localhost" === window.location.hostname || "[::1]" === window.location.hostname || window.location.hostname.match(/^127(?:\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$/));
            s.a.render(Object(D.jsx)(wt, {}), document.getElementById("root")), vt(), "serviceWorker" in navigator && navigator.serviceWorker.ready.then((function(e) {
                e.unregister()
            }))
        },
        43: function(e, t, n) {
            "use strict";

            function r(e) {
                var t = "MobileView";
                return e > 1220 ? t = "DesktopView" : e > 767 && (t = "TabView"), t
            }
            n.d(t, "b", (function() {
                return r
            }));
            var a = {
                COLLPSE_CHANGE: "COLLPSE_CHANGE",
                COLLPSE_OPEN_DRAWER: "COLLPSE_OPEN_DRAWER",
                CHANGE_OPEN_KEYS: "CHANGE_OPEN_KEYS",
                TOGGLE_ALL: "TOGGLE_ALL",
                CHANGE_CURRENT: "CHANGE_CURRENT",
                CLEAR_MENU: "CLEAR_MENU",
                UPDATE_MODULE_TITLE: "UPDATE_MODULE_TITLE",
                toggleCollapsed: function() {
                    return {
                        type: a.COLLPSE_CHANGE
                    }
                },
                toggleAll: function(e, t) {
                    var n = r(e),
                        o = "DesktopView" !== n;
                    return {
                        type: a.TOGGLE_ALL,
                        collapsed: o,
                        view: n,
                        height: t
                    }
                },
                toggleOpenDrawer: function() {
                    return {
                        type: a.COLLPSE_OPEN_DRAWER
                    }
                },
                changeOpenKeys: function(e) {
                    return {
                        type: a.CHANGE_OPEN_KEYS,
                        openKeys: e
                    }
                },
                changeCurrent: function(e) {
                    return {
                        type: a.CHANGE_CURRENT,
                        current: e
                    }
                },
                updateModuleTitle: function(e) {
                    return {
                        type: a.UPDATE_MODULE_TITLE,
                        module: e
                    }
                },
                clearMenu: function() {
                    return {
                        type: a.CLEAR_MENU
                    }
                }
            };
            t.a = a
        },
        45: function(e, t, n) {
            "use strict";
            n.d(t, "a", (function() {
                return s
            })), n.d(t, "f", (function() {
                return c
            })), n.d(t, "g", (function() {
                return l
            })), n.d(t, "d", (function() {
                return u
            })), n.d(t, "e", (function() {
                return d
            })), n.d(t, "c", (function() {
                return p
            })), n.d(t, "b", (function() {
                return f
            }));
            var r = n(27),
                a = n(89),
                o = n(83),
                i = n.n(o);

            function s() {
                localStorage.removeItem("accessToken"), localStorage.removeItem("currentUser"), localStorage.removeItem("selectedUser")
            }

            function c() {
                try {
                    var e = localStorage.getItem("accessToken");
                    return new r.Map({
                        idToken: e
                    })
                } catch (t) {
                    return s(), new r.Map
                }
            }
            var l = "undefined" === typeof window;

            function u() {
                if (!l && window.location.pathname) {
                    var e = window.location.pathname.split("/");
                    if (e.length > 1) return function(e) {
                        var t = [];
                        return e ? (t.push(e), a.a.forEach((function(n) {
                            n.children && n.children.forEach((function(r) {
                                r.key === e && t.push(n.key)
                            }))
                        })), t) : t
                    }(e[e.length - 1])
                }
                return []
            }
            var d = function(e) {
                    return i()().subtract(e, "day").endOf("day")
                },
                p = function() {
                    return i()().endOf("day")
                },
                f = function(e) {
                    var t = 1;
                    return "-" === e[0] && (t = -1, e = e.substr(1)),
                        function(n, r) {
                            return -1 === t ? r[e].localeCompare(n[e]) : n[e].localeCompare(r[e])
                        }
                }
        },
        67: function(e, t, n) {
            "use strict";
            n.d(t, "b", (function() {
                return o
            }));
            var r = n(120),
                a = {
                    defaultLanguage: Object(r.a)("userLanguage"),
                    options: [{
                        languageId: "english",
                        locale: "en",
                        text: "English"
                    }, {
                        languageId: "hindi",
                        locale: "hi",
                        text: "Hindi"
                    }]
                };

            function o(e) {
                var t = a.options[0];
                return a.options.forEach((function(n) {
                    n.languageId === e && (t = n)
                })), t
            }
            t.a = a
        },
        89: function(e, t, n) {
            "use strict";
            var r = [{
                key: "dashboard",
                label: "sidebar.dashboard",
                displayName: "sidebar.dashboard",
                leftIcon: n(156).a,
                leftPosition: 24
            }, {
                key: "report",
                label: "sidebar.report",
                displayName: "sidebar.report.realtime",
                leftIcon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB0AAAAoCAYAAAACJPERAAAABHNCSVQICAgIfAhkiAAAAqpJREFUWEfNmI01A0EUhbMVoAJUgApEBahAVIAKRAWoQDoQFUgqIBVYHVBB3G/N5MzOT2QyEeacPbt2Z96d+959b15U0+n0sdPpnOhax3gVyFEl0A89bKwD0WAcANrVH/Y6NB/udWczpePaGHjXfaDrtaqqYWWtCryv52aSPszel6DK5tSsH8skxJrxf0CbYFfVqIQla7OYav6BQFFa0cgC/ZOYit6WgIvVm8X0F2L6JBKzAuSql5dUJwbxLGYqGzZNbgTaj6UME56LlJNefCXQuwDUk/iqsVvZ0Ko8CvxAaGcrRnwXyx3Xpg/6Gy5uuRbwoMaK7VDvj1fEdiKW+76tGOimJtW6So+7T9Qbq2zR00Rs2d2oEPhcgGgkGMkjTMAEH1fvZbo6yTCaMjHjmYoey8bJTyV07mEtQA71fiZTvINrkxVtnnv9hg0WNkYPZiO0NcSeEurm93cDlgBOCQmjPWOY/qZnD3XTU9lyOctBowE2ZfusJHAsZS618NYATnRH9i1XCQB2CI1vtet+TwNDfT/1w+NXJHL0TRd3VLjzkyhi8RYwLK3qg9bHB+UkuDCGknkWA/LY4gU2zxhp40fudx+UiSwIirRnFOFsyxhCig7PzbtuGNxDnCr0Yiy0Dt05LE5ljBQJhkDdpqDlNRe0q5VWlUEcrFVPvcnNMd/pkZKdQ1/z7M+AKKiMIDDSyfY7CAYW0XbVAb3XHLKiGdEOX+8DUHMI4AmA3UE6ka8D38dF3aAW92SQ3PUBXZw7AV+5L5YGzay/sIV1U0yyQbWGOMEOljljVv5yQSldiCpoNRZEhykFwabgWOy7dm1KSAvanjsNYKuBNqhJA+okbsx15aKbw+WkDJWu5uc/LljWjYuC2nmwb/7nUOthO3d1wfxdQPH7upjWuPcLDlVtrBIkqPEAAAAASUVORK5CYII=",
                leftPosition: 38
            }, {
                key: "settings",
                label: "sidebar.settings",
                displayName: "sidebar.settings",
                leftIcon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB8AAAAfCAYAAAAfrhY5AAAABHNCSVQICAgIfAhkiAAAAedJREFUSEvNV4FRwzAMrDeACVomgE5AR4AJaDeACWgnACYgTAAj0AlINkgngA3Cf87OxY4t2z0S6jvf5WxLL1nSW1GzzNE0TQmRS49YqZRa5qhTOYcBfIXzX4LMEgbQuKQRBAfQDTTMoezFaMLaPb6fBM0POP/snK+x9uGT8YIDZI3Dr1qAnuz0VXN9IYDX2CswKbPF5E1xbGAA160xAHeABZzsrYEBFviIwMZSy4AOXMf4PduffIFbkwN98Fgy5cP4JbqkdK89VMN/BVzBa5OEMxec5TXm1XdXTm9ccJbHY8TNA/YZok948YNcOcP3CpP1PY/I7iBDjHa04Jq5rrXShaDgDcLr0D70FNi7E+RrbeSeTKgErnZ1HCAgGdaehz4CxG6AR1vwJnJVZtuKl+B9ct7kgJ8zxjFDdQ58x861MU/1HMDJL2CyztSDMPT/PAf4KDFPZTW+yxexWGZke9Wv85Wuc6lMChiw8RmgE42NRpAHsEeCIhmRoMpjGI51TIYjURiGI0FRaYwHhgxnPJngWRW5PTX+sdCH9q0O9zTe8wmufEDTp9HD9ZKOpWLa5mNj65OTu9eAARXWt5hsf2iYxAOs4wLT9O3mtyqtb3fKbuH5Axn3jyV015P9qwkGhLjA6kxTkuUXOmvvpcOiUqsAAAAASUVORK5CYII=",
                leftPosition: 30
            }, {
                key: "profile",
                label: "sidebar.users",
                displayName: "sidebar.users",
                leftIcon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB8AAAAfCAYAAAAfrhY5AAAABHNCSVQICAgIfAhkiAAAAnJJREFUSEu1l4tRAjEQhrkKtAPPCtQKhAqUCsQKlAqECsQKxArUDrACsQKxArEC/L8jYcKRS/YY3ZkbHtndf9/ZKzpGWq1Wp2I913PpRLruc6FP/7zo+1tRFEuL2iLHJNCBeO70lDne4Hyq72MZgVGN1AjuPH2UJB7vSyMZMG4SjoI7bwH+C5pLSS+Wih1wAY9cmFPAXzpEKU9XD7WQImoAA+Df0Ba40eOhlExCJZIr9XuaMWInAhtwl+P3jAd9AVPRUZIOAE4SOmaS7/nzEDwn+CpB32ZN4ETgM+PAtfQQpU4Fbgz3Riil3OD9QuDHIfhCP44yFlMwswwPjsCTK8DKkcKYazCT+fZGGcGrFAJ+K8H7nEc6f5LAIBPyQ51/G3R1pKsA3BImr++s3qshkHTRCRcWcPH02oJHh4UrWnr/xgi8TqOsXbUQ8Kx4SGtSqMx+WrBsqWe8L3hLnCh7BY71uTb7C7C6jqE15z+SJKcU5zx2Q7mWJfQDY9FVBTcV81XCtQ+dda3bCXqME/MMcIrluQEcj8s2wF5P5mr+ks4S8NRgeBATQ6g1WfT6i6Up9KZ53mRZYoAdy6mFB6dQYlfhf4BvxnR4n8cm1Iss7LeO+broGD715YQaOsVrdIbg5J5Wqm8i/McVWAlYyF1WrNvoDGlrJ6jvcDADclATYqYzUscpI1yL0bbdiJHIjsL/Y9sr4ZpGIuDlMI5ohJEALAboZXaAOWja24kAnuY2koiDW3+R41u2lhhj8nXJhZFQ7TP7nyTHG0sYoS0bsu9qcDsjmIS5RYGXCSI2sRSoCTw0V4aQW9JCbUAUI3f7MrXlxML+C6FWEIo0vAqMAAAAAElFTkSuQmCC",
                leftPosition: 24
            }, {
                key: "logout",
                label: "sidebar.logout",
                displayName: "sidebar.logout",
                leftIcon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB8AAAAfCAYAAAAfrhY5AAAABHNCSVQICAgIfAhkiAAAActJREFUSEvtl/FVwyAQxsMEuoHdQDewnUCdwDpB6wa6gU5gO4G6QdygmcB0ArtB/L4UeAQvAinx+Ud5j9dXOO53dxxwUU3TnBZF8YJ+jT601Vj4hv6olNrFKlGAc9FV7IKAXAn4LFYX4U2sMOTW6PTSb3MMnOnBOxiwitGZCqfOGyhntGzTW/elB54xvxwL/gHlU1+5E0FxXjJmiOcF4Orfw/V2LGDoO+zd+AaP6rlzknj8Zr4BOeFPANBLm5CAl/h/qT3+YUA2uJRQHpwiHQP+Gt4xIAscHl5Aq3RL8vKZCFHZRyDxhmv1uEcN66n8M+ZS8WTqHHA+TDX6SaIB1cFwAvV5Zuj9xhNwLoxXGJtmgfd5LGQ7RfdgPL1Z4Uw89yIR4Bbc5s6hCWe8dkD3MIDh5naU+DGXTAecG27qAvuqAb4C5NYNtbtFOT2X4DwJTMSNVF6NCg8dvSM8FKHOfKCSWWN+HqNwUNih+MFTzqQydb89aiEDhsL79FbwWrpmRXnC+bylPgqSMtb0y9QvFobrNRQib979KtlJxWGMvrYE1m8yC30aYr48+tZvAZvEKA/JSPU394zZ2mdIdDYnw90FujxyDUne198M+AbbPCMS1L37RwAAAABJRU5ErkJggg==",
                leftPosition: 36
            }];
            t.a = r
        }
    },
    [
        [357, 8, 9]
    ]
]);
//# sourceMappingURL=main.5f4aaa6e.chunk.js.map