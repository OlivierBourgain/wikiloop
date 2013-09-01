En cliquant sur le premier lien d'un article de Wikipédia, et en répétant l'opération, 85% des articles mènent à la page Philosophie.
Cette application permet de le vérifier.

Voir également [mon post](http://www.obourgain.com/blog/2013/08/26/Wikipedia-philosophie.html) sur le sujet.

##Contenu du projet

Le projet contient trois partie :

- Le package `com.obourgain.wikiloop.parser` contient le code pour parser un dump de Wikipédia, et générer un fichier contenant
la cible du premier lien pour chaque page.

- Le package `com.obourgain.wikiloop.parser` contient le code d'un service REST qui renvoie la suite des pages atteintes à partir d'une page initiale.

- Le répertoire `www` contient la page HTML qui appelle le service REST. Cette page est disponible sur mon site : [www.obourgain.com/wikiloop/](http://www.obourgain.com/wikiloop)

##Fichiers de données
Les fichiers de données sont assez gros (+ de 10 Go pour le dump initial de Wikipédia, 120 Mo pour le fichier issu du parsing), ils ne sont pas dans ce repository.


