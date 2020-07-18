import React from 'react';
import ReactDOM from 'react-dom';
import { Route, Switch, BrowserRouter } from 'react-router-dom';
import {Navbar, Nav, NavDropdown, NavItem} from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';

import {ManagementConsole} from './management/main';
import {Statistics} from './stats/main';

class MBQApp extends React.Component {
    render() {
        return (<div>
                <Navbar collapseOnSelect expand='lg' bg='dark' variant='dark'>
                    <Navbar.Brand>
                        <LinkContainer to="/stats"><NavItem>MBQ</NavItem></LinkContainer>
                    </Navbar.Brand>

                    <Navbar.Collapse id='responsive-navbar-nav'>
                        <Nav>
                            <LinkContainer to="/stats"><NavItem>Statistics</NavItem></LinkContainer>
                            <LinkContainer to="/console"><NavItem>Console</NavItem></LinkContainer>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>
                <Routes />
                </div>);
    }
}

class Routes extends React.Component{
    render() {
        return (<Switch>
                    <Route exact path='/stats'><Statistics /></Route>
                    <Route exact path='/console'><ManagementConsole /></Route>
                </Switch>);
    }
}

ReactDOM.render(<BrowserRouter><MBQApp /></BrowserRouter>, document.getElementById('app'));