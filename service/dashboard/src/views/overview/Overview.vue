<template>
  <el-container class="is-vertical overview-section">
    <el-main>
      <h2>Benchmark Overview</h2>
      <el-row
        class="panel"
        v-for="graphName in graphNames"
        v-bind:key="graphName"
      >
        <overview-commits-chart
          :name="graphName"
          :executions="completedExecutions"
          :spans="filterSpans(graphName)"
        ></overview-commits-chart>
      </el-row>
    </el-main>
  </el-container>
</template>

<script>
import BenchmarkClient from "@/util/BenchmarkClient.js";
import OverviewCommitsChart from "./OverviewCommitsChart.vue";

export default {
  name: "OverviewPage",
  components: { OverviewCommitsChart },
  data() {
    return {
      numberOfCompletedExecutions: 3,
      completedExecutions: null,
      graphNames: [],
      spans: null
    };
  },
  async created() {
    // Get the last N completed executions
    // Note: we need to reverse the array because in the chart we want to show the most recent execution not as first but as last (rightmost)
    this.completedExecutions = (await BenchmarkClient.getLatestCompletedExecutions(
      this.numberOfCompletedExecutions
    )).reverse();
    // Get all the spans relative to those executions
    this.spans = await BenchmarkClient.getExecutionsSpans(
      this.completedExecutions
    );
    // Compute graph names from spans, for each graph name we draw a chart
    this.graphNames = Array.from(
      new Set(this.spans.map(span => span.tags.graphName))
    );
  },
  methods: {
    filterSpans(name) {
      return this.spans.filter(span => span.tags.graphName === name);
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.overview-section {
  overflow: scroll;
}
.el-main{
    padding: 20px 50px;
}
.el-container {
  background-color: #f4f3ef;
}
.panel {
  background-color: white;
  margin-bottom: 20px;
}
h2 {
  margin-bottom: 20px;
}
</style>
