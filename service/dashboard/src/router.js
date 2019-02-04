import Vue from "vue";
import Router from "vue-router";
import Home from "./views/Home.vue";
import Overview from "./views/overview/Overview.vue";
import Executions from "./views/executions/Executions.vue";

Vue.use(Router);

export default new Router({
  routes: [
    { path: "/", redirect: "/overview" },
    {
      path: "/",
      name: "home",
      component: Home,
      children: [
        {
          path: "overview",
          component: Overview
        },
        {
          path: "executions",
          component: Executions
        }
      ]
    },
    {
      path: "/about",
      name: "about",
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () =>
        import(/* webpackChunkName: "about" */ "./views/About.vue")
    }
  ]
});
