import Vue from 'vue'
import Vuex from 'vuex'
import user from '@/store/modules/user'
import cultural from '@/store/modules/cultural'
import getters from './getters'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    user,
    cultural
  },
  getters
})

export default store
