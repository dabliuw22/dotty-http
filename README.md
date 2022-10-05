# dotty-http

## Requirements
* JDK >= 19
* Sbt
* Dotty
* Docker Compose

## Local Setup
#### Run `docker-compose.yml`
```shell
$ docker-compose up -d
```
#### Run Migration
```shell
$ sbt api/flywayMigrate
```
#### Run Test
```shell
$ sbt test
```
#### Integration Test
```shell
$ sbt it:test
```
#### Apply Formatter
```shell
$ sbt scalafmtAll
```

## Run Api
### With Sbt
## Compile and Run
```shell
$ sbt api/compile api/run
```
### With Sbt Exec
#### Exec and Run
```shell
$ sbt api/exec api/run
```
### With Sbt Assembly
#### Add environment variables on your system 
Reference file:[`.env`](.env)
#### Assembly
```shell
$ sbt api/assembly
```
#### Run .jar
```shell
$ java -jar main/target/scala-3.1.0/api.jar
```