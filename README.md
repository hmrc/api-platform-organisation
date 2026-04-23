
# API Platform Organisation

The API Platform Organisation microservice is responsible for maintaining the state of organisations
created by (or on behalf of) third parties who consume APIs registered on the API Platform.

These third parties must register their organisation details before they are able to get
production credentials.

## Running the tests

Some tests require `MongoDB` to run.
Thus, remember to start up MongoDB if you want to run the tests locally.
The tests include unit tests and integration tests.
In order to run them, use this command line:

```
./run_all_tests.sh
```

## Running locally

There is a convenience script to spin up the service locally

```
./run_local.sh
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").