import styled from "styled-components";
import {
    palette
} from "styled-theme";
import {
    variables
} from "@assets/styles/variables";
import {
    transition,
    borderRadius
} from "@lib/helpers/style_utils";
import WithDirection from "@lib/helpers/rtl";

const SidebarWrapper = styled.div `
  .isomorphicSidebar {
    z-index: 1000;
    background: ${palette("secondary", 0)};
    flex: 0 0 280px;

    .scrollarea {
      height: calc(100vh - 70px);
    }

    &.ant-layout-sider-collapsed {
      width: 0;
      min-width: 0 !important;
      max-width: 0 !important;
      flex: 0 0 0 !important;
    }

    @media only screen and (max-width: 767px) {
      width: 200px !important;
      flex: 0 0 240px !important;
    }

    .isoLogoWrapper {
      height: 70px;
      background: ${variables.PRIMARY_COLOR};
      margin: 0;
      text-align: center;
      overflow: hidden;
      ${borderRadius()};

      .logoContent {
        height: 100px;
        width: 160px;
      }

      img {
        height: 70%;
      }

      h3 {
        a {
          font-size: 21px;
          font-weight: 300;
          line-height: 70px;
          letter-spacing: 3px;
          text-transform: uppercase;
          color: ${palette("grayscale", 6)};
          display: block;
          text-decoration: none;
        }
      }
    }

    &.ant-layout-sider-collapsed {
      .isoLogoWrapper {
        padding: 0;

        h3 {
          a {
            font-size: 27px;
            font-weight: 500;
            letter-spacing: 0;
          }
        }
      }
    }

    .isoDashboardMenu {
      padding: 20px;
      background: transparent;

      a {
        text-decoration: none;
        font-weight: 400;
      }

      .ant-menu-item {
        display: -ms-flexbox;
        display: flex;
        -ms-flex-align: center;
        align-items: center;
        margin: 0;
        height: 100px;
      }

      .isoMenuHolder {
        display: block;
        text-align: center;

        i {
          font-size: 25px;
          color: inherit;
          position: absolute;
          margin: ${(props) =>
            props["data-rtl"] === "rtl"
              ? "0 0 0 30px"
              : "-34px 2px 15px -12px"};
          ${transition()};
        }
      }

      .anticon {
        font-size: 18px;
        margin-right: 30px;
        color: inherit;
        ${transition()};
      }

      .nav-text {
        font-size: 13px;
        color: inherit;
        font-weight: 400;
        text-transform: uppercase;
        ${transition()};
      }

      .ant-menu-item-selected {
        background-color: rgba(0, 0, 0, 0.4) !important;
        border-radius: 10px;
        .anticon {
          color: #fff;
        }

        i {
          color: #fff;
        }

        .nav-text {
          color: #fff;
        }
      }

      > li {
        &:hover {
          i,
          .nav-text {
            color: #ffffff;
          }
        }
      }
    }

    .ant-menu-dark .ant-menu-inline.ant-menu-sub {
      background: ${palette("secondary", 5)};
    }

    .ant-menu-submenu-inline,
    .ant-menu-submenu-vertical {
      > .ant-menu-submenu-title {
        width: 100%;
        display: flex;
        align-items: center;
        /* padding: 0 24px; */

        > span {
          display: flex;
          align-items: center;
        }

        .ant-menu-submenu-arrow {
          left: ${(props) => (props["data-rtl"] === "rtl" ? "25px" : "auto")};
          right: ${(props) => (props["data-rtl"] === "rtl" ? "auto" : "25px")};

          &:before,
          &:after {
            width: 8px;
            ${transition()};
          }

          &:before {
            transform: rotate(-45deg) translateX(3px);
          }

          &:after {
            transform: rotate(45deg) translateX(-3px);
          }

          ${
            "" /* &:after {
            content: '\f123';
            font-family: 'Ionicons' !important;
            font-size: 16px;
            color: inherit;
            left: ${props => (props['data-rtl'] === 'rtl' ? '16px' : 'auto')};
            right: ${props => (props['data-rtl'] === 'rtl' ? 'auto' : '16px')};
            ${transition()};
          } */
          };
        }

        &:hover {
          .ant-menu-submenu-arrow {
            &:before,
            &:after {
              color: #ffffff;
            }
          }
        }
      }

      .ant-menu-inline,
      .ant-menu-submenu-vertical {
        > li:not(.ant-menu-item-group) {
          padding-left: ${(props) =>
            props["data-rtl"] === "rtl" ? "0px !important" : "74px !important"};
          padding-right: ${(props) =>
            props["data-rtl"] === "rtl" ? "74px !important" : "0px !important"};
          font-size: 13px;
          font-weight: 400;
          margin: 0;
          color: inherit;
          ${transition()};

          &:hover {
            a {
              color: #ffffff !important;
            }
          }
        }

        .ant-menu-item-group {
          padding-left: 0;

          .ant-menu-item-group-title {
            padding-left: 100px !important;
          }
          .ant-menu-item-group-list {
            .ant-menu-item {
              padding-left: 125px !important;
            }
          }
        }
      }

      .ant-menu-sub {
        box-shadow: none;
        background-color: transparent !important;
      }
    }

    &.ant-layout-sider-collapsed {
      .nav-text {
        display: none;
      }

      .ant-menu-submenu-inline > {
        .ant-menu-submenu-title:after {
          display: none;
        }
      }

      .ant-menu-submenu-vertical {
        > .ant-menu-submenu-title:after {
          display: none;
        }

        .ant-menu-sub {
          background-color: transparent !important;

          .ant-menu-item {
            height: 35px;
          }
        }
      }
    }
  }
`;

export default WithDirection(SidebarWrapper);