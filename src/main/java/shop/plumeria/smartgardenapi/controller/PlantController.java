package shop.plumeria.smartgardenapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.plumeria.smartgardenapi.dto.PlantDTO;
import shop.plumeria.smartgardenapi.dto.SensorDTO;
import shop.plumeria.smartgardenapi.repository.PlantRepository;

import java.util.List;

@RestController
@RequestMapping("/plant")
@Slf4j
public class PlantController {

    @Autowired
    private PlantRepository plantRepository;

    @GetMapping
    public PlantDTO getPlant(@RequestParam String name) {
        return plantRepository.getPlantDTOByName(name);
    }

    @PostMapping
    public PlantDTO savePlant(@RequestBody PlantDTO plantDTO) {
        plantRepository.save(plantDTO);
        return plantRepository.getPlantDTOByName(plantDTO.getName());
    }

    @GetMapping("/data")
    public List<SensorDTO> getData(@RequestParam String name) {
        return plantRepository.getPlantDTOByName(name).getData();
    }

    @PostMapping("/data")
    public PlantDTO saveData(@RequestParam String name, @RequestBody SensorDTO newData) {
        log.info("New data: {}", newData);
        PlantDTO plant = plantRepository.getPlantDTOByName(name);
        List<SensorDTO> oldData = plant.getData();
        oldData.add(newData);
        plantRepository.save(plant);
        return plantRepository.getPlantDTOByName(name);
    }

}
