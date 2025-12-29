-- ============================================
-- INSERTION DES UTILISATEURS (5 minimum requis)
-- ============================================

-- 1 ADMIN
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone) VALUES
    ('Admin', 'System', 'admin@event.ma', '$2a$10$H8RqTeE9Rija1nCMJt4WcOdITXLz0Y64P3vBacL5GBPRm0IOLHB76', 'ADMIN', CURRENT_TIMESTAMP, true, '0661234567');

-- 2 ORGANIZERS
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone) VALUES
                                                                                               ('Alami', 'Hassan', 'organizer1@event.ma', '$2a$10$AAGqbSCuJX6aUfskgKTpKeCVmG6FuohH9mrtz9.lS6zJ4kIOWPiJu', 'ORGANIZER', CURRENT_TIMESTAMP, true, '0662345678'),
                                                                                               ('Bennani', 'Fatima', 'organizer2@event.ma', '$2a$10$AAGqbSCuJX6aUfskgKTpKeCVmG6FuohH9mrtz9.lS6zJ4kIOWPiJu', 'ORGANIZER', CURRENT_TIMESTAMP, true, '0663456789');

-- 2 CLIENTS
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone) VALUES
                                                                                               ('Tazi', 'Ahmed', 'client1@event.ma', '$2a$10$AAGqbSCuJX6aUfskgKTpKeCVmG6FuohH9mrtz9.lS6zJ4kIOWPiJu', 'CLIENT', CURRENT_TIMESTAMP, true, '0664567890'),
                                                                                               ('Idrissi', 'Amal', 'client2@event.ma', '$2a$10$AAGqbSCuJX6aUfskgKTpKeCVmG6FuohH9mrtz9.lS6zJ4kIOWPiJu', 'CLIENT', CURRENT_TIMESTAMP, true, '0665678901');


-- ============================================
-- INSERTION DES ÉVÉNEMENTS (15 minimum requis)
-- ============================================

-- CONCERTS (3 événements)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, organisateur_id, date_creation, date_modification, image_url) VALUES
                                                                                                                                                                                             ('Festival Gnaoua', 'Le plus grand festival de musique Gnaoua du Maroc avec des artistes internationaux', 'CONCERT', '2025-12-20 20:00:00', '2025-12-20 23:30:00', 'Stade Municipal', 'Essaouira', 5000, 150.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Concert de RAI', 'Soirée exceptionnelle de musique RAI avec les plus grands artistes', 'CONCERT', '2025-12-22 21:00:00', '2025-12-23 01:00:00', 'Complexe Sportif Mohammed V', 'Casablanca', 8000, 200.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Jazz Night Marrakech', 'Nuit de jazz sous les étoiles de Marrakech', 'CONCERT', '2026-01-10 19:00:00', '2026-01-10 23:00:00', 'Jardin Majorelle', 'Marrakech', 300, 250.0, 'BROUILLON', 3, CURRENT_TIMESTAMP, null, null);

-- THÉÂTRE (3 événements)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, organisateur_id, date_creation, date_modification, image_url) VALUES
                                                                                                                                                                                             ('Molière - Le Malade Imaginaire', 'Comédie classique de Molière revisitée par la troupe nationale', 'THEATRE', '2025-12-25 19:00:00', '2025-12-25 21:30:00', 'Théâtre Mohammed V', 'Rabat', 500, 120.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Spectacle Comique', 'Soirée stand-up avec les meilleurs humoristes marocains', 'THEATRE', '2025-12-28 20:00:00', '2025-12-28 22:00:00', 'Palais des Arts et de la Culture', 'Casablanca', 400, 100.0, 'PUBLIE', 3, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Tragédie Moderne', 'Pièce dramatique contemporaine primée au festival international', 'THEATRE', '2026-01-05 18:00:00', '2026-01-05 20:30:00', 'Théâtre National', 'Tanger', 350, 90.0, 'ANNULE', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- CONFÉRENCES (3 événements)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, organisateur_id, date_creation, date_modification, image_url) VALUES
                                                                                                                                                                                             ('Innovation & IA au Maroc', 'Conférence sur l''intelligence artificielle et l''innovation digitale', 'CONFERENCE', '2025-12-30 09:00:00', '2025-12-30 17:00:00', 'Sofitel Casablanca', 'Casablanca', 1000, 500.0, 'PUBLIE', 3, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Entrepreneuriat Digital', 'Summit des startups et entrepreneurs du Maghreb', 'CONFERENCE', '2026-01-15 08:30:00', '2026-01-15 18:00:00', 'Palais des Congrès', 'Marrakech', 800, 300.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Sommet du Climat Africain', 'Conférence internationale sur le changement climatique en Afrique', 'CONFERENCE', '2026-02-01 09:00:00', '2026-02-03 18:00:00', 'Centre International de Conférences', 'Rabat', 2000, 50.0, 'BROUILLON', 3, CURRENT_TIMESTAMP, null, null);

-- SPORT (3 événements)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, organisateur_id, date_creation, date_modification, image_url) VALUES
                                                                                                                                                                                             ('Finale Coupe du Trône', 'Grande finale de la coupe du Trône de football', 'SPORT', '2026-01-20 16:00:00', '2026-01-20 18:00:00', 'Stade Mohammed V', 'Casablanca', 45000, 80.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Marathon International de Rabat', 'Course internationale marathon 42km à travers la capitale', 'SPORT', '2026-02-10 07:00:00', '2026-02-10 13:00:00', 'Centre-ville', 'Rabat', 5000, 200.0, 'PUBLIE', 3, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Tournoi de Tennis ATP', 'Tournoi international de tennis professionnel', 'SPORT', '2026-03-01 10:00:00', '2026-03-07 20:00:00', 'Complexe Sportif Al Amal', 'Marrakech', 3000, 150.0, 'TERMINE', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- AUTRE (3 événements)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, organisateur_id, date_creation, date_modification, image_url) VALUES
                                                                                                                                                                                             ('Festival Gastronomique', 'Découvrez les saveurs du Maroc et du monde entier', 'AUTRE', '2026-01-25 11:00:00', '2026-01-25 22:00:00', 'Esplanade de la Corniche', 'Casablanca', 10000, 50.0, 'PUBLIE', 2, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Salon du Livre de Casablanca', 'Rencontres littéraires et dédicaces d''auteurs marocains et internationaux', 'AUTRE', '2026-02-15 10:00:00', '2026-02-20 19:00:00', 'Parc des Expositions', 'Casablanca', 15000, 0.0, 'PUBLIE', 3, CURRENT_TIMESTAMP, null, null),
                                                                                                                                                                                             ('Exposition d''Art Contemporain', 'Exposition des œuvres d''artistes marocains émergents', 'AUTRE', '2026-03-15 10:00:00', '2026-03-30 18:00:00', 'Musée Mohammed VI', 'Rabat', 200, 30.0, 'BROUILLON', 2, CURRENT_TIMESTAMP, null, null);


-- ============================================
-- INSERTION DES RÉSERVATIONS (20 minimum requis)
-- ============================================

-- Réservations pour "Festival Gnaoua" (Event ID: 1)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (4, 1, 2, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10001', 'Places VIP s''il vous plaît'),
                                                                                                                                                   (5, 1, 4, 600.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10002', 'Réservation pour famille'),
                                                                                                                                                   (4, 1, 1, 150.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10003', null);

-- Réservations pour "Concert de RAI" (Event ID: 2)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (5, 2, 3, 600.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10004', 'Groupe d''amis'),
                                                                                                                                                   (4, 2, 5, 1000.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10005', null),
                                                                                                                                                   (5, 2, 2, 400.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-10006', 'Changement de plans');

-- Réservations pour "Le Malade Imaginaire" (Event ID: 4)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (4, 4, 2, 240.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10007', 'Places centrales préférées'),
                                                                                                                                                   (5, 4, 3, 360.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10008', null);

-- Réservations pour "Spectacle Comique" (Event ID: 5)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (4, 5, 4, 400.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10009', null),
                                                                                                                                                   (5, 5, 2, 200.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10010', 'Cadeau d''anniversaire');

-- Réservations pour "Innovation & IA au Maroc" (Event ID: 7)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (4, 7, 1, 500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10011', 'Intéressé par l''IA'),
                                                                                                                                                   (5, 7, 2, 1000.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10012', 'Pour mon équipe'),
                                                                                                                                                   (4, 7, 3, 1500.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10013', null);

-- Réservations pour "Entrepreneuriat Digital" (Event ID: 8)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (5, 8, 5, 1500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10014', 'Startup team'),
                                                                                                                                                   (4, 8, 1, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10015', null);

-- Réservations pour "Finale Coupe du Trône" (Event ID: 10)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (4, 10, 6, 480.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10016', 'Fan du Wydad'),
                                                                                                                                                   (5, 10, 4, 320.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10017', null),
                                                                                                                                                   (4, 10, 2, 160.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-10018', 'Indisponible ce jour');

-- Réservations pour "Marathon International de Rabat" (Event ID: 11)
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
                                                                                                                                                   (5, 11, 1, 200.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10019', 'Premier marathon !'),
                                                                                                                                                   (4, 11, 2, 400.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10020', 'Avec mon frère');