import { combineReducers } from 'redux';

import { REDUX_ACTIONS } from './actions';

const mbqStats = (state = {}, action) => {
  switch (action.type) {
    case REDUX_ACTIONS.ALL_MBQ_STATS :
      return Object.assign({},state,{fetching:action.fetching, stats:action.stats});;
    default:
      return state;
  }
};

const searchItems = (state = {items : [], fetching : false}, action) => {
    switch (action.type) {
        case REDUX_ACTIONS.SEARCH_ITEMS :
        return Object.assign({}, state, {fetching: action.fetching, items: action.items});;
    default:
        return state;
    }
}

export default combineReducers({
    mbqStats,
    searchItems
});