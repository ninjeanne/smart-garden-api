package shop.plumeria.smartgardenapi.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "sensor")
public class SensorDAO {

    @Column(name = "time")
    private Instant time;
    @Column(name = "name")
    private String name;
    @Column(name = "macAddress")
    private String macAddress;
    @Column(name = "light")
    private int light;
    @Column(name = "temperature")
    private double temperature;
    @Column(name = "moisture")
    private int moisture;
    @Column(name = "conductivity")
    private int conductivity;
    @Column(name = "battery")
    private int battery;

}
