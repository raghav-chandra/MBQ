import React from 'react';
import './styles.css';

export class Metric extends React.Component {

    render() {
        let height = this.props.height || '100%';
        let width = this.props.width || '100%';
        return (<div className='stats' style = {{width, height}}>

                </div>);
    }
}