# Projet ACOL / CAWEB 2021

Plateforme de découverte et d'édition d'**Histoires dont vous êtes le héros**™.

Michael GELLENONCOURT - Alexandre JEUNOT-CAIRE - Tanguy POISON - Guillaume RICARD

## Installation

Ce projet nécessite Java/J2E 7 et tourne sur un serveur Tomcat 8. Pour le
déployer installez `maven`.

## Déploiement

1. Par défaut le serveur de déploiement est `localhost:8080` pensez donc à
   enregistrer vos identifiants dans `~/.m2/settings.xml` (pour déployer sur un
       autre serveur changer `pom.xml`)
2. Si votre base de données est hébergée sur `adminer`, vous devez être
   connecté au VPN. (pour configurer la source de données éditer `context.xml`)
3. `mvn tomcat:deploy`

## Tests

TODO
