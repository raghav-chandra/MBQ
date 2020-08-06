//Contains overall queue Stats
//Depth, processed, etc
import React from 'react';
import { connect } from 'react-redux';

import Carousel from 'react-multi-carousel';
import { Card } from 'react-bootstrap';
import 'react-multi-carousel/lib/styles.css';
const responsive = {
  superLargeDesktop: {
    // the naming can be any, depends on you.
    breakpoint: { max: 4000, min: 3000 },
    items: 5
  },
  desktop: {
    breakpoint: { max: 3000, min: 1024 },
    items: 3
  },
  tablet: {
    breakpoint: { max: 1024, min: 464 },
    items: 2
  },
  mobile: {
    breakpoint: { max: 464, min: 0 },
    items: 1
  }
};


class Aggregated extends React.Component {
    render() {
        //Overall Depth, Pending , In progress, processed, Throughput, Error Messages, Oldest item, Throughput, Connected Clients
        let agStats = this.props.stats;
        return <Carousel
                 showDots={true}
                 responsive={responsive}
                 infinite={true}
                 autoPlaySpeed={1000}
                 keyBoardControl={true}
                 customTransition="all .5"
                 transitionDuration={500}
                 containerClass="carousel-container"
                 deviceType={'desktop'}
                 >
                  <Card style={{ width: '900px' }}>
                                         <Card.Body>
                                           <Card.Title>Card Title 1111</Card.Title>
                                           <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                                           <Card.Text>
                                             Some quick example text to build on the card title and make up the bulk of
                                             the card's content.
                                           </Card.Text>
                                           <Card.Link href="#">Card Link</Card.Link>
                                           <Card.Link href="#">Another Link</Card.Link>
                                         </Card.Body>
                                       </Card>

                                        <Card style={{ width: '900px' }}>
                                                               <Card.Body>
                                                                 <Card.Title>Card Title 2222</Card.Title>
                                                                 <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                                                                 <Card.Text>
                                                                   Some quick example text to build on the card title and make up the bulk of
                                                                   the card's content.
                                                                 </Card.Text>
                                                                 <Card.Link href="#">Card Link</Card.Link>
                                                                 <Card.Link href="#">Another Link</Card.Link>
                                                               </Card.Body>
                                                             </Card>
                                                              <Card style={{ width: '900px' }}>
                                                                                     <Card.Body>
                                                                                       <Card.Title>Card Title 3333</Card.Title>
                                                                                       <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                                                                                       <Card.Text>
                                                                                         Some quick example text to build on the card title and make up the bulk of
                                                                                         the card's content.
                                                                                       </Card.Text>
                                                                                       <Card.Link href="#">Card Link</Card.Link>
                                                                                       <Card.Link href="#">Another Link</Card.Link>
                                                                                     </Card.Body>
                                                                                   </Card>

                 <Card style={{ width: '900px' }}>
                        <Card.Body>
                          <Card.Title>Card Title 1</Card.Title>
                          <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                          <Card.Text>
                            Some quick example text to build on the card title and make up the bulk of
                            the card's content.
                          </Card.Text>
                          <Card.Link href="#">Card Link</Card.Link>
                          <Card.Link href="#">Another Link</Card.Link>
                        </Card.Body>
                      </Card>
                      <Card style={{ width: '900px' }}>
                         <Card.Body>
                           <Card.Title>Card Title2</Card.Title>
                           <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                           <Card.Text>
                             Some quick example text to build on the card title and make up the bulk of
                             the card's content.
                           </Card.Text>
                           <Card.Link href="#">Card Link</Card.Link>
                           <Card.Link href="#">Another Link</Card.Link>
                         </Card.Body>
                       </Card>
                       <Card style={{ width: '900px' }}>
                        <Card.Body>
                          <Card.Title>Card Title3</Card.Title>
                          <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                          <Card.Text>
                            Some quick example text to build on the card title and make up the bulk of
                            the card's content.
                          </Card.Text>
                          <Card.Link href="#">Card Link</Card.Link>
                          <Card.Link href="#">Another Link</Card.Link>
                        </Card.Body>
                      </Card>
                 <Card style={{ width: '900px' }}>
                        <Card.Body>
                          <Card.Title>Card Title4</Card.Title>
                          <Card.Subtitle className="mb-2 text-muted">Card Subtitle</Card.Subtitle>
                          <Card.Text>
                            Some quick example text to build on the card title and make up the bulk of
                            the card's content.
                          </Card.Text>
                          <Card.Link href="#">Card Link</Card.Link>
                          <Card.Link href="#">Another Link</Card.Link>
                        </Card.Body>
                      </Card>
               </Carousel>;
    }

}

const mapStateToProps = state => {
    return {
        stats : state.mbqStats.stats
    }
}
export default connect(mapStateToProps, null) (Aggregated);