package shop.plumeria.smartgardenapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorDTO {

    private String name;
    private String mac_address;
    private long dateAndTime;
    private int light;
    private double temperature;
    private int moisture;
    private int conductivity;
    private int battery;

}
