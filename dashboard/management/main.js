import React from 'react';

import Search from './search';
import Result from './result';

export class ManagementConsole extends React.Component {

    render () {
        return (
            <React.Fragment>
                <Search />
                <Result />
            </React.Fragment>);
    }
}