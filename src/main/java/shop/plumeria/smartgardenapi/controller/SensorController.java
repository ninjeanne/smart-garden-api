package shop.plumeria.smartgardenapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.plumeria.smartgardenapi.dao.SensorDAO;
import shop.plumeria.smartgardenapi.dto.SensorDTO;
import shop.plumeria.smartgardenapi.service.PlantService;

import java.util.List;

@RestController
@RequestMapping("/sensor")
@Slf4j
public class SensorController {

    @Autowired
    private PlantService plantService;

    @GetMapping("/{mac}")
    public SensorDAO getNewestDeviceData(@PathVariable String mac) {
        return plantService.getLatestSensorData(mac);
    }

    @GetMapping("/{mac}/all")
    public List<SensorDAO> getAllDeviceData(@PathVariable String mac) {
        return plantService.getSensorData(mac);
    }

    @PostMapping("/{mac}")
    public void savePlant(@PathVariable String mac, @RequestBody SensorDTO sensorDTO) {
        log.info("New data: {}", sensorDTO);
        plantService.saveSensorData(mac, sensorDTO);
    }
}
