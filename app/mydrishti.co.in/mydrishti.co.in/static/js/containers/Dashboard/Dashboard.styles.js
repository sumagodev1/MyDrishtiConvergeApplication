import styled, {
    createGlobalStyle
} from "styled-components";
import {
    palette
} from "styled-theme";

export const DashboardGlobalStyles = createGlobalStyle `
body {
  -webkit-overflow-scrolling: touch;
}

html h1,
html h2,
html h3,
html h4,
html h5,
html h6,
html a,
html p,
html li,
input,
textarea,
span,
div,
html,
body,
html a {
  margin-bottom: 0;
  font-family: 'Roboto', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.004);
}

html ul {
  -webkit-padding-start: 0px;
  list-style: none;
  margin-bottom: 0;
}

.scrollbar-track-y,
.scrollbar-thumb-y {
  width: 5px !important;
}

.scrollbar-track-x,
.scrollbar-thumb-x {
  height: 5px !important;
}

.scrollbar-thumb {
  border-radius: 0 !important;
}

.scrollbar-track {
  background: rgba(222, 222, 222, 0.15) !important;
}

.scrollbar-thumb {
  border-radius: 0 !important;
  background: rgba(0, 0, 0, 0.5) !important;
}

`;

export const DashboardContainer = styled.div `
  -webkit-overflow-scrolling: touch;
  .trigger {
    font-size: 18px;
    line-height: 64px;
    padding: 0 16px;
    cursor: pointer;
    transition: color 0.3s;
  }

  .trigger:hover {
    color: ${palette("primary", 0)};
  }

  .ant-layout-sider-collapsed .anticon {
    font-size: 16px;
  }

  .ant-layout-sider-collapsed .nav-text {
    display: none;
  }

  .ant-layout {
    background: ${palette("secondary", 1)};

    &.isoContentMainLayout {
      overflow: auto;
      overflow-x: hidden;
      @media only screen and (min-width: 768px) and (max-width: 1220px) {
        width: calc(100% - 80px);
        flex-shrink: 0;
      }

      @media only screen and (max-width: 767px) {
        width: 100%;
        flex-shrink: 0;
      }
    }
  }

  .isoLayoutContent {
    width: 100%;
    padding: 35px;
    background-color: #ffffff;
    border: 1px solid ${palette("border", 0)};
    height: 100%;
  }

  .isomorphicLayout {
    width: calc(100% - 240px);
    flex-shrink: 0;
    overflow-x: hidden !important;

    @media only screen and (max-width: 767px) {
      width: 100%;
    }

    @media only screen and (min-width: 768px) and (max-width: 1220px) {
      width: calc(100% - 80px);
      width: 100%;
    }
  }

  .ant-layout-footer {
    font-size: 13px;
    @media (max-width: 767px) {
      padding: 10px 20px;
    }
  }

  .ant-drawer-footer {
    display: none;
  }
`;