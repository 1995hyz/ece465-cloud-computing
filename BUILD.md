# Required dependencies to build this project (Only tested on Ubuntu 18.04)
## Frontend:
    node.js version 14 or above:
        sudo apt install nodejs
    npm:
        sudo apt install npm
## Backend:
    Python 3.6
    Scrapy 2.5.0
        pip install Scrapy
    BeautifulSoup4
        pip install beautifulsoup4
    Serverless:
        npm install -g serverless
    Install serverless plugin to resolve python dependencies that are needed for serverless deployments 
        sls plugin install --name serverless-python-requirements
    