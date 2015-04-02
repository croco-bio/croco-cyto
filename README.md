General
=========
CroCo provides a new view on regulatory networks (transcription factor-target) for many species derived from context-specific ENCODE projects, the scientific literature and structured databases. Networks can be easily retrieved from a remote croco network repository (croco-repo) and combined via many networks operations.

consists of five components:

1. a network repository croco-repo,
2. an Application Programming Interface (API): croco-api
3. a Cytoscape plug-in: croco-cyto
4. a web application: croco-web
5. a web-service for remote access to the central repository: croco-service

See also: http://services.bio.ifi.lmu.de/croco-web/

croco-cyto
=========

The CroCo Cytoscape plug-in allows access to the croco network repository containing some 7.000 context-specific as well as global regulatory networks organized in an multi-dimensional ontology.

![Screenshot](http://services.bio.ifi.lmu.de/croco-web/data/croco-cyto.png)

croco-cyto installation
=========
see http://services.bio.ifi.lmu.de/croco-web for latest (stable) build.


* Start Cytoscape (tested with Cytoscape 3.2.1)
* Select: File/Import/Network/Public database
* Select: Data Source: croco-cyto
* Insert the web-service URL: http://services.bio.ifi.lmu.de/croco-web/services/
* Click on: connect
* Browse the ontology and select networks of interest from the croco-repo and drag them into the selection list
* Click on Union/Intersect/... to add a network operation.
* In order to apply network operation on networks: drag&drop the desired networks onto the network operation node (see screenshot for an example)
* Click on "Generate final Network"


croco-cyto build (from source)
=========

Install
mvn install -Dmaven.test.skip=true

The install command creates a bundles jar with all dependencies.

In Cytoscape:
install mvn:de.lmu.ifi.bio.croco/croco-cyto/1.0

Note
=========
The newest source code may not always be compatible with the remote croco instance. For croco-service compatible versions see: http://services.bio.ifi.lmu.de/croco-web.

Contact & Bug-reports & Suggestions
=========
We are constantly improving the croco components. So if you find any bugs, or have general suggestions/comments, please send a mail to: robert.pesch@bio.ifi.lmu.de

