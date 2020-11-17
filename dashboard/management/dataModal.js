import React from 'react';
import { connect } from 'react-redux';

import { Modal, Button } from 'react-bootstrap';

import { queueItem } from '../redux/actions';

class QueueItem extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        if(!this.props.open) {
            return (<React.Fragment />);
        }

        return (
            <React.Fragment>
                <Modal show={this.props.open} onHide={this.props.close} keyboard={false}>
                    <Modal.Header closeButton>
                        <Modal.Title>Detail</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <pre>{JSON.stringify(this.props.data, null, 50)}</pre>
                    </Modal.Body>
                </Modal>
            </React.Fragment>
        );
    }
}

const mapStateToProps = state => {
    return {
        open : state.queueItem.open,
        data : state.queueItem.item
    };
}

const mapDispatchToProps = dispatch => {
    return {
        close : () => dispatch (queueItem(null, false, false))
    };
}

export default connect(mapStateToProps, mapDispatchToProps) (QueueItem);