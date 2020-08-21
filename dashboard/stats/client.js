import React from 'react';

import { connect } from 'react-redux';
import Carousel from 'react-multi-carousel';
import { Col, Row, Container } from 'react-bootstrap';
import 'react-multi-carousel/lib/styles.css';

import { RESPONSIVE_SCREEN, screen } from '../constant';

import { Statistics } from '../Statistics';

export class ClientStats extends React.Component {

    render() {
        let agStats = this.props.stats;
        
        if(agStats.fetching) {
            return <React.Fragment> Loading Client Stats </React.Fragment>
        }

        let clientStats = agStats.clientStats;
        let allClients = Object.keys(clientStats);

        if( allClients.length === 0 ){
            return <React.Fragment> No active clients of the queue. </React.Fragment>
        }

        let cardWidth = 300;
        let clientCards = allClients.map(id => (
                                <Statistics height='200px' width={cardWidth + 'px'} title={ clientStats[id].name +'(' +clientStats[id].host + ')' }>
                                    <Container>
                                        <Row>
                                            <Col>InProcess</Col>
                                            <Col>{ clientStats[id].processing.length }</Col>
                                        </Row>
                                        <Row>
                                            <Col>Processed</Col>
                                            <Col>{ clientStats[id].completed }</Col>
                                        </Row>
                                        <Row>
                                            <Col>Pushed</Col>
                                            <Col>{ clientStats[id].pushed }</Col>
                                        </Row>
                                        <Row>
                                            <Col>Error</Col>
                                            <Col>{ clientStats[id].markedError }</Col>
                                        </Row>
                                        <Row>
                                            <Col>Held</Col>
                                            <Col>{ clientStats[id].markedHeld }</Col>
                                        </Row>
                                        <Row>
                                            <Col>Oldest</Col>
                                            <Col>Item</Col>
                                        </Row>
                                    </Container>
                                 </Statistics>));

        let count = parseInt(window.innerWidth/(cardWidth+10));
        return <Carousel
                 showDots={true}
                 responsive={screen(count)}
                 infinite={true}
                 autoPlaySpeed={300}
                 keyBoardControl={true}
                 transitionDuration={100}
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