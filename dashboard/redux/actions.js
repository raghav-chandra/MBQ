export const REDUX_ACTIONS = {
    ALL_MBQ_STATS : 'ALL_MBQ_STATS'
}

export function mbqStats(stats, fetching = false) {
    return {
        type: REDUX_ACTIONS.ALL_MBQ_STATS,
        stats,
        fetching
    }
}