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
        logging.info("****" + str(item) + "****")
        self.create_job_entry(item)
        return item

    def create_job_entry(self, item):
        print("UPLOADING TO DATABASE")
        self.client.put_item(
            TableName=self.JOBS_TABLE,
            Item={
                'jobId': {'S': item['Url']},
                'Title': {'S': item['Title']},
                'Company': {'S': item['Company']},
                'Locality': {'S': item['Locality']},
                'Region': {'S': item['Region']},
                'Country': {'S': item['Country']},
                'Date_Posted': {'S': item['Date_Posted']},
                'Description': {'S': item['Description']},
                'Salary': {'S': item['Salary']},
                'entryTime': {'S': datetime.datetime.now(datetime.timezone.utc).isoformat()}
            }
        )
