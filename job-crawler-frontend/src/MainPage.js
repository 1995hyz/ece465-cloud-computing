import React, { Component } from 'react';
import Axios from "axios";

class MainPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            crawlUrl: "https://scrapy.org",
            crawlAmount: "100"
        };
        // Set the correct baseURL once deploying the infrastructure to aws via serverless framework
        Axios.defaults.baseURL = "https://3i9k6rs7o3.execute-api.us-east-1.amazonaws.com/dev";
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
    }
}

export default MainPage;