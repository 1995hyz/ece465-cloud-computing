# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
import os
import logging
import boto3
import datetime


class MySlsScraperPipeline:
    def __init__(self):
        self.JOBS_TABLE = os.environ['JOBS_TABLE']
        self.client = boto3.client('dynamodb')

    def process_item(self, item, spider):
        response_item = {
            "header": str(item["header"]),
            "url": str(item["url"])
        }
        logging.info("****" + str(response_item) + "****")
        self.create_job_entry(item)
        return item

    def create_job_entry(self, item):
       header = str(item["header"])
       url = str(item["url"])
       self.client.put_item(
           TableName=self.JOBS_TABLE,
           Item={
               'jobId': {'S': url},
               'entryTime': {'S': datetime.datetime.now(datetime.timezone.utc).isoformat()}
           }
       )
