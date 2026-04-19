package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.UserMiniDTO;
import tn.esprit.pi_back.services.IPartnerService;
import tn.esprit.pi_back.repositories.UserRepository;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/partners")
public class PartnerRestController {

    private final IPartnerService service;
    private final UserRepository userRepository;
    @PostMapping("/add")
    public UserMiniDTO add(@RequestBody tn.esprit.pi_back.entities.User u) {
        return service.addPartner(u);
    }

    @PutMapping("/update")
    public UserMiniDTO update(@RequestBody tn.esprit.pi_back.entities.User u) {
        return service.updatePartner(u);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePartner(id);
    }

    @GetMapping("/get/{id}")
    public UserMiniDTO get(@PathVariable Long id) {
        return service.getPartner(id);
    }

    @GetMapping("/all")
    public List<UserMiniDTO> all() {
        return service.getAllPartners();
    }
    @GetMapping("/count")
    public long count() {
        return service.countPartners();
    }
}