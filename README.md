# Objets Perdus — Application Android

Application communautaire de déclaration d'objets perdus ou trouvés.

## Compte de démonstration
- **Email :** jean@test.com
- **Mot de passe :** 123456

## Fonctionnalités
- Déclarer un objet perdu ou trouvé avec photo, géolocalisation, catégorie
- Liste filtrable par type (Perdu/Trouvé) et recherche textuelle
- Détail avec carte Google Maps, appel, SMS, partage
- Historique personnel avec filtre par statut (En cours / Résolu)
- Notifications locales lors de la publication
- Widget d'accueil affichant le nombre d'objets

## Briques techniques
| Brique | Implémentation |
|---|---|
| **RecyclerView** | LostItemsActivity, MyAdsActivity, HistoryActivity |
| **Widget** | LostFoundWidget (AppWidgetProvider) |
| **SharedPreferences** | Session utilisateur, toggle notifications |
| **SQLite** | DatabaseHelper — users + items, CRUD complet |
| **Fragments** | SupportMapFragment dans LostItemDetailActivity |
| **Intents explicites** | Navigation entre activités |
| **Intents implicites** | ACTION_CALL, ACTION_SENDTO (SMS), ACTION_SEND (partage), ACTION_IMAGE_CAPTURE, ACTION_PICK |
| **AsyncTask** | AsyncTaskHelper — deprecated + alternative Executor |
| **Caméra** | ACTION_IMAGE_CAPTURE via FileProvider |
| **Appels** | ACTION_CALL avec permission CALL_PHONE |
| **SMS** | ACTION_SENDTO smsto: avec message pré-rempli |
| **Google Maps** | MapView (CreateAd) + SupportMapFragment (Detail) |
| **Retrofit** | RetrofitService avec intercepteur mock (bouchonné) |
| **Notifications** | NotificationHelper — canal + notifications locales |

## Structure
```
com.example.myapplication/
├── model/LostItem.java          -- Modèle de données
├── data/DatabaseHelper.java     -- SQLite (users + items)
├── util/
│   ├── NotificationHelper.java  -- Notifications locales
│   ├── RetrofitService.java     -- Retrofit bouchonné
│   ├── AsyncTaskHelper.java     -- AsyncTask + Executor
│   ├── LocaleHelper.java        -- Forcer le français
│   └── ThemeHelper.java         -- Thème clair/sombre
├── widget/LostFoundWidget.java  -- Widget d'accueil
├── MyApplication.java           -- Initialisation
├── MainActivity.java            -- Écran d'accueil
├── LoginActivity.java           -- Connexion SQLite
├── RegisterActivity.java        -- Inscription
├── BaseActivity.java            -- Base avec Toolbar + BottomNav
├── LostItemsActivity.java      -- Liste principale
├── CreateAdActivity.java        -- Nouvelle déclaration
├── LostItemDetailActivity.java  -- Détail + carte + contact
├── MyAdsActivity.java           -- Mes déclarations
├── HistoryActivity.java         -- Historique
├── ProfileActivity.java         -- Profil + stats
└── LostItemAdapter.java         -- Adaptateur RecyclerView
```

## Configuration
1. Remplacer `YOUR_GOOGLE_MAPS_API_KEY` dans `AndroidManifest.xml`
2. Ouvrir avec Android Studio
3. Sync Gradle et lancer sur émulateur/device

## Design
- Material Design 3 avec thème clair/sombre automatique
- Couleurs : bleu (#1976D2), rouge (perdu), vert (trouvé)
- Cards arrondies, chips de filtre, badges colorés
