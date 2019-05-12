
# Akka-stream

---

## Traitement naïf de données
```scala
def saveToDb(input: String): Future[String]

val lines: Iterator[String] = Source.fromFile("big-input.csv").getLines()

while (lines.nonEmpty) {
  saveToDb(lines.next())
}
```

---

Deux schémas sont possibles

- La capacité de consommation de données est plus rapide que la production
- La production de données est plus rapide que la consommation

---

### Production lente - Consommation rapide

- Le streaming se déroule sans problème
- Rarement garanti dans un système d'information avec de nombreux acteurs s'impactant mutuellement 

---

### Production rapide - Consommation lente

Pour une taille de données suffisamment grande, une des erreurs suivantes est forcément rencontrée 

- Mémoire insuffisante
- Threads insuffisants
- Exception "garde-fou" (seuil de connexions jdbc, http, file-descriptors, etc.)

---

### Le problème

Le **débit** du flux de données est contrôlée par le producteur.

Le consommateur **subit** la vitesse du producteur.

---

### Mauvaises solutions

- Throttling "artificiel" du producteur
- Scaling manuel du hardware sous-jacent
- Augmentation des buffers

---

### Solution

Il faut réguler le flux en fonction du **consommateur**.

On parle alors de **back-pressure**.

Sans régulation de cette dernière, le bon traitement de la donnée dépend de **facteurs externes** (taille de la donnée, vitesse de services tiers, etc.)

---

## Flow API

Le premier niveau d'utilisation d'akka-stream.

Réponds à la majorité des besoins tout en restant très expressif.

---

### Flow API - limitations

Modélisation de streams **linéaires** uniquement.


```
Source ==> transformation ==> transormation ==> Sink
```

---

## Graph API

Comme son nom l'indique, permet de modéliser des graphes.

```
Source1 ==>                ==> transormation1 
Source2 ==> transformation ==> transormation2 ==> Sink
Source3 ==>                
```

---

### Graph API

- Api mutable
- Partiellement vérifié à la compilation
- Danger de deadlock
- Très puissant (Broadcast, Partition, Bidirectional etc.)

---

## GraphStage Api

Si les *stages* de la graph api ne suffisent pas.

Les stages custom sont usuellement mutables.

L'utilisation est délicate: 
- Callbacks
- Effets de bord
- Pull/push manuel
- Complétion normale/exceptionnelle
- Etc.

---

### GraphStage Api - Exemples

- Compteur d'éléments
- Monitoring de vélocité
- Agrégation

---

## Remarques de fin

- Ne jamais streamer sans considérer la backpressure
- Toujours utiliser l'api la plus simple
- Comme dans akka-actor il ne faut pas bloquer
- Ne pas commencer par optimiser, les paramètres par défaut suffisent usuellement
