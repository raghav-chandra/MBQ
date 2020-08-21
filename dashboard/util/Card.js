import React from 'react';
import './styles.css';

export class Card extends React.Component {

    render() {
        let height = this.props.height || '100%';
        let width = this.props.width || '100%';
        return (<div className='stats' style = {{width, height}}>
                    {this.props.title ? <h5 className='stats-title'>{ this.props.title }</h5> : ''}
                    <div className='stats-body'>
                        {this.props.children}
                    </div>
                </div>);
    }
}