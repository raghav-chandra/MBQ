export const REDUX_ACTIONS = {
    ALL_MBQ_STATS : 'ALL_MBQ_STATS',
    SEARCH_ITEMS : 'SEARCH_ITEMS',
    CHANGE_STATUS : 'CHANGE_STATUS',
    GET_DATA : 'GET_DATA',
    GET_MESSAGES : 'GET_MESSAGES',
    QUEUE_ITEM : 'QUEUE_ITEM',
    LOAD_SETTING : 'LOAD_SETTING'
}

export function mbqStats(stats, fetching = false) {
    return {
        type: REDUX_ACTIONS.ALL_MBQ_STATS,
        stats,
        fetching
    }
}

export function searchItems(items, fetching = false) {
    return {
        type: REDUX_ACTIONS.SEARCH_ITEMS,
        items,
        fetching
    }
}

export function queueItem(item, fetching = false, open = true) {
    return {
        type: REDUX_ACTIONS.QUEUE_ITEM,
        item,
        open,
        fetching
    }
}

export function setting(open = true, interval, dispType) {
    return {
        type: REDUX_ACTIONS.LOAD_SETTING,
        open,
        interval,
        dispType
    }
}