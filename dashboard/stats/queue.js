import React from 'react';

import { connect } from 'react-redux';

import Carousel from 'react-multi-carousel';
import { Card } from 'react-bootstrap';
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

//private int depth;
//private int pending;
//private int processed;
//private int rolledBack;
//private IdSeqKey oldestItem;
//
//private final String queueName;
//private final List<String> errors;
//private final List<String> processing;

            let qCards = allQueues.map(qName => (<Card className="bg-dark text-white">
                                <Card.Body>
                                    <Card.Title>Queue : { qName }</Card.Title>
                                    <Card.Text>
                                        Some quick example text to build
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