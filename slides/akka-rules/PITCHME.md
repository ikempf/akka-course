# Akka - régles génerales


---

## Mailbox

Un message n'est jamais directement envoyé à un acteur en utilisant **tell** ou **ask**.

Plusieurs méchanismes entrent en jeu avant que le message soit éffectivement traité.

---

Le cycle de vie d'un message

- Envoi à un *actorRef*
- Mise en file (ou non) dans la mailbox
- Le message est retiré de la mailbox par l'acteur
- Le message est traité (ou non) par l'acteur

---

### Récéption - réseau

- Local: l'envoi est iso avec un appel de fonction et atteindra la mailbox
- Cluster: l'envoi est sumis aux aléas du réseau, aucun renvoi automatique en cas de perte

---

### Récéption - mailbox

- La mailbox a été **interompu**, le message part en DeadLetter queue
- La mailbox est **bounded** (ex: BoundedMailbox)
- Une mailbox custom peut **refuser** le message

---

### Récéption - acteur

- L'acteur a été **interompu**, le message part en DeadLetter queue 
- Le message est **inconnu** de l'acteur (pas dans le receive) 
- Le traitement du message **échoue**

---

### Récéption - résumé

La récéption et le traitement d'un message ne sont pas garanti par akka.

Tout message peut être 
- *perdu* par le réseau (dans un contexte cluster)
- *refusé* par la mailbox
- *ignoré* par l'acteur

---

## At-most-once delivery

Akka ne renverra **jamais** automatiquement un message.

Vous pouvez être certain de ne jamais recevoir de doublon suite à des problèmes techniques.


---

Si la bonne récéption/traitement est primordiale, un ack manuel doit etre implementé.

Vous pouvez pour cela utiliser le ask pattern.

```
actor1 =BusinessMessage=> actor2 
actor1      <=Done=       actor2 
```

---

## 
 
Au plus *un seul* message est en traitement 