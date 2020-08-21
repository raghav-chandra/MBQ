import React from 'react';
import { connect } from 'react-redux';

class Aggregated extends React.Component {
    render() {
        //Overall Depth, Pending , In progress, processed, Throughput, Error Messages, Oldest item, Throughput, Connected Clients
        let agStats = this.props.stats;
        if(agStats.fetching) {
            return <React.Fragment> Loading Aggregated Stats </React.Fragment>
        }

        return <React.Fragment> Aggregated to be calculated </React.Fragment>;
    }

}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}
export default connect(mapStateToProps, null) (Aggregated);