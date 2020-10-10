# Smart Garden API
Access the time series data here [grafana.jeanne.tech](https://grafana.jeanne.tech)

## Requirements
- Java
- [InfluxDB](https://docs.influxdata.com/influxdb/v1.8/introduction/get-started/)

## Run the backend
If you haven't installed maven you can use the included maven wrapper!
Build the complete project including backend and frontend
```
mvn clean install -DskipTests
java -jar <name>.jar
```

Running tests
```
mvn test
```
