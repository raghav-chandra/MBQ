import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import { Route, Switch, BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'

import {Navbar, Nav, NavDropdown, NavItem} from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCogs, faCog } from "@fortawesome/free-solid-svg-icons";

import { ManagementConsole } from './management/main';
import Statistics from './stats/main';
import Alerts from './alerts'
import Settings from './setting'
import MBQReducer from './redux/reducers';
import { MBQService } from './mbqService';
import { mbqStats, setting } from './redux/actions';

const store = createStore(MBQReducer, applyMiddleware(thunkMiddleware, createLogger()));

let interval = 0;
const statsReq = '{"type": "ALL_STATS"}';
const websocket = new WebSocket('ws://'+window.location.href.split('/')[2]+'/mbq/ws');

websocket.onmessage = event => {
    let data = JSON.parse(event.data);
    if(data.type === 'ALL_STATS') {
        let stats = data.response;
        store.dispatch(mbqStats(stats));
    }
};
websocket.onopen = () => {
    websocket.send(statsReq);
    setRefresh(10000);
}
websocket.onclose = event => {
    clearInterval(interval);
    alert('Connection to the server is lost. Please refresh.');
};

export function setRefresh(seconds) {
    clearInterval(interval);
    interval = setInterval(() => websocket.send(statsReq) , seconds);
}

class MBQApp extends React.Component {
    render() {
        return (<div>
                <Navbar fluid='true' collapseOnSelect expand='lg' bg='dark' variant='dark'>
                    <Navbar.Brand>
                        <LinkContainer to='/'><NavItem>MBQ</NavItem></LinkContainer>
                    </Navbar.Brand>

                    <Navbar.Collapse id='responsive-navbar-nav'>
                        <Nav className="mr-auto">
                            <Nav.Link><LinkContainer to='/'><NavItem>Statistics</NavItem></LinkContainer></Nav.Link>
                            <Nav.Link><LinkContainer to='/console'><NavItem>Console</NavItem></LinkContainer></Nav.Link>
                        </Nav>
                        <Nav>
                            <Navbar.Text>Last Updated : {new Date().toDateString() }</Navbar.Text>
                            <Nav.Link onClick = {e => this.props.loadSetting()}><FontAwesomeIcon pulse inverse icon={faCog} /></Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <Alerts />
                <Settings />
                <Routes />
        </div>);
    }
}

const mapDispatchToProps = dispatch => {
    return {
        loadSetting  : () => dispatch(setting())
    }
}

const App = connect(null, mapDispatchToProps)(MBQApp);

class Routes extends React.Component{
    render() {
        return (<Switch>
                    <Route exact path='/'><Statistics /></Route>
                    <Route exact path='/console'><ManagementConsole /></Route>
                </Switch>);
    }
}

ReactDOM.render(<Provider store={store}>
                    <BrowserRouter><App /></BrowserRouter>
                </Provider>, document.getElementById('app'));