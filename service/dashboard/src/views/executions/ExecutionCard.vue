<template>
  <el-card>
    <el-row>
      <el-col :span="6">{{ execution.status }}</el-col>
      <el-col :span="6">{{ execution.prNumber }}</el-col>
      <el-col :span="6">{{ execution.executionStartedAt }}</el-col>
      <el-col :span="6">{{ execution.executionCompletedAt }}</el-col>
    </el-row>
  </el-card>
</template>
<script>
export default {
  props: ["execution"]
};
</script>
<style scoped>
.el-card {
  margin-bottom: 2px;
}
</style>
