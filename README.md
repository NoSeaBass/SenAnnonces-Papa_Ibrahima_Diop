# Projet Android — SenAnnonces

Les sites de petites annonces (CoinAfrique, Expat-Dakar…) font partie du
quotidien à Dakar : on y vend un téléphone, on y cherche un appartement,
on y trouve un canapé d'occasion. Ce projet consiste à développer
SenAnnonces, une application Android qui consomme une API de petites
annonces localisées à Dakar, avec des prix en FCFA.

# Arborescence du projet

```txt
app/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── senannonces/
        │               ├── Annonce.java
        │               ├── AnnonceAdapter.javaa
        │               ├── AnnonceViewHolder.java
        │               ├── Authentification.java
        │               ├── DetailAnnonce.java
        │               ├── MainActivity.java
        │               └── Publication.java
        │
        └── res/
            ├── drawable/
            │   ├── filter.png
            │   └── profile.jpg
            │
            ├── layout/
            │   ├── activity_authentification.xml
            │   ├── activity_detail_annonce.xml
            │   ├── activity_main.xml
            │   ├── activity_publication.xml
            │   └── annonce_design.xml
            │
            └── values/
                ├── colors.xml
                └── strings.xml
```

# Présentation de l'applications et des fonctionnalités du projet

À l'ouverture de l'application vous vous retrouverez face au menu pincipale où vous pourrez consulter les différents produits vendus à travers le serveur. L'interface de départ contiente aussi une barre de recherche, une liste déroulante ces catégories pour facilité la recherche mais aussi nous y retrouvons une section qui indique si vous êtes connecté ou pas et un bouton `+` pour ajouter un produit si nous somme connecter.

Cliquer sur le profil vous mêne à la page d'authentification qui varie selon votre status :
- Si vous êtes connecter, vous ne pouvez que vous deconnectez
- Sinon, vous avez l'option de vous connecter avec un compte existant ou vous inscrire (ces deux choix sont séparer par les boutons radios... selon le chois fait, le code jouera sur la visibiliter des champs et sur le texte du bouton). Bien evidement vous reinscrire avec un mail qui existe déja (Un message d'erreur indiquant que le mail est deja pris s'affichera).

Clique sur le plus `+` vous enmenera sur un formulaire de publication pour un nouveau produit (si vous êtes connecter). Si vous n'êtes pas connecter cliquer sur le `+` vous renvois à la page d'authentification. Et si par chance l'utilisateur by pass cette redirection, la publication d'un produit verifira aussi si l'uilisateur est connecté

# API Backend

L'application communique avec l'API REST hébergée sur https://senannonces.89-167-122-158.sslip.io/.
Endpoints consommés :
- GET /api/categories : Récupération de la liste des catégories.
- GET /api/annonces : Récupération des annonces.
- POST /api/auth/login : Authentification de l'utilisateur.
- POST /api/auth/register : Inscription d'un nouvel utilisateur.
- POST /api/annonces : Ajout d'une nouvelle annonce.

# Installation

Clonez ce dépôt sur votre machine locale :
```
git clone https://github.com/
```
