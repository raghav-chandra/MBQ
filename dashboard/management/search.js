import React from 'react';

import { connect } from 'react-redux';

class Search extends React.Component {

    render () {
        return <div>SearchContent in Side Pane</div>;
    }
}

const mapStateToProps = state => {
    return {

    }
};

const mapDispatchToProps = dispatch => {
    return {

    }
};

export default connect(mapStateToProps, mapDispatchToProps) (Search);