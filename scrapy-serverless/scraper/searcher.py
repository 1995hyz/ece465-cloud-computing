import boto3
import os
import json
import traceback
from datetime import datetime, timedelta
import dateutil.parser
import re


def search(event={}, context={}):
    try:
        print(event)
        request_body = json.loads(event['body'])
        JOBS_TABLE = os.environ['JOBS_TABLE']
        client = boto3.client('dynamodb')
        if request_body['toDate'] == "":
            to_date = datetime.now().isoformat()
        else:
            to_date = request_body["toDate"]
        if request_body['fromDate'] == "":
            from_date = (dateutil.parser.parse(to_date) - timedelta(days=2)).isoformat()
        else:
            from_date = request_body["fromDate"]
        if request_body['jobTitle'] != "":
            job_title = request_body['jobTitle']
        else:
            job_title = ""
        if request_body['company'] != "":
            company = request_body['company']
        else:
            company = ""
        if request_body['description'] != "":
            description = request_body['description']
        else:
            description = ""

        scan_result = client.scan(
            TableName=JOBS_TABLE,
            FilterExpression="#T < :time_stamp_to AND #T > :time_stamp_from",
            ExpressionAttributeNames={"#T": "entryTime"},
            ExpressionAttributeValues={
                ":time_stamp_to": {"S": to_date},
                ":time_stamp_from": {"S": from_date}
            }
        )

        result_list = scan_result.get('Items', [])
        if job_title != "":
            result_list = filter_entity(job_title, "Title", result_list)
        if company != "":
            result_list = filter_entity(company, "Company", result_list)
        if description != "":
            result_list = filter_entity(description, "Description", result_list)

        response = {
            "statusCode": 200,
            "headers": {
                "Access-Control-Allow-Headers": "Content-Type",
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "OPTIONS, POST, GET"
            },
            "body": json.dumps(result_list)
        }
    except Exception as e:
        traceback.print_exception(type(e), e, e.__traceback__)
        response = {
            "statusCode": 500,
            "headers": {
                "Access-Control-Allow-Headers": "Content-Type",
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "OPTIONS, POST, GET"
            },
            "body": json.dumps({"error": str(e)})
        }

    return response


def filter_entity(entity, entity_key, filter_list):
    result_list = []
    for entry in filter_list:
        if re.search(entity, entry[entity_key]["S"], re.IGNORECASE):
            result_list.append(entry)
    return result_list


if __name__ == "__main__":
    search()
