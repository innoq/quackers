# quackers

A twitter clone for ducks written in Clojure.

This application is intended to illustrate the different security libraries 
which are available in the Clojure universe. 

***This is still WIP***

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Setup

This application requires SSL. To generate keys and certificates, follow the 
guide [here][].

[here]: http://www.eclipse.org/jetty/documentation/9.4.x/configuring-ssl.html

The application must be configured using environmental variables.
These are:

	DATABASE_URL=jdbc:h2:./db/app  // a url to an H2 database

	HOST=example.com // defaults to 'localhost' if not set

	HTTP_PORT=3030 // defaults to 3000 if not set

	SSL_PORT=4040  // defaults to 4000 if not set

	KEYSTORE="keystore" // sets the location of the generated keystore file

	KEY_PASSWORD="-------" // the password for the specified keystore

Alternatively, these can be defined in a file `profiles.clj` in the root
of the project so that they will be started using lein run.

	{:dev {:env {:database-url "jdbc:h2:./db/app"
             	 :http-port "3000"
             	 :ssl-port  "4000"
             	 :keystore  "keystore"
             	 :key-password "blAH432971"
             	 :host "awesome.app"}}}

## Populate the database

The database can be populated with

	lein migrate

The database can be rolled back with

	lein rollback

## Running

To start the application:

    lein run

## License

Copyright © 2016 innoQ Deutschland GmbH

Published under the Apache 2.0 license.
