ppm (personal photoset manager)
===============================

Documentation
=============

See http://soahowto.blogspot.de/2014/09/create-flickr-application-hosted-on.html

Deploying an Update
===================

Install JDK 8 as default:
```
sdk install java 8.0.352-zulu
sdk default java 8.0.352-zulu
```

Deploy to Google App Engine:
```
mvn package appengine:deploy -Dapp.deploy.projectId=flickrpm -Dapp.deploy.version=1
```

Updating the Cron Job
=====================

Update the job definition in `cron.yaml` and then deploy it with:
```
gcloud app deploy cron.yaml
```

Monitoring
==========

- [Dashboard](https://console.cloud.google.com/appengine?project=flickrpm&serviceId=default&versionId=1)
- [Error Reporting](https://console.cloud.google.com/errors?serviceId=default&versionId=1&project=flickrpm)

TODOs
=====

- use HTTP POST method on API calls that require it according to Flickr API documentation
- recreate the project using the latest version of appengine-skeleton-archetype (this will result in a single Maven project as opposed to the current setup with a parent project and two sub-projects)
