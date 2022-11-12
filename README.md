# multikey-storage

With this RESTful application, one can save a value with a set of symbolic tags and then retrieve it using an at least
minimal subset of tags identifying the data element. Full CRUD support is provided.

## TOC

* [An explanation of the idea](#an-explanation-of-the-idea)
* [Installation](#installation)
    + [Installation prerequisites](#installation-prerequisites)
        - [Java](#java)
        - [Containers](#containers)
    + [Build & run](#build---run)
* [Usage](#usage)
* [API](#api)
    + [Create or update an item](#create-or-update-an-item)
        - [HTTP return codes](#http-return-codes)
    + [Delete an item](#delete-an-item)
        - [HTTP return codes](#http-return-codes-1)
    + [Retrieve an item](#retrieve-an-item)
        - [HTTP return codes](#http-return-codes-2)
* [Common problems](#common-problems)
    + [Storage conflicts](#storage-conflicts)
    + [Internal errors](#internal-errors)
* [Storage engines](#storage-engines)
    + [Dummy engine](#dummy-engine)
* [Metrics & Health check](#metrics---health-check)
* [Contacts](#contacts)

## An explanation of the idea

For example, you have saved the following values in the MKS:

* "ABC" with tags "A", "B" and "C"
* "ADE" with tags "A", "D" and "E"
* "CDE" with tags "C", "D" and "E"

Given that, you may later retrieve "ABC", for instance, using the following unique tag subsets:

* just **["B"]** as "B" is a tag that applies only to the "ABC" value of all the values given
* or **["A","C"]** as this pair is the pair that applies only to "ABC", too, though both "A" and "C" are tags that apply to
  multiple values
* or **["A","B"]**, **["B","C"]**, or even **["A","B","C"]** - they all identify the value definitely, too, though they are not
  minimal unique tag subsets

## Installation

### Installation prerequisites

#### Java

To build the library, you'll need a Java SE Development Kit, version 11 or later, and a Maven installation.

To run it, everything is included in the container.

#### Containers

When using the `multikey-storage` as a storage supplier for your own project, a recommended way to run an installation of the
`multikey-storage` is using Docker (https://docker.com).

You'll probably need to install a local Docker registry as described here: https://docs.docker.com/registry/deploying/ . In short,
it is as simple as running:

```bash
$ docker run -d -p 5000:5000 --restart=always --name registry registry:2
```

In production mode, the container may be run within a [Kubernetes](https://kubernetes.io) cluster or anything similar.

### Build & run

Run the following command within the `multikey-storage` project folder:

```bash
$ mvn clean package docker:run
```

## Usage

An item in the storage is pointed to by a unique set of tags. A tag is a non-empty case-sensitive symbolic string that satisfies
the following:

* starts with a latin letter
* may contain:
    * latin letters `A`-`Z`, `a`-`z`
    * arabic numbers `0`-`9`
    * dots `.`
    * hyphens `-`
    * underscores `_`
* is no longer than 64 symbols

Note that any unexpected symbol in the tag may result in an unexpected behaviour of `multikey-storage`.

A section name is a symbolic string that satisfies the same rules as the tag, except that it may be no longer than 256 symbols

The entry itself may be an arbitrary byte entity. Its type is identified using the `Content-Type` HTTP header when it is added or
updated in the storage.

## API

### Create or update an item

```http request
POST /v1/{sectionName}/{tags}
```

Create or update a stored item identified by its section name and tags. The tag list is delimited by commas `,`

The operation blocks all other operations on the storage section identified by the section name. If the storage section does not
exist, it is created.

The entry itself is the body of the HTTP request.

The type of the entry is identified using the `Content-Type` HTTP header of the request. This type will be set for the item when
it is returned.

#### HTTP return codes

| Code  | Meaning                                                     | Suggested way of resolution   |
|-------|-------------------------------------------------------------|-------------------------------|
| 200   | Success                                                     | _(not needed)_                |
| 409   | Conflict when trying to find an item identified by the tags | See "Storage conflicts" below |
| 500   | Internal error                                              | See "Internal errors" below   |

### Delete an item

```http request
DELETE /v1/{sectionName}/{tags}
```

```http request
DELETE /v1/{sectionName}/{tags}?force=true
```

Delete an entry identified by its section name and tags. The tag list is delimited by commas `,`

The operation blocks all other operations on the storage section identified by the section name. If the storage section does not
exist, no operation is run, and .

If the optional `force` parameter is set to `true`, no multiple-found check will run, all found occurences for the given tag set
will be deleted.

#### HTTP return codes

| Code  | Meaning                                                     | Suggested way of resolution   |
|-------|-------------------------------------------------------------|-------------------------------|
| 200   | Success                                                     | _(not needed)_                |
| 409   | Conflict when trying to find an item identified by the tags | See "Storage conflicts" below |
| 500   | Internal error                                              | See "Internal errors" below   |

### Retrieve an item

```http request
GET /v1/{sectionName}/{tags}
```

Get an item identified by its section name and tags. The tag list is delimited by commas `,`

#### HTTP return codes

| Code  | Meaning                                                     | Suggested way of resolution           |
|-------|-------------------------------------------------------------|---------------------------------------|
| 200   | Success                                                     | _(not needed)_                        |
| 404   | No item is found for the given tags                         | Try to check if the item should exist |
| 409   | Conflict when trying to find an item identified by the tags | See "Storage conflicts" below         |
| 500   | Internal error                                              | See "Internal errors" below           |

## Common problems

### Storage conflicts

A storage conflict is a situation when a search is performed on a tag set and multiple items are found that all satisfy that set.

The only general recommendation is to clarify the request by supplying a tag set that identifies an entry in a unique way.

Sometimes it is hard to determine if a storage conflict is provoked when an item is added to the collection. Imagine the following
situation:

1. The item collection is empty.
2. Item A is created with the tags `a,b` - the item is successfully created.
3. Item B is created with the tags `a,b,c` - sub-checks for all subsets of the tag set for the item being created can easily take
   too long, so sub-checks are not performed, so item B is successfully created too.
4. Now, item A is inaccessible: trying to get it or update it with tags `a,b` returns code 409, as `a,b` points both to item A and
   item B, and no other way to access item A exists ever since!

If the conflict is easy to investigate, you could try deleting item B, that action would make item A visible.

You may also consider the drastic way of resolving the conflict, that is removing all conflicting items. To do it, you may use
the `DELETE` request with the generalized tag set that would point to all the items involved in the conflict, with `force`
request parameter set to true (See "Deleting an item" above).

### Internal errors

First, take a look at the application logs. The default log level is `INFO`, so the errors and the generally useful information
are included in the log by default. If it is still unclear what the cause of the error is, try setting the log level to
`DEBUG` or even `TRACE` by setting `logging.level.ru.dmerkushov.mkstorage` to the desired level in
`src/main/resources/application.yml`. To investigate a complex case, you may also change `logging.level.root` or even introduce
special levels for specific packages, but it is not recommended.

It is sometimes useful to filter the log based on the request id included in every log message, in order to see only the log
messages related to the request you are investigating.

Every log message is textually unique, so you can easily find the source location where the message comes from. Every exception
message in the log comes with a stack trace that will lead you to the exact source location where the exception was thrown.

For more info on log levels, see https://logging.apache.org/log4j/2.x/manual/architecture.html , "Log Levels" section.

## Storage engines

Several storage engines are provided (see below).

Engines can be selected using the property `mkstorage.storage.engine` in `application.yml`, or using the command line parameter,
for example, `--mkstorage.storage.engine=sql`. Command line parameter takes precedence.

### Dummy engine

`mkstorage.storage.engine` : `dummy`

The Dummy engine does not provide any function. Every request is "successful".

### MemMap engine

`mkstorage.storage.engine` : `memmap`

The MemMap engine stores items in a couple of maps in RAM.

### SQL engine (in development)

`mkstorage.storage.engine` : `sql`

The SQL engine stores items in a JDBC-compliant relational database. The database is required to support the following SQL92
statements:

* `INSERT`
* `SELECT`
* `UPDATE`
* `DELETE`
* `CREATE TABLE` - with primary keys and indexes

The database is required to support simultaneous access to data.

See also [the SQL engine documentation](docs/sqlEngine/index.md)

### ClickHouse engine (based on SQL engine, not yet implemented)

`mkstorage.storage.engine` : `clickhouse`

The ClickHouse engine stores items in a ClickHouse database.

The ClickHouse engine is planned to be developed separately from the JDBC engine, as ClickHouse DB has limited support for the SQL
statements required by the JDBC engine.

## Metrics & Health check

Metrics are available via a Prometheus (https://prometheus.io/) endpoint `/actuator/prometheus` when the application is started.

A common prefix for the `multikey-storage` application-specific metrics is `storage_`. All metrics are described using the
Prometheus's `HELP` comment.

Health check is available via `/actuator/health`.

## Contacts

Please feel free to contact the author or even file a pull request to fix a bug or to propose a feature.

I'd like to hear from you via issues at the project's GitHub repo https://github.com/dmerkushov/multikey-storage or via email.

* Dmitriy Merkushov
* Email: d.merkushov(at)gmail.com
* http://dmerkushov.ru
