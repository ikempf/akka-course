
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

### Production lente - Consommation rapide

Si la production est plus lente que la consommation, le streaming se déroule sans problèmes.

Cela est rarement garanti dans un système d'information avec de nombreux acteurs s'impactant mutuellement. 

---

### Production rapide - Consommation lente

Pour une taille de donnée suffisamment grande une des erreurs suivantes est forcément rencontrée. 

- Memoire insuffisante
- Threads insuffisants
- Exception "garde-fou" (seuil de connexions jdbc, http, file-descriptors, etc.)

---

### Le problème

Le **débit** du flux de donnée est controlée par le producteur.

Le consommateur **subit** la vitesse du producteur.

---

### Mauvaises solutions

- Throttling "artificiel" du producteur
- Scaling manuel du hardware sous-jacent
- Augmentation des buffers

---


### Solution

Pour que le traitement de la donnée fonctionne en toutes circonstances, il faut réguler le flux en fonction du consommateur.

On parle alors de **back-pressure**.

Sans régulation de cette dernière, le bon traitement de la donnée dépend de facteurs externes (taille de la donnée, vitesse de services tiers, etc.)

---

## Flow API

Le premier niveau d'utilisation d'akka-stream.

Répond à la majorité des besoins tout en restant très expressif.

---

### Flow API - limitations

Modélisation de stream **linéaires**

```
Source --> transformation --> transormation --> Sink
```

---

## Graph API

Comme son nom l'indique, permet de modéliser des graphes.

```
Source1 -->                --> transormation1 
Source2 --> transformation --> transormation2 --> Sink
Source3 -->                
```

---

### Graph API

- Api mutable
- Partiellement vérifié à la compilation
- Danger de deadlock
- Très puissant (Broadcast, Partition, Bidirectional etc.)

---

## GraphStage Api

Si les *stages* de la graph api ne suffisent pas

Exemples
- Compteur
- Monitoring débit
- Aggregation

---

## Remarques de fin

- Ne jamais streamer sans considérer la backpressure
- Toujours utiliser l'api la plus simple
- Comme dans akka-actor il ne faut pas bloquer
- Ne pas commencer par optimiser
