# Akka-Http vs Play 2

This code is an skeleton used to comper the ways that both frameworks are used to create a microservice and also serve as a performance comparison.

To run the load-test project you need to first get up one of the api-play or api-akka-http project to have running one microservice server to test.


To run api-play server:

```sh
$ sbt

> project api-play
> aspectj-runner:run
```

To run api-akka-http server:

```sh
$ sbt

> project api-akka-http
> aspectj-runner:run
```

To run load-test:

First get an api server running (play or akka-http), then in other terminal:

```sh
$ sbt

> project load-test
> test
```
