package shop.plumeria.smartgardenapi.service;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shop.plumeria.smartgardenapi.dao.SensorDAO;
import shop.plumeria.smartgardenapi.dto.SensorDTO;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PlantService {

    private InfluxDB influxDB;

    @Value("${influxDB.databaseUrl}")
    private String databaseUrl;

    @Value("${influxDB.userName}")
    private String userName;

    @Value("${influxDB.password}")
    private String password;

    @Value("${influxDB.dbname}")
    private String dbname;

    @Value("${influxDB.measurement}")
    private String measurement;

    public boolean saveSensorData(String mac_address, SensorDTO sensorData) {
        influxDB = InfluxDBFactory.connect(databaseUrl, userName, password);

        BatchPoints batchPoints = BatchPoints.database(dbname).build();
        Point point = Point.measurement(measurement).time(sensorData.getDateAndTime(), TimeUnit.MILLISECONDS).addField("name", sensorData.getName())
                .addField("mac_address", mac_address).addField("battery", sensorData.getBattery())
                .addField("conductivity", sensorData.getConductivity()).addField("light", sensorData.getLight()).addField("moisture", sensorData.getMoisture())
                .addField("temperature", sensorData.getTemperature()).build();
        log.info("Created Point {}", point);
        batchPoints.point(point);
        influxDB.write(batchPoints);
        influxDB.close();
        return true;
    }

    public List<SensorDAO> getSensorData(String mac_address) {
        influxDB = InfluxDBFactory.connect(databaseUrl, userName, password);
        influxDB.setDatabase(dbname);
        QueryResult queryResult = influxDB.query(new Query("Select * from " + measurement + " where mac_address = '" + mac_address + "'"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorDAO> data = resultMapper
                .toPOJO(queryResult, SensorDAO.class);
        influxDB.close();
        return data;
    }

    public SensorDAO getLatestSensorData(String mac_address) {
        influxDB = InfluxDBFactory.connect(databaseUrl, userName, password);
        influxDB.setDatabase(dbname);
        QueryResult queryResult = influxDB.query(new Query("SELECT * FROM "+ measurement +" where mac_address = '" + mac_address + "' GROUP BY * ORDER BY DESC LIMIT 1"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorDAO> data = resultMapper
                .toPOJO(queryResult, SensorDAO.class);
        influxDB.close();
        return data.get(0);
    }
}
