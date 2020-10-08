package shop.plumeria.smartgardenapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.plumeria.smartgardenapi.dto.StatusDTO;

@RestController
public class StatusController {

    @GetMapping(value = "/status")
    public StatusDTO getStatus() {
        return StatusDTO.builder().status("up and running").build();
    }

}
