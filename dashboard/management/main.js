import React from 'react';

import Search from './search';
import Result from './result';
import ItemDetailPopup from './dataModal';

export class ManagementConsole extends React.Component {

    render () {
        return (
            <React.Fragment>
                <Search />
                <Result />
                <ItemDetailPopup />
            </React.Fragment>);
    }
}