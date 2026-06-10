# Dossier Technique — API REST plugin-examplemuz

**Projet :** plugin-examplemuz  
**Date :** juin 2026  
**Auteur :** Matthieu Uzan  

---

## Table des matières

1. [Contexte et objectif](#1-contexte-et-objectif)
2. [Architecture générale](#2-architecture-générale)
3. [Dépendances](#3-dépendances)
4. [Composants implémentés](#4-composants-implémentés)
   - 4.1 [Service de cache](#41-service-de-cache--projectcacheservicejava)
   - 4.2 [Invalidation du cache](#42-invalidation-du-cache--projecthomejava)
   - 4.3 [Service REST](#43-service-rest--projectrestservicejava)
5. [Endpoints REST](#5-endpoints-rest)
   - 5.1 [Liste des projets](#51-get-restexamplemuzprojects)
   - 5.2 [Projet par identifiant](#52-get-restexamplemuzprojectsid)
   - 5.3 [Projet avec compteur de vues](#53-get-restexamplemuzprojectsidviews)
6. [Sécurité](#6-sécurité)
7. [Configuration Spring](#7-configuration-spring)
8. [Journalisation](#8-journalisation)
9. [Tests](#9-tests)
10. [Flux d'une requête](#10-flux-dune-requête)

---

## 1. Contexte et objectif

Le plugin Lutece `plugin-examplemuz` gère des objets **Project** (nom, description, image, coût). Dans le cadre de ce TP, on y a ajouté une **API REST** permettant à des clients externes de consulter les projets au format JSON.

Les objectifs techniques étaient :
- Exposer les données via des endpoints HTTP normalisés
- Mettre en cache les réponses pour limiter les accès base de données
- Invalider le cache lors des mises à jour
- Sécuriser l'API par signature HMAC
- Enrichir un endpoint avec des données de `plugin-extend` (compteur de vues)

---

## 2. Architecture générale

L'API REST de Lutece repose sur **Jersey** (implémentation JAX-RS), intégré via `plugin-rest`. Ce plugin déclare un filtre servlet mappé sur `/rest/*`. Toute requête vers ce chemin est interceptée et routée vers la classe de service REST correspondante, identifiée par son annotation `@Path`.

```
Requête HTTP
    │
    ▼
Filtre Jersey  (plugin-rest — /rest/*)
    │  authentification (HeaderHashAuthenticator)
    ▼
ProjectRestService  (@Path("/rest/examplemuz"))
    │
    ├── ProjectCacheService  (cache en mémoire)
    └── ProjectHome  (accès base de données)
         └── IHitService  (compteur de vues, plugin-extend)
```

---

## 3. Dépendances

Deux dépendances ont été ajoutées dans `pom.xml` :

```xml
<!-- Fournit le filtre Jersey et les annotations JAX-RS -->
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-rest</artifactId>
    <version>[3.3.4]</version>
    <type>lutece-plugin</type>
</dependency>

<!-- Fournit IHitService pour le comptage de vues -->
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-extend</artifactId>
    <version>[1.3.6]</version>
    <type>lutece-plugin</type>
</dependency>
```

`plugin-rest` expose la constante `RestConstants.BASE_PATH = "/rest/"` et enregistre automatiquement tout bean Spring annoté `@Path` comme ressource Jersey.

---

## 4. Composants implémentés

### 4.1 Service de cache — `ProjectCacheService.java`

**Chemin :** `src/java/fr/paris/lutece/plugins/examplemuz/rs/ProjectCacheService.java`

Ce service étend `AbstractCacheableService` de Lutece, qui fournit un cache EHCache configurable. Il est implémenté en singleton.

```java
public class ProjectCacheService extends AbstractCacheableService
{
    private static final String SERVICE_NAME = "examplemuz.ProjectCacheService";
    private static ProjectCacheService _singleton;

    private ProjectCacheService( ) { initCache( SERVICE_NAME ); }

    public static ProjectCacheService getInstance( ) {
        if ( _singleton == null ) { _singleton = new ProjectCacheService( ); }
        return _singleton;
    }
}
```

Les méthodes utilisées (héritées de `AbstractCacheableService`) :

| Méthode | Rôle |
|---------|------|
| `getFromCache(String key)` | Récupère une entrée du cache |
| `putInCache(String key, Object value)` | Stocke une entrée |
| `removeKey(String key)` | Supprime une entrée |

La clé de cache utilisée est `"project_" + id` (ex. `"project_3"`).

---

### 4.2 Invalidation du cache — `ProjectHome.java`

**Chemin :** `src/java/fr/paris/lutece/plugins/examplemuz/business/ProjectHome.java`

Pour garantir la cohérence entre le cache et la base de données, l'invalidation est déclenchée automatiquement lors de toute modification ou suppression d'un projet.

```java
public static Project update( Project project ) {
    _dao.store( project, _plugin );
    ProjectCacheService.getInstance( ).removeKey( "project_" + project.getId( ) );
    return project;
}

public static void remove( int nKey ) {
    _dao.delete( nKey, _plugin );
    ProjectCacheService.getInstance( ).removeKey( "project_" + nKey );
}
```

> Sans cette invalidation, le cache renverrait des données périmées aux clients REST après une modification via l'interface d'administration.

---

### 4.3 Service REST — `ProjectRestService.java`

**Chemin :** `src/java/fr/paris/lutece/plugins/examplemuz/rs/ProjectRestService.java`

Classe principale de l'API. Elle est annotée `@Path("/rest/examplemuz")` et déclarée comme bean Spring pour être découverte par Jersey.

La sérialisation JSON est assurée par **Jackson** (`ObjectMapper`, `ObjectNode`, `ArrayNode`), disponible dans le classpath Lutece.

---

## 5. Endpoints REST

### 5.1 `GET /rest/examplemuz/projects`

Retourne la liste complète des projets.

**Réponse 200 OK :**
```json
{
  "projects": [
    { "id": 1, "name": "Projet A", "description": "...", "cost": "10,00 €" },
    { "id": 2, "name": "Projet B", "description": "...", "cost": "25,00 €" }
  ]
}
```

**Implémentation :**
```java
@GET
@Path( "/projects" )
@Produces( MediaType.APPLICATION_JSON )
public String getProjects( ) {
    // parcourt ProjectHome.getProjectsList() et construit le JSON
}
```

---

### 5.2 `GET /rest/examplemuz/projects/{id}`

Retourne un projet unique identifié par son `id`. La réponse est mise en cache.

**Paramètre :** `{id}` — identifiant numérique du projet

**Réponse 200 OK :**
```json
{ "id": 1, "name": "Projet A", "description": "...", "cost": "10,00 €" }
```

**Codes d'erreur :**

| Code | Condition |
|------|-----------|
| 400 Bad Request | `{id}` n'est pas un entier (`NumberFormatException`) |
| 404 Not Found | Aucun projet trouvé pour cet identifiant |

**Logique de cache :**

```
1. parseInt(strId)  →  400 si échec
2. Chercher dans le cache
   ├── cache hit  →  retourner directement (200)
   └── cache miss →  interroger ProjectHome.findByPrimaryKey(id)
                         ├── absent  →  404
                         └── présent →  sérialiser en JSON
                                        stocker dans le cache
                                        retourner (200)
```

---

### 5.3 `GET /rest/examplemuz/projects/{id}/views`

Retourne un projet enrichi de son nombre de vues, fourni par `plugin-extend`.

**Réponse 200 OK :**
```json
{ "id": 1, "name": "Projet A", "description": "...", "cost": "10,00 €", "nbViews": 42 }
```

**Codes d'erreur :** identiques au endpoint précédent (400, 404).

**Accès au compteur de vues :**
```java
IHitService hitService = SpringContextService.getBean( "extend.hitService" );
Hit hit = hitService.findByParameters( String.valueOf( nId ), "project" );
int nNbViews = ( hit != null ) ? hit.getNbHits( ) : 0;
```

> Ce endpoint ne passe pas par le cache, car le compteur de vues évolue fréquemment et indépendamment des données du projet.

---

## 6. Sécurité

L'API REST est sécurisée par **signature HMAC** via la bibliothèque `library-signrequest`.

**Mécanisme :** chaque requête doit inclure un header contenant une signature calculée à partir d'une clé privée partagée. Le bean `rest.requestAuthenticator` vérifie cette signature avant de router la requête vers le service.

**Configuration dans `examplemuz_context.xml` :**

```xml
<bean id='rest.hashService'
      class='fr.paris.lutece.util.signrequest.security.Sha1HashService' />

<bean id='rest.requestAuthenticator'
      class='fr.paris.lutece.util.signrequest.HeaderHashAuthenticator'>
    <property name='hashService' ref='rest.hashService' />
    <property name='signatureElements'>
        <list><value>key</value></list>
    </property>
    <property name='privateKey'><value>change me</value></property>
    <property name='validityTimePeriod'><value>0</value></property>
</bean>
```

> **Important :** la valeur de `privateKey` doit être modifiée avant toute mise en production.

---

## 7. Configuration Spring

Tous les beans nécessaires sont déclarés dans `webapp/WEB-INF/conf/plugins/examplemuz_context.xml` :

```xml
<!-- DAO et service métier (existants) -->
<bean id="examplemuz.projectDAO"
      class="fr.paris.lutece.plugins.examplemuz.business.ProjectDAO" />

<!-- Bean REST — découvert automatiquement par Jersey -->
<bean id="examplemuz.ProjectRest"
      class="fr.paris.lutece.plugins.examplemuz.rs.ProjectRestService" />

<!-- Sécurité REST -->
<bean id='rest.hashService'
      class='fr.paris.lutece.util.signrequest.security.Sha1HashService' />
<bean id='rest.requestAuthenticator'
      class='fr.paris.lutece.util.signrequest.HeaderHashAuthenticator'>
    ...
</bean>
```

---

## 8. Journalisation

Les échanges REST sont tracés via le logger `lutece.rest` configuré en niveau `DEBUG` dans `log4j2.xml` :

```xml
<Logger name="lutece.rest" level="debug">
    <AppenderRef ref="Application"/>
</Logger>
```

Cela permet de suivre les requêtes reçues et les réponses émises par le filtre Jersey dans les logs de l'application.

---

## 9. Tests

Un fichier de tests unitaires a été créé pour valider les trois endpoints :

**Chemin :** `src/test/java/fr/paris/lutece/plugins/examplemuz/rs/ProjectRestServiceTest.java`

| Test | Description | Résultat attendu |
|------|-------------|-----------------|
| `testGetProjects` | Appel sur `/projects` | Réponse non nulle |
| `testGetProject` | Appel avec un id valide | Code 200 |
| `testGetProjectNotFound` | Appel avec id inexistant (99999) | Code 404 |
| `testGetProjectInvalidId` | Appel avec id non numérique (`"abc"`) | Code 400 |

Les tests étendent `LuteceTestCase` (JUnit 3) et utilisent un contexte Spring de test.

---

## 10. Flux d'une requête

Exemple complet pour `GET /rest/examplemuz/projects/3` :

```
1. Le client envoie :
   GET /rest/examplemuz/projects/3
   Header: signature=<hash HMAC>

2. Le filtre Jersey (plugin-rest) intercepte la requête.

3. HeaderHashAuthenticator vérifie la signature.
   └── Signature invalide → 401 Unauthorized

4. Jersey route vers ProjectRestService.getProject("3").

5. parseInt("3") → nId = 3  (OK)

6. Lookup cache avec clé "project_3"
   ├── Cache HIT  → retourner le JSON mis en cache (200)
   └── Cache MISS → continuer

7. ProjectHome.findByPrimaryKey(3)
   ├── Absent → 404 Not Found
   └── Présent → sérialiser en JSON

8. Stocker dans le cache avec clé "project_3".

9. Retourner :
   HTTP 200 OK
   Content-Type: application/json
   { "id": 3, "name": "...", "description": "...", "cost": "..." }
```
