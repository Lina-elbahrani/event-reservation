# 🎭 Event Reservation System

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24.x-blue.svg)](https://vaadin.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

**Event Reservation System** est une application web complète de gestion de réservations d'événements culturels développée avec **Java 17**, **Spring Boot 3.x** et **Vaadin 24.x**.

Cette plateforme permet aux organisateurs de publier leurs événements et aux utilisateurs de réserver des places en ligne de manière simple et sécurisée.

---

## ✨ Fonctionnalités Principales

### 👤 Pour les Clients
- ✅ Consultation des événements disponibles
- ✅ Réservation de places (jusqu'à 10 par réservation)
- ✅ Gestion du profil utilisateur
- ✅ Historique complet des réservations
- ✅ Annulation de réservations (sous conditions)
- ✅ Code unique par réservation (EVT-XXXXX)

### 🎪 Pour les Organisateurs
- ✅ Création et gestion d'événements
- ✅ Publication/Brouillon/Annulation d'événements
- ✅ Gestion des réservations par événement
- ✅ Statistiques détaillées (revenus, taux de remplissage)
- ✅ Export CSV des réservations

### 👨‍💼 Pour les Administrateurs
- ✅ Gestion complète des utilisateurs
- ✅ Supervision de tous les événements
- ✅ Gestion globale des réservations
- ✅ Statistiques de la plateforme
- ✅ Activation/Désactivation de comptes

---

## 🛠️ Stack Technique

| Technologie | Version | Usage |
|------------|---------|-------|
| **Java** | 17+ | Langage principal |
| **Spring Boot** | 3.2.x | Framework backend |
| **Vaadin** | 24.x | Framework UI |
| **Spring Data JPA** | - | Persistance des données |
| **Hibernate** | 6.x | ORM |
| **H2 Database** | 2.x | Base de données (dev) |
| **Spring Security** | 6.x | Authentification/Autorisation |
| **Lombok** | 1.18.x | Réduction du boilerplate |
| **Maven** | 3.9.x | Gestion des dépendances |

---

## 🏗️ Architecture

```
event-reservation-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ma/event/eventreservationsystem/
│   │   │       ├── entity/          # Entités JPA
│   │   │       │   ├── Event.java
│   │   │       │   ├── Reservation.java
│   │   │       │   ├── User.java
│   │   │       │   └── enums/       # Énumérations
│   │   │       ├── repository/      # Repositories Spring Data
│   │   │       │   ├── EventRepository.java
│   │   │       │   ├── ReservationRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       ├── service/         # Logique métier
│   │   │       │   ├── EventService.java
│   │   │       │   ├── ReservationService.java
│   │   │       │   └── UserService.java
│   │   │       ├── security/        # Configuration sécurité
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── SecurityService.java
│   │   │       ├── views/           # Vues Vaadin
│   │   │       │   ├── publicviews/ # Pages publiques
│   │   │       │   ├── client/      # Interface client
│   │   │       │   ├── organizer/   # Interface organisateur
│   │   │       │   └── admin/       # Interface admin
│   │   │       └── exception/       # Exceptions personnalisées
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql             # Données de test
│   └── test/                        # Tests unitaires
├── pom.xml                          # Dépendances Maven
└── README.md
```

---

## 🚀 Installation et Lancement

### Prérequis

- **Java 17+** : [Télécharger Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** : [Télécharger Maven](https://maven.apache.org/download.cgi)
- **Git** : [Télécharger Git](https://git-scm.com/)

### Étapes d'installation

1. **Cloner le repository**
```bash
git clone https://github.com/votre-username/event-reservation-system.git
cd event-reservation-system
```

2. **Compiler le projet**
```bash
mvn clean install
```

3. **Lancer l'application**
```bash
mvn spring-boot:run
```

4. **Accéder à l'application**
```
http://localhost:8080
```

5. **Accéder à la console H2** (optionnel)
```
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:eventdb
Username: sa
Password: password
```

---

## 👥 Comptes de Test

| Rôle | Email | Mot de passe | Permissions |
|------|-------|--------------|-------------|
| **Admin** | admin@event.ma | admin123 | Accès complet |
| **Organisateur** | organizer@event.ma | admin123 | Gestion événements |
| **Client** | client@event.ma | admin123 | Réservations |

---


## 🔧 Configuration

### Base de Données H2 (Développement)

Par défaut, l'application utilise H2 en mémoire. Pour personnaliser :

```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:eventdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

### Migration vers PostgreSQL (Production)

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eventdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Configuration de Sécurité

Modifier `SecurityConfig.java` pour personnaliser les règles d'accès :

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/events/**").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
    );
    return http.build();
}
```

---

## 🧪 Tests

### Lancer tous les tests
```bash
mvn test
```

### Lancer un test spécifique
```bash
mvn test -Dtest=EventServiceTest
```

### Générer le rapport de couverture
```bash
mvn jacoco:report
```

---

## 📦 Build et Déploiement

### Créer un JAR exécutable
```bash
mvn clean package
```

Le fichier JAR sera généré dans `target/event-reservation-system-1.0.0.jar`

### Lancer le JAR
```bash
java -jar target/event-reservation-system-1.0.0.jar
```

### Docker (optionnel)

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t event-reservation-system .
docker run -p 8080:8080 event-reservation-system
```

---

## 🎯 Règles Métier Principales

### Événements
- ✅ Date de début obligatoirement dans le futur
- ✅ Date de fin après date de début
- ✅ Capacité maximale > 0
- ✅ Prix unitaire ≥ 0
- ✅ Événement terminé non modifiable
- ✅ Suppression impossible si réservations existent

### Réservations
- ✅ Maximum 10 places par réservation
- ✅ Vérification temps réel des places disponibles
- ✅ Code unique généré automatiquement (EVT-XXXXX)
- ✅ Annulation possible uniquement 48h avant l'événement
- ✅ Calcul automatique du montant total

### Utilisateurs
- ✅ Email unique obligatoire
- ✅ Mot de passe crypté (BCrypt)
- ✅ Rôles : CLIENT, ORGANIZER, ADMIN
- ✅ Un organisateur ne peut modifier que ses propres événements

---

## 🔐 Sécurité

- **Authentification** : Spring Security avec BCrypt
- **Autorisation** : Contrôle d'accès basé sur les rôles
- **Sessions** : Gérées par Spring Security
- **Protection CSRF** : Activée par défaut
- **Validation** : Bean Validation (JSR-303)
- **SQL Injection** : Prévenue par JPA/Hibernate

---

## 🐛 Problèmes Connus et Solutions

### LazyInitializationException

**Problème** : `Could not initialize proxy - no session`

**Solution** :
```java
@Query("SELECT e FROM Event e LEFT JOIN FETCH e.organisateur WHERE e.id = :id")
Optional<Event> findByIdWithOrganisateur(@Param("id") Long id);
```

### Navigation Vaadin

**Problème** : URL avec paramètres ne fonctionne pas

**Solution** :
```java
@Route("event") // Sans /:id
public class EventDetailView implements HasUrlParameter<Long> {
    @Override
    public void setParameter(BeforeEvent event, Long id) {
        // ...
    }
}
```

---

## 🚧 Améliorations Futures

### Court Terme
- [ ] Upload d'images pour événements
- [ ] Notifications par email
- [ ] Paiement en ligne (Stripe/PayPal)
- [ ] Export PDF des réservations

### Moyen Terme
- [ ] Système de notation et commentaires
- [ ] Géolocalisation et carte interactive
- [ ] Application mobile (React Native)
- [ ] Analytics avancées avec graphiques

### Long Terme
- [ ] Migration microservices
- [ ] API REST publique
- [ ] Intégration réseaux sociaux
- [ ] Intelligence artificielle (recommandations)

---

## 📚 Documentation Technique

### Concepts Java Avancés Utilisés

- **Streams API** : Filtrage et transformation de collections
- **Optional** : Gestion sécurisée des valeurs nullables
- **Lambda Expressions** : Programmation fonctionnelle
- **Generics** : Repository pattern type-safe
- **Enums enrichis** : Statuts avec méthodes et couleurs
- **Builder Pattern** : Construction d'objets complexes (Lombok)
- **Dependency Injection** : Couplage faible avec Spring

### Design Patterns Implémentés

- **Repository Pattern** : Abstraction de l'accès aux données
- **Service Layer Pattern** : Logique métier centralisée
- **DTO Pattern** : Transfert de données sécurisé
- **Factory Pattern** : Création de codes uniques
- **Observer Pattern** : Listeners Vaadin

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. **Fork** le projet
2. **Créer** une branche (`git checkout -b feature/AmazingFeature`)
3. **Commit** vos changements (`git commit -m 'Add AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. **Ouvrir** une Pull Request

### Guidelines

- Suivre les conventions de code Java
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation
- Respecter l'architecture existante

---

## 📄 Licence

Ce projet est sous licence **MIT**. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---


---

## 🙏 Remerciements

- **Hassan Zili** - Encadrant du projet
- **Université Abdelmalek Saadi** - FST Tanger
- **Spring Boot Team** - Excellent framework
- **Vaadin Team** - Composants UI modernes
- **Stack Overflow Community** - Aide précieuse

---

## 📞 Support

Pour toute question ou problème :

1. **Ouvrir une issue** : [GitHub Issues](https://github.com/Lina-elbahrani/event-reservation-system/issues)


---

## 📊 Statistiques du Projet

- **Lignes de code** : ~5000+
- **Nombre de classes** : 50+
- **Tests unitaires** : En cours
- **Couverture de code** : ~60%
- **Durée de développement** : 4 semaines

---

**⭐ Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile !**

