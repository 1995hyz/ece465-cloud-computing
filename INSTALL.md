# Instructions on how to deploy the project to AWS
1. Create IAM admin user and access key pair. This is quite a common procedure. For detail, please reference https://www.serverless.com/framework/docs/providers/aws/guide/credentials/ .
Store access key pair in local computer's ~/.aws/credentials
2. Launch your terminal and navigate to the scrapy-serverless directory of this project. Start to deploy infrastructure to 
AWS by "sls deploy"
3. After the deployment finishes, copy down the api gateway url and paste it to job-crawler-frontend/src/MainPage.js Line#24 baseUrl. This is to make sure the frontend has the correct fqdn.
4. Now that the backend infrastructure is ready. You can launch the frontend by navigating to job-crawler-frontend directory and temporarily start the frontend with "npm start"
5. If a more permanent frontend is desired, you can deploy the frontend to AWS amplify by following the steps here: https://aws.amazon.com/getting-started/hands-on/build-react-app-amplify-graphql/module-one/ Default settings on Amplify will work. The only note is that, since this is a monorepo project, when using github with amplify, you need to set the frontend root to be "job-crawler-frontend".
