import random
import logging
import json

from my_sls_scraper.crawl import crawl

CRAWL_LIST = ["https://scrapy.org/", "https://scrapy.org/"]
DEFAULT_CRAWL_NUMBER = 20


def scrape(event={}, context={}):
    if 'body' in event:
        request_body = event['body']
        print("*** Receive request body: " + str(request_body) + " ***")
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
    else:
        # Randomly choose a url to crawl. This is the case when the function is periodically triggered by AWS EventBridge
        random_index = random.randint(0, len(CRAWL_LIST) - 1)
        event['body'] = {'crawlUrl': CRAWL_LIST[random_index], "crawlAmount": DEFAULT_CRAWL_NUMBER}
        print("*** Randomly select URL: " + CRAWL_LIST[random_index] + " ***")
        crawl(event=event)
        response = {
            "statusCode": 200,
            "headers": {
                "Access-Control-Allow-Headers": "Content-Type",
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "OPTIONS, POST, GET"
            },
            "body": "Starting to scrape " + CRAWL_LIST[random_index] + "..."
        }
    return response


if __name__ == "__main__":
    scrape()
