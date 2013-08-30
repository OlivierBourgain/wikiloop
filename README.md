Code support de l'article [http://www.obourgain.com/blog/2013/08/26/Wikipedia-philosophie.html]

##Contenu du projet

Le projet contient deux partie :

- Le package `com.obourgain.wikiloop.parser` contient le code pour parser un dump de Wikipédia, et générer un fichier contenant
la cible du premier lien pour chaque page.

- Le package `com.obourgain.wikiloop.parser` contient le code d'un service REST qui renvoie la suite des pages atteintes à partir d'une page initiale.


##Fichiers de données
Les fichiers de données sont assez gros (+ de 10 Go pour le dump initial de Wikipédia, 120 Mo pour le fichier issu du parsing), ils ne sont pas dans ce repository.


