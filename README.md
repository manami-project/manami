# Manami

[![License: GNU Affero General Public License v3 (AGPLv3)](https://img.shields.io/badge/license-AGPLv3-blue.svg)](http://www.gnu.org/licenses/agpl-3.0.de.html) [![Build Status](https://travis-ci.org/manami-project/manami.svg?branch=development)](https://travis-ci.org/manami-project/manami) [![codecov](https://codecov.io/gh/manami-project/manami/branch/development/graph/badge.svg)](https://codecov.io/gh/manami-project/manami/branch/development)

## What does it do?
Manami let's you create an index file for the anime that you already watched and stored on your hard drive. Additionally based on this list you can search for related anime and recommendations.

### All features at a glance
* Import entries from MAL
* Automatically fill out meta information upon adding a new entry
    * You only have to add the info link and the location
* Creates an index list for your local anime which can be opened in your browser
* Import/Export from/to CSV/JSON
    * The basic file format is XML
* Recommendation
* Related anime finder
* Search genres, studios and seasons
* Check your list for inconsistency
    * Check meta data
    * Check CRC sum of your files
    * Check the amount of episodes against the number of files
    * Check dead entries
* Use a filter list to narrow down your search results
* Use a watch list with anime you are currently watching or you plan to watch
* No need to provide username or password
* No installer, it's portable

### Why?
Manami is not meant to be an alternative to MAL. It should be seen as an extension. It provides an index list for your local anime and features that I personally missed on MAL which have never been implemented.

### Installation
* Needs Java8 u66 or higher
* Download the *.jar file of the latest [release](https://github.com/manami-project/manami/releases) 
* No installation or additional setup needed. Just Download the *.jar and start it by double click
    * or via console: `java -jar manami.jar`