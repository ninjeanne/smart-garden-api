package shop.plumeria.smartgardenapi.service;

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


    @PostConstruct
    private void initialize() {
        influxDB = InfluxDBFactory.connect(databaseUrl, userName, password);
    }

    public boolean saveSensorData(String mac_address, SensorDTO sensorData) {
        BatchPoints batchPoints = BatchPoints.database(dbname).retentionPolicy("defaultPolicy").build();
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z").parse(sensorData.getDateAndTime());
        } catch (ParseException e) {
            return false;
        }
        Point point = Point.measurement(measurement).time(date.getTime(), TimeUnit.MILLISECONDS).addField("name", sensorData.getName())
                .addField("mac_address", mac_address).addField("battery", sensorData.getBattery())
                .addField("conductivity", sensorData.getConductivity()).addField("light", sensorData.getLight()).addField("moisture", sensorData.getMoisture())
                .addField("temperature", sensorData.getTemperature()).build();
        batchPoints.point(point);
        influxDB.write(batchPoints);
        return true;
    }

    public List<SensorDAO> getSensorData(String mac_address) {
        influxDB.setDatabase(dbname);
        QueryResult queryResult = influxDB.query(new Query("Select * from " + measurement + " where mac_address = \"" + mac_address + "\""));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper
                .toPOJO(queryResult, SensorDAO.class);
    }

}
