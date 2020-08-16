import React from 'react';
import { connect } from 'react-redux';

import AggregatedStats from './aggregated';
import QueueStats from './queue';
import ClientStats from './client';

const style = {
    margin :'10px',
    marginBottom :'20px'
}
export class Statistics extends React.Component {

    render () {
        return (<React.Fragment>
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