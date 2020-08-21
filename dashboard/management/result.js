import React from 'react';
import { connect } from 'react-redux';

const columns = [
    {key: 'id', name: 'Id'},
    {key: 'queue', name: 'Summary'},
    {key: 'status', name: 'Status'},
    {key: 'seqKey', name: 'Sequence Key'},
    {key: 'scheduled', name: 'Scheduled Time'},
    {key: 'created', name: 'Created Time'},
    {key: 'updated', name: 'Updated Time'}
];

class Result extends React.Component {
    render () {
        return <React.Fragment>
                    This is Result Content
               </React.Fragment>;
    }
}
const mapStateToProps = state => {
    return {
        items : state.searchItems.items
    }
};

const mapDispatchToProps = dispatch => {
    return {

    }
};

export default connect(mapStateToProps, mapDispatchToProps) (Result);