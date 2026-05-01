package tn.esprit.pi_back.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.deliveryzone.*;
import tn.esprit.pi_back.entities.DeliveryZone;
import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;
import tn.esprit.pi_back.repositories.DeliveryZoneRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryZoneServiceImpl implements DeliveryZoneService {

    private final DeliveryZoneRepository deliveryZoneRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DeliveryZoneResponse create(DeliveryZoneCreateRequest req) {
        validatePolygon(req.geoJsonPolygon());

        DeliveryZone zone = new DeliveryZone();
        zone.setName(req.name().trim());
        zone.setGovernorate(trimOrNull(req.governorate()));
        zone.setDelegation(trimOrNull(req.delegation()));
        zone.setLocality(trimOrNull(req.locality()));
        zone.setRiskLevel(req.riskLevel());
        zone.setFeeAdjustment(req.feeAdjustment());
        zone.setExtraDelayDays(req.extraDelayDays());
        zone.setRequiresAdminApproval(req.requiresAdminApproval());
        zone.setActive(req.active());
        zone.setReason(trimOrNull(req.reason()));
        zone.setGeoJsonPolygon(req.geoJsonPolygon().trim());

        return toResponse(deliveryZoneRepository.save(zone));
    }

    @Override
    public DeliveryZoneResponse update(Long id, DeliveryZoneUpdateRequest req) {
        DeliveryZone zone = deliveryZoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeliveryZone not found: " + id));

        if (req.geoJsonPolygon() != null) {
            validatePolygon(req.geoJsonPolygon());
            zone.setGeoJsonPolygon(req.geoJsonPolygon().trim());
        }

        if (req.name() != null) zone.setName(req.name().trim());
        if (req.governorate() != null) zone.setGovernorate(trimOrNull(req.governorate()));
        if (req.delegation() != null) zone.setDelegation(trimOrNull(req.delegation()));
        if (req.locality() != null) zone.setLocality(trimOrNull(req.locality()));
        if (req.riskLevel() != null) zone.setRiskLevel(req.riskLevel());
        if (req.feeAdjustment() != null) zone.setFeeAdjustment(req.feeAdjustment());
        if (req.extraDelayDays() != null) zone.setExtraDelayDays(req.extraDelayDays());
        if (req.requiresAdminApproval() != null) zone.setRequiresAdminApproval(req.requiresAdminApproval());
        if (req.active() != null) zone.setActive(req.active());
        if (req.reason() != null) zone.setReason(trimOrNull(req.reason()));

        return toResponse(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryZoneResponse getById(Long id) {
        return deliveryZoneRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("DeliveryZone not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryZoneResponse> getAll() {
        return deliveryZoneRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryZoneResponse> getActive() {
        return deliveryZoneRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        if (!deliveryZoneRepository.existsById(id)) {
            throw new IllegalArgumentException("DeliveryZone not found: " + id);
        }
        deliveryZoneRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryZoneCheckResponse checkPoint(Double latitude, Double longitude) {
        return findMatchingZone(latitude, longitude)
                .map(zone -> new DeliveryZoneCheckResponse(
                        true,
                        zone.getId(),
                        zone.getName(),
                        zone.getRiskLevel(),
                        riskLabel(zone.getRiskLevel()),
                        riskColor(zone.getRiskLevel()),
                        safeFee(zone),
                        safeDelay(zone),
                        Boolean.TRUE.equals(zone.getRequiresAdminApproval()),
                        buildMessage(zone.getRiskLevel())
                ))
                .orElseGet(() -> new DeliveryZoneCheckResponse(
                        false,
                        null,
                        null,
                        DeliveryZoneRisk.NORMAL,
                        riskLabel(DeliveryZoneRisk.NORMAL),
                        riskColor(DeliveryZoneRisk.NORMAL),
                        0.0,
                        0,
                        false,
                        "No configured risk zone matched this location."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryFeeCheckResponse checkAddress(DeliveryFeeCheckRequest req) {
        String area = firstPresent(req.governorate(), req.city());
        boolean grandTunis = isGrandTunis(area);

        if (grandTunis) {
            return new DeliveryFeeCheckResponse(
                    "Grand Tunis",
                    "#16a34a",
                    6.0,
                    1,
                    "Livraison Grand Tunis disponible a 6 DT."
            );
        }

        return new DeliveryFeeCheckResponse(
                "Tunisie",
                "#2563eb",
                8.0,
                2,
                "Livraison nationale disponible a 8 DT."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeliveryZone> findMatchingZone(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }

        return deliveryZoneRepository.findByActiveTrueOrderByRiskLevelDesc().stream()
                .filter(zone -> containsPoint(zone.getGeoJsonPolygon(), latitude, longitude))
                .max(Comparator.comparingInt(zone -> riskPriority(zone.getRiskLevel())));
    }

    private int riskPriority(DeliveryZoneRisk riskLevel) {
        if (riskLevel == null) return 0;
        return switch (riskLevel) {
            case NORMAL -> 0;
            case REMOTE -> 1;
            case SENSITIVE -> 2;
            case DANGEROUS -> 3;
        };
    }

    private String riskLabel(DeliveryZoneRisk riskLevel) {
        if (riskLevel == null) return "Zone normale";
        return switch (riskLevel) {
            case NORMAL -> "Zone normale";
            case REMOTE -> "Zone eloignee";
            case SENSITIVE -> "Zone sensible";
            case DANGEROUS -> "Zone a validation";
        };
    }

    private String riskColor(DeliveryZoneRisk riskLevel) {
        if (riskLevel == null) return "#16a34a";
        return switch (riskLevel) {
            case NORMAL -> "#16a34a";
            case REMOTE -> "#2563eb";
            case SENSITIVE -> "#f59e0b";
            case DANGEROUS -> "#dc2626";
        };
    }

    private boolean containsPoint(String geoJson, double latitude, double longitude) {
        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode coordinates = root.path("type").asText().equalsIgnoreCase("Feature")
                    ? root.path("geometry").path("coordinates")
                    : root.path("coordinates");

            JsonNode typeNode = root.path("type").asText().equalsIgnoreCase("Feature")
                    ? root.path("geometry").path("type")
                    : root.path("type");

            if (!typeNode.asText().equalsIgnoreCase("Polygon") || !coordinates.isArray() || coordinates.isEmpty()) {
                return false;
            }

            List<double[]> polygon = new ArrayList<>();
            for (JsonNode point : coordinates.get(0)) {
                if (point.isArray() && point.size() >= 2) {
                    double lng = point.get(0).asDouble();
                    double lat = point.get(1).asDouble();
                    polygon.add(new double[]{lat, lng});
                }
            }

            return isPointInPolygon(latitude, longitude, polygon);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isPointInPolygon(double latitude, double longitude, List<double[]> polygon) {
        boolean inside = false;
        int size = polygon.size();

        for (int i = 0, j = size - 1; i < size; j = i++) {
            double latI = polygon.get(i)[0];
            double lngI = polygon.get(i)[1];
            double latJ = polygon.get(j)[0];
            double lngJ = polygon.get(j)[1];

            boolean intersects = ((lngI > longitude) != (lngJ > longitude))
                    && (latitude < (latJ - latI) * (longitude - lngI) / (lngJ - lngI) + latI);

            if (intersects) {
                inside = !inside;
            }
        }

        return inside;
    }

    private void validatePolygon(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalArgumentException("geoJsonPolygon is required.");
        }
        if (!containsPoint(geoJson, 0.0, 0.0)) {
            try {
                JsonNode root = objectMapper.readTree(geoJson);
                String type = root.path("type").asText();
                String geometryType = type.equalsIgnoreCase("Feature")
                        ? root.path("geometry").path("type").asText()
                        : type;
                if (!geometryType.equalsIgnoreCase("Polygon")) {
                    throw new IllegalArgumentException("geoJsonPolygon must be a GeoJSON Polygon or Feature<Polygon>.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid GeoJSON polygon.");
            }
        }
    }

    private DeliveryZoneResponse toResponse(DeliveryZone zone) {
        return new DeliveryZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getGovernorate(),
                zone.getDelegation(),
                zone.getLocality(),
                zone.getRiskLevel(),
                riskLabel(zone.getRiskLevel()),
                riskColor(zone.getRiskLevel()),
                safeFee(zone),
                safeDelay(zone),
                Boolean.TRUE.equals(zone.getRequiresAdminApproval()),
                Boolean.TRUE.equals(zone.getActive()),
                zone.getReason(),
                zone.getGeoJsonPolygon(),
                zone.getCreatedAt(),
                zone.getUpdatedAt()
        );
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) return first.trim();
        if (second != null && !second.isBlank()) return second.trim();
        return "";
    }

    private boolean isGrandTunis(String value) {
        String normalized = normalize(value);
        return normalized.equals("tunis")
                || normalized.equals("ariana")
                || normalized.equals("manouba")
                || normalized.equals("ben arous")
                || normalized.equals("ben arouss");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim()
                .toLowerCase()
                .replace("â", "a")
                .replace("à", "a")
                .replace("ä", "a")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("î", "i")
                .replace("ï", "i")
                .replace("ô", "o")
                .replace("ö", "o")
                .replace("û", "u")
                .replace("ü", "u")
                .replaceAll("\\s+", " ");
    }

    private Double safeFee(DeliveryZone zone) {
        return zone.getFeeAdjustment() != null ? zone.getFeeAdjustment() : 0.0;
    }

    private Integer safeDelay(DeliveryZone zone) {
        return zone.getExtraDelayDays() != null ? zone.getExtraDelayDays() : 0;
    }

    private String buildMessage(DeliveryZoneRisk riskLevel) {
        return switch (riskLevel) {
            case NORMAL -> "Delivery is available normally for this area.";
            case REMOTE -> "This area may require additional delivery time.";
            case SENSITIVE -> "This area requires delivery confirmation.";
            case DANGEROUS -> "This area requires admin review before delivery.";
        };
    }
}
