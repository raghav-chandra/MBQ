export const REDUX_ACTIONS = {
    ALL_MBQ_STATS : 'ALL_MBQ_STATS',
    SEARCH_ITEMS : 'SEARCH_ITEMS'
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