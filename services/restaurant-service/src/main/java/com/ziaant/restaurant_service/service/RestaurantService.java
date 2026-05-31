package com.ziaant.restaurant_service.service;

import com.ziaant.restaurant_service.dto.*;
import com.ziaant.restaurant_service.entity.*;
import com.ziaant.restaurant_service.repository.*;
import com.ziaant.restaurant_service.security.JwtUtil;
import com.ziaant.restaurant_service.config.RabbitMQConfig;
import com.ziaant.restaurant_service.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository   menuItemRepository;
    private final JwtUtil              jwtUtil;
    private final RabbitTemplate       rabbitTemplate;

    private void sendNotification(String to, String subject, String body) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .to(to).subject(subject).body(body).build();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event);
        } catch (Exception e) { }
    }

    private String extraireToken(String authHeader) {
        if (authHeader == null) throw new IllegalArgumentException("Token manquant.");
        String token = authHeader.trim();
        while (token.startsWith("Bearer ")) token = token.substring(7).trim();
        return token;
    }

    private void verifierToken(String token) {
        if (!jwtUtil.isTokenValid(token))
            throw new IllegalArgumentException("Token invalide ou expire.");
    }

    private void verifierAdmin(String token) {
        verifierToken(token);
        if (!"ADMIN".equals(jwtUtil.extractRole(token)))
            throw new IllegalArgumentException("Acces refuse. Reserve a l'administrateur.");
    }

    private void verifierRestaurateur(String token) {
        verifierToken(token);
        String role = jwtUtil.extractRole(token);
        if (!"RESTAURATEUR".equals(role) && !"ADMIN".equals(role))
            throw new IllegalArgumentException("Acces refuse. Reserve au restaurateur.");
    }

    // ── Endpoints publics ──────────────────────────────────────

    public List<RestaurantResponse> getPublicList(String ville, String cuisine, String search, Integer prixMin, Integer prixMax) {
        List<Restaurant> tous = restaurantRepository.findByStatut(StatutRestaurant.ACTIF);
        return tous.stream()
            .filter(r -> {
                if (ville != null && !ville.isBlank() && !r.getVille().equalsIgnoreCase(ville)) return false;
                if (cuisine != null && !cuisine.isBlank() && !r.getCuisine().equalsIgnoreCase(cuisine)) return false;
                if (search != null && !search.isBlank()) {
                    String q = search.toLowerCase();
                    boolean match = (r.getName() != null && r.getName().toLowerCase().contains(q))
                            || (r.getVille() != null && r.getVille().toLowerCase().contains(q))
                            || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(q))
                            || (r.getAddress() != null && r.getAddress().toLowerCase().contains(q));
                    if (!match) return false;
                }
                if (prixMin != null && r.getPrixMax() != null && r.getPrixMax() < prixMin) return false;
                if (prixMax != null && r.getPrixMin() != null && r.getPrixMin() > prixMax) return false;
                return true;
            })
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public RestaurantResponse getPublicDetail(Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant introuvable."));
        if (r.getStatut() != StatutRestaurant.ACTIF)
            throw new IllegalArgumentException("Ce restaurant n'est pas disponible.");
        return toResponseAvecMenu(r);
    }

    public MenuResponse getMenu(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant introuvable."));
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);
        return MenuResponse.builder()
                .entrees(filtrerCategorie(items, CategorieMenu.ENTREE))
                .plats(filtrerCategorie(items, CategorieMenu.PLAT))
                .desserts(filtrerCategorie(items, CategorieMenu.DESSERT))
                .boissons(filtrerCategorie(items, CategorieMenu.BOISSON))
                .build();
    }

    // ── Endpoints restaurateur ─────────────────────────────────

    @Transactional
    public RestaurantResponse creer(String authHeader, RestaurantRequest request) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);
        String email = jwtUtil.extractEmail(token);

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cuisine(request.getCuisine())
                .address(request.getAddress())
                .ville(request.getVille())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .openHours(request.getOpenHours())
                .priceRange(request.getPriceRange())
                .prixMin(request.getPrixMin())
                .prixMax(request.getPrixMax())
                .tags(request.getTags())
                .rating(0.0)
                .reviewCount(0)
                .featured(false)
                .statut(StatutRestaurant.EN_ATTENTE)
                .restaurateurId(jwtUtil.extractUserId(token))
                .build();

        restaurantRepository.save(restaurant);
        log.info("Nouveau restaurant cree : {} par {}", restaurant.getName(), email);
        return toResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse modifier(String authHeader, Long id, RestaurantRequest request) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant introuvable."));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setVille(request.getVille());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOpenHours(request.getOpenHours());
        restaurant.setPriceRange(request.getPriceRange());
        restaurant.setTags(request.getTags());

        restaurantRepository.save(restaurant);
        return toResponse(restaurant);
    }

    public List<RestaurantResponse> getMesRestaurants(String authHeader) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);
        Long restaurateurId = jwtUtil.extractUserId(token);
        return restaurantRepository.findByRestaurateurId(restaurateurId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ── Menu ───────────────────────────────────────────────────

    @Transactional
    public MenuItemResponse ajouterPlat(String authHeader, Long restaurantId, MenuItemRequest request) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant introuvable."));
        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categorie(request.getCategorie())
                .disponible(request.isDisponible())
                .restaurant(restaurant)
                .build();
        menuItemRepository.save(item);
        return toMenuItemResponse(item);
    }

    @Transactional
    public MenuItemResponse modifierPlat(String authHeader, Long restaurantId, Long itemId, MenuItemRequest request) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Plat introuvable."));
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategorie(request.getCategorie());
        item.setDisponible(request.isDisponible());
        menuItemRepository.save(item);
        return toMenuItemResponse(item);
    }

    @Transactional
    public void supprimerPlat(String authHeader, Long restaurantId, Long itemId) {
        String token = extraireToken(authHeader);
        verifierRestaurateur(token);
        menuItemRepository.deleteById(itemId);
    }

    // ── Admin ──────────────────────────────────────────────────

    public List<RestaurantResponse> getTous(String authHeader) {
        String token = extraireToken(authHeader);
        verifierAdmin(token);
        return restaurantRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<RestaurantResponse> getEnAttente(String authHeader) {
        String token = extraireToken(authHeader);
        verifierAdmin(token);
        return restaurantRepository.findByStatut(StatutRestaurant.EN_ATTENTE).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse changerStatut(String authHeader, Long id, String statut) {
        String token = extraireToken(authHeader);
        verifierAdmin(token);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant introuvable."));
        restaurant.setStatut(StatutRestaurant.valueOf(statut));
        restaurantRepository.save(restaurant);
        if ("ACTIF".equals(statut)) {
            sendNotification(restaurant.getEmail(),
                    "Votre restaurant est en ligne - ReserveTable CM",
                    "Bonjour,\n\nVotre restaurant \"" + restaurant.getName() + "\" a ete valide.\nIl est maintenant visible sur ReserveTable CM.\n\nL'equipe ReserveTable CM");
        } else if ("SUSPENDU".equals(statut)) {
            sendNotification(restaurant.getEmail(),
                    "Restaurant suspendu - ReserveTable CM",
                    "Bonjour,\n\nVotre restaurant \"" + restaurant.getName() + "\" a ete suspendu.\n\nL'equipe ReserveTable CM");
        }
        String msg = "ACTIF".equals(statut) ? "Restaurant valide et publie." : "Restaurant suspendu.";
        return new MessageResponse(msg);
    }

    // ── Mappers ────────────────────────────────────────────────

    private RestaurantResponse toResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .cuisine(r.getCuisine())
                .address(r.getAddress())
                .ville(r.getVille())
                .phone(r.getPhone())
                .email(r.getEmail())
                .imageUrl(r.getImageUrl())
                .openHours(r.getOpenHours())
                .priceRange(r.getPriceRange())
                .prixMin(r.getPrixMin())
                .prixMax(r.getPrixMax())
                .rating(r.getRating())
                .reviewCount(r.getReviewCount())
                .featured(r.getFeatured())
                .tags(r.getTags())
                .statut(r.getStatut().name())
                .restaurateurId(r.getRestaurateurId())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private RestaurantResponse toResponseAvecMenu(Restaurant r) {
        List<MenuItem> items = menuItemRepository.findByRestaurantId(r.getId());
        RestaurantResponse response = toResponse(r);
        response.setMenuItems(items.stream().map(this::toMenuItemResponse).collect(Collectors.toList()));
        return response;
    }

    private MenuItemResponse toMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .categorie(item.getCategorie().name())
                .disponible(item.isDisponible())
                .build();
    }

    private List<MenuItemResponse> filtrerCategorie(List<MenuItem> items, CategorieMenu cat) {
        return items.stream()
                .filter(i -> i.getCategorie() == cat)
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }
}
