import React from 'react';

import { connect } from 'react-redux';

import Carousel from 'react-multi-carousel';
import { Card, Form, Col } from 'react-bootstrap';
import 'react-multi-carousel/lib/styles.css';

import { RESPONSIVE_SCREEN } from '../constant';

export class QueueStats extends React.Component {
     render() {
            //Overall Depth, Pending , In progress, processed, Throughput, Error Messages, Oldest item, Throughput, Connected Clients
            let agStats = this.props.stats;
            if(agStats.fetching) {
                return <React.Fragment> Loading Queue Stats </React.Fragment>
            }

            let queueStats = agStats.queueStats;
            let allQueues = Object.keys(queueStats);

            if(allQueues.length === 0 ){
                return <React.Fragment> Queue is empty and so the stats. </React.Fragment>
            }

//depth;
//pending;
//processed;
//rolledBack;

//oldestItem;
//List<String> errors;
//List<String> processing;

            let qCards = allQueues.map(qName => (<Card className="bg-dark text-white">
                                <Card.Body>
                                    <Card.Title>Queue : { qName }</Card.Title>
                                    <Card.Text>
                                        <Form>
                                            <Form.Row>
                                                <Form.Group as={Col}><Form.Label>Depth</Form.Label></Form.Group>
                                                <Form.Group as={Col}><Form.Label>Processed</Form.Label></Form.Group>
                                                <Form.Group as={Col}><Form.Label>Pending</Form.Label></Form.Group>
                                            </Form.Row>
                                            <Form.Row>
                                                <Form.Group as={Col}><Form.Label>{ queueStats[qName].depth }</Form.Label></Form.Group>
                                                <Form.Group as={Col}><Form.Label>{ queueStats[qName].processed }</Form.Label></Form.Group>
                                                <Form.Group as={Col}><Form.Label>{ queueStats[qName].pending }</Form.Label></Form.Group>
                                            </Form.Row>
                                        </Form>
                                    </Card.Text>
                                </Card.Body>
                            </Card>));

            return <Carousel
                     showDots={true}
                     responsive={RESPONSIVE_SCREEN}
                     infinite={true}
                     autoPlaySpeed={300}
                     keyBoardControl={true}
                     transitionDuration={200}
                     containerClass="carousel-container">
                      {qCards}
                   </Carousel>;
        }
}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}

export default connect(mapStateToProps, null) (QueueStats);