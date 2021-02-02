import { combineReducers } from 'redux';

import { REDUX_ACTIONS } from './actions';

const mbqStats = (state = {fetching: false, stats:{queueStats:{}, clientStats:{}}}, action) => {
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

const queueItem = (state = {item : null, fetching : false, open: false}, action) => {
    switch (action.type) {
        case REDUX_ACTIONS.QUEUE_ITEM :
            return Object.assign({}, state, {fetching: action.fetching, item: action.item, open: action.open});;
        default:
            return state;
    }
}

const setting = (state = {open: false}, action) => {
    switch (action.type) {
        case REDUX_ACTIONS.LOAD_SETTING :
            return Object.assign({}, state, {open: action.open});;
        default:
            return state;
    }
}

export default combineReducers({
    mbqStats,
    searchItems,
    queueItem,
    setting
});