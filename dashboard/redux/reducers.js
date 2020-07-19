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

export default combineReducers({
    mbqStats
});