(this["webpackJsonpmydrishti-ui"] = this["webpackJsonpmydrishti-ui"] || []).push([
    [14], {
        373: function(n, e, t) {
            "use strict";
            t.d(e, "a", (function() {
                return i
            }));
            var i = {
                PRIMARY_BLUE: "#3775F2",
                WHITE_COLOR: "#ffffff",
                LIGHT_GRAY_COLOR: "#efefef",
                PRIMARY_COLOR: "#0E8012",
                SECONDARY_COLOR: "#f4f9e7",
                HEARDER_COLOR: "#788195",
                PRIMARY_BG_COLOR: "#f8f9fe",
                SECONDARY_BG_COLOR: "",
                BORDER_COLOR: "",
                SECONDARY_BUTTON_COLOR: "",
                TOPBAR_HEIGHT: "70px",
                PRIMARY_BUTTON_HEIGHT: "30px",
                LINE_HEIGHT: "30px",
                PRIMARY_BOX_SHADOW: "30px",
                HEADER_FONT_SIZE: "19px",
                PRIMARY_FONT_FAMILY: "Roboto",
                FONT_WEIGHT: "500",
                LINK_COLOR: "#3F9A42",
                DISABLE_COLOR: "#f5f5f5"
            }
        },
        374: function(n, e, t) {
            "use strict";
            var i = t(5),
                a = (t(0), t(12)),
                r = "ltr";
            "undefined" !== typeof window && (r = document.getElementsByTagName("html")[0].getAttribute("dir"));
            e.a = function(n) {
                return function(e) {
                    return Object(a.jsx)(n, Object(i.a)(Object(i.a)({}, e), {}, {
                        "data-rtl": r
                    }))
                }
            }
        },
        387: function(n, e, t) {
            "use strict";
            var i = t(5),
                a = (t(0), t(601)),
                r = t(132),
                o = t(12);
            e.a = Object(r.c)((function(n) {
                return Object(o.jsx)(a.a, Object(i.a)({}, n))
            }), {
                withRef: !1
            })
        },
        388: function(n, e, t) {
            "use strict";
            t.d(e, "a", (function() {
                return i
            })), t.d(e, "b", (function() {
                return a
            }));
            var i = "http://65.0.150.52:8888/",
                a = {
                    siteName: "My Drishti",
                    siteIcon: "ion-flash",
                    footerText: "Copyright MyDrishti @ ".concat((new Date).getFullYear()),
                    enableAnimatedRoute: !1,
                    dashboard: "/dashboard"
                }
        },
        389: function(n, e, t) {
            "use strict";

            function i() {
                var n = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : .3;
                return "\n      -webkit-transition: all ".concat(n, "s cubic-bezier(0.215, 0.61, 0.355, 1);\n      -moz-transition: all ").concat(n, "s cubic-bezier(0.215, 0.61, 0.355, 1);\n      -ms-transition: all ").concat(n, "s cubic-bezier(0.215, 0.61, 0.355, 1);\n      -o-transition: all ").concat(n, "s cubic-bezier(0.215, 0.61, 0.355, 1);\n      transition: all ").concat(n, "s cubic-bezier(0.215, 0.61, 0.355, 1);\n  ")
            }

            function a() {
                var n = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : 0;
                return "\n      -webkit-border-radius: ".concat(n, ";\n      -moz-border-radius: ").concat(n, ";\n      -ms-transition: ").concat(n, ";\n      -o-border-radius: ").concat(n, ";\n      border-radius: ").concat(n, ";\n  ")
            }

            function r() {
                var n = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "none";
                return "\n      -webkit-box-shadow: ".concat(n, ";\n      -moz-box-shadow: ").concat(n, ";\n      box-shadow: ").concat(n, ";\n  ")
            }
            t.d(e, "c", (function() {
                return i
            })), t.d(e, "a", (function() {
                return a
            })), t.d(e, "b", (function() {
                return r
            }))
        },
        411: function(n, e, t) {
            "use strict";
            e.a = t.p + "static/media/logo.9adf61f1.png"
        },
        444: function(n, e, t) {
            "use strict";
            var i = t(369),
                a = t(0),
                r = t.n(a);
            e.a = function() {
                var n = "object" === typeof window,
                    e = r.a.useCallback((function() {
                        return {
                            width: n ? window.innerWidth : void 0,
                            height: n ? window.innerHeight : void 0
                        }
                    }), [n]),
                    t = r.a.useState(e),
                    a = Object(i.a)(t, 2),
                    o = a[0],
                    c = a[1];
                return r.a.useEffect((function() {
                    if (!n) return !1;
                    var t = function() {
                        c(e)
                    };
                    return window.addEventListener("resize", t),
                        function() {
                            return window.removeEventListener("resize", t)
                        }
                }), [e, n]), o
            }
        },
        518: function(n, e, t) {
            "use strict";
            t(0);
            var i = t(622),
                a = t(12);
            e.a = function(n) {
                var e = n.id,
                    t = n.style,
                    r = n.children,
                    o = n.className;
                return Object(a.jsx)(i.Scrollbars, {
                    id: e,
                    className: o,
                    style: t,
                    autoHide: !0,
                    autoHideTimeout: 1e3,
                    autoHideDuration: 200,
                    autoHeightMin: 0,
                    autoHeightMax: 200,
                    thumbMinSize: 30,
                    universal: !0,
                    children: r
                })
            }
        },
        869: function(n, e, t) {
            "use strict";
            t.r(e), t.d(e, "default", (function() {
                return Un
            }));
            var i, a = t(116),
                r = t(369),
                o = (t(442), t(447)),
                c = (t(443), t(502)),
                s = t(0),
                l = t.n(s),
                d = t(47),
                p = t(401),
                u = t(444),
                m = t(373),
                h = t(120),
                f = t(15),
                g = t(43),
                b = t(89),
                x = t(518),
                A = (t(520), t(435).a),
                j = t(124),
                y = t(388),
                v = t(411),
                O = t(12),
                w = g.a.changeCurrent,
                E = g.a.updateModuleTitle,
                C = function(n) {
                    var e = n.collapsed,
                        t = Object(d.b)();
                    return Object(O.jsx)("div", {
                        className: "isoLogoWrapper",
                        children: e ? Object(O.jsx)("div", {
                            children: Object(O.jsx)("h3", {
                                children: Object(O.jsx)(j.b, {
                                    to: "/dashboard",
                                    children: Object(O.jsx)("i", {
                                        className: y.b.siteIcon
                                    })
                                })
                            })
                        }) : Object(O.jsx)("div", {
                            className: "logoContent",
                            children: Object(O.jsx)(j.b, {
                                to: "/dashboard",
                                onClick: function() {
                                    t(w(["dashboard"])), t(E("sidebar.dashboard")), localStorage.setItem("currentModule", JSON.stringify("sidebar.dashboard"))
                                },
                                children: Object(O.jsx)("img", {
                                    alt: "logo",
                                    src: v.a
                                })
                            })
                        })
                    })
                },
                I = t(71),
                D = t(52),
                k = t(60),
                B = t(389),
                N = t(374),
                M = D.d.div(i || (i = Object(I.a)(["\n  .isomorphicSidebar {\n    z-index: 1000;\n    background: ", ";\n    flex: 0 0 280px;\n\n    .scrollarea {\n      height: calc(100vh - 70px);\n    }\n\n    &.ant-layout-sider-collapsed {\n      width: 0;\n      min-width: 0 !important;\n      max-width: 0 !important;\n      flex: 0 0 0 !important;\n    }\n\n    @media only screen and (max-width: 767px) {\n      width: 200px !important;\n      flex: 0 0 240px !important;\n    }\n\n    .isoLogoWrapper {\n      height: 70px;\n      background: ", ";\n      margin: 0;\n      text-align: center;\n      overflow: hidden;\n      ", ";\n\n      .logoContent {\n        height: 100px;\n        width: 160px;\n      }\n\n      img {\n        height: 70%;\n      }\n\n      h3 {\n        a {\n          font-size: 21px;\n          font-weight: 300;\n          line-height: 70px;\n          letter-spacing: 3px;\n          text-transform: uppercase;\n          color: ", ";\n          display: block;\n          text-decoration: none;\n        }\n      }\n    }\n\n    &.ant-layout-sider-collapsed {\n      .isoLogoWrapper {\n        padding: 0;\n\n        h3 {\n          a {\n            font-size: 27px;\n            font-weight: 500;\n            letter-spacing: 0;\n          }\n        }\n      }\n    }\n\n    .isoDashboardMenu {\n      padding: 20px;\n      background: transparent;\n\n      a {\n        text-decoration: none;\n        font-weight: 400;\n      }\n\n      .ant-menu-item {\n        display: -ms-flexbox;\n        display: flex;\n        -ms-flex-align: center;\n        align-items: center;\n        margin: 0;\n        height: 100px;\n      }\n\n      .isoMenuHolder {\n        display: block;\n        text-align: center;\n\n        i {\n          font-size: 25px;\n          color: inherit;\n          position: absolute;\n          margin: ", ";\n          ", ";\n        }\n      }\n\n      .anticon {\n        font-size: 18px;\n        margin-right: 30px;\n        color: inherit;\n        ", ";\n      }\n\n      .nav-text {\n        font-size: 13px;\n        color: inherit;\n        font-weight: 400;\n        text-transform: uppercase;\n        ", ";\n      }\n\n      .ant-menu-item-selected {\n        background-color: rgba(0, 0, 0, 0.4) !important;\n        border-radius: 10px;\n        .anticon {\n          color: #fff;\n        }\n\n        i {\n          color: #fff;\n        }\n\n        .nav-text {\n          color: #fff;\n        }\n      }\n\n      > li {\n        &:hover {\n          i,\n          .nav-text {\n            color: #ffffff;\n          }\n        }\n      }\n    }\n\n    .ant-menu-dark .ant-menu-inline.ant-menu-sub {\n      background: ", ";\n    }\n\n    .ant-menu-submenu-inline,\n    .ant-menu-submenu-vertical {\n      > .ant-menu-submenu-title {\n        width: 100%;\n        display: flex;\n        align-items: center;\n        /* padding: 0 24px; */\n\n        > span {\n          display: flex;\n          align-items: center;\n        }\n\n        .ant-menu-submenu-arrow {\n          left: ", ";\n          right: ", ";\n\n          &:before,\n          &:after {\n            width: 8px;\n            ", ";\n          }\n\n          &:before {\n            transform: rotate(-45deg) translateX(3px);\n          }\n\n          &:after {\n            transform: rotate(45deg) translateX(-3px);\n          }\n\n          ", ";\n        }\n\n        &:hover {\n          .ant-menu-submenu-arrow {\n            &:before,\n            &:after {\n              color: #ffffff;\n            }\n          }\n        }\n      }\n\n      .ant-menu-inline,\n      .ant-menu-submenu-vertical {\n        > li:not(.ant-menu-item-group) {\n          padding-left: ", ";\n          padding-right: ", ";\n          font-size: 13px;\n          font-weight: 400;\n          margin: 0;\n          color: inherit;\n          ", ";\n\n          &:hover {\n            a {\n              color: #ffffff !important;\n            }\n          }\n        }\n\n        .ant-menu-item-group {\n          padding-left: 0;\n\n          .ant-menu-item-group-title {\n            padding-left: 100px !important;\n          }\n          .ant-menu-item-group-list {\n            .ant-menu-item {\n              padding-left: 125px !important;\n            }\n          }\n        }\n      }\n\n      .ant-menu-sub {\n        box-shadow: none;\n        background-color: transparent !important;\n      }\n    }\n\n    &.ant-layout-sider-collapsed {\n      .nav-text {\n        display: none;\n      }\n\n      .ant-menu-submenu-inline > {\n        .ant-menu-submenu-title:after {\n          display: none;\n        }\n      }\n\n      .ant-menu-submenu-vertical {\n        > .ant-menu-submenu-title:after {\n          display: none;\n        }\n\n        .ant-menu-sub {\n          background-color: transparent !important;\n\n          .ant-menu-item {\n            height: 35px;\n          }\n        }\n      }\n    }\n  }\n"])), Object(k.palette)("secondary", 0), m.a.PRIMARY_COLOR, Object(B.a)(), Object(k.palette)("grayscale", 6), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0 0 0 30px" : "-34px 2px 15px -12px"
                }), Object(B.c)(), Object(B.c)(), Object(B.c)(), Object(k.palette)("secondary", 5), (function(n) {
                    return "rtl" === n["data-rtl"] ? "25px" : "auto"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "auto" : "25px"
                }), Object(B.c)(), "", (function(n) {
                    return "rtl" === n["data-rtl"] ? "0px !important" : "74px !important"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "74px !important" : "0px !important"
                }), Object(B.c)()),
                R = Object(N.a)(M),
                L = t(5),
                S = t(133),
                z = t(17),
                Q = t(387),
                U = t(32),
                Y = ["singleOption", "submenuStyle", "submenuColor"],
                P = U.a.logout,
                G = g.a.updateModuleTitle,
                T = g.a.changeCurrent,
                J = l.a.memo((function(n) {
                    var e, t = n.singleOption,
                        i = (n.submenuStyle, n.submenuColor),
                        a = Object(S.a)(n, Y),
                        r = Object(z.i)(),
                        o = Object(d.c)((function(n) {
                            return n.Auth.currentUser
                        })),
                        c = Object(h.a)("currentUser"),
                        s = (null === o || void 0 === o ? void 0 : o.userId) ? o : c,
                        l = Object(d.b)(),
                        p = Object(z.g)(),
                        u = t.key,
                        m = t.label,
                        f = t.displayName,
                        g = t.leftIcon,
                        b = t.leftPosition,
                        x = "/" === (e = r.url).substr(-1) ? e.substr(0, e.length - 1) : e;
                    return Object(O.jsx)(A.Item, Object(L.a)(Object(L.a)({}, a), {}, {
                        inlineindent: b,
                        children: "logout" === u ? Object(O.jsxs)("div", {
                            className: "isoMenuHolder",
                            style: i,
                            onClick: function() {
                                var n = {
                                    email: null === s || void 0 === s ? void 0 : s.email
                                };
                                l(P("logout_url", n, null === s || void 0 === s ? void 0 : s.accessToken, p)), l(T(["dashboard"])), l(G("sidebar.dashboard")), localStorage.setItem("currentModule", JSON.stringify("sidebar.dashboard"))
                            },
                            children: [Object(O.jsx)("img", {
                                alt: "leftIcon",
                                src: g
                            }), Object(O.jsx)("div", {
                                className: "nav-text",
                                children: Object(O.jsx)(Q.a, {
                                    id: m
                                })
                            })]
                        }) : Object(O.jsx)(j.b, {
                            to: "dashboard" === u ? "".concat(x) : "".concat(x, "/").concat(u),
                            onClick: function() {
                                l(G(f)), localStorage.setItem("currentModule", JSON.stringify(f))
                            },
                            children: Object(O.jsxs)("div", {
                                className: "isoMenuHolder",
                                style: i,
                                children: [Object(O.jsx)("img", {
                                    alt: "leftIcon",
                                    src: g
                                }), Object(O.jsx)("div", {
                                    className: "nav-text",
                                    children: Object(O.jsx)(Q.a, {
                                        id: m
                                    })
                                })]
                            })
                        })
                    }), u)
                })),
                H = o.a.Sider,
                F = o.a.Footer,
                V = {
                    footer: {
                        background: m.a.PRIMARY_COLOR,
                        textAlign: "center",
                        position: "absolute",
                        bottom: "0",
                        padding: "21.5px",
                        color: m.a.WHITE_COLOR,
                        fontSize: "x-small"
                    }
                },
                W = g.a.changeOpenKeys,
                K = g.a.changeCurrent,
                X = g.a.toggleCollapsed;

            function Z() {
                var n = Object(d.b)(),
                    e = Object(d.c)((function(n) {
                        return n.App
                    })),
                    t = e.view,
                    i = e.openKeys,
                    a = e.collapsed,
                    r = e.openDrawer,
                    o = e.current,
                    c = e.height,
                    s = Object(d.c)((function(n) {
                        return n.ThemeSwitcher.sidebarTheme
                    }));
                var l = function(n) {
                        return {
                            sub3: ["sub2"]
                        }[n] || []
                    },
                    p = a && !r,
                    u = !0 === p ? "vertical" : "inline",
                    h = {
                        backgroundColor: m.a.PRIMARY_COLOR
                    },
                    f = {
                        backgroundColor: "rgba(0,0,0,0.3)",
                        color: s.textColor
                    },
                    g = {
                        color: "#fff"
                    };
                return Object(O.jsx)(R, {
                    children: Object(O.jsxs)(H, {
                        trigger: null,
                        collapsible: !0,
                        collapsed: p,
                        width: 160,
                        className: "isomorphicSidebar",
                        style: h,
                        children: [Object(O.jsx)(C, {
                            collapsed: p
                        }), Object(O.jsxs)(x.a, {
                            style: {
                                height: c - 70
                            },
                            children: [Object(O.jsx)(A, {
                                onClick: function(e) {
                                    n(K([e.key])), "MobileView" === t && setTimeout((function() {
                                        n(X())
                                    }), 100)
                                },
                                theme: "dark",
                                className: "isoDashboardMenu",
                                mode: u,
                                openKeys: p ? [] : i,
                                selectedKeys: o,
                                onOpenChange: function(e) {
                                    var t = e.find((function(n) {
                                            return !(i.indexOf(n) > -1)
                                        })),
                                        a = i.find((function(n) {
                                            return !(e.indexOf(n) > -1)
                                        })),
                                        r = [];
                                    t && (r = l(t).concat(t)), a && (r = l(a)), n(W(r))
                                },
                                children: b.a.map((function(n) {
                                    return Object(O.jsx)(J, {
                                        submenuStyle: f,
                                        submenuColor: g,
                                        singleOption: n
                                    }, n.key)
                                }))
                            }), Object(O.jsxs)(F, {
                                style: V.footer,
                                children: [Object(O.jsx)("div", {
                                    children: "info@casglobals.com"
                                }), Object(O.jsx)("div", {
                                    children: "Copyright \xa9 2021"
                                }), Object(O.jsx)("div", {
                                    children: "All Rights Reserved"
                                })]
                            })]
                        })]
                    })
                })
            }
            t(386);
            var q, _ = t(382),
                $ = D.d.div(q || (q = Object(I.a)(["\n  .isomorphicTopbar {\n    display: flex;\n    justify-content: space-between;\n    background-color: ", ";\n    padding: ", ";\n    z-index: 1000;\n    ", ";\n\n    @media only screen and (max-width: 767px) {\n      padding: ", ";\n    }\n\n    &.collapsed {\n      padding: ", ";\n      @media only screen and (max-width: 767px) {\n        padding: ", ";\n      }\n    }\n\n    .isoLeft {\n      display: flex;\n      align-items: center;\n\n      @media only screen and (max-width: 767px) {\n        margin: ", ";\n      }\n\n      .triggerBtn {\n        width: 24px;\n        height: 100%;\n        display: -webkit-inline-flex;\n        display: -ms-inline-flex;\n        display: inline-flex;\n        align-items: center;\n        justify-content: center;\n        background-color: transparent;\n        border: 0;\n        outline: 0;\n        position: relative;\n        cursor: pointer;\n\n        &:before {\n          content: '\f20e';\n          font-family: 'Ionicons';\n          font-size: 26px;\n          color: white;\n          line-height: 0;\n          position: absolute;\n        }\n      }\n    }\n\n    .isoCenter {\n      text-transform: uppercase;,\n      font-size: 22px;\n      color: ", ";\n    }\n\n    .addTiles {\n      background: ", ";\n      height: 35px;\n      border-radius: 12px;\n      border-color: ", ";\n    }\n\n    @media only screen and (max-width: 480px) {\n      .menuCollapsed {\n        top: -5px;\n      }\n\n      .addTiles {\n        width: 95px !important;\n        height: 30px !important;\n        font-size: 12px;\n      }\n\n      .isoRight img {\n        height: 14px !important;\n        width: 14px !important;\n        margin-top: -2px !important;\n      }\n    }\n\n    .isoRight {\n      display: flex;\n      align-items: center;\n\n      img {\n        height: 16px;\n        width: 18px;\n        margin-right: 5px;\n      }\n\n      li {\n        margin-left: ", ";\n        margin-right: ", ";\n        cursor: pointer;\n        line-height: normal;\n        position: relative;\n        display: inline-block;\n\n        @media only screen and (max-width: 360px) {\n          margin-left: ", ";\n          margin-right: ", ";\n        }\n\n        &:last-child {\n          margin: 0;\n        }\n\n        i {\n          font-size: 24px;\n          color: ", ";\n          line-height: 1;\n        }\n\n        .isoIconWrapper {\n          position: relative;\n          line-height: normal;\n\n          span {\n            font-size: 12px;\n            color: #fff;\n            background-color: ", ";\n            width: 20px;\n            height: 20px;\n            display: -webkit-inline-flex;\n            display: -ms-inline-flex;\n            display: inline-flex;\n            align-items: center;\n            justify-content: center;\n            text-align: center;\n            line-height: 20px;\n            position: absolute;\n            top: -8px;\n            left: ", ";\n            right: ", ";\n            ", ";\n          }\n        }\n      }\n    }\n  }\n\n  .isoUserDropdown {\n    .ant-popover-inner {\n      .ant-popover-inner-content {\n        .isoUserDropdownContent {\n          padding: 7px 0;\n          display: flex;\n          flex-direction: column;\n          position: absolute;\n          top: 0;\n          right: 0;\n          background-color: #ffffff;\n          width: 220px;\n          min-width: 160px;\n          flex-shrink: 0;\n          .isoBorderRadius(5px);\n          ", ";\n          ", ";\n          ", ";\n\n          .isoDropdownLink {\n            font-size: 13px;\n            color: ", ";\n            line-height: 1.1;\n            padding: 7px 15px;\n            background-color: transparent;\n            text-decoration: none;\n            display: flex;\n            justify-content: flex-start;\n            ", ";\n\n            &:hover {\n              background-color: ", ";\n            }\n          }\n        }\n      }\n    }\n  }\n\n  // Dropdown\n  .ant-popover {\n    .ant-popover-inner {\n      .ant-popover-inner-content {\n        .isoDropdownContent {\n          display: flex;\n          flex-direction: column;\n          position: absolute;\n          top: 0;\n          right: 0;\n          background-color: #ffffff;\n          width: 360px;\n          min-width: 160px;\n          flex-shrink: 0;\n          ", ";\n          ", ";\n          ", ";\n\n          @media only screen and (max-width: 767px) {\n            width: 310px;\n          }\n\n          .isoDropdownHeader {\n            border-bottom: 1px solid #f1f1f1;\n            margin-bottom: 0px;\n            padding: 15px 30px;\n            width: 100%;\n            display: flex;\n            align-items: center;\n            justify-content: center;\n\n            h3 {\n              font-size: 14px;\n              font-weight: 500;\n              color: ", ";\n              text-align: center;\n              text-transform: uppercase;\n              margin: 0;\n            }\n          }\n\n          .isoDropdownBody {\n            width: 100%;\n            height: 300px;\n            overflow-y: auto;\n            display: flex;\n            flex-direction: column;\n            margin-bottom: 10px;\n            background-color: ", ";\n\n            .isoDropdownListItem {\n              padding: 15px 30px;\n              flex-shrink: 0;\n              text-decoration: none;\n              display: flex;\n              flex-direction: column;\n              text-decoration: none;\n              width: 100%;\n              ", ";\n\n              &:hover {\n                background-color: ", ";\n              }\n\n              .isoListHead {\n                display: flex;\n                justify-content: space-between;\n                align-items: center;\n                margin-bottom: 5px;\n              }\n\n              h5 {\n                font-size: 13px;\n                font-weight: 500;\n                color: ", ";\n                margin-top: 0;\n              }\n\n              p {\n                font-size: 12px;\n                font-weight: 400;\n                color: ", ";\n                white-space: nowrap;\n                text-overflow: ellipsis;\n                overflow: hidden;\n              }\n\n              .isoDate {\n                font-size: 11px;\n                color: ", ";\n                flex-shrink: 0;\n              }\n            }\n          }\n\n          .isoViewAllBtn {\n            font-size: 13px;\n            font-weight: 500;\n            color: ", ";\n            padding: 10px 15px 20px;\n            display: flex;\n            text-decoration: none;\n            align-items: center;\n            justify-content: center;\n            text-align: center;\n            ", ";\n\n            &:hover {\n              color: ", ";\n            }\n          }\n\n          .isoDropdownFooterLinks {\n            display: flex;\n            align-items: center;\n            justify-content: space-between;\n            padding: 10px 30px 20px;\n\n            a {\n              font-size: 13px;\n              font-weight: 500;\n              color: ", ";\n              text-decoration: none;\n              padding: 10px 20px;\n              line-height: 1;\n              border: 1px solid ", ";\n              display: flex;\n              align-items: center;\n              justify-content: center;\n              ", ";\n\n              &:hover {\n                background-color: ", ";\n                border-color: ", ";\n                color: #ffffff;\n              }\n            }\n\n            h3 {\n              font-size: 14px;\n              font-weight: 500;\n              color: ", ";\n              line-height: 1.3;\n            }\n          }\n\n          &.withImg {\n            .isoDropdownListItem {\n              display: flex;\n              flex-direction: row;\n\n              .isoImgWrapper {\n                width: 35px;\n                height: 35px;\n                overflow: hidden;\n                margin-right: 15px;\n                display: -webkit-inline-flex;\n                display: -ms-inline-flex;\n                display: inline-flex;\n                align-items: center;\n                justify-content: center;\n                flex-shrink: 0;\n                background-color: ", ";\n                ", ";\n\n                img {\n                  width: 100%;\n                  height: 100%;\n                  object-fit: cover;\n                }\n              }\n\n              .isoListContent {\n                width: 100%;\n                display: flex;\n                flex-direction: column;\n\n                .isoListHead {\n                  display: flex;\n                  justify-content: space-between;\n                  align-items: center;\n                  margin-bottom: 10px;\n                }\n\n                h5 {\n                  margin-bottom: 0;\n                  padding-right: 15px;\n                }\n\n                .isoDate {\n                  font-size: 11px;\n                  color: ", ";\n                  flex-shrink: 0;\n                }\n\n                p {\n                  white-space: normal;\n                  line-height: 1.5;\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarMail {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 519px) {\n              right: -170px;\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarMessage {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 500px) {\n              right: -69px;\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarNotification {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 500px) {\n              right: -120px;\n            }\n          }\n        }\n      }\n    }\n  }\n"], ["\n  .isomorphicTopbar {\n    display: flex;\n    justify-content: space-between;\n    background-color: ", ";\n    padding: ", ";\n    z-index: 1000;\n    ", ";\n\n    @media only screen and (max-width: 767px) {\n      padding: ", ";\n    }\n\n    &.collapsed {\n      padding: ", ";\n      @media only screen and (max-width: 767px) {\n        padding: ", ";\n      }\n    }\n\n    .isoLeft {\n      display: flex;\n      align-items: center;\n\n      @media only screen and (max-width: 767px) {\n        margin: ", ";\n      }\n\n      .triggerBtn {\n        width: 24px;\n        height: 100%;\n        display: -webkit-inline-flex;\n        display: -ms-inline-flex;\n        display: inline-flex;\n        align-items: center;\n        justify-content: center;\n        background-color: transparent;\n        border: 0;\n        outline: 0;\n        position: relative;\n        cursor: pointer;\n\n        &:before {\n          content: '\\f20e';\n          font-family: 'Ionicons';\n          font-size: 26px;\n          color: white;\n          line-height: 0;\n          position: absolute;\n        }\n      }\n    }\n\n    .isoCenter {\n      text-transform: uppercase;,\n      font-size: 22px;\n      color: ", ";\n    }\n\n    .addTiles {\n      background: ", ";\n      height: 35px;\n      border-radius: 12px;\n      border-color: ", ";\n    }\n\n    @media only screen and (max-width: 480px) {\n      .menuCollapsed {\n        top: -5px;\n      }\n\n      .addTiles {\n        width: 95px !important;\n        height: 30px !important;\n        font-size: 12px;\n      }\n\n      .isoRight img {\n        height: 14px !important;\n        width: 14px !important;\n        margin-top: -2px !important;\n      }\n    }\n\n    .isoRight {\n      display: flex;\n      align-items: center;\n\n      img {\n        height: 16px;\n        width: 18px;\n        margin-right: 5px;\n      }\n\n      li {\n        margin-left: ", ";\n        margin-right: ", ";\n        cursor: pointer;\n        line-height: normal;\n        position: relative;\n        display: inline-block;\n\n        @media only screen and (max-width: 360px) {\n          margin-left: ", ";\n          margin-right: ", ";\n        }\n\n        &:last-child {\n          margin: 0;\n        }\n\n        i {\n          font-size: 24px;\n          color: ", ";\n          line-height: 1;\n        }\n\n        .isoIconWrapper {\n          position: relative;\n          line-height: normal;\n\n          span {\n            font-size: 12px;\n            color: #fff;\n            background-color: ", ";\n            width: 20px;\n            height: 20px;\n            display: -webkit-inline-flex;\n            display: -ms-inline-flex;\n            display: inline-flex;\n            align-items: center;\n            justify-content: center;\n            text-align: center;\n            line-height: 20px;\n            position: absolute;\n            top: -8px;\n            left: ", ";\n            right: ", ";\n            ", ";\n          }\n        }\n      }\n    }\n  }\n\n  .isoUserDropdown {\n    .ant-popover-inner {\n      .ant-popover-inner-content {\n        .isoUserDropdownContent {\n          padding: 7px 0;\n          display: flex;\n          flex-direction: column;\n          position: absolute;\n          top: 0;\n          right: 0;\n          background-color: #ffffff;\n          width: 220px;\n          min-width: 160px;\n          flex-shrink: 0;\n          .isoBorderRadius(5px);\n          ", ";\n          ", ";\n          ", ";\n\n          .isoDropdownLink {\n            font-size: 13px;\n            color: ", ";\n            line-height: 1.1;\n            padding: 7px 15px;\n            background-color: transparent;\n            text-decoration: none;\n            display: flex;\n            justify-content: flex-start;\n            ", ";\n\n            &:hover {\n              background-color: ", ";\n            }\n          }\n        }\n      }\n    }\n  }\n\n  // Dropdown\n  .ant-popover {\n    .ant-popover-inner {\n      .ant-popover-inner-content {\n        .isoDropdownContent {\n          display: flex;\n          flex-direction: column;\n          position: absolute;\n          top: 0;\n          right: 0;\n          background-color: #ffffff;\n          width: 360px;\n          min-width: 160px;\n          flex-shrink: 0;\n          ", ";\n          ", ";\n          ", ";\n\n          @media only screen and (max-width: 767px) {\n            width: 310px;\n          }\n\n          .isoDropdownHeader {\n            border-bottom: 1px solid #f1f1f1;\n            margin-bottom: 0px;\n            padding: 15px 30px;\n            width: 100%;\n            display: flex;\n            align-items: center;\n            justify-content: center;\n\n            h3 {\n              font-size: 14px;\n              font-weight: 500;\n              color: ", ";\n              text-align: center;\n              text-transform: uppercase;\n              margin: 0;\n            }\n          }\n\n          .isoDropdownBody {\n            width: 100%;\n            height: 300px;\n            overflow-y: auto;\n            display: flex;\n            flex-direction: column;\n            margin-bottom: 10px;\n            background-color: ", ";\n\n            .isoDropdownListItem {\n              padding: 15px 30px;\n              flex-shrink: 0;\n              text-decoration: none;\n              display: flex;\n              flex-direction: column;\n              text-decoration: none;\n              width: 100%;\n              ", ";\n\n              &:hover {\n                background-color: ", ";\n              }\n\n              .isoListHead {\n                display: flex;\n                justify-content: space-between;\n                align-items: center;\n                margin-bottom: 5px;\n              }\n\n              h5 {\n                font-size: 13px;\n                font-weight: 500;\n                color: ", ";\n                margin-top: 0;\n              }\n\n              p {\n                font-size: 12px;\n                font-weight: 400;\n                color: ", ";\n                white-space: nowrap;\n                text-overflow: ellipsis;\n                overflow: hidden;\n              }\n\n              .isoDate {\n                font-size: 11px;\n                color: ", ";\n                flex-shrink: 0;\n              }\n            }\n          }\n\n          .isoViewAllBtn {\n            font-size: 13px;\n            font-weight: 500;\n            color: ", ";\n            padding: 10px 15px 20px;\n            display: flex;\n            text-decoration: none;\n            align-items: center;\n            justify-content: center;\n            text-align: center;\n            ", ";\n\n            &:hover {\n              color: ", ";\n            }\n          }\n\n          .isoDropdownFooterLinks {\n            display: flex;\n            align-items: center;\n            justify-content: space-between;\n            padding: 10px 30px 20px;\n\n            a {\n              font-size: 13px;\n              font-weight: 500;\n              color: ", ";\n              text-decoration: none;\n              padding: 10px 20px;\n              line-height: 1;\n              border: 1px solid ", ";\n              display: flex;\n              align-items: center;\n              justify-content: center;\n              ", ";\n\n              &:hover {\n                background-color: ", ";\n                border-color: ", ";\n                color: #ffffff;\n              }\n            }\n\n            h3 {\n              font-size: 14px;\n              font-weight: 500;\n              color: ", ";\n              line-height: 1.3;\n            }\n          }\n\n          &.withImg {\n            .isoDropdownListItem {\n              display: flex;\n              flex-direction: row;\n\n              .isoImgWrapper {\n                width: 35px;\n                height: 35px;\n                overflow: hidden;\n                margin-right: 15px;\n                display: -webkit-inline-flex;\n                display: -ms-inline-flex;\n                display: inline-flex;\n                align-items: center;\n                justify-content: center;\n                flex-shrink: 0;\n                background-color: ", ";\n                ", ";\n\n                img {\n                  width: 100%;\n                  height: 100%;\n                  object-fit: cover;\n                }\n              }\n\n              .isoListContent {\n                width: 100%;\n                display: flex;\n                flex-direction: column;\n\n                .isoListHead {\n                  display: flex;\n                  justify-content: space-between;\n                  align-items: center;\n                  margin-bottom: 10px;\n                }\n\n                h5 {\n                  margin-bottom: 0;\n                  padding-right: 15px;\n                }\n\n                .isoDate {\n                  font-size: 11px;\n                  color: ", ";\n                  flex-shrink: 0;\n                }\n\n                p {\n                  white-space: normal;\n                  line-height: 1.5;\n                }\n              }\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarMail {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 519px) {\n              right: -170px;\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarMessage {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 500px) {\n              right: -69px;\n            }\n          }\n        }\n      }\n    }\n\n    &.topbarNotification {\n      .ant-popover-inner {\n        .ant-popover-inner-content {\n          .isoDropdownContent {\n            @media only screen and (max-width: 500px) {\n              right: -120px;\n            }\n          }\n        }\n      }\n    }\n  }\n"])), m.a.PRIMARY_COLOR, (function(n) {
                    return "rtl" === n["data-rtl"] ? "0 265px 0 31px" : "0 31px 0 265px"
                }), Object(B.c)(), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0px 260px 0px 15px !important" : "0px 15px 0px 260px !important"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0 109px 0 31px" : "0 31px 0 38px"
                }), (function(n) {
                    return n["data-rtl"], "0px 15px !important"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0 0 0 20px" : "0 20px 0 0"
                }), m.a.WHITE_COLOR, m.a.PRIMARY_COLOR, m.a.WHITE_COLOR, (function(n) {
                    return "rtl" === n["data-rtl"] ? "35px" : "0"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0" : "35px"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "25px" : "0"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "0" : "25px"
                }), Object(k.palette)("text", 0), Object(k.palette)("secondary", 1), (function(n) {
                    return "rtl" === n["data-rtl"] ? "inherit" : "10px"
                }), (function(n) {
                    return "rtl" === n["data-rtl"] ? "10px" : "inherit"
                }), Object(B.a)("50%"), Object(B.a)("5px"), Object(B.b)("0 2px 10px rgba(0,0,0,0.2)"), Object(B.c)(), Object(k.palette)("text", 1), Object(B.c)(), Object(k.palette)("secondary", 6), Object(B.a)("5px"), Object(B.b)("0 2px 10px rgba(0,0,0,0.2)"), Object(B.c)(), Object(k.palette)("text", 0), Object(k.palette)("grayscale", 6), Object(B.c)(), Object(k.palette)("grayscale", 3), Object(k.palette)("text", 0), Object(k.palette)("text", 2), Object(k.palette)("grayscale", 1), Object(k.palette)("text", 2), Object(B.c)(), Object(k.palette)("primary", 0), Object(k.palette)("text", 0), Object(k.palette)("border", 1), Object(B.c)(), Object(k.palette)("primary", 0), Object(k.palette)("primary", 0), Object(k.palette)("text", 0), Object(k.palette)("grayscale", 9), Object(B.a)("50%"), Object(k.palette)("grayscale", 1)),
                nn = Object(N.a)($),
                en = t(156),
                tn = o.a.Header,
                an = g.a.toggleCollapsed;

            function rn(n) {
                var e = Object(d.c)((function(n) {
                        return n.ThemeSwitcher.topbarTheme
                    })),
                    t = Object(d.c)((function(n) {
                        return n.App
                    })),
                    i = t.collapsed,
                    a = t.openDrawer,
                    r = Object(d.c)((function(n) {
                        return n.App
                    })).currentModule,
                    o = Object(h.a)("currentModule"),
                    c = o || r,
                    s = Object(d.b)(),
                    p = l.a.useCallback((function() {
                        return s(an())
                    }), [s]),
                    u = i && !a,
                    m = "sidebar.report.realtime" === c || "sidebar.report.historical" === c,
                    f = {
                        background: e.backgroundColor,
                        position: "fixed",
                        width: "100%",
                        height: 70
                    };
                return Object(O.jsx)(nn, {
                    children: Object(O.jsxs)(tn, {
                        style: f,
                        className: u ? "isomorphicTopbar collapsed" : "isomorphicTopbar",
                        children: [Object(O.jsx)("div", {
                            className: "isoLeft",
                            style: u ? {
                                position: "unset",
                                right: "0"
                            } : {
                                position: "relative",
                                right: "90px"
                            },
                            children: Object(O.jsx)("button", {
                                className: u ? "triggerBtn menuCollapsed" : "triggerBtn menuOpen",
                                style: {
                                    color: e.textColor
                                },
                                onClick: p
                            })
                        }), Object(O.jsx)("div", {
                            className: "isoCenter",
                            children: Object(O.jsx)(Q.a, {
                                id: c
                            })
                        }), "sidebar.dashboard" === c || m ? Object(O.jsx)("div", {
                            className: "isoRight",
                            onClick: n.showDrawer,
                            children: Object(O.jsxs)(_.a, {
                                type: "primary",
                                className: "addTiles",
                                style: {
                                    width: m ? 136 : 120
                                },
                                onClick: n.showDrawer,
                                children: [Object(O.jsx)("img", {
                                    alt: "logo",
                                    src: en.a
                                }), " ", m ? Object(O.jsx)(Q.a, {
                                    id: "topbar.selectDevice"
                                }) : Object(O.jsx)(Q.a, {
                                    id: "topbar.addTiles"
                                })]
                            })
                        }) : Object(O.jsx)("div", {
                            className: "isoRight"
                        })]
                    })
                })
            }
            var on = t(134),
                cn = [{
                    path: "",
                    component: Object(s.lazy)((function() {
                        return Promise.all([t.e(0), t.e(1), t.e(3), t.e(5), t.e(10)]).then(t.bind(null, 867))
                    })),
                    exact: !0
                }, {
                    path: "report",
                    component: Object(s.lazy)((function() {
                        return Promise.all([t.e(0), t.e(3), t.e(6), t.e(12), t.e(15)]).then(t.bind(null, 872))
                    }))
                }, {
                    path: "profile",
                    component: Object(s.lazy)((function() {
                        return t.e(19).then(t.bind(null, 871))
                    }))
                }, {
                    path: "settings",
                    component: Object(s.lazy)((function() {
                        return Promise.all([t.e(6), t.e(5), t.e(16)]).then(t.bind(null, 876))
                    }))
                }];

            function sn(n) {
                var e = Object(z.i)().url;
                return Object(O.jsx)(s.Suspense, {
                    fallback: Object(O.jsx)(on.a, {}),
                    children: Object(O.jsx)(z.d, {
                        children: cn.map((function(t, i) {
                            return Object(O.jsx)(z.b, {
                                exact: t.exact,
                                path: "".concat(e, "/").concat(t.path),
                                children: Object(p.isEmpty)(t.path) ? Object(O.jsx)(t.component, {
                                    editWidgetDetails: n.editWidgetDetails
                                }) : Object(O.jsx)(t.component, {})
                            }, i)
                        }))
                    })
                })
            }
            t(640);
            var ln, dn, pn, un = t(822),
                mn = (t(642), t(649)),
                hn = (t(527), t(602)),
                fn = (t(528), t(505)),
                gn = (t(529), t(510)),
                bn = (t(647), t(821)),
                xn = t(190),
                An = t.n(xn),
                jn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAABHNCSVQICAgIfAhkiAAABFxJREFUeF7tXEtLG1EUvuMjJj6S2BStz1VLS2KMRSEIxj5od2rbTbu0hVoolEJ/gki76KLQRaUtXQhCoQvrwq66qlEkbiRqIrQqiOAzJpnmQTRG7XdDDCZ1MkbIY5I7MMxi7uSc+93zyp3zDUdEjsXFRa3f79cXFxffwFDjwcHB5aOjI6XYc5m8z3Gcp7CwcAl6WnCOQ2d7S0uLPZFOXKKbc3Nzw/hBI0C4WFJSUimTyQg9CwoKMjlPUdmHh4ckGAyGz729Pff+/r4jFApZDAZDr9DDpwIxOzt7Gw/8qKysDFRUVFwQlSyBAV6v18XzvAyqdjU3N4/Hq/wfELCC/qKiomfV1dWXsn3lk8WfWsrW1tYmroNNTU0DJ5+PAcJut7+HC/RqNBpVskKkNN7pdPJwmyGtVvvqWO8oELCEO7CEbzU1NTnhCmILs7m56ULseAA3MdOxJ4EI1NXVyXPNHYQAQSYh6+vrfgBRHgUC1vAVgfF+eXm5QgzJXLrv8/m8brf7O8B4zCEu6JBrR2ENV3JpkmedC6xiCam1i7NarY8UCsXHqqoq9VkfzqVxDoeDDwQCfdzCwsLnsrKyPqUyq4vFlGHv8XgIXOQTZ7PZrEiXBrlcnjJh2fzDu7u7xOVyWTkEyr+ID8p8yRbxi0KLrLW1NQ+HcvqosbExmxct5bqtrq6StAOBep8YjUayvb0tOMH6+noyPT1NSktLUw4CFZARIJaXl0lbW5voBOfn5wkFJB0HAyKCMgOCARHrcMwimEVI3CImJycJNmYFE0lDQwM5T00kKdeg6bSzszNhNlWpVGRlZSXpjCspIKg1dHd3i04S+wuiY+IHMCCkGCyZRURWjQHBgIj908UsglmEhCzCZDIR7HkK5m1a4JjN5phq77z7EVntGngpJFq8jI2NkY6Ojug4BkQECgYEA4K5RtgGWIyIuAIDggERm1GZRTCLYBZxapXJXIO5BnMN5hqJ/oKyGMFiBIsR+RsjxLbqaO/mxMREzFYdeBOktbWVoNFTMLTQliGLxULQ/xkdQ1/LUXm0L1LoAPUgLC/ZIyOv/JJVMh3jGRARlMNAsIbTSMMptuNn0IJ8PZ9bkHd2dmYoTWEQPI3n+dyUjgD+gUMnykNQGL+AplCRjsCUbTLQAexFY/oTjhJcQfQaqa2tvZZtSqZDHzSk/wGtqSfcmYWAOaRWq++B45lX5BW4BI9WoxEQY5+eJLf5QVcozRe6QoTc5gOfKxwSokAge9wC3XEUxNec5nweu9vGxgYPLluPTqcLl6IxTYsAox9p9CXSaU67CNKlEwHynV6vf3MMzGmU6AFYxgtKdgNBPh3xKm0yqDsgS3hwfQsQXp8ULESSN6HD9Sde+8tRY6RN0VQKQmAMgjQTwrzuAoSpeFkJP5uAGmMYwbMdFqKBy6gl+tkEHtRnJ6xgkhJdhcBOCAR9CJyvq7jo4SY3EVzacUriQxpY+SWcUwDgF/S3IUX+TmRx/wBuEkKZcKpaFwAAAABJRU5ErkJggg==",
                yn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAABHNCSVQICAgIfAhkiAAABPxJREFUeF5jZCAA2Np4tNh+s+lK8ojaMzEymr///knlx+8ffIT0DaQ8ByvHJ0FOvjt///078eLLm4O//v66+qv5y1V8bmLEJyncJLZYVkDaXFFIXsRcxlhQS1yDQUdcnUGMW3Qg/UnQ7ldfXzNceXmT4drLGwwnnpx+/+Dd49ePPzw98bbuVTwuzVgDgr9B2ImFiXlLi1vV91jDcCGCNg8BBYvOrXhXu6eD7e//vz4f6t4cRHcyRkBItEg1ygvKpW2IXSzBy84zBLxIvBM/fP/IELAk7sWTz8+nvah83IysEyUg9CcZTjSXNorv82nhJ974oacyf0vVh/PPLy84l3OmEOZ6eECIN0u5qIoqrdyRuHpYZAVC0eM6L+jd9Zd3Aj80vD4EUgsPCJEm8e+XC45wDLfsgCtA3gOzidEUh6+va16A8z84ICTbZJY1ulQEROkHcxIKyeEkv/Dsis8N+zrXAQMjgZGtiUdbS0B1/ZH0barDyZPE+sVyuuedW2/u+jDyNAqFuynZzVgUNl2AWM3DSV30qowPe+8eSGXU7NOeFW8UkZpjmTKc/Ee0XyYdm8Ww7OLqmYxafdoXOjwb9J2UbIjWPJwU7rt3mKFiR+MFRrFWqY8nMnfwDfZmM60CH9Qct5ju8YmRr0Ho/7NKvP0RWrlh0Jgr1a7NQPeAeP/jI4P9LF+GN1/f4gwIaX4JhoOpmxm4WLnoElgDEhD33j9gsJnhTdCDp7P3MEjzSRJURw0FowEBDcXRgBgNCNQMNZoiRlPEEE8Rxx6eAvaXcQ+zyvBJMcgJSJNckQyprHHl5XUGt3kheD3Jz8HLcL3wxPAOCFBqCFmWSNCT5LSSh1SKGA0IaBoYDYjRgEDta4ymiNEUMZoiwGkAvRs+qLOGy9wghmuvbuKs20ENnJ2Ja1Fae+SORwzqgAA1RAiBNVHzGazkzeDKRgMCGhSjATEaEKNZA5wGRssIaFYYDYjRgECtUEdTxGiKGE0RWBuZo1ljNGuMZo3RrIGvBzpaRoyWEaNlxMgtIwgN1fEBtznsSlqHMlT3+dcXBqsZngxvv77DWbRI8QHXUKVtZuBGWkP16MNT4NxnEMOnn19w6tMSU2fYk7yO0KAZhvyATPmR7Eo6aBgNCGgggwNidMEpdMGpRq/muS6vJsORvAS5bFv9OUbVXvVpycYxmSN5UfqCc8unMvI0CIW5qdjPWRQ6jZcO5dKgsyJmZcbnPfcPJDKyNQhpaQjKrz2WsU1j0LmSDg4yn+Z+686Xm37glVlCzWILWlyr/OMMw0fU5pV5Z5Z9aNjXtfZd7csU+BI10WaJr+dzD3AJcA7rnY7wNPb22zsG06kuX17VPAcXCfCAkGiVdlQVVlq/PXHViAgJ5zkBHx59fOb3vOrxYZSAAHEUulQanZVt8ib6tA/rLJK9qeztnrsH+p5WPG6DJRGM1ZuKXcrNknwSOSsj5goIDrNsAsoO4ctTPj39+KzrceXDVuSyGOsyVt4mQVsuJs5dza6VHFEG+Bd50qFgp4oVi86v+NW8r/fPl1/fXD83vDuGbijeYxMk22UWi3OJWsoLygqbyxoL6EhoAo9N0BgixybcYLjy4jrDiUdnPzz48Pjtq8+vjryoeZqAK1TxBgRIE3sNrzorC5OuFK+kAxMDo+W7Hx+HxEEaQhz8d/4w/D324svLA7+Zflz5WfsT99IeoD8Br4vnLmso5ggAAAAASUVORK5CYII=",
                vn = [{
                    id: 1,
                    value: "bar_chart",
                    label: "barChart",
                    icon: jn,
                    selectedIcon: yn
                }, {
                    id: 2,
                    value: "hourly_bar_chart",
                    label: "hourlybarChart",
                    icon: jn,
                    selectedIcon: yn
                }, {
                    id: 3,
                    value: "gauge_chart",
                    label: "gaugeChart",
                    icon: "data:image/png;base64,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",
                    selectedIcon: "data:image/png;base64,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"
                }, {
                    id: 4,
                    value: "line_chart",
                    label: "lineChart",
                    icon: "data:image/png;base64,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",
                    selectedIcon: "data:image/png;base64,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"
                }, {
                    id: 5,
                    value: "metric_chart",
                    label: "metricChart",
                    icon: "data:image/png;base64,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",
                    selectedIcon: "data:image/png;base64,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"
                }, {
                    id: 6,
                    value: "cal_chart",
                    label: "calChart",
                    icon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEIAAABCCAYAAADjVADoAAAABHNCSVQICAgIfAhkiAAABXlJREFUeF7tXEtMY1UYPpdnkVcZRpBnWGg0QCm4mQxBfEQ3QBjdaMpmNHFMIMbEBYygLiYTQZnExIVtMS4mmaSNi3GiGVi4cpCQWTHlURKFBe9XaSktTXnj93fapoU+b2nae9uT3LRwz+M/3/n+/z899/8vx4KUubm5apvNJktPT38TVa+dnJy8fHZ2lhesXSzvcxxnSU1NnYecT3E9gcz6+vp6fSCZuEA3p6amHqDDawDhamZmZkFGRgajKyUlJZbzDDr26ekpOzw8dFwHBwc7R0dHhuPj46dyufymv8Y+gZicnHwHDR4XFBTYc3NzrwQdWQAVrFaryWw2Z0DUtrq6uifnRb4ABFhwJy0t7bPi4uKX4n3lw8WfmLK5ubmBT2Vtbe1dz/ZeQOj1+p+gAjcLCwvzwx1ESPWNRqMZanO/urr6S5fcbiDAhHfBhN9KSkpEoQrBFmZjY8ME2/EB1GSU6noCYS8rK5OITR38AQJPwtbW1mwAIscNBNiggWF8PycnJysYkmK6v7e3Z93Z2fkdYHzMwS7UwNc+AhteEdMkQ50LWDEP19rG6XS6j7KystRFRUXSUBuLqZ7BYDDb7fZb3Ozs7C/Z2dm38vLierMYNewtFguDigxxMzMzOrhLuUQiidpg8dzx/v4+M5lMOg6Gchf2IS9RvMX5RaFN1urqqoXDdvqssrIynhct6rItLS2xiIAYUg8xtWqILS8vR11YfwNUVFQwRYeC9dzu5i1DRECoVWr2zdff8h78sht293Sz21/18Oo2IiAa5K87mIBfp0wmk/ES4DIaTU9PM/yyZMSMZ5MTvLqMCIirV150DNrY2Mj+fPwHLwEuo1F72w02Pj7u6GrbZODVZRIIJ2xJIJJAeGtQ1Bmxu7vL+vr62PDwMKPvfEp+fj5rbW1l/f39jL6fL4KwEV1dXUyr1fKZ/4U2CoWCKZVKYQKBM45LAYE6ITYsLCwIHwha0YGBAZ/09oUWqVJvb68Xo3CIInwgaDV96XggyhAYVVVV7iqiAMLXJELRG0/1SlggRMmIjo4Ovy7Qn40g16vRaMSlGqGoQbA6olCNYJMM5b6ogKAdIhWifbhFNEAQCGQrqJDunwejpaXFfbZB5wwjIyNeWIkGCNpQ0caKCm29acPkWUZHR72AaG5uFicQNKukaoRrEHzUF41qRIqFaIDAMxQ2NjbmwKOpqYkhpikxbQTZh87OTsfkVSrVBa9BBzCuE3HyGnSg41lEw4hgQCSM+0yqRqRW0tleNKoRCR4Uv7G4uHihi+ThrRMSQQDhOnck60+RKXwKMYG8ib/zTkEAwWfi4bZJAiEk1Qh3dfnUjzkj6usa2MrKCiMdjnV8BNmf8vJyppt6xgdLFtGzzx++H2T3Bu/xGjgajWIWMUOTITC0Gq2DGbEqxASKoeIbNkRyR8SIWE08GuMmgXCi6gAiGXDqDDhFCPIEQpAbEjkEeXt7e4LSFJTI0+hM5KB0hCf+zOEk6EOkMP6KNIXcaBiieO9za2vLisD0TzhKcEWi18PS0tLX4l3oaMiHgPT/kNbU7sjpgsG8L5VKbyCKNqGSV6ASZhz2PMQh8qeeyW02pCu8kCjpCs7ktj3kczlMghsIeI+3ke74CImvos75dKnX+vq6Gbls7TU1Nf94AUF/AIw7cKNfwJ2KWkXgLo0wkD/ix+LzR/OejHD9A/biLpjxOSW7IUE+GvYpZn2SOsBLWPA5CBC+8xTEX5L8G3j1wF8I5JJgjxEzwS9zYBjGQyTJH2Ne7wGE5+H8HiXgaxOwx3gA43kdDCmEykgF+toEM1KfjWDBGCW6+gM3IBDUCA9lXsWHDGryFozLdVyCeJEGVn4e1zgA+JvMH1zkv4EY9j+4RYSZYnWhMgAAAABJRU5ErkJggg==",
                    selectedIcon: "data:image/png;base64,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"
                }],
                On = t(33),
                wn = t(45),
                En = D.d.div(ln || (ln = Object(I.a)(["\n  .chartOptions {\n    padding: 0 25px;\n    margin-top: 5%;\n    display: flex;\n    flex-wrap: nowrap;\n    gap: 5px;\n    justify-content: flex-start;\n  }\n\n  .ant-radio-button-wrapper {\n    display: flex !important;\n    flex-direction: column;\n    align-items: center;\n    justify-content: center;\n    width: 130px;\n    height: 130px;\n    padding: 10px;\n    border-radius: 0px;\n    background-color: #f5f5f5;\n  }\n\n  .ant-radio-button-wrapper img {\n    width: 100%;\n    height: 50px;\n    object-fit: contain;\n    margin-bottom: 6px;\n  }\n\n  .chartOptionsTitle {\n    color: black;\n    text-align: center;\n    font-size: 14px;\n    font-weight: 500;\n    line-height: 1.5;\n  }\n\n  .deviceParameterList {\n    float: right;\n    width: 50%;\n  }\n\n  .updateButton {\n    height: 35px;\n    width: 150px;\n    border-radius: 12px;\n  }\n\n  .deviceParamAccordianContainer {\n    margin: 0 7%;\n    max-height: 650px;\n    overflow-y: auto;\n  }\n\n  .deviceParamCheckbox {\n    width: 45%;\n    margin-left: 8px;\n  }\n\n  .headings {\n    margin-bottom: 10px;\n    background: green;\n    color: white;\n    padding: 5px 10px;\n    border-radius: 5px;\n  }\n"]))),
                Cn = Object(N.a)(En),
                In = f.a.getUserDevices,
                Dn = On.a.getUserParameters,
                kn = On.a.getReportsSettings,
                Bn = bn.a.Panel;

            function Nn(n) {
                var e = n.visible,
                    t = n.onClose,
                    i = n.addWidget,
                    o = n.editWidgetSettings,
                    c = n.widgetDetailsSettings,
                    l = Object(u.a)().width,
                    f = Object(d.c)((function(n) {
                        return n.AddWidgets
                    })),
                    g = f.deviceList,
                    b = f.isLoading,
                    x = Object(d.c)((function(n) {
                        return n.Reports
                    })).userParameterList,
                    A = Object(d.b)(),
                    j = Object(h.a)("reportsSettingsStorage"),
                    v = Object(s.useState)(""),
                    w = Object(r.a)(v, 2),
                    E = w[0],
                    C = w[1],
                    I = Object(s.useState)(""),
                    D = Object(r.a)(I, 2),
                    k = D[0],
                    B = D[1],
                    N = Object(s.useState)([]),
                    M = Object(r.a)(N, 2),
                    R = M[0],
                    L = M[1],
                    S = Object(s.useState)([]),
                    z = Object(r.a)(S, 2),
                    U = z[0],
                    Y = z[1],
                    P = Object(s.useState)(""),
                    G = Object(r.a)(P, 2),
                    T = G[0],
                    J = G[1],
                    H = Object(s.useState)([]),
                    F = Object(r.a)(H, 2),
                    V = F[0],
                    W = F[1],
                    K = Object(s.useState)([]),
                    X = Object(r.a)(K, 2),
                    Z = X[0],
                    q = X[1],
                    $ = Object(s.useState)([]),
                    nn = Object(r.a)($, 2),
                    en = nn[0],
                    tn = nn[1],
                    an = Object(s.useState)([]),
                    rn = Object(r.a)(an, 2),
                    cn = rn[0],
                    sn = rn[1],
                    ln = Object(s.useState)({}),
                    dn = Object(r.a)(ln, 2),
                    pn = dn[0],
                    xn = dn[1],
                    jn = Object(s.useState)({}),
                    yn = Object(r.a)(jn, 2),
                    On = yn[0],
                    En = yn[1],
                    Nn = Object(s.useState)([]),
                    Mn = Object(r.a)(Nn, 2),
                    Rn = Mn[0],
                    Ln = Mn[1],
                    Sn = Object(s.useState)([]),
                    zn = Object(r.a)(Sn, 2),
                    Qn = zn[0],
                    Un = zn[1],
                    Yn = Object(s.useState)([]),
                    Pn = Object(r.a)(Yn, 2),
                    Gn = Pn[0],
                    Tn = Pn[1],
                    Jn = Object(s.useState)([]),
                    Hn = Object(r.a)(Jn, 2),
                    Fn = Hn[0],
                    Vn = Hn[1],
                    Wn = Object(s.useState)(!1),
                    Kn = Object(r.a)(Wn, 2),
                    Xn = Kn[0],
                    Zn = Kn[1],
                    qn = Object(s.useState)(!1),
                    _n = Object(r.a)(qn, 2),
                    $n = _n[0],
                    ne = _n[1],
                    ee = Object(s.useState)(!1),
                    te = Object(r.a)(ee, 2),
                    ie = te[0],
                    ae = te[1],
                    re = Object(d.c)((function(n) {
                        return n.App
                    })).currentModule,
                    oe = Object(h.a)("currentModule"),
                    ce = oe || re,
                    se = Object(h.a)("currentUser"),
                    le = Object(h.b)("accessToken"),
                    de = "gauge_chart" === E,
                    pe = "metric_chart" === E,
                    ue = "cal_chart" === E,
                    me = "" === k,
                    he = "sidebar.report.realtime" === ce || "sidebar.report.historical" === ce,
                    fe = (me || "" === T) && (me || Object(p.isEmpty)(V)) && (Object(p.isEmpty)(Z) || Object(p.isEmpty)(V)),
                    ge = Object(p.isEmpty)(en) || Object(p.isEmpty)(cn),
                    be = fe && Object(p.isEmpty)(U),
                    xe = he ? ge : be,
                    Ae = xe ? m.a.DISABLE_COLOR : m.a.PRIMARY_COLOR,
                    je = function() {
                        B(""), J(""), W([]), xn({}), En([]), Ln([]), q([]), Vn([]), Y([]), Zn(!1)
                    };
                Object(s.useEffect)((function() {
                    if (je(), !Object(p.isEmpty)(o)) {
                        var n = Object(p.first)(o).chartName;
                        ye(n)
                    }
                }), [o]), Object(s.useEffect)((function() {
                    if (!Object(p.isEmpty)(o)) {
                        var n, e = Object(p.first)(o),
                            t = e.chartName,
                            i = e.selectedDeviceAndParameterList,
                            a = e.parameter,
                            r = e.device,
                            c = !Object(p.isEmpty)(R) && R.filter((function(n) {
                                return n.deviceEntity.iotDeviceMapId === r.iotDeviceMapId
                            }));
                        if (Vn(null === (n = Object(p.first)(c)) || void 0 === n ? void 0 : n.parameterEntityList), C(t), Y(i), En(a), "metric_chart" === t || "cal_chart" === t) {
                            var s = [];
                            a.map((function(n) {
                                s.push(n.parameterId)
                            })), W(s), Ln(a)
                        }
                        xn(r), B(r.iotDeviceMapId), J(a.parameterId), Zn(!0)
                    }
                }), [R, o]), Object(s.useEffect)((function() {
                    if (je(), he && j) {
                        var n = j.device,
                            e = j.parameter,
                            t = j.selectedReportUserDeviceList,
                            i = j.selectedReportUserParameterList,
                            a = j.allReportDeviceSelected,
                            r = j.allReportParametersSelected;
                        tn(t), sn(i), Un(n), Tn(e), ne(a), ae(r)
                    }
                }), [oe]), Object(s.useEffect)((function() {
                    if (e) {
                        var n = "".concat(y.a, "user/").concat(null === se || void 0 === se ? void 0 : se.userEmailId, "/device");
                        A(In(n, {
                            headers: {
                                Authorization: "Bearer " + le,
                                "Content-Type": "application/json"
                            }
                        }))
                    }
                }), [le, null === se || void 0 === se ? void 0 : se.userEmailId, A, e]), Object(s.useEffect)((function() {
                    if (e) {
                        var n = "".concat(y.a, "user/").concat(null === se || void 0 === se ? void 0 : se.userEmailId, "/parameter");
                        A(Dn(n, {
                            headers: {
                                Authorization: "Bearer " + le,
                                "Content-Type": "application/json"
                            }
                        }))
                    }
                }), [A, e, he, null === se || void 0 === se ? void 0 : se.userEmailId, le]);
                var ye = function(n) {
                        var e = {
                                headers: {
                                    Authorization: "Bearer " + le,
                                    "Content-Type": "application/json"
                                }
                            },
                            t = {
                                userEmailId: null === se || void 0 === se ? void 0 : se.userEmailId,
                                type: n.replace(/_/g, "-")
                            },
                            i = "".concat(y.a, "user/device-parameter");
                        An.a.post(i, t, e).then((function(n) {
                            if (n.data) {
                                var e = n.data.deviceParameter;
                                L(e)
                            }
                        }))
                    },
                    ve = function(n) {
                        return V.includes(n.parameterId)
                    },
                    Oe = function() {
                        return pe || ue
                    },
                    we = function(n, e) {
                        return Object(O.jsx)(_.a, {
                            type: "primary",
                            className: "updateButton",
                            style: {
                                background: Ae,
                                borderColor: xe ? "#d9d9d9" : Ae,
                                color: xe ? "rgba(0,0,0,.25)" : "white"
                            },
                            disabled: xe,
                            onClick: n,
                            children: Object(O.jsx)(Q.a, {
                                id: e
                            })
                        })
                    },
                    Ee = function() {
                        if ("gauge_chart" === E || "metric_chart" === E || "cal_chart" === E) return Object(O.jsx)(fn.a.Group, {
                            value: k,
                            children: Object(O.jsx)(hn.b, {
                                direction: "vertical",
                                children: R.map((function(n, e) {
                                    var t = n.deviceEntity;
                                    return Object(O.jsx)(fn.a, {
                                        value: t.iotDeviceMapId,
                                        onChange: function(n) {
                                            var e;
                                            J(""), W([]), B(n.target.value), xn(t);
                                            var i = !Object(p.isEmpty)(R) && R.filter((function(e) {
                                                return e.deviceEntity.iotDeviceMapId === n.target.value
                                            }));
                                            Vn(null === (e = Object(p.first)(i)) || void 0 === e ? void 0 : e.parameterEntityList)
                                        },
                                        children: t.deviceDisplayName
                                    }, e)
                                }))
                            })
                        })
                    },
                    Ce = function() {
                        return Object(O.jsxs)(O.Fragment, {
                            children: [Object(O.jsx)("div", {
                                className: "userDeviceList",
                                children: Ee()
                            }), Object(O.jsx)("div", {
                                className: "deviceParameterList",
                                children: !Object(p.isEmpty)(E) && Fn && Fn.sort(Object(wn.b)("parameterDisplayName")).map((function(n, e) {
                                    return Object(O.jsxs)("div", {
                                        children: [Object(O.jsx)(gn.a, {
                                            checked: ve(n),
                                            disabled: !Oe() && Object(p.isEmpty)(Z) || Oe() && me || "cal_chart" === E && !ve(n) && 5 === V.length || "metric_chart" === E && !ve(n) && 8 === V.length,
                                            onChange: function(e) {
                                                if (e.target.checked) {
                                                    W((function(e) {
                                                        return [].concat(Object(a.a)(e), [n.parameterId])
                                                    }));
                                                    var t = [].concat(Object(a.a)(Rn), [n]);
                                                    Ln(t)
                                                } else {
                                                    var i = V.indexOf(n.parameterId);
                                                    W(V.filter((function(n, e) {
                                                        return e !== i
                                                    })));
                                                    var r = Rn.filter((function(e) {
                                                        return e.parameterId !== (null === n || void 0 === n ? void 0 : n.parameterId)
                                                    }));
                                                    Ln(r)
                                                }
                                            },
                                            children: n.parameterDisplayName
                                        }, e), Object(O.jsx)("br", {})]
                                    }, e)
                                }))
                            })]
                        })
                    },
                    Ie = function() {
                        switch (E) {
                            case "bar_chart":
                                return Object(O.jsx)("div", {
                                    className: "deviceParamAccordianContainer",
                                    children: R.map((function(n, e) {
                                        return Object(O.jsx)(bn.a, {
                                            accordion: !0,
                                            style: {
                                                marginBottom: "2%"
                                            },
                                            defaultActiveKey: Xn && n.deviceEntity.deviceDisplayName,
                                            children: Object(O.jsx)(Bn, {
                                                header: n.deviceEntity.deviceDisplayName,
                                                children: Array.isArray(n.parameterEntityList) ? n.parameterEntityList.sort(Object(wn.b)("parameterDisplayName")).map((function(e, t) {
                                                    return Object(O.jsx)(gn.a, {
                                                        checked: !Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedDevice === n.deviceEntity.iotDeviceMapId && t.selectedParameter === e.parameterId
                                                        }))),
                                                        className: "deviceParamCheckbox",
                                                        disabled: 1 === U.length && Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedParameter === e.parameterId && t.selectedDevice === n.deviceEntity.iotDeviceMapId
                                                        }))),
                                                        onChange: function(t) {
                                                            if (t.target.checked) {
                                                                var i = {
                                                                    selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                    selectedParameter: e.parameterId
                                                                };
                                                                Y((function(n) {
                                                                    return [].concat(Object(a.a)(n), [i])
                                                                }))
                                                            } else {
                                                                var r = [{
                                                                        selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                        selectedParameter: e.parameterId
                                                                    }],
                                                                    o = U.filter((function(n) {
                                                                        return !r.find((function(e) {
                                                                            return e.selectedDevice === n.selectedDevice && n.selectedParameter === e.selectedParameter
                                                                        }))
                                                                    }));
                                                                Y(o)
                                                            }
                                                        },
                                                        children: e.parameterDisplayName
                                                    }, t)
                                                })) : Object(O.jsx)("div", {
                                                    style: {
                                                        padding: "1rem",
                                                        color: "gray"
                                                    },
                                                    children: "No parameters available."
                                                })
                                            }, Xn ? n.deviceEntity.deviceDisplayName : e)
                                        }, n.deviceEntity.deviceDisplayName)
                                    }))
                                });
                            case "hourly_bar_chart":
                                return Object(O.jsx)("div", {
                                    className: "deviceParamAccordianContainer",
                                    children: R.map((function(n, e) {
                                        return Object(O.jsx)(bn.a, {
                                            accordion: !0,
                                            style: {
                                                marginBottom: "2%"
                                            },
                                            defaultActiveKey: Xn && n.deviceEntity.deviceDisplayName,
                                            children: Object(O.jsx)(Bn, {
                                                header: n.deviceEntity.deviceDisplayName,
                                                children: Array.isArray(n.parameterEntityList) ? n.parameterEntityList.sort(Object(wn.b)("parameterDisplayName")).map((function(e, t) {
                                                    return Object(O.jsx)(gn.a, {
                                                        checked: !Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedDevice === n.deviceEntity.iotDeviceMapId && t.selectedParameter === e.parameterId
                                                        }))),
                                                        className: "deviceParamCheckbox",
                                                        disabled: 1 === U.length && Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedParameter === e.parameterId && t.selectedDevice === n.deviceEntity.iotDeviceMapId
                                                        }))),
                                                        onChange: function(t) {
                                                            if (t.target.checked) {
                                                                var i = {
                                                                    selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                    selectedParameter: e.parameterId
                                                                };
                                                                Y((function(n) {
                                                                    return [].concat(Object(a.a)(n), [i])
                                                                }))
                                                            } else {
                                                                var r = [{
                                                                        selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                        selectedParameter: e.parameterId
                                                                    }],
                                                                    o = U.filter((function(n) {
                                                                        return !r.find((function(e) {
                                                                            return e.selectedDevice === n.selectedDevice && n.selectedParameter === e.selectedParameter
                                                                        }))
                                                                    }));
                                                                Y(o)
                                                            }
                                                        },
                                                        children: e.parameterDisplayName
                                                    }, t)
                                                })) : Object(O.jsx)("div", {
                                                    style: {
                                                        padding: "1rem",
                                                        color: "gray"
                                                    },
                                                    children: "No parameters available."
                                                })
                                            }, Xn ? n.deviceEntity.deviceDisplayName : e)
                                        }, n.deviceEntity.deviceDisplayName)
                                    }))
                                });
                            case "gauge_chart":
                                return Object(O.jsxs)(O.Fragment, {
                                    children: [Object(O.jsx)("div", {
                                        className: "userDeviceList",
                                        children: Ee()
                                    }), Object(O.jsx)("div", {
                                        className: "deviceParameterList",
                                        children: de && Fn && Object(O.jsx)(fn.a.Group, {
                                            value: T,
                                            children: Object(O.jsx)(hn.b, {
                                                direction: "vertical",
                                                children: Fn.sort(Object(wn.b)("parameterDisplayName")).map((function(n, e) {
                                                    return Object(O.jsx)(fn.a, {
                                                        value: n.parameterId,
                                                        disabled: me,
                                                        onChange: function(e) {
                                                            J(e.target.value), En(n)
                                                        },
                                                        children: n.parameterDisplayName
                                                    }, e)
                                                }))
                                            })
                                        })
                                    })]
                                });
                            case "line_chart":
                                return Object(O.jsx)("div", {
                                    className: "deviceParamAccordianContainer",
                                    children: R.map((function(n, e) {
                                        return Object(O.jsx)(bn.a, {
                                            accordion: !0,
                                            style: {
                                                marginBottom: "2%"
                                            },
                                            defaultActiveKey: Xn && n.deviceEntity.deviceDisplayName,
                                            children: Object(O.jsx)(Bn, {
                                                header: n.deviceEntity.deviceDisplayName,
                                                children: Array.isArray(n.parameterEntityList) ? n.parameterEntityList.sort(Object(wn.b)("parameterDisplayName")).map((function(e, t) {
                                                    return Object(O.jsx)(gn.a, {
                                                        checked: !Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedDevice === n.deviceEntity.iotDeviceMapId && t.selectedParameter === e.parameterId
                                                        }))),
                                                        className: "deviceParamCheckbox",
                                                        disabled: 2 === U.length && Object(p.isEmpty)(U.filter((function(t) {
                                                            return t.selectedParameter === e.parameterId && t.selectedDevice === n.deviceEntity.iotDeviceMapId
                                                        }))),
                                                        onChange: function(t) {
                                                            if (t.target.checked) {
                                                                var i = {
                                                                    selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                    selectedParameter: e.parameterId
                                                                };
                                                                Y((function(n) {
                                                                    return [].concat(Object(a.a)(n), [i])
                                                                }))
                                                            } else {
                                                                var r = [{
                                                                        selectedDevice: n.deviceEntity.iotDeviceMapId,
                                                                        selectedParameter: e.parameterId
                                                                    }],
                                                                    o = U.filter((function(n) {
                                                                        return !r.find((function(e) {
                                                                            return e.selectedDevice === n.selectedDevice && n.selectedParameter === e.selectedParameter
                                                                        }))
                                                                    }));
                                                                Y(o)
                                                            }
                                                        },
                                                        children: e.parameterDisplayName
                                                    }, t)
                                                })) : Object(O.jsx)("div", {
                                                    style: {
                                                        padding: "1rem",
                                                        color: "gray"
                                                    },
                                                    children: "No parameters available."
                                                })
                                            }, Xn ? n.deviceEntity.deviceDisplayName : e)
                                        }, n.deviceEntity.deviceDisplayName)
                                    }))
                                });
                            case "metric_chart":
                            case "cal_chart":
                                return Ce();
                            default:
                                return Object(O.jsx)("div", {})
                        }
                    };
                Object(s.useEffect)((function() {
                    Object(p.isEmpty)(en) && !Object(p.isEmpty)(cn) && sn([])
                }), [en, cn]);
                var De = "MobileView" === function() {
                    var n = "MobileView";
                    return l > 1220 ? n = "DesktopView" : l > 767 && (n = "TabView"), n
                }() ? 400 : 805;
                return Object(O.jsxs)(un.a, {
                    width: De,
                    onClose: function() {
                        he || (je(), C("")), t()
                    },
                    visible: e,
                    bodyStyle: {
                        paddingBottom: 80
                    },
                    footer: Object(O.jsx)("div", {
                        style: {
                            textAlign: "center",
                            borderTop: "transparent"
                        },
                        children: he ? we((function() {
                            var n = 1 + Math.random(),
                                e = {
                                    id: "item_".concat(n, "_reports_settings"),
                                    device: Qn,
                                    parameter: Gn,
                                    allReportDeviceSelected: $n,
                                    allReportParametersSelected: ie,
                                    selectedReportUserDeviceList: en,
                                    selectedReportUserParameterList: cn,
                                    startDate: Object(wn.e)(6).format("YYYY-MM-DD"),
                                    endDate: Object(wn.c)().format("YYYY-MM-DD")
                                };
                            A(kn(e)), t()
                        }), "sidedrawer.updateData") : we((function() {
                            var n = 1 + Math.random(),
                                e = [];
                            R.forEach((function(n) {
                                Array.isArray(n.parameterEntityList) && n.parameterEntityList.sort(Object(wn.b)("parameterDisplayName")).forEach((function(n) {
                                    e.push(n)
                                }))
                            }));
                            var t = {
                                id: "item_".concat(n, "_").concat(E),
                                device: pn,
                                parameter: de ? On : Rn,
                                chartName: E,
                                startDate: Object(wn.e)(6).format("YYYY-MM-DD"),
                                endDate: Object(wn.c)().format("YYYY-MM-DD"),
                                selectedDeviceAndParameterList: U,
                                calculatedCalValue: [],
                                calculatedResult: [],
                                customChartName: "",
                                displayCalModal: "cal_chart" === E
                            };
                            if (Xn) {
                                var a = c.filter((function(n) {
                                        return n.id === Object(p.first)(o).id
                                    })),
                                    r = Object(p.first)(a);
                                r.selectedDeviceAndParameterList = U, r.parameter = de ? On : Rn, r.device = pn, r.displayCalModal = "cal_chart" === E, i(r)
                            } else i(t);
                            localStorage.setItem("deviceList", JSON.stringify(g)), localStorage.setItem("parametersList", JSON.stringify(Object(p.uniqBy)(e, "parameterId"))), je(), C("")
                        }), "sidedrawer.updateTiles")
                    }),
                    children: [Object(O.jsxs)(Cn, {
                        children: [" ", he ? Object(O.jsxs)("div", {
                            style: {
                                marginTop: "5%"
                            },
                            children: [Object(O.jsx)(mn.a, {
                                plain: !0
                            }), Object(O.jsxs)("div", {
                                className: "userDeviceList",
                                children: [Object(O.jsx)("div", {
                                    className: "headings",
                                    children: "Select Devices"
                                }), !b && Object(O.jsx)(gn.a, {
                                    checked: $n,
                                    onChange: function(n) {
                                        var e = [];
                                        g.map((function(n) {
                                            e.push(n.iotDeviceMapId)
                                        })), n.target.checked ? (ne(n.target.checked), tn(e), Un(g)) : (ne(n.target.checked), tn([]), Un([]), ae(!1))
                                    },
                                    children: "Select All"
                                }), g.map((function(n, e) {
                                    return Object(O.jsxs)("div", {
                                        children: [Object(O.jsx)(gn.a, {
                                            checked: en.includes(n.iotDeviceMapId),
                                            onChange: function(e) {
                                                if (e.target.checked) {
                                                    tn((function(e) {
                                                        return [].concat(Object(a.a)(e), [n.iotDeviceMapId])
                                                    }));
                                                    var t = [].concat(Object(a.a)(Qn), [n]);
                                                    Un(t), ne(!1)
                                                } else {
                                                    var i = en.indexOf(n.iotDeviceMapId);
                                                    tn(en.filter((function(n, e) {
                                                        return e !== i
                                                    })));
                                                    var r = Qn.filter((function(e) {
                                                        return e.iotDeviceMapId !== (null === n || void 0 === n ? void 0 : n.iotDeviceMapId)
                                                    }));
                                                    Un(r), ne(!1)
                                                }
                                            },
                                            children: n.deviceDisplayName
                                        }, e), Object(O.jsx)("br", {})]
                                    }, e)
                                }))]
                            }), Object(O.jsxs)("div", {
                                className: "deviceParameterList",
                                children: [Object(O.jsx)("div", {
                                    className: "headings",
                                    children: "Select Parameters"
                                }), !b && Object(O.jsx)(gn.a, {
                                    checked: ie,
                                    disabled: Object(p.isEmpty)(en),
                                    onChange: function(n) {
                                        var e = [];
                                        x.map((function(n) {
                                            e.push(n.parameterId)
                                        })), n.target.checked ? (ae(n.target.checked), sn(e), Tn(x)) : (ae(n.target.checked), sn([]), Tn([]))
                                    },
                                    children: "Select All"
                                }), x.map((function(n, e) {
                                    return Object(O.jsxs)("div", {
                                        children: [Object(O.jsx)(gn.a, {
                                            checked: cn.includes(n.parameterId),
                                            disabled: Object(p.isEmpty)(en),
                                            onChange: function(e) {
                                                if (e.target.checked) {
                                                    sn((function(e) {
                                                        return [].concat(Object(a.a)(e), [n.parameterId])
                                                    }));
                                                    var t = [].concat(Object(a.a)(Gn), [n]);
                                                    Tn(t), ae(!1)
                                                } else {
                                                    var i = cn.indexOf(n.parameterId);
                                                    sn(cn.filter((function(n, e) {
                                                        return e !== i
                                                    })));
                                                    var r = Gn.filter((function(e) {
                                                        return e.parameterId !== (null === n || void 0 === n ? void 0 : n.parameterId)
                                                    }));
                                                    Tn(r), ae(!1)
                                                }
                                            },
                                            children: n.parameterDisplayName
                                        }, e), Object(O.jsx)("br", {})]
                                    }, e)
                                }))]
                            })]
                        }) : Object(O.jsxs)(O.Fragment, {
                            children: [Object(O.jsx)(fn.a.Group, {
                                defaultValue: E,
                                value: E,
                                buttonStyle: "solid",
                                checked: E,
                                className: "chartOptions",
                                onChange: function(n) {
                                    var e = n.target.value;
                                    C(e), ye(e), je()
                                },
                                children: vn.map((function(n, e) {
                                    return Object(O.jsxs)(fn.a.Button, {
                                        value: n.value,
                                        disabled: !Object(p.isEmpty)(o) && E !== n.value,
                                        children: [Object(O.jsx)("img", {
                                            alt: "chart",
                                            src: E === n.value ? n.selectedIcon : n.icon
                                        }), Object(O.jsx)("div", {
                                            className: "chartOptionsTitle",
                                            children: Object(O.jsx)(Q.a, {
                                                id: "sidedrawer.".concat(n.label)
                                            })
                                        })]
                                    }, e)
                                }))
                            }), Object(O.jsx)(mn.a, {
                                plain: !0
                            }), E && Ie()]
                        })]
                    }), b && Object(O.jsx)(on.a, {})]
                })
            }
            var Mn = Object(D.b)(dn || (dn = Object(I.a)(["\nbody {\n  -webkit-overflow-scrolling: touch;\n}\n\nhtml h1,\nhtml h2,\nhtml h3,\nhtml h4,\nhtml h5,\nhtml h6,\nhtml a,\nhtml p,\nhtml li,\ninput,\ntextarea,\nspan,\ndiv,\nhtml,\nbody,\nhtml a {\n  margin-bottom: 0;\n  font-family: 'Roboto', sans-serif;\n  -webkit-font-smoothing: antialiased;\n  -moz-osx-font-smoothing: grayscale;\n  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.004);\n}\n\nhtml ul {\n  -webkit-padding-start: 0px;\n  list-style: none;\n  margin-bottom: 0;\n}\n\n.scrollbar-track-y,\n.scrollbar-thumb-y {\n  width: 5px !important;\n}\n\n.scrollbar-track-x,\n.scrollbar-thumb-x {\n  height: 5px !important;\n}\n\n.scrollbar-thumb {\n  border-radius: 0 !important;\n}\n\n.scrollbar-track {\n  background: rgba(222, 222, 222, 0.15) !important;\n}\n\n.scrollbar-thumb {\n  border-radius: 0 !important;\n  background: rgba(0, 0, 0, 0.5) !important;\n}\n\n"]))),
                Rn = D.d.div(pn || (pn = Object(I.a)(["\n  -webkit-overflow-scrolling: touch;\n  .trigger {\n    font-size: 18px;\n    line-height: 64px;\n    padding: 0 16px;\n    cursor: pointer;\n    transition: color 0.3s;\n  }\n\n  .trigger:hover {\n    color: ", ";\n  }\n\n  .ant-layout-sider-collapsed .anticon {\n    font-size: 16px;\n  }\n\n  .ant-layout-sider-collapsed .nav-text {\n    display: none;\n  }\n\n  .ant-layout {\n    background: ", ";\n\n    &.isoContentMainLayout {\n      overflow: auto;\n      overflow-x: hidden;\n      @media only screen and (min-width: 768px) and (max-width: 1220px) {\n        width: calc(100% - 80px);\n        flex-shrink: 0;\n      }\n\n      @media only screen and (max-width: 767px) {\n        width: 100%;\n        flex-shrink: 0;\n      }\n    }\n  }\n\n  .isoLayoutContent {\n    width: 100%;\n    padding: 35px;\n    background-color: #ffffff;\n    border: 1px solid ", ";\n    height: 100%;\n  }\n\n  .isomorphicLayout {\n    width: calc(100% - 240px);\n    flex-shrink: 0;\n    overflow-x: hidden !important;\n\n    @media only screen and (max-width: 767px) {\n      width: 100%;\n    }\n\n    @media only screen and (min-width: 768px) and (max-width: 1220px) {\n      width: calc(100% - 80px);\n      width: 100%;\n    }\n  }\n\n  .ant-layout-footer {\n    font-size: 13px;\n    @media (max-width: 767px) {\n      padding: 10px 20px;\n    }\n  }\n\n  .ant-drawer-footer {\n    display: none;\n  }\n"])), Object(k.palette)("primary", 0), Object(k.palette)("secondary", 1), Object(k.palette)("border", 0)),
                Ln = (c.a.warning, o.a.Content),
                Sn = g.a.toggleAll,
                zn = {
                    layout: {
                        flexDirection: "row"
                    },
                    content: {
                        padding: "95px 0 0",
                        flexShrink: "0",
                        background: m.a.SECONDARY_COLOR,
                        position: "relative",
                        overflowX: "auto"
                    }
                },
                Qn = f.a.updateSelectedParameters;

            function Un() {
                var n = Object(d.b)(),
                    e = Object(s.useState)(!1),
                    t = Object(r.a)(e, 2),
                    i = t[0],
                    c = t[1],
                    l = Object(d.c)((function(n) {
                        return n.App.height
                    })),
                    m = Object(u.a)(),
                    f = m.width,
                    g = m.height,
                    b = Object(h.a)("widgetSettings"),
                    x = Object(s.useState)([]),
                    A = Object(r.a)(x, 2),
                    j = A[0],
                    y = A[1],
                    v = function() {
                        c(!0)
                    },
                    w = function() {
                        c(!1), y([])
                    };
                Object(s.useEffect)((function() {
                    n(Sn(f, g))
                }), [f, g, n]);
                return Object(O.jsxs)(Rn, {
                    children: [Object(O.jsx)(Mn, {}), Object(O.jsxs)(o.a, {
                        style: {
                            height: g
                        },
                        children: [Object(O.jsx)(rn, {
                            showDrawer: v
                        }), Object(O.jsxs)(o.a, {
                            style: zn.layout,
                            children: [Object(O.jsx)(Z, {}), Object(O.jsx)(o.a, {
                                className: "isoContentMainLayout",
                                style: {
                                    height: l
                                },
                                children: Object(O.jsxs)(Ln, {
                                    className: "isomorphicContent",
                                    style: zn.content,
                                    children: [Object(O.jsx)(sn, {
                                        editWidgetDetails: function(n) {
                                            if (n) {
                                                var e = b.filter((function(e) {
                                                    return e.id === n
                                                }));
                                                v(), y(e)
                                            }
                                        }
                                    }), Object(O.jsx)(Nn, {
                                        visible: i,
                                        onClose: w,
                                        addWidget: function(e) {
                                            if (b) {
                                                var t = b.filter((function(n) {
                                                    return n.id === e.id
                                                }));
                                                if (Object(p.isEmpty)(t)) {
                                                    var i = b,
                                                        r = [].concat(Object(a.a)(i), [e]);
                                                    n(Qn(r)), w()
                                                } else {
                                                    var o = Object(p.remove)(b, (function(n) {
                                                            return n.id !== e.id
                                                        })),
                                                        c = [].concat(Object(a.a)(o), [e]);
                                                    n(Qn(c)), w()
                                                }
                                            } else n(Qn([e])), w()
                                        },
                                        editWidgetSettings: j,
                                        widgetDetailsSettings: b
                                    })]
                                })
                            })]
                        })]
                    })]
                })
            }
        }
    }
]);
//# sourceMappingURL=14.9a458124.chunk.js.map