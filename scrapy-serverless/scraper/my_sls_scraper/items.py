# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

from scrapy import Item, Field


class JobItem(Item):
    # define the fields for your item here like:
    Url = Field()
    Title = Field()
    Company = Field()
    Locality = Field()
    Region = Field()
    Country = Field()
    Date_Posted = Field()
    Description = Field()
    Salary = Field()
    pass
