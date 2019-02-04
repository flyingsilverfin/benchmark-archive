<template>
  <el-container class="main-super">
    <side-nav></side-nav>
    <router-view></router-view>
  </el-container>
</template>

<script>
// @ is an alias to /src
import SideNav from "@/components/SideNav.vue";
export default {
  name: "home",
  components: { SideNav }
};
</script>
<style>
.main-super{
  height: 100%;
}
</style>

