package shop.plumeria.smartgardenapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import shop.plumeria.smartgardenapi.dto.PlantDTO;

@Repository
public interface PlantRepository extends CrudRepository<PlantDTO, String> {

    PlantDTO getPlantDTOByName(String name);

}
