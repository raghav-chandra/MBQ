import React from 'react';
import { connect } from 'react-redux';

import { MBQService } from '../mbqService';

class Metric extends React.Component {

    render() {
        return (<div className='stats' style = {{width: '120px', height: '80px', margin: '10px', float: 'left'}}>
                    {this.props.title ? <h6 className='stats-title'>{ this.props.title }</h6> : ''}
                    <div className='stats-body'>
                        <span> {this.props.metric} </span>
                    </div>
                </div>);
    }
}

class Aggregated extends React.Component {
    render() {
        //Overall Depth, Pending , In progress, processed, Throughput, Error Messages, Oldest item, Throughput, Connected Clients
        let stats = this.props.stats;

        if(stats.fetching || !stats.queueStats) {
            return <React.Fragment> Loading Aggregated Stats </React.Fragment>
        }

        let qStats = stats.queueStats;

        let activeClients = Object.keys(stats.clientStats).length;
        let agg = { activeClients, depth: 0, pend: 0, inProgress: 0, processed: 0, error: 0, throughput: 0 };
        let pending = [], errors = [];
        Object.keys(qStats).forEach(q => {
            agg.depth += qStats[q].depth;
            agg.pend += qStats[q].pending;
            agg.inProgress += qStats[q].processing.length;
            agg.processed += qStats[q].processed;
            agg.error += qStats[q].errors.length;
            errors = errors.concat(qStats[q].errors);
            pending = pending.concat(qStats[q].processing);
        });

        let metric = [];
        let inProgress = agg.inProgress == 0 ? 0 : <a href='#' onClick = {e=>this.props.showData(pending)}>{agg.inProgress}</a>;
        let inErr = agg.error == 0 ? 0 : <a href='#' onClick = {e=>this.props.showData(pending)}>{agg.error}</a>;
        metric.push(<Metric title='Depth' metric= {agg.depth}/>);
        metric.push(<Metric title='Pending' metric= {agg.pend}/>);
        metric.push(<Metric title='In Progress' metric= {inProgress}/>);
        metric.push(<Metric title='Processed' metric= {agg.processed}/>);
        metric.push(<Metric title='Errored' metric= {inErr}/>);
        metric.push(<Metric title='Active Clients' metric= {activeClients}/>);


        return (<div style = {{height:'100px'}}>{metric}</div>);
    }

}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}

const mapDispatchToProps = dispatch => {
    return {
        showData : ids => dispatch(MBQService.getItems(ids))
    }
}

export default connect(mapStateToProps, mapDispatchToProps) (Aggregated);