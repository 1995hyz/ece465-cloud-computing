from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
from scrapy.exceptions import CloseSpider
from w3lib.html import remove_tags
import json
from ..items import JobItem
from bs4 import BeautifulSoup

class JobSpider(CrawlSpider):
    name = "job_spider"

    def __init__(self, start_url, *args, **kwargs):
        super(JobSpider, self).__init__(*args, **kwargs)
        self.start_urls = [start_url]
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
        return self.parse_page(response)

    def parse_page(self, response):
        print("PROCESSING URL: " + response.url)
        # indeed template
        soup = BeautifulSoup(response.text, 'html.parser')
        cards = soup.find_all('div', 'jobsearch-SerpJobCard')
        if len(cards) > 0:
            for card in cards:
                item = JobItem()
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
                print("ITEM: "+str(item))

                # populate fields with "UNAVAILABLE" if None
                for name, value in item.items():
                    if value is None:
                        item[name] = "UNAVAILABLE"
                yield item
        else:
            # schema.org template
            str_data = response.xpath('//script[@type="application/ld+json"]//text()').extract_first()
            if str_data is not None:
                try:
                    data = json.loads(str_data)
                    item = JobItem()
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
                    item['Salary'] = 'UNAVAILABLE'
                    item['Company'] = 'UNAVAILABLE'
                    # populate fields with "UNAVAILABLE" if None
                    for name, value in item.items():
                        if value is None:
                            item[name] = "UNAVAILABLE"
                    yield item
                except:
                    print("Cannot load schema template for" + response.url)

        # header = response.css("h1, h2").extract_first(
        # ) or response.css("title").extract_first() or response.url
        # return {
        #     "header": remove_tags(header),
        #     "url": response.url,
        # }