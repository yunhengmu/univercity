/**
 * 文化导览内容管理
 */
const state = {
  // 文化导览应用的 content（来自需求管理）
  culturalContent: null
}

const mutations = {
  // 设置文化导览 content
  SET_CULTURAL_CONTENT(state, content) {
    state.culturalContent = content
  },
  
  // 清除文化导览 content
  CLEAR_CULTURAL_CONTENT(state) {
    state.culturalContent = null
  }
}

const actions = {
  // 应用需求内容
  applyCulturalContent({ commit }, content) {
    commit('SET_CULTURAL_CONTENT', content)
  },
  
  // 取消需求内容
  cancelCulturalContent({ commit }) {
    commit('CLEAR_CULTURAL_CONTENT')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
