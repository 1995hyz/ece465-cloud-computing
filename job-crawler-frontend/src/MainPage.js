import React, { Component } from 'react';
import Axios from "axios";
import "./css/MainPageStyles.css"

class MainPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            crawlUrl: "https://scrapy.org",
            crawlAmount: "100",
            fromDate: "5/10/2021",
            toDate: "5/11/2021",
            jobTitle: "",
            company: "",
            description: "",
            crawlMessage: "",
            searchInputError: "",
            crawlError: "",
            searchResult: [],
            searchRequestError: "",
            searchResultError: ""
        };
        // Set the correct baseURL once deploying the infrastructure to aws via serverless framework
        Axios.defaults.baseURL = "https://sj41wrtjy2.execute-api.us-east-1.amazonaws.com/dev";
    }

    onClickCrawlHandler = () => {
        if (this.validateInput()) {
            const url = "crawl";
            const data = {
                "crawlUrl": this.state.crawlUrl,
                "crawlAmount": this.state.crawlAmount
            };
            this.setState({crawlMessage: "Sent crawling request " + this.state.crawlUrl});
            Axios.post(url, data)
                .then(res => {
                    if (res.status === 200) {
                        console.log(res);
                    }
                })
                .catch(err => {
                    console.log(err);
                });
        } else {

        }
    };

    onClickSearchHandler = () => {
        try {
            let selectedFromDate = new Date(this.state.fromDate);
            let selectedToDate = new Date(this.state.toDate);
            this.validateDate(selectedFromDate);
            this.validateDate(selectedToDate);
            const url = "search";
            const data = {
                "jobTitle": this.state.jobTitle,
                "company": this.state.company,
                "description": this.state.description,
                "fromDate": selectedFromDate.toISOString(),
                "toDate": selectedToDate.toISOString()
            };
            console.log(data);
            Axios.post(url, data)
                .then(res => {
                    if (res.status === 200) {
                        this.setState({searchResult: res.data});
                    } else  {
                        this.setState({searchResultError: res.data});
                        console.log(res.data);
                    }
                })
                .catch(err => {
                    this.setState({searchRequestError: err.toString});
                })
        } catch (error) {
            this.setState({searchInputError: error.toString});
            console.log(error.toString());
        }
    };

    validateDate = (day) => {
        if (! (day instanceof Date && !isNaN(day))) {
            this.setState(prevState => {
                return {
                    ...prevState, searchInputError: "Date input is not valid..."
                };
            });
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
            <div>
                <label>Url:</label>
                <input
                    type="text"
                    name="crawlUrl"
                    value={this.state.crawlUrl}
                    onChange={this.handleInput}
                />
                <label>Size:</label>
                <input
                    type="text"
                    name="crawlAmount"
                    value={this.state.crawlAmount}
                    onChange={this.handleInput}
                />
                <button onClick={this.onClickCrawlHandler}>Crawl</button>
            </div>
            <div>
                <p>{this.state.crawlMessage}</p>
            </div>
            <div>
                <label>Job title:</label>
                <input
                    type="text"
                    name="jobTitle"
                    value={this.state.jobTitle}
                    onChange={this.handleInput}
                />
                <label>Company</label>
                <input
                    type="text"
                    name="company"
                    value={this.state.company}
                    onChange={this.handleInput}
                />
                <label>Description</label>
                <input
                    type="text"
                    name="description"
                    value={this.state.description}
                    onChange={this.handleInput}
                />
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
                <button onClick={this.onClickSearchHandler}>Search</button>
            </div>
            <div>
                <p>{this.state.searchInputError}</p>
            </div>
            <div>
                <table id="jobs">
                    <tr>
                        <th>Title</th>
                        <th>Company</th>
                        <th>Description</th>
                        <th>Salary</th>
                        <th>Locality</th>
                    </tr>
                    {
                        this.state.searchResult.map(entry => {
                            return <tr>
                                <th>{entry["Title"]["S"]}</th>
                                <th>{entry["Company"]["S"]}</th>
                                <th>{entry["Description"]["S"]}</th>
                                <th>{entry["Salary"]["S"]}</th>
                                <th>{entry["Locality"]["S"]}</th>
                            </tr>
                        })
                    }
                </table>
            </div>
        </div>
    }
}

export default MainPage;