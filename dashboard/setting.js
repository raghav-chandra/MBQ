import React from 'react';
import { connect } from 'react-redux';

import { Modal, Button } from 'react-bootstrap';

import { loadSetting } from './redux/actions';

class Setting extends React.Component {

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
                        <pre>This is for setting. Table vs Carousel view, Alert mechanism and settings. </pre>
                    </Modal.Body>
                </Modal>
            </React.Fragment>
        );
    }
}

const mapStateToProps = state => {
    return {
        open : state.loadSetting.open
    };
}

const mapDispatchToProps = dispatch => {
    return {
        close : () => dispatch (loadSetting(false))
    };
}

export default connect(mapStateToProps, mapDispatchToProps) (Setting);