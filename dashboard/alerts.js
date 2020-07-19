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
                    <Alert.Heading>Item for Attention</Alert.Heading>
                    <p>
                        list down all the problems with the queue items
                        That requires attention and may be link attached to that.
                    </p>
                </Alert>
            );
        }
        return  <Alert variant="danger"><Button onClick={() => this.setState({show: true})}>Show Alert</Button></Alert>;
    }
}

const mapStateToProps = state => {
    return {
        show : false
    }
}

export default connect (mapStateToProps, null) (QueueAlert);