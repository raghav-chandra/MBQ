import React from 'react';

import { connect } from 'react-redux';

import Carousel from 'react-multi-carousel';
import { Col, Row, Container } from 'react-bootstrap';
import 'react-multi-carousel/lib/styles.css';

import { MBQService } from '../mbqService';
import { RESPONSIVE_SCREEN, screen } from '../constant';

import { Card } from '../util/Card';

export class QueueStats extends React.Component {

     render() {
            let stats = this.props.stats;
            if(stats.fetching) {
                return <React.Fragment> Loading Queue Stats </React.Fragment>
            }

            let queueStats = stats.queueStats;
            let allQueues = Object.keys(queueStats);

            if(allQueues.length === 0 ){
                return <React.Fragment> Queue is empty and so the stats. </React.Fragment>
            }

            let cardWidth = 270;
            let qCards = allQueues.map(qName => (
                            <Card height='230px' width={cardWidth+'px'} title={ qName }>
                                <Container>
                                    <Row>
                                        <Col>Depth</Col>
                                        <Col>{ queueStats[qName].depth }</Col>
                                    </Row>
                                    <Row>
                                        <Col>Pending</Col>
                                        <Col>{ queueStats[qName].pending }</Col>
                                    </Row>
                                    <Row>
                                        <Col>InProcess</Col>
                                        <Col>{ queueStats[qName].processing.length }</Col>
                                    </Row>
                                    <Row>
                                        <Col>Processed</Col>
                                        <Col>{ queueStats[qName].processed }</Col>
                                    </Row>
                                    <Row>
                                        <Col>Error</Col>
                                        <Col>{ queueStats[qName].errors.length }</Col>
                                    </Row>
                                    <Row>
                                        <Col>RolledBack</Col>
                                        <Col>{ queueStats[qName].rolledBack }</Col>
                                    </Row>
                                    <Row>
                                        <Col>Oldest</Col>
                                        <Col>{queueStats[qName].oldestItem ? <a href='#' onClick = {e=>this.props.showData(queueStats[qName].oldestItem && queueStats[qName].oldestItem.id)}>Detail</a> : 'None'}</Col>
                                    </Row>
                                </Container>
                             </Card>));

            let count = parseInt(window.innerWidth/(cardWidth+10));
            return <Carousel
                     showDots={true}
                     responsive={screen(count)}
                     infinite={true}
                     autoPlaySpeed={300}
                     keyBoardControl={true}
                     transitionDuration={100}
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

const mapDispatchToProps = dispatch => {
    return {
        showData : id => dispatch(MBQService.getData(id))
    }
}

export default connect(mapStateToProps, mapDispatchToProps) (QueueStats);