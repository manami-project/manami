[![Tests](https://github.com/manami-project/manami/actions/workflows/tests.yml/badge.svg)](https://github.com/manami-project/manami/actions/workflows/tests.yml) [![codecov](https://codecov.io/gh/manami-project/manami/graph/badge.svg?token=DkoslLUvTn)](https://codecov.io/gh/manami-project/manami) ![jdk21](https://img.shields.io/badge/jdk-21-informational)
# Manami

## What does it do?
Manami creates an index file for the anime that you already watched and stored on your hard drive. Additionally based on this list the tool can assist you in finding more anime that you might enjoy.

## All features at a glance
* Categorize anime in watched, plan to watch/watching, ignore
* Automatically fill out anime data upon adding a new entry
  * supports multiple meta data providers using data from [anime-offline-database](https://github.com/manami-project/anime-offline-database)
* Find anime by tags
* Browse anime seasons
* Find related anime for all entries in your anime list
* Find similar anime based on tags
* Suggestions to ignore anime
* Migrate entries from one meta data provider to another
* Find inconsistencies
  * Find updated meta data in animelist/watchlist/ignorelist entries and automatically fix them
  * Find dead entries in animelist
  * Find dead entries in watchlist/ignorelist and automatically remove them
  * Check if number of files equals number of expected episodes
* No lock-in. The data is persisted as XML file and therefore can be converted and migrated very easily
* No need to provide username or password
* No installer, it's portable

## What it doesn't do
* The tool won't let you update your profile on sites like myanimelist.net, kistu.io,...

## Why?
* I created this tool for my personal needs to support my workflow.

## Installation
* Requires JDK 21 and the possibility to run JavaFX
* Download the *.jar file of the latest release
* No installation or additional setup needed. Just Download the `*.jar` and start it by double click or via console `java -jar manami.jar`.

## Configuration

See ["Configuration Management"](https://github.com/manami-project/modb-core/tree/master#configuration-management)

| parameter                    | type      | default | description                                                                                                  |
|------------------------------|-----------|---------|--------------------------------------------------------------------------------------------------------------|
| `manami.cache.useLocalFiles` | `Boolean` | `true`  | Downloads anime-offline-database files once and stores them next to the *.jar file. Redownload after 24 hrs. |

# Dataset

Uses data from [https://github.com/manami-project/anime-offline-database](https://github.com/manami-project/anime-offline-database), which is made available
here under the [Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/1-0/).