import styled from "styled-components";
import WithDirection from "@lib/helpers/rtl";

const SideDrawerWrapper = styled.div `
  .chartOptions {
    padding: 0 25px;
    margin-top: 5%;
    display: flex;
    flex-wrap: nowrap;
    gap: 5px;
    justify-content: flex-start;
  }

  .ant-radio-button-wrapper {
    display: flex !important;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 130px;
    height: 130px;
    padding: 10px;
    border-radius: 0px;
    background-color: #f5f5f5;
  }

  .ant-radio-button-wrapper img {
    width: 100%;
    height: 50px;
    object-fit: contain;
    margin-bottom: 6px;
  }

  .chartOptionsTitle {
    color: black;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.5;
  }

  .deviceParameterList {
    float: right;
    width: 50%;
  }

  .updateButton {
    height: 35px;
    width: 150px;
    border-radius: 12px;
  }

  .deviceParamAccordianContainer {
    margin: 0 7%;
    max-height: 650px;
    overflow-y: auto;
  }

  .deviceParamCheckbox {
    width: 45%;
    margin-left: 8px;
  }

  .headings {
    margin-bottom: 10px;
    background: green;
    color: white;
    padding: 5px 10px;
    border-radius: 5px;
  }
`;

export default WithDirection(SideDrawerWrapper);