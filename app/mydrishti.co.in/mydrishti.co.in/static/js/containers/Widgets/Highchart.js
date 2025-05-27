import React, {
    useRef
} from "react";
import HighchartsReact from "highcharts-react-official";
import Highcharts from "highcharts";

export default function Highchart({
    chartOptions
}) {
    const chartRef = useRef(null);
    return ( <
        HighchartsReact ref = {
            chartRef
        }
        containerProps = {
            {
                style: {
                    height: "100%"
                }
            }
        }
        highcharts = {
            Highcharts
        }
        options = {
            chartOptions
        }
        />
    );
}