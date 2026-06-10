![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=plugin-examplemuz-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-examplemuz&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-examplemuz)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-examplemuz&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-examplemuz)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-examplemuz&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-examplemuz)

# Plugin examplemuz

## Introduction

`plugin-examplemuz` est un plugin Lutece de démonstration gérant des objets **Project**. Il illustre les fonctionnalités suivantes :

- Gestion CRUD de projets via l'interface d'administration Lutece
- API REST exposant les projets au format JSON
- Cache des ressources REST avec invalidation automatique
- Validation personnalisée (`@MultipleOf`) sur le champ coût
- Comptage de vues via `plugin-extend`

Un projet possède les attributs suivants :

| Champ | Type | Description |
|-------|------|-------------|
| id | int | Identifiant unique |
| name | String | Nom du projet |
| description | String | Description |
| image_url | String | URL de l'image |
| cost | int | Coût en centimes (doit être un multiple valide) |

## Configuration

### Dépendances

Le plugin requiert :
- `plugin-rest` ≥ 3.3.4 — filtre Jersey pour l'API REST
- `plugin-extend` ≥ 1.3.6 — comptage de vues

### Sécurité de l'API REST

L'API REST est sécurisée par signature HMAC (`HeaderHashAuthenticator`). La clé privée est à configurer dans `examplemuz_context.xml` :

```xml
<bean id='rest.requestAuthenticator'
      class='fr.paris.lutece.util.signrequest.HeaderHashAuthenticator'>
    <property name='privateKey'><value>MA_CLE_PRIVEE</value></property>
    ...
</bean>
```

## Usage

### Interface d'administration

Les projets sont gérables depuis **Plugins > Examplemuz** dans le back-office Lutece.

### API REST

Base URL : `/rest/examplemuz`

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/projects` | Liste tous les projets |
| GET | `/projects/{id}` | Retourne un projet par id (réponse mise en cache) |
| GET | `/projects/{id}/views` | Retourne un projet avec son nombre de vues |

**Exemple de réponse — `/projects/1`**
```json
{
  "id": 1,
  "name": "Mon projet",
  "description": "Description du projet",
  "cost": "10,00 €"
}
```

**Exemple de réponse — `/projects/1/views`**
```json
{
  "id": 1,
  "name": "Mon projet",
  "description": "Description du projet",
  "cost": "10,00 €",
  "nbViews": 42
}
```

**Codes de retour**

| Code | Cas |
|------|-----|
| 200 | Succès |
| 400 | Identifiant invalide (non numérique) |
| 404 | Projet introuvable |

[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-examplemuz/)
