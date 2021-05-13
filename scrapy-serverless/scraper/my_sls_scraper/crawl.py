import sys
import imp
import os
import logging
import json

from scrapy.spiderloader import SpiderLoader
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings

# Need to "mock" sqlite for the process to not crash in AWS Lambda / Amazon Linux
sys.modules["sqlite"] = imp.new_module("sqlite")
sys.modules["sqlite3.dbapi2"] = imp.new_module("sqlite.dbapi2")


def is_in_aws():
    return os.getenv('AWS_EXECUTION_ENV') is not None


def crawl(settings={}, spider_name="job_spider", spider_kwargs={}, event={}):
    project_settings = get_project_settings()
    spider_loader = SpiderLoader(project_settings)

    spider_cls = spider_loader.load(spider_name)

    feed_uri = ""
    feed_format = "json"

    try:
        #spider_key = urlparse(spider_kwargs.get("start_urls")[0]).hostname if spider_kwargs.get(
        #    "start_urls") else urlparse(spider_cls.start_urls[0]).hostname
        request_body = event['body']
        spider_key = request_body['crawlUrl']
        crawl_amount = request_body['crawlAmount']
        print("spider_key: " + spider_key)
        if is_in_aws():
            # Lambda can only write to the /tmp folder.
            settings['HTTPCACHE_DIR'] = "/tmp"
        else:
            feed_uri = "file://{}/%(name)s-{}-%(time)s.json".format(
                os.path.join(os.getcwd(), "feed"), spider_key,
            )

        settings['FEED_URI'] = feed_uri
        settings['FEED_FORMAT'] = feed_format
        #settings['CLOSESPIDER_PAGECOUNT'] = crawl_amount
        process = CrawlerProcess({**project_settings, **settings})
        print("STARTING CRAWL")
        process.crawl(spider_cls, start_url=spider_key, page_limit=crawl_amount)
        process.start(stop_after_crawl=False)
    except Exception as e:
        logging.exception("Spider or kwargs need start_urls.")
