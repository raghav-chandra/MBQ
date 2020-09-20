import React from 'react';
import { connect } from 'react-redux';

import { Table } from 'react-bootstrap';

class Result extends React.Component {

    constructor(props) {
        super(props);
        this.state = {key: 'id', prevKey: '', dir: 'A'};
        this.renderItems = this.renderItems.bind(this);
    }

    renderItems(key) {
        let dir =  this.state.key === key ? this.state.dir === 'D' ? 'A' : 'D' : 'A';
        this.setState({key, dir});
    }

    static getDerivedStateFromProps(props, state) {
        let items = props.items;
        let key = state.key;
        let dir = state.dir;
        if(dir === 'A') {
            items.sort((a,b) => a[key] > b[key] ? 1 : -1)
        } else {
            items.sort((a,b) => a[key] < b[key] ? 1 : -1)
        }
        return {items, key: state.key, dir};
    }

    render () {
        let items = this.state.items
        console.log(this.state.key);
        console.log(items);
        let trs = items.map(item => <tr>
                                        <td>{item.id}</td>
                                        <td>{item.queue}</td>
                                        <td>{item.status}</td>
                                        <td>{item.seqKey}</td>
                                        <td>{item.scheduledAt}</td>
                                        <td>{item.createdTimeStamp}</td>
                                        <td>{item.updatedTimeStamp === 0 ? '' : item.updatedTimeStamp}</td>
                                    </tr>);

        return <div style = {{width: this.props.width || '100%', height: this.props.height || '100%'}}>
                    <h5> Total Items found : {items.length}</h5>
                    <table className='table table-dark table-sm table-striped table-bordered table-hover'>
                        <thead>
                            <tr>
                                <th onClick={e=>this.renderItems('id')}>Id</th>
                                <th onClick={e=>this.renderItems('queue')}>Queue Name</th>
                                <th onClick={e=>this.renderItems('status')}>Status</th>
                                <th onClick={e=>this.renderItems('seqKey')}>Sequence</th>
                                <th onClick={e=>this.renderItems('scheduledAt')}>Scheduled At</th>
                                <th onClick={e=>this.renderItems('createdTimeStamp')}>Created At</th>
                                <th onClick={e=>this.renderItems('updatedTimeStamp')}>Updated At</th>
                            </tr>
                        </thead>
                        <tbody>
                            {trs}
                        </tbody>
                    </table>
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