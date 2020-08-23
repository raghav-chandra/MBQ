import React from 'react';
import { connect } from 'react-redux';

import { Accordion, Card, Form, Button, Col, Row } from 'react-bootstrap';

import { MBQService } from '../mbqService';
import { searchItems } from '../redux/actions';

const VALID_STATUS = ['COMPLETED', 'PENDING', 'ERROR', 'HELD'];

class Search extends React.Component {

    constructor(props) {
        super(props);
        this.state = {ids: [], sequence: null, status: null};
        this.handleChange = this.handleChange.bind(this);
        this.submit = this.submit.bind(this);
    }

    handleChange(e) {
        let state = this.state;
        let val = e.target.value;
        if(e.target.name === 'ids') {
            state[e.target.name] = val && val.trim() && val.split(',') || [];
        } else {
            state[e.target.name] = e.target.value;
        }
        this.setState(state);
    }

    submit(e) {
        let state = this.state;
        let req = {};
        if(state.ids && state.ids.length) {
            req.ids = state.ids;
        }

        if(state.status && VALID_STATUS.indexOf(state.status) > -1) {
            req.status = state.status;
        }

        if(state.sequence && sequence.length) {
            req.sequence = state.sequence;
        }
        console.log(req);
        if(Object.keys(req).length) {
            this.props.searchItems(req);
        } else {
            alert('Request is invalid');
        }
    }

    render () {
        return <React.Fragment>
                    <Accordion defaultActiveKey='0'>
                        <Card>
                            <Accordion.Toggle as={Card.Header} eventKey='0'>>></Accordion.Toggle>
                            <Accordion.Collapse eventKey='0'>
                                <Card.Body>
                                    <Form>
                                        <Form.Group>
                                            <Form.Control type='text' name='ids' onChange = {this.handleChange} placeholder='Comma separated Ids' />
                                        </Form.Group>
                                        <Form.Group as={Row}>
                                            <Col sm='2'>
                                                <Form.Control as='select' name='status'>
                                                    <option value=''>Select Status</option>
                                                    <option value='PENDING'>PENDING</option>
                                                    <option value='ERROR'>ERROR</option>
                                                    <option value='HELD'>HELD</option>
                                                    <option value='COMPLETED'>COMPLETED</option>
                                                </Form.Control>
                                            </Col>
                                            <Col sm='4'>
                                                <Form.Control type='text' name='sequence' onChange = {this.handleChange} placeholder='Sequence Key' />
                                            </Col>
                                            <Col sm='1'>
                                                <Button variant="dark" onClick={this.submit}>Search</Button>
                                            </Col>

                                        </Form.Group>
                                    </Form>
                                </Card.Body>
                            </Accordion.Collapse>
                        </Card>
                    </Accordion>
                </React.Fragment>;
    }
}

const mapDispatchToProps = dispatch => {
    return {
        searchItems : data => {
            dispatch(searchItems([], true));
            dispatch(MBQService.searchItems(data));
        }
    }
};

export default connect(null, mapDispatchToProps) (Search);