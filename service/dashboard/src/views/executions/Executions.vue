<template>
  <section class="el-container is-vertical page-container">
    <!-- <header class="el-header">Header</header> -->
    <main class="el-main">
      <h2>Benchmark Executions</h2>
      <el-row class="cards-row">
        <executions-in-progress></executions-in-progress>
      </el-row>
      <execution-card
        v-for="exec in executions"
        v-bind:key="exec.prNumber"
        :execution="exec"
      ></execution-card>
    </main>
  </section>
</template>

<script>
import ExecutionCard from "./ExecutionCard.vue";
import ExecutionsInProgress from "./ExecutionsInProgress.vue";
import BenchmarkClient from "@/util/BenchmarkClient.js";

export default {
  name: "ExecutionsPage",
  data() {
    return {
      executions: []
    };
  },
  components: { ExecutionsInProgress, ExecutionCard },
  created() {
    BenchmarkClient.getExecutions(
      `{ executions(status:["COMPLETED"], orderBy: "prMergedAt"){ 
          prMergedAt 
          prNumber 
          prUrl 
          commit 
          status 
          executionStartedAt 
          executionCompletedAt 
          vmName } }`
    ).then(execs => {
      this.executions = execs.data.executions;
    });
  }
};
</script>

<style scoped>
.el-container {
  padding: 0 30px;
  background-color: #f4f3ef;
}

.el-header {
  text-align: start;
  height: 30px;
}
.panel {
  background-color: white;
}
h2 {
  margin-bottom: 20px;
}
.cards-row {
  margin-bottom: 20px;
}
</style>
