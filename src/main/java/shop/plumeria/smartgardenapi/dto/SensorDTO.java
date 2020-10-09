package shop.plumeria.smartgardenapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SensorDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int uuid;
    private String dateAndTime;
    private int light;
    private double temperature;
    private int moisture;
    private int conductivity;
    private int battery;

}
