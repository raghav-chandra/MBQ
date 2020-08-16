import React from 'react';
import ReactDOM from 'react-dom';
import { Route, Switch, BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'

import {Navbar, Nav, NavDropdown, NavItem} from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';

import { ManagementConsole } from './management/main';
import { Statistics } from './stats/main';
import Alerts from './alerts'
import MBQReducer from './redux/reducers';
import { MBQService } from './mbqService';

import { mbqStats } from './redux/actions';

const store = createStore(MBQReducer, applyMiddleware(thunkMiddleware, createLogger()));

let interval = 0;
console.log(window.location.href)
let websocket = new WebSocket('ws://'+window.location.href.split('/')[2]+'/mbq/ws');
websocket.onmessage = event => {
    let data = JSON.parse(event.data);
    if(data.type === 'ALL_STATS') {
        let stats = data.response;
        store.dispatch(mbqStats(stats));
    }
};
websocket.onopen = () => {
    websocket.send('{"type": "ALL_STATS"}');
    interval = setInterval(() => websocket.send('{"type": "ALL_STATS"}') , 500);
}
websocket.onclose = event => {
    clearInterval(interval);
    alert('Connection to the server is lost. Please refresh.');
};

class MBQApp extends React.Component {
    render() {
        return (<div>
                <Navbar fluid='true' collapseOnSelect expand='lg' bg='dark' variant='dark'>
                    <Navbar.Brand>
                        <LinkContainer to='/'><NavItem>MBQ</NavItem></LinkContainer>
                    </Navbar.Brand>

                    <Navbar.Collapse id='responsive-navbar-nav'>
                        <Nav>
                            <Nav.Link><LinkContainer to='/stats'><NavItem>Statistics</NavItem></LinkContainer></Nav.Link>
                            <Nav.Link><LinkContainer to='/console'><NavItem>Console</NavItem></LinkContainer></Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <Alerts />
                <Routes />
        </div>);
    }
}

class Routes extends React.Component{
    render() {
        return (<Switch>
                    <Route exact path='/'><Statistics /></Route>
                    <Route exact path='/stats'><Statistics /></Route>
                    <Route exact path='/console'><ManagementConsole /></Route>
                </Switch>);
    }
}

ReactDOM.render(<Provider store={store}>
                    <BrowserRouter><MBQApp /></BrowserRouter>
                </Provider>, document.getElementById('app'));