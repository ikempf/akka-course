# Akka - régles génerales

---

# Délivrance des messages

---

### Cycle de vie

Un message n'est jamais directement envoyé à un acteur en utilisant **tell** ou **ask**.

Plusieurs méchanismes entrent en jeu avant que le message soit éffectivement traité.

---

### Cycle de vie

- Envoi à un *actorRef*
- Mise en file (ou non) dans la mailbox
- Le message est retiré de la mailbox par l'acteur
- Le message est traité (ou non) par l'acteur

---

### Récéption - réseau

- **Local**: l'envoi est iso avec un appel de fonction et atteindra la mailbox
- **Cluster**: l'envoi est sumis aux aléas du réseau, aucun renvoi automatique en cas de perte

---

### Récéption - mailbox

- La mailbox a été **interompue**, le message part en DeadLetter queue
- La mailbox est **bounded** (ex: BoundedMailbox)
- Une mailbox custom peut **refuser** le message

---

### Récéption - acteur

- L'acteur a été **interompu**, le message part en DeadLetter queue 
- Le message est **inconnu** de l'acteur (*unhandled*) 
- Le traitement du message **échoue**

---

### Récéption - résumé

La récéption et le traitement d'un message ne sont pas garanti par akka, tout message peut être : 
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
- **exactly-once**: tout message est reçu exactement une fois

Le premier est le plus performant. Il est possible d'implémenter les autres sémantiques.  

---

### At-least-once delivery

Si la bonne récéption/traitement est primordiale, un ack manuel doit etre implémenté.

Vous pouvez pour cela utiliser le ask pattern.

```
actor1 ==== BusinessMessage ====> actor2 
actor1      <==== Done ====       actor2 
```

---

### Exactly-once delivery

Similaire au **at-least-once** mais il faut garder un historique des tous les messages déjà traités.

Des mécanismes idempotent sont préférables.

---

# Traitement des messages

---

## Thread-safety 

Les messages sont dépilés **séquentiellement** depuis la mailbox.

À tout instant, au plus *un seul* message est en traitement par l'acteur.

---

## Thread-safety

Malgré l'isolation des acteurs les problèmes de concurrences persistent.

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

La récéption de messages est **découplée** du traitement.

La mailbox n'est pas affecté par l'activité de l'acteur.

---

# Ordre des messages

---

L'ordre de récéption est garanti entre **deux acteurs**

```
A envoi  M1, M2, M3, M4 à B
B recoit M1, M2, M3, M4 dans l'ordre
```

Attention: des messages en provenance d'autres acteurs peuvent s'entrelacer dans la séquence.

---

La régle d'ordre s'applique seulement 
- entre **deux acteurs**
- si la mailbox est FIFO


---
 