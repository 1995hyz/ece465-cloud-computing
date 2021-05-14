from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
from scrapy.exceptions import CloseSpider
from w3lib.html import remove_tags
import json
from ..items import JobItem
from bs4 import BeautifulSoup
from urllib.parse import urlparse

class JobSpider(CrawlSpider):
    name = "job_spider"

    def __init__(self, start_url, page_limit, *args, **kwargs):
        super(JobSpider, self).__init__(*args, **kwargs)
        self.start_urls = [start_url]
        self.custom_settings = {
            'CLOSESPIDER_PAGECOUNT': page_limit
        }
        print('INITIALIZED WITH START URL: ' + start_url)

    #start_urls=_start_urls
    #allowed_domains = ["scrapy.org"]

    rules = [  # Get all links on start url
        Rule(
            link_extractor=LinkExtractor(
                deny=r"\?",
            ),
            follow=False,
            callback="parse_page",
        )
    ]

    def parse_start_url(self, response):
        print("FOUND STARTING URL: " + response.url)
        return self.parse_page(response)

    def parse_page(self, response):
        print("PROCESSING URL: " + response.url)
        # determine domain
        domain = str(urlparse(response.url).netloc)
        print("DOMAIN: " + domain)
        soup = BeautifulSoup(response.text, 'html.parser')
        if "indeed" in domain:
            cards = soup.find_all('div', 'jobsearch-SerpJobCard')
            if len(cards) > 0:
                print("MATCHED TEMPLATE TO INDEED LINK")
                for card in cards:
                    item = JobItem()
                    item.set_all(None)
                    atag = card.h2.a
                    item['Title'] = atag.get('title')
                    item['Url'] = 'https://www.indeed.com' + atag.get('href')
                    item['Company'] = card.find('span', 'company').text.strip()
                    item['Locality'] = card.find('div', 'recJobLoc').get('data-rc-loc')
                    item['Region'] = 'UNAVAILABLE'
                    item['Country'] = 'UNAVAILABLE'
                    item['Date_Posted'] = card.find('span', 'date').text
                    item['Description'] = card.find('div', 'summary').text.strip()
                    try:
                        item['Salary'] = card.find('span', 'salaryText').text.strip()
                    except AttributeError:
                        item['Salary'] = 'UNAVAILABLE'

                    yield item
            else:
                print("COULD NOT MATCH INDEED LINK TO TEMPLATE")
        elif "glassdoor" in domain:
            # glassdoor template
            cards = soup.findAll('li', 'react-job-listing')
            if len(cards) > 0:
                print("MATCHED TEMPLATE TO GLASSDOOR LINK")
                for card in cards:
                    item = JobItem()
                    item.set_all(None)
                    item['title'] = str(card.get('data-normalize-job-title'))
                    item['location'] = card.get('data-job-loc')
                    div1 = card.find('div', {'class': 'pl-sm'})
                    div2 = div1.find('a', {'class': 'jobLink'})
                    item['url'] = 'https://www.glassdoor.com' + div2.get('href')
                    item['company'] = div2.span.text

                    yield item
            else:
                print("COULD NOT MATCH TEMPLATE TO GLASSDOOR LINK")
        else:
            # schema.org template
            str_data = response.xpath('//script[@type="application/ld+json"]//text()').extract_first()
            if str_data is not None:
                try:
                    data = json.loads(str_data)
                    item = JobItem()
                    item.set_all("UNAVAILABLE")
                    item['Url'] = str(data.get('url'))
                    item['Title'] = str(data.get('title'))
                    location = data.get('jobLocation', {}).get('address')
                    # get specific location fields
                    if location is not None:
                        item['Locality'] = str(location.get('addressLocality'))
                        item['Region'] = str(location.get('addressRegion'))
                        item['Country'] = str(location.get('addressCountry'))
                    item['Date_Posted'] = str(data.get('datePosted'))
                    if data.get('description') is not None:
                        item['Description'] = str(remove_tags(data.get('description')))
                    # item['Salary'] = 'UNAVAILABLE'
                    # item['Company'] = 'UNAVAILABLE'
                    # populate fields with "UNAVAILABLE" if None
                    yield item
                except:
                    print("Cannot load schema template for" + response.url)
