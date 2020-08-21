import React from 'react';
import { connect } from 'react-redux';

import { Alert, Button } from 'react-bootstrap';

class QueueAlert extends React.Component {
    constructor(props) {
        super(props);
        this.state = {show : this.props.show}
    }
    render () {
        if (this.state.show) {
            return (
                <Alert variant="danger" onClose={() => this.setState({show: false})} dismissible>
                    Anything here is an alert
                </Alert>
            );
        }
        return  <Alert variant="danger" show = {false}><Button onClick={() => this.setState({show: false})}>Show Alert</Button></Alert>;
    }
}

const mapStateToProps = state => {
    return {
        show : false/*,
        alerts : stats.alerts.items*/
    }
}

export default connect (mapStateToProps, null) (QueueAlert);