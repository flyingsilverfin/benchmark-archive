<template>
  <div>
    <div ref="chart"></div>
    <el-popover
      ref="popover"
      placement="top"
      title="Title"
      width="200"
      trigger="manual"
      content="this is content, this is content, this is content"
      v-model="popoverVisible"
    >
    </el-popover>
  </div>
</template>
<script>
import ChartFactory from "./ChartFactory";
import QueriesUtil from "./QueriesUtil";

export default {
  props: ["name", "executions", "spans"],
  data() {
    return {
      popoverVisible: false
    };
  },
  created() {
    // Compute array of unique queries that have been executed on this graph
    const queries = Array.from(
      new Set(this.spans.map(span => span.tags.query))
    );
    // queriesMap will map a full query to a legend identifier, e.g. { "match $x isa person; get;": "matchQuery1", ... }
    const queriesMap = QueriesUtil.buildQueriesMap(queries);
    // queriesTimes will map a query legend identifier to its avgTime per commit
    const queriesTimes = QueriesUtil.buildQueriesTimes(
      queries,
      this.spans,
      this.executions
    );

    this.$nextTick(() => {
      const chartComponent = this.$refs.chart;
      chartComponent.style.height = "500px";

      ChartFactory.createChart(
        chartComponent,
        this.name,
        queriesTimes,
        queriesMap
      );

      //TODO decide on how to use the tooltip
      // const popover = this.$refs.popover.$el;
      // popover.style.position = "absolute";
      // popover.style.display = "block";
      // myChart.on('click', (e) => {
      //   popover.style.right = e.event.offsetX+"px";
      //   popover.style.top = e.event.offsetY+"px";
      //   // popover.style.transform = "translate(50%, 0%)";
      //   this.popoverVisible=!this.popoverVisible;
      // });
    });
  }
};
</script>
<style scoped></style>
