# Akka - règles génerales

---

# Délivrance des messages

---

### Cycle de vie

Un message n'est jamais directement envoyé à un acteur en utilisant **tell** ou **ask**.

Plusieurs mécanismes entrent en jeu avant que le message soit effectivement traité.

---

### Cycle de vie

- Envoi a un *actorRef*
- Mise en file (ou non) dans la mailbox
- Le message est retiré de la mailbox par l'acteur
- Le message est traité (ou non) par l'acteur

---

### Envoi du message

- **Local**: l'envoi est identique avec un appel de fonction et atteindra toujours la mailbox
- **Cluster**: l'envoi est sumis aux aléas du réseau, aucun renvoi automatique en cas de perte

---

### Réception du message

- La mailbox a été **interompue**, le message part en DeadLetter queue
- Une mailbox **bounded** (ex: BoundedMailbox) peut ignorer le message
- Une mailbox custom peut **refuser** le message

---

### Traitement message

- L'acteur a été **interompu**, le message part en DeadLetter queue 
- Le message est **inconnu** de l'acteur (*unhandled*) 
- Le traitement du message **échoue**

---

### Perte de message - résumé

La réception et le traitement d'un message ne sont pas garantis par akka, tout message peut être : 
- *perdu* par le réseau (dans un contexte cluster)
- *refusé* par la mailbox
- *ignoré* par l'acteur

---

## At-most-once delivery

Akka ne renverra **jamais** automatiquement un message.

Vous pouvez être certain de ne pas recevoir de doublon suite à des problèmes techniques.

---

La sémantique du **at-most-one** s'oppose à
- **at-least-once**: tout message est réçu une ou plusieurs fois
- **exactly-once**: tout message est reçu exactement/traité une fois

Le premier est le plus performant. Il est possible d'implémenter les deux autres manuellement.  

---

## At-least-once delivery

Si la bonne réception/traitement est primordiale, un ack manuel doit être implémenté.

```
actor1 ==== BusinessMessage ====> actor2 
actor1      <==== Done ====       actor2 
```

Vous pouvez pour cela utiliser le *ask* pattern.

---

## Exactly-once delivery

Similaire au **at-least-once** mais il faut garder un historique de tous les messages déjà traités.

Des mécanismes idempotents sont préférables.

---

# Traitement des messages

---

## Thread-safety 

Les messages sont dépilés **séquentiellement** depuis la mailbox.

À tout instant, au plus **un seul** message est en traitement par l'acteur.

---

## Thread-safety

Malgré l'isolation des acteurs les problèmes de concurrence persistent.

```
var counter = 0
asyncOperation.onComplete(_ => counter = counter + 1)
```

La lambda est éxécuté sur un autre thread !

---

## Thread-safety

Attention aux closures, ne pas référencer
- vars
- objets mutables
- sender()
- context
- etc.

---

## Thread-safety

Suppression des vars en utilisant `context.become`

```
var counter = 0
def receive = { case Msg => counter = counter + 1 }
```

```
def receive = counting(0)
def counting(counter: Int): Receive = { case Msg => context.become(counting(counter + 1)) }
```

---

## Récéption et traitement

La réception de messages est **découplée** du traitement.

La mailbox n'est pas affectée par l'activité de l'acteur.

---

# Ordre des messages

---

L'ordre de réception est garanti entre **deux acteurs**

```
A envoi  M1, M2, M3, M4 à B
B recoit M1, M2, M3, M4 dans l'ordre
```

Attention: des messages en provenance d'autres acteurs peuvent s'entrelacer dans la séquence.

---

La règle d'ordre s'applique seulement 
- entre **deux acteurs**
- si la mailbox est FIFO

---

# Cycle de vie d'un acteur

---

En plus de la méthode obligatoire **receive** il est possible de surcharger d'autres méthodes
- preStart
- postStop
- preRestart
- postRestart 

---

### preStart

- Appelé après le démarrage de l'acteur
- Permet d'exécuter du code d'initialisation
- Par défaut cette méthode ne fait rien

---

### postStop

- Appelé après l'arrêt de l'acteur
- Permet de libérer des ressources
- Par défaut cette méthode ne fait rien

---

### preRestart

- Appelé avant le redémarrage de l'acteur
- Permet de libérer des ressources
- Par défaut: termine tous les enfants de l'acteur, appelle **postStop** 
- Deux arguments
 - L'exception à l'origine du redémarrage
 - L'éventuel message en cours de traitement

---

### postRestart

- Appelé après le redémarrage de l'acteur
- Par défaut: appelle preStart

 