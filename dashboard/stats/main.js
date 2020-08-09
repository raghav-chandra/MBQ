import React from 'react';
import { connect } from 'react-redux';

import AggregatedStats from './aggregated';
import QueueStats from './queue';
import ClientStats from './client';

export class Statistics extends React.Component {

    render () {
        return (<React.Fragment>
            <AggregatedStats />
            <QueueStats />
            <ClientStats />
        </React.Fragment>);
    }
}