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
    padding: '20px',
    backgroundColor:'#e8e8e8'
}

export class Statistics extends React.Component {

    render () {
        return (<React.Fragment>
            <ItemDetailPopup />
            <div style={style}>
                <AggregatedStats />
            </div>
            <div style={style}>
                <QueueStats />
            </div>
            <div style={style}>
                <ClientStats />
            </div>
        </React.Fragment>);
    }
}