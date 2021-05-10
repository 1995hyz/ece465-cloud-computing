import boto3
import os
import json


def search(event={}, context={}):
    JOBS_TABLE = os.environ['JOBS_TABLE']
    client = boto3.client('dynamodb')

    scan_result = client.scan(
        TableName=JOBS_TABLE,
        FilterExpression="#T < :time_stamp",
        ExpressionAttributeNames={"#T": "entryTime"},
        ExpressionAttributeValues={
            ":time_stamp": {"S": "2021-05-10T03:56:09.329406+00:00"}
        }
    )

    response = {
        "statusCode": 200,
        "headers": {
            "Access-Control-Allow-Headers": "Content-Type",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "OPTIONS, POST, GET"
        },
        "body": json.dumps(scan_result)
    }

    return response


if __name__ == "__main__":
    search()
