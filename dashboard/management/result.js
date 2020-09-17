import React from 'react';
import { connect } from 'react-redux';

import { Table } from 'react-bootstrap';

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
        let items = this.props.items || [];
        let trs = items.map(item => <tr>
                                        <td>{item.id}</td>
                                        <td>{item.queue}</td>
                                        <td>{item.status}</td>
                                        <td>{item.seqKey}</td>
                                        <td>{item.scheduledAt}</td>
                                        <td>{item.createdTimeStamp}</td>
                                        <td>{item.updatedTimeStamp === 0 ? '' : item.updatedTimeStamp}</td>
                                    </tr>);

        return <div style = {{width: this.props.width || '100%', height: this.props.height || '50%'}}>
                    <Table striped bordered hover variant="dark" size="sm">
                        <thead>
                            <tr>
                                <th>Id</th>
                                <th>Queue Name</th>
                                <th>Status</th>
                                <th>Sequence</th>
                                <th>Scheduled At</th>
                                <th>Created At</th>
                                <th>Updated At</th>
                            </tr>
                        </thead>
                        <tbody>
                            {trs}
                        </tbody>
                    </Table>
               </div>;
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