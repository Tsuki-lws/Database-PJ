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
          path: 'questions/uncategorized',
          name: 'UncategorizedQuestions',
          component: () => import('../views/questions/UncategorizedList.vue')
        },
        {
          path: 'questions/without-answer',
          name: 'QuestionsWithoutAnswer',
          component: () => import('../views/questions/WithoutAnswer.vue')
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
        },
        {
          path: 'questions/detail/:id',
          name: 'QuestionDetail',
          component: () => import('../views/questions/Detail.vue')
        },
        {
          path: 'answers',
          name: 'Answers',
          component: () => import('../views/answers/List.vue')
        },
        {
          path: 'answers/model',
          name: 'ModelAnswers',
          component: () => import('../views/models/ModelAnswers.vue')
        },
        {
          path: 'model-answers',
          name: 'ModelAnswersRoot',
          redirect: '/answers/model'
        },
        {
          path: 'model-answers/import',
          name: 'ImportModelAnswer',
          component: () => import('../views/import/ImportModelAnswer.vue')
        },
        {
          path: 'model-answers/dataset/:id',
          name: 'DatasetQuestions',
          component: () => import('../views/models/DatasetQuestions.vue')
        },
        {
          path: 'model-answers/dataset/:datasetId/question/:questionId',
          name: 'QuestionAnswers',
          component: () => import('../views/models/QuestionAnswers.vue')
        },
        {
          path: 'model-answers/evaluation',
          name: 'ImportModelEvaluation',
          component: () => import('../views/import/ImportModelEvaluation.vue')
        },
        {
          path: 'models',
          name: 'Models',
          component: () => import('../views/models/List.vue')
        },
        {
          path: 'answers/create/:questionId',
          name: 'CreateAnswer',
          component: () => import('../views/answers/Create.vue')
        },
        {
          path: 'answers/edit/:id',
          name: 'EditAnswer',
          component: () => import('../views/answers/Edit.vue')
        },
        {
          path: 'crowdsourcing',
          name: 'CrowdsourcingTasks',
          component: () => import('../views/crowdsourcing/TaskList.vue')
        },
        {
          path: 'crowdsourcing/create',
          name: 'CreateCrowdsourcingTask',
          component: () => import('../views/crowdsourcing/CreateTask.vue')
        },
        {
          path: 'crowdsourcing/detail/:id',
          name: 'CrowdsourcingTaskDetail',
          component: () => import('../views/crowdsourcing/TaskDetail.vue')
        },
        {
          path: 'crowdsourcing/answers/:taskId',
          name: 'CrowdsourcingAnswers',
          component: () => import('../views/crowdsourcing/AnswerList.vue')
        },
        {
          path: 'crowdsourcing/collection/:id',
          name: 'CrowdsourcingCollection',
          component: () => import('../views/crowdsourcing/AnswerCollection.vue')
        },
        {
          path: 'datasets',
          name: 'Datasets',
          component: () => import('../views/datasets/List.vue')
        },
        {
          path: 'datasets/create',
          name: 'CreateDataset',
          component: () => import('../views/datasets/Create.vue')
        },
        {
          path: 'datasets/detail/:id',
          name: 'DatasetDetail',
          component: () => import('../views/datasets/Detail.vue')
        },
        {
          path: 'evaluations',
          name: 'Evaluations',
          component: () => import('../views/evaluations/List.vue')
        },
        {
          path: 'evaluations/create',
          name: 'CreateEvaluation',
          component: () => import('../views/evaluations/Create.vue')
        },
        {
          path: 'evaluations/detail/:id',
          name: 'EvaluationDetail',
          component: () => import('../views/evaluations/Detail.vue')
        },
        {
          path: 'evaluations/results/:batchId',
          name: 'EvaluationResults',
          component: () => import('../views/evaluations/Results.vue')
        },
        {
          path: 'evaluations/result/:id',
          name: 'EvaluationResult',
          component: () => import('../views/evaluations/EvaluationResult.vue')
        },
        {
          path: 'evaluations/model',
          name: 'ModelEvaluations',
          component: () => import('../views/evaluations/ModelEvaluations.vue')
        },
        {
          path: 'evaluations/results',
          name: 'AllEvaluationResults',
          component: () => import('../views/evaluations/AllResults.vue')
        },
        {
          path: 'evaluations/question/:questionId',
          name: 'QuestionEvaluations',
          component: () => import('../views/evaluations/QuestionEvaluations.vue')
        },
        {
          path: 'evaluations/manual',
          name: 'ManualEvaluation',
          component: () => import('../views/evaluations/ManualEvaluation.vue')
        },
        {
          path: 'import/evaluation',
          name: 'ImportEvaluation',
          component: () => import('../views/import/ImportEvaluation.vue')
        },
        {
          path: 'import/model-answer',
          redirect: '/model-answers/import'
        },
        {
          path: 'import/model-evaluation',
          redirect: '/model-answers/evaluation'
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('../views/categories/List.vue')
        },
        {
          path: 'tags',
          name: 'Tags',
          component: () => import('../views/tags/List.vue')
        },
        {
          path: 'raw-questions',
          name: 'RawQuestions',
          component: () => import('../views/raw/QuestionList.vue')
        },
        {
          path: 'raw-questions/detail/:id',
          name: 'RawQuestionDetail',
          component: () => import('../views/raw/QuestionDetail.vue')
        },
        {
          path: 'raw-answers',
          name: 'RawAnswers',
          component: () => import('../views/raw/AnswerList.vue')
        },
        {
          path: 'import/raw',
          name: 'ImportRaw',
          component: () => import('../views/import/ImportRaw.vue')
        },
        {
          path: 'import/standard',
          name: 'ImportStandard',
          component: () => import('../views/import/ImportStandard.vue')
        },
        {
          path: 'import',
          name: 'Import',
          component: () => import('../views/import/Import.vue')
        }
      ]
    }
  ]
})

export default router 