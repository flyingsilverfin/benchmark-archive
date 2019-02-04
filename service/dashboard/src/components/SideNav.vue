<template>
  <aside class="el-aside">
    <el-menu
      default-active="overview"
      class="el-menu-vertical-demo"
      :router="true"
    >
      <div class="logo"><img src="assets/grakn-logo.png" /></div>
      <el-menu-item index="overview">
        <i class="el-icon-menu"></i> <span slot="title">Overview</span>
      </el-menu-item>
      <el-menu-item index="executions">
        <i class="el-icon-document"></i> <span slot="title">Executions</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>
<script>
export default {
  data() {
    return {};
  },
  methods: {}
};
</script>
<style scoped>
.el-aside {
  background-color: white;
}
img {
  width: 130px;
}
.logo {
  border-bottom: 1px solid rgba(102, 97, 91, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  margin: 0 20px;
}
.el-menu {
  height: 100vh;
}
.el-menu-item.is-active {
  color: #eb5e28;
}
.el-menu-item:focus {
  background-color: transparent;
}
.is-active:before {
  border-right: 17px solid rgb(233, 233, 233);
  border-top: 17px solid transparent;
  border-bottom: 17px solid transparent;
  content: "";
  display: inline-block;
  position: absolute;
  right: -4px;
  top: 12px;
}
</style>
