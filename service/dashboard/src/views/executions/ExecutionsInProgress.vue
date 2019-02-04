<template>
  <el-container direction="vertical">
    <div class="title-row">IN PROGRESS</div>
    <el-card
      class="status-card"
      shadow="hover"
      :body-style="{ padding: '0px' }"
    >
      <section class="el-component">
        <el-row class="inner-row">
          <el-table
            :data="tableData"
            max-height="200"
            :cell-style="{ 'text-align': 'center' }"
            :header-cell-style="{
              'text-align': 'center',
              'font-weight': 'bold',
              color: 'black'
            }"
            :default-sort="{ prop: 'prMergedAt', order: 'descending' }"
          >
            <el-table-column
              prop="prMergedAt"
              label="PR Merged At"
              min-width="150px;"
            >
            </el-table-column>
            <el-table-column
              prop="commit"
              label="PR Merge Commit"
              width="330px;"
            ></el-table-column>
            <el-table-column prop="prNumber" label="PR">
              <template slot-scope="scope">
                <a :href="scope.row.prUrl">#{{ scope.row.prNumber }}</a>
              </template></el-table-column
            >
            <el-table-column prop="status" label="Status" width="150px;">
            </el-table-column>
            <el-table-column
              prop="executionStartedAt"
              label="Started At"
              width="200px;"
            >
            </el-table-column>
            <el-table-column prop="vmName" label="VM Name" min-width="300px;">
              <template slot-scope="scope">
                <span style="margin-right: 10px">{{ scope.row.vmName }}</span>
                <el-button
                  icon="el-icon-more"
                  size="mini"
                  @click="copyStringToClipboard(scope.row.vmName)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-row>
      </section>
    </el-card>
  </el-container>
</template>
<style scoped>
.el-card__body {
  padding: 10px;
}
.title-row {
  font-size: 80%;
  font-weight: bold;
  padding: 5px;
}
.inner-row {
  margin-bottom: 8px;
}
</style>
<script>
import BenchmarkClient from "@/util/BenchmarkClient.js";
export default {
  data() {
    return {
      tableData: []
    };
  },
  created() {
    BenchmarkClient.getExecutions(
      `{ executions(status: ["INITIALISING", "RUNNING"]) { prMergedAt prNumber prUrl commit status executionStartedAt vmName } }`
    ).then(res => {
      this.tableData = res.data.executions;
    });
  },
  methods: {
    copyStringToClipboard(vmName) {
      copyToClipboard(
        "gcloud compute ssh ubuntu@" + vmName + " --zone=us-east1-b"
      );
    }
  }
};
const copyToClipboard = str => {
  const el = document.createElement("textarea"); // Create a <textarea> element
  el.value = str; // Set its value to the string that you want copied
  el.setAttribute("readonly", ""); // Make it readonly to be tamper-proof
  el.style.position = "absolute";
  el.style.left = "-9999px"; // Move outside the screen to make it invisible
  document.body.appendChild(el); // Append the <textarea> element to the HTML document
  const selected =
    document.getSelection().rangeCount > 0 // Check if there is any content selected previously
      ? document.getSelection().getRangeAt(0) // Store selection if found
      : false; // Mark as false to know no selection existed before
  el.select(); // Select the <textarea> content
  document.execCommand("copy"); // Copy - only works as a result of a user action (e.g. click events)
  document.body.removeChild(el); // Remove the <textarea> element
  if (selected) {
    // If a selection existed before copying
    document.getSelection().removeAllRanges(); // Unselect everything on the HTML document
    document.getSelection().addRange(selected); // Restore the original selection
  }
};
</script>
