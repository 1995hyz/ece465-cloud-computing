# Web Crawler for Job Postings

This project is a web crawler using the Scrapy Python framework to scrape job postings from career websites. It is implemented as a serverless application hosted etnriely on AWS. 

Video Presentation:

Slide Deck: https://docs.google.com/presentation/d/1yd5OlCOemNr5vCchp0f0nfdybKGMOZ9cq3WRSKdTeTc/edit?usp=sharing

## Usage
The crawler can be used to mass fetch job postings from career websites, such as Indeed.com, given a starting link and present relevant data from each posting on the frontend. A front end was built to display the scraping results in a table. A search bar can be used to filter the query based on job title, company, job description, and date range of when the posting was entered into the database. A separate crawler runs in the background periodically to scrape data from pre-set URLs to provide data to users who do not provide a URL of their own to parse. NOTE: Many websites have restricted policies on what web pages can be crawled. It is the users' responsiblility to find out such policy on each website. Usually such policy can be found in <website domain>/robots.txt. DO NOT try to use this tool to crawl web-pages without permission. 

## Scraping

We utilized Scrapy to perform the scraping of websites. We initialize a crawling spider with a starting URL and a max page limit, then the spider extracts all the links from the starting URL, and finally parses each page. Customized templates for parsing are required to extract relevant data from each domain. We wrote templates for Indeed.com, Glassdoor.com, and websites that follow the Schema.org template for job postings. The fields that we search for are job URL, company, description, location, salary, and date posted.

Since we instruct the spider to obey a website's robot.txt file, they do not consistently scrape data from websites that restrict scraping. Generally, Indeed allows for one run of our spider, while Glassdoor restricts our spider completely. We tried user-agent spoofing and delaying download times, though in order to remain within legal limits, we did not experiment with any more workarounds of the website's restrictions.

## Cloud Deployment

We use the Serverless framework to deploy our back-end code. An API Gateway can trigger a 'crawl' Lambda function with a starting URL and max page count; this Lambda function runs a spider and uploads the scraping results to DynamoDB. The API Gateway can also trigger a 'fetch' Lambda function with search filters; this function returns the matching job postings from DynamoDB. We use EventBridge to periodically trigger the 'crawl' Lambda function with a pre-defined set of URL's. 

The front end is hosted on Amplify. After the serverless deployment returns the ID of the API Gateway, the ID is plugged into the front end code so that it can trigger the gateway and send crawl and fetch requests.

Two reasons why we decide to use Lambda functions as our backend. First, Lambda functions can scale really well. If we have many users sending requests at the same time, Lambda function can automatically spawn new instances to handle those requests. We think this scalability is very helpful to our application. Second reason is that, from a running cost perspective, Lambda functions only cost money when they are actually running. So we think this architecture can be inexpensive to implement.

## Future Work
Our work fulfilled our initial goals for the project as an MVP, though it is limited to the ability of the spiders to perform the scraping. Future work would entail improving the consistency of the spiders. Also, we would like to develop more comprehensive templates for websites so that our spiders can be applied to a wider range of websites.
