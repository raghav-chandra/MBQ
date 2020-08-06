import React from 'react';
import { connect } from 'react-redux';

import AggregatedStats from './aggregated';
import { QueueStats } from './queue';
import { ClientStats } from './client';

export class Statistics extends React.Component {

    render () {
        return (<div>
            <AggregatedStats />
            <QueueStats />
            <ClientStats />
        </div>);
    }
}


export default connect(null, null) (Statistics);