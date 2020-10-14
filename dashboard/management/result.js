import React from 'react';
import { connect } from 'react-redux';

import { BootstrapTable }  from 'react-bootstrap-table';
import 'react-bootstrap-table/css/react-bootstrap-table.css';

import { Table, DropdownButton, Dropdown } from 'react-bootstrap';

import { MBQService } from '../mbqService';

class Result extends React.Component {

    constructor(props) {
        super(props);
        this.state = {key: 'id', prevKey: '', dir: 'A', selected:[]};
        this.renderItems = this.renderItems.bind(this);
        this.handleSelectAll = this.handleSelectAll.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
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

    handleSelect(row, isSelected) {
        let selected = this.state.selected;
        if (isSelected) {
            selected.push(row.id);
        } else {
            const index = selected.indexOf(row.id);
            if (index > -1) {
                selected.splice(index, 1);
            }
        }
        this.setState({selected});
    }

    handleSelectAll(isSelected, rows) {
        if (isSelected) {
            let ids = rows.map(row=> row.id);
            this.setState({selected: ids});
        } else {
            this.setState({selected: []});
        }
    }

    updateStatus(status) {
        return (e) => {
            let selected = this.state.selected;
            if (selected.length) {
                let conf = confirm('Total no of Items to be marked ' + status + ' is ' + selected.length);
                if(conf) {
                    this.props.updateStatus(selected, status);
                }
            } else {
                alert('Please select Item to change the status');
            }
        }
    }

    render () {
        let items = this.state.items
        /*let trs = items.map(item => <tr>
                                        <td style={{width:'30px', tableLayout:'fixed'}}>{item.id}</td>
                                        <td style={{zIndex:20}}>{item.queue}</td>
                                        <td style={{zIndex:20}}>{item.status}</td>
                                        <td style={{zIndex:20}}>{item.seqKey}</td>
                                        <td style={{zIndex:20}}>{item.scheduledAt}</td>
                                        <td style={{zIndex:20}}>{item.createdTimeStamp}</td>
                                        <td style={{zIndex:20}}>{item.updatedTimeStamp === 0 ? '' : item.updatedTimeStamp}</td>
                                    </tr>);*/

        let height = parseInt(document.documentElement.clientHeight  - 350);

        /*const columns = [
            {dataField:'id', text: 'Id', sort: true},
            {dataField:'queue', text: 'Queue', sort: true},
            {dataField:'status', text: 'Status', sort: true},
            {dataField:'seqKey', text: 'Sequence', sort: true},
            {dataField:'scheduledAt', text: 'Scheduled', sort: true},
            {dataField:'createdTimeStamp', text: 'Created', sort: true},
            {dataField:'updatedTimeStamp', text: 'Updated', sort: true}
        ];*/

        return <div>
                    <div style = {{float:'left', width: '100%'}}>
                        <h5 style = {{float:'left'}}> Total Items found : {items.length}</h5>
                        <div style = {{float:'right'}}>
                            <DropdownButton id='dropdown-basic-button' title='Mark Item'>
                                <Dropdown.Item onClick = { this.updateStatus('COMPLETED') }>COMPLETED</Dropdown.Item>
                                <Dropdown.Item onClick = { this.updateStatus('PENDING') }>PENDING</Dropdown.Item>
                                <Dropdown.Item onClick = { this.updateStatus('ERROR') }>ERROR</Dropdown.Item>
                                <Dropdown.Item onClick = { this.updateStatus('HELD') }>HELD</Dropdown.Item>
                            </DropdownButton>
                        </div>
                    </div>

                    <div style={{width: this.props.width || '100%', height: this.props.height || height+'px'}}>

                    {/*<BootstrapTable
                        keyField='id'
                        data={ items }
                        columns={ columns }
                        height= {height}
                        scrollTop={ 'Bottom' }
                        striped
                        hover
                        condensed
                        bootstrap4
                    />*/}
                    <BootstrapTable
                        data={ items }
                        height= {height}
                        scrollTop={ 'Bottom' }
                        selectRow={{ mode: 'checkbox', clickToSelect: true, onSelect:this.handleSelect,onSelectAll:this.handleSelectAll}}
                        striped
                        hover
                        condensed
//                        search
//                        exportCSV
//                        pagination
                        version='4'
                        sortIndicator
                        multiColumnSort={4}
                        options = {{ sortName:['id', 'status', 'seqKey'], sortOrder:['asc', 'asc', 'asc'],sortIndicator:true }} >
                              <TableHeaderColumn dataField='id' isKey>Id</TableHeaderColumn>
                              <TableHeaderColumn dataField='queue'>Queue</TableHeaderColumn>
                              <TableHeaderColumn dataField='status'>Status</TableHeaderColumn>
                              <TableHeaderColumn dataField='seqKey'>Sequence</TableHeaderColumn>
                              <TableHeaderColumn dataField='scheduledAt'>Scheduled</TableHeaderColumn>
                              <TableHeaderColumn dataField='createdTimeStamp'>Created</TableHeaderColumn>
                              <TableHeaderColumn dataField='updatedTimeStamp'>Updated</TableHeaderColumn>
                          </BootstrapTable>
                    {/*<table className='table table-dark table-sm table-bordered table-scroll'>
                        <thead>
                            <tr>
                                <th onClick={e=>this.renderItems('id')} style={{width:'30px'}}>Id</th>
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
                    </table>*/}
                    </div>
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
        updateStatus : (ids, status) => dispatch(MBQService.updateStatus(ids, status))
    }
};

export default connect(mapStateToProps, mapDispatchToProps) (Result);