import React from 'react';
import { connect } from 'react-redux';

import { Modal, Button, Form, Col, Row, ButtonGroup} from 'react-bootstrap';

import { setting } from './redux/actions';

class Setting extends React.Component {

    constructor(props) {
        super(props);
        let type = 'CAR', interval = 10000;
        try {
            type = localStorage.getItem('statsType');
            interval = localStorage.getItem('interval');
        } catch(e){
        }

        this.state = { type, interval: parseInt(interval) };
        this.setSetting = this.setSetting.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.save = this.save.bind(this);
    }

    setSetting(setting, value) {
        return (e) => this.setState({[setting]: value});
    }

    handleChange(e) {
        let interval = parseInt(Math.abs(e.target.value)/1000);
        this.setState({interval});
    }

    save(e) {
        let interval = localStorage.getItem('interval');
        let type = localStorage.getItem('statsType');

        localStorage.setItem('interval',this.state.interval);
        localStorage.setItem('statsType',this.state.type);

        if(interval !== this.state.interval) {
            //reset interval
        }

        if(type !== this.state.type) {
            //Change Type
        }
        console.log(this.state);
    }

    render() {
        if(!this.props.open) {
            return (<React.Fragment />);
        }

        let type = localStorage.getItem('statsType');

        return (
            <React.Fragment>
                <Modal show={this.props.open} onHide={this.props.close} keyboard={false}>
                    <Modal.Header closeButton>
                        <Modal.Title>Stats Settings</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Form>
                            <Form.Group as={Row}>
                                <Form.Label column sm="3">Stats Display</Form.Label>
                                <Col sm="9">
                                    <ButtonGroup>
                                        <Button variant="secondary" onClick={this.setSetting('type', 'CAR')}> Carousel </Button>
                                        <Button variant="secondary" onClick={this.setSetting('type', 'TAB')}> Table </Button>
                                    </ButtonGroup>
                                </Col>
                            </Form.Group>
                            <Form.Group as={Row}>
                                <Form.Label column sm="3">Refresh Interval</Form.Label>
                                <Col sm="9">
                                    <Form.Control type='number' name='internal' onChange = {this.handleChange} placeholder='In +ve Seconds' />
                                </Col>
                            </Form.Group>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="dark" onClick={this.save}>Save</Button>
                        <Button variant="secondary" onClick={this.props.close}>Cancel</Button>
                    </Modal.Footer>
                </Modal>
            </React.Fragment>
        );
    }
}

const mapStateToProps = state => {
    return {
        open : state.setting.open
    };
}

const mapDispatchToProps = dispatch => {
    return {
        close : () => dispatch (setting(false))
    };
}

export default connect(mapStateToProps, mapDispatchToProps) (Setting);