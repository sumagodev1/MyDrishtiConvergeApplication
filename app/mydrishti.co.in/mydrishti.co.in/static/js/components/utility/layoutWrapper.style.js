import styled from "styled-components";

const LayoutContentWrapper = styled.div `
  padding-left: 20px;
  display: flex;
  flex-flow: row wrap;

  @media only screen and (max-width: 767px) {
    padding-left: 20px;
  }

  @media (max-width: 580px) {
    padding-top: 20px;
  }
`;

export {
    LayoutContentWrapper
};