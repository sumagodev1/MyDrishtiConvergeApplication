import styled from "styled-components";
import WithDirection from "@lib/helpers/rtl";

const ChartsWrapper = styled.div `
  .width100 {
    width: 100%;
  }

  .headerContainer {
    height: 40px;
    margin-top: 5px;
    cursor: move;

    @media only screen and (max-width: 480px) {
      .headerTitle {
        width: 100px;
      }

      .col-sm-7 {
        width: 80%;
      }

      .col-sm-1 {
        top: -43px !important;
      }
    }
  }

  .headerTitle {
    font-size: 12px;
    width: 170px;
  }

  .chartMenuOptions {
    float: right;
    position: absolute;
    right: 5px;
    top: 0px;
    z-index: 5;
  }

  .deviceDisplayNameHeder {
    height: 30px;
    font-size: 12px;
    cursor: move;
  }

  .highChartNoData {
    font-size: 12px;
    text-align: center;
    padding-top: 12%;
    min-height: 260px;
  }

  .calMetricChartNoData {
    font-size: 12px;
    text-align: center;
    margin: 0 auto;
    padding-top: 10%;
    max-width: 170px;
  }

  .calMetricChartContainer {
    clear: both;
    margin: 0 auto;
    text-align: center;
    margin-top: 10px;
  }

  .alignCenter {
    font-size: 12px;
    text-align: center;
  }

  .gaugeChartError {
    font-size: 12px;
    text-align: center;
    padding-top: 35%;
  }
`;

export default WithDirection(ChartsWrapper);