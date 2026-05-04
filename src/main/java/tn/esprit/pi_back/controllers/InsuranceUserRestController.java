package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.services.IInsuranceUserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance/users")
public class InsuranceUserRestController {

    private final IInsuranceUserService service;

    @PostMapping("/add")
    public User add(@RequestBody User u){
        return service.addInsuranceUser(u);
    }

    @PutMapping("/update")
    public User update(@RequestBody User u){
        return service.updateInsuranceUser(u);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id){
        service.deleteInsuranceUser(id);
    }

    @GetMapping("/get/{id}")
    public User get(@PathVariable Long id){
        return service.getInsuranceUser(id);
    }

    @GetMapping("/all")
    public List<User> all(){
        return service.getAllInsuranceUsers();
    }
}