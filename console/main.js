import React from 'react';
import ReactDOM from 'react-dom';
import {App} from './app';

const title = 'MBQ Console';

class Header extends React.Component {
    render() {
        return (<div>{title}</div>);
    }
}

class Console extends React.Component {
    render() {
        return (<div>
            <Header />
            <App />
        </div>);
    }
}

ReactDOM.render(<div>{title}</div>, document.getElementById('app'));
