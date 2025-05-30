import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/Layout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('../views/Home.vue')
        },
        {
          path: 'questions',
          name: 'Questions',
          component: () => import('../views/questions/List.vue')
        },
        {
          path: 'questions/create',
          name: 'CreateQuestion',
          component: () => import('../views/questions/Create.vue')
        },
        {
          path: 'questions/edit/:id',
          name: 'EditQuestion',
          component: () => import('../views/questions/Edit.vue')
        }
      ]
    }
  ]
})

export default router 