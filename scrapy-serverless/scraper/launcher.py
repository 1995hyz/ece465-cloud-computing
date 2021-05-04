import sys
import json

from my_sls_scraper.crawl import crawl


def scrape(event={}, context={}):
    crawl()
    response = {
        "statusCode": 200,
        "body": "Scrape succeed!"
    }

    return response


if __name__ == "__main__":
    scrape()
