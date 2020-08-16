import React from 'react';

import { connect } from 'react-redux';
import Carousel from 'react-multi-carousel';
import { Card } from 'react-bootstrap';
import 'react-multi-carousel/lib/styles.css';

import { RESPONSIVE_SCREEN } from '../constant';

export class ClientStats extends React.Component {
     render() {
            let agStats = this.props.stats;
            if(agStats.fetching) {
                return <React.Fragment> Loading Client Stats </React.Fragment>
            }

            let clientStats = agStats.clientStats;
            let allClients = Object.keys(clientStats);

            if(allClients.length === 0 ){
                return <React.Fragment> No active clients of the queue. </React.Fragment>
            }

            let clientCards = allClients.map(qName => (<Card className="bg-dark text-white">
                                <Card.Body>
                                    <Card.Title>ClientId : { qName }</Card.Title>
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
                     {clientCards}
                   </Carousel>;
        }
}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}

export default connect(mapStateToProps, null) (ClientStats);