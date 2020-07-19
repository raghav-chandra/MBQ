import React from 'react';

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