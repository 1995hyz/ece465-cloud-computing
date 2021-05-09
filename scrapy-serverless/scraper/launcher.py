import sys
import json
import logging
import json

from my_sls_scraper.crawl import crawl


def scrape(event={}, context={}):
    request_body = json.loads(event['body'])
    crawl(event=event)
    response = {
        "statusCode": 200,
        "headers": {
            "Access-Control-Allow-Headers": "Content-Type",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "OPTIONS, POST, GET"
        },
        "body": "Starting to scrape " + request_body['crawlUrl'] + "..."
    }

    return response


if __name__ == "__main__":
    scrape()
