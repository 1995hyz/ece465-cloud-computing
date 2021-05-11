import React, { Component } from 'react';
import Axios from "axios";

class MainPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            crawlUrl: "https://scrapy.org",
            crawlAmount: "100",
            fromDate: "5/10/2021",
            toDate: "5/11/2021"
        };
        // Set the correct baseURL once deploying the infrastructure to aws via serverless framework
        Axios.defaults.baseURL = "https://vhztunt4wk.execute-api.us-east-1.amazonaws.com/dev";
    }

    onClickCrawlHandler = () => {
        if (this.validateInput()) {
            const url = "crawl";
            const data = {
                "crawlUrl": this.state.crawlUrl,
                "crawlAmount": this.state.crawlAmount
            };
            Axios.post(url, data)
                .then(res => {
                    if(res.status === 200) {
                        console.log(res);
                    }
                })
                .catch(err => {
                    console.log(err);
                });
        } else {

        }
    };

/*    onClickSearchHandler = () => {
        try {
            let selectedFromDate = new Date()
        }
    };*/

    handleInput = (event) => {
        let value = event.target.value;
        let name = event.target.name;
        this.setState(prevState => {
            return {
                ...prevState, [name]: value
            };
        });
    };

    validateInput = () => {
        return !isNaN(parseInt(this.state.crawlAmount)) && parseInt(this.state.crawlAmount) > 0;
    };

    render() {
        return <div>
            <div>
                <input
                    type="text"
                    name="crawlUrl"
                    value={this.state.crawlUrl}
                    onChange={this.handleInput}
                />
                <input
                    type="text"
                    name="crawlAmount"
                    value={this.state.crawlAmount}
                    onChange={this.handleInput}
                />
                <button onClick={this.onClickCrawlHandler}>Crawl</button>
            </div>
            <div>
                <label>From date:</label>
                <input
                    type="text"
                    name="fromDate"
                    value={this.state.fromDate}
                    onChange={this.handleInput}
                />
                <label>To date:</label>
                <input
                    type="text"
                    name="toDate"
                    value={this.state.toDate}
                    onChange={this.handleInput}
                />
                <button>Search</button>
            </div>
        </div>
    }
}

export default MainPage;