import React from 'react';
import { connect } from 'react-redux';

import AggregatedStats from './aggregated';
import QueueStats from './queue';
import ClientStats from './client';

import ItemDetailPopup from '../management/dataModal';

const style = {
    margin :'10px',
    marginBottom :'20px',
    borderRadius: '10px',
    padding: '10px',
    backgroundColor:'#e8e8e8'
}

export class Statistics extends React.Component {

    render () {
        return (<React.Fragment>
            <ItemDetailPopup />
            <div style={style}>
                <span><h5>Aggregated Stats</h5></span>
                <AggregatedStats />
            </div>
            <div style={style}>
                <span><h5>Queue Stats</h5></span>
                <QueueStats />
            </div>
            <div style={style}>
                <span><h5>Client Stats</h5></span>
                <ClientStats />
            </div>
        </React.Fragment>);
    }
}

const mapStatsToProps = state => {
    return {
//        view : state.setting
    };
}

export default connect(mapStatsToProps, null)(Statistics);