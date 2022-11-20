ppm (personal photoset manager)
===============================

Documentation
=============

See http://soahowto.blogspot.de/2014/09/create-flickr-application-hosted-on.html

Deploying an Update
===================

```
mvn package appengine:deploy -Dapp.deploy.projectId=flickrpm -Dapp.deploy.version=1
```

Monitoring
==========
See Dashboard at https://console.cloud.google.com/appengine?project=flickrpm&serviceId=default&versionId=1

TODOs
=====

- use HTTP POST method on API calls that require it according to Flickr API documentation
- recreate the project using the latest version of appengine-skeleton-archetype (this will result in a single Maven project as opposed to the current setup with a parent project and two sub-projects)
