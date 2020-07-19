//Contains overall queue Stats
//Depth, processed, etc
import React from 'react';
import { connect } from 'react-redux';

class Aggregated extends React.Component {
    render() {
        //Overall Depth, Pending , In progress, processed, Throughput, Error Messages, Oldest item, Throughput
        let agStats = this.props.stats;
        return (<div> Overall depth, Pending, in Progress, processed, Error messages </div>);
    }

}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}
export default connect(mapStateToProps, null) (Aggregated);