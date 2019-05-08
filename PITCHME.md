
# Akka-stream

---

## Traitement naïf
```scala
def saveToDb(input: String): Future[String]

val lines: Iterator[String] = Source.fromFile("big-input.csv").getLines()

while (lines.nonEmpty) {
  saveToDb(lines.next())
}
```

---

### Production lente - Consommation rapide

Si la production (lecture fichier) est plus lente que la consommation (écriture en base), le scénario se déroule sans problèmes.

---

### Prouction rapide - Consommation lente

Pour la taille de donnée suffisamment grande une des erreurs suivantes est forcément rencontrée. 

- Memoire insuffisante
- Thread insuffisants
- Exception "garde-fou" (nombre de connexion jdbc, http, etc.)

---

### Le problème

Le **débit** du flux de donnée est controlée par le producteur.

Le consommateur subit la vitesse du producteur.

---

### Solution

Pour que le traitement de la donnée fonctionne en toutes circonstancesm il faut réguler le flux en fonction du consommateur.

C'est la **backpressure**.

Sans régulation de cette dernière, le bon traitement de la donnée dépend de facteurs externes (taille de la donnée, vitesse de services tiers, 9 etc.)

---

## Stream API

---

## Graph API
