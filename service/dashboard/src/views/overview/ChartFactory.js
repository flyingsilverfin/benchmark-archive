import echarts from "echarts";

function mapToSerie(x, queriesMap) {
  return {
    name: queriesMap[x.query],
    type: "line",
    data: x.times.map(x => {
      return {
        value: Number(x.avgTime).toFixed(3),
        symbolSize: Math.min(x.stdDeviation / 10, 45) + 5,
        symbol: "circle",
        stdDeviation: x.stdDeviation
      };
    }),
    smooth: true,
    emphasis: { label: { show: false }, itemStyle: { color: "yellow" } },
    showAllSymbol: true,
    tooltip: {
      formatter: args => {
        return `avgTime: ${Number(args.data.value).toFixed(
          3
        )} ms <br> stdDeviation: ${Number(args.data.stdDeviation).toFixed(3)}`;
      }
    }
  };
}

function createChart(htmlComponent, title, queriesTimes, queriesMap) {
  const myChart = echarts.init(htmlComponent);
  // specify chart configuration item and data
  const option = {
    title: {
      text: title,
      subtext: "Grakn graph"
    },
    tooltip: {
      show: true,
      trigger: "item"
    },
    legend: {
      type: "scroll",
      orient: "vertical",
      right: 10,
      top: 20,
      bottom: 20,
      data: Object.values(queriesMap).sort(),
      tooltip: {
        show: true,
        showDelay: 500,
        triggerOn: "mousemove",
        formatter: args => {
          return Object.keys(queriesMap).filter(
            x => queriesMap[x] === args.name
          );
        }
      }
    },
    calculable: true,
    xAxis: [
      {
        type: "category",
        boundaryGap: false,
        data: queriesTimes[0].times.map(x => x.commit.substring(0, 15)),
        triggerEvent: true
      }
    ],
    yAxis: [
      {
        type: "value",
        axisLabel: {
          formatter: "{value} ms"
        }
      }
    ],
    series: queriesTimes.map(x => mapToSerie(x, queriesMap)),
    dataZoom: [
      {
        type: "inside",
        filterMode: "none",
        orient: "vertical"
      }
    ]
  };
  myChart.setOption(option);
  return myChart;
}

export default { createChart };
