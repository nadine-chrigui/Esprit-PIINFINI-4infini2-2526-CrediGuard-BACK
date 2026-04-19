package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.profil.ProfilCreditRequestDTO;
import tn.esprit.pi_back.dto.profil.ProfilCreditResponseDTO;
import tn.esprit.pi_back.entities.ProfilCredit;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.mappers.ProfilCreditMapper;
import tn.esprit.pi_back.repositories.ProfilCreditRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilCreditServiceImpl implements ProfilCreditService {

    private final ProfilCreditRepository profilCreditRepository;
    private final UserRepository userRepository;
    private final ProfilCreditMapper profilCreditMapper;

    @Override
    public ProfilCreditResponseDTO createMyProfile(String email, ProfilCreditRequestDTO dto) {
        User client = findUserByEmail(email);

        if (profilCreditRepository.existsByClientId(client.getId())) {
            throw new IllegalStateException("ProfilCredit already exists for this client");
        }

        ProfilCredit profil = profilCreditMapper.toEntity(dto);
        profil.setClient(client);

        ProfilCredit saved = profilCreditRepository.save(profil);
        return profilCreditMapper.toResponse(saved);
    }

    @Override
    public ProfilCreditResponseDTO updateMyProfile(String email, ProfilCreditRequestDTO dto) {
        ProfilCredit profil = profilCreditRepository.findByClientEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found for user: " + email));

        profilCreditMapper.updateEntityFromDto(profil, dto);
        ProfilCredit updated = profilCreditRepository.save(profil);
        return profilCreditMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilCreditResponseDTO getMyProfile(String email) {
        ProfilCredit profil = profilCreditRepository.findByClientEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found for user: " + email));

        return profilCreditMapper.toResponse(profil);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilCreditResponseDTO getById(Long id) {
        ProfilCredit profil = profilCreditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found: " + id));

        return profilCreditMapper.toResponse(profil);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilCreditResponseDTO getByClientId(Long clientId) {
        ProfilCredit profil = profilCreditRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found for client: " + clientId));

        return profilCreditMapper.toResponse(profil);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilCreditResponseDTO> getAll(Long clientId) {
        if (clientId != null) {
            return profilCreditRepository.retrieveProfilsByClientId(clientId)
                    .stream()
                    .map(profilCreditMapper::toResponse)
                    .toList();
        }

        return profilCreditRepository.findAll()
                .stream()
                .map(profilCreditMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        ProfilCredit profil = profilCreditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found: " + id));

        profilCreditRepository.delete(profil);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
