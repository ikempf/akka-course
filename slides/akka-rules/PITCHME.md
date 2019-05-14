# Akka - general rules

---

# Message delivery guarantees

---

### Message lifecycle

A message is never directly sent to an actor when using **tell** or **ask**.

Multiple mechanics have to be accounted for before the message is effectively processed by the target actor. 

---

### Message lifecycle

- Sent to an *actorRef*
- Enqueued (*or not*) in the mailbox
- The message is dequeued from the mailbox by the actor
- The message is processed (*or not*) by the actor

---

### Message sending

- **Local**: delivery is identical to calling a function, the message will always reach the mailbox
- **Cluster**: delivery is subject to network failures, there is no automatic retransmission

---

### Message reception

Enqueuing can fail for multiple reasons
- The mailbox has been **suspended**, the message goes to the DeadLetter queue
- The mailbox is **bounded** and thus drops the message (ex: BoundedMailbox) 
- A custom mailbox might simply **ignore** the message

---

### Message processing

Message handling by the target actor can fail
- The target actor has been **terminated**, the message goes to the DeadLetter queue
- The message is **unknown** to the actor (*unandled*)
- The processing of the message **fails**

---

### Message delivery failures summary

Delivery and processing are not guaranteed by akka, any message is subject to being
- **lost** through network failures (in a cluster context)
- **refused** through by the mailbox
- **ignored** by the target actor

---

## At-most-once delivery

Akka will **never** automatically resend a message.

You can be certain that you will never receive duplicated messages due to technical reasons.

---

**at-most-one** semantics are opposed to
- **at-least-once**: every message is received once or more
- **exactly-once**: every message is received exactly once

The former is the simplest and most performant. It is possible to implement the second without too much overhead.

---

## At-least-once delivery

If correct delivery/processing is mandatory, a manual ack can be implemented.

```
actor1 ==== BusinessMessage ====> actor2 
actor1      <==== Done ====       actor2 
```

It is possible to use the **ask patterm** for this.

---

## Exactly-once delivery

Exactly-once delivery is difficult, exactly-once processing is impossible.

Strive for idempotent message handling whenever possible.

---

# Message processing

---

## Thread-safety 

Messages are dequed **sequentially** from the mailbox.

At any moment, **at most one** message is being processed by the actor.

---

## Thread-safety

Desplite the strong isolation of actors concurrency issures can still arise.

```
var counter = 0
asyncOperation.onComplete(_ => counter = counter + 1)
```

The completion lambda is executed on another thread !

---

## Thread-safety

Be careful with **closures**, you should never close-over
- vars
- objets mutables
- sender()
- context
- etc.

---

## Thread-safety

It's possible to avoid vars through the use of `context.become`

```
var counter = 0
def receive = { case Msg => counter = counter + 1 }
```

```
def receive = counting(0)
def counting(counter: Int): Receive = { case Msg => context.become(counting(counter + 1)) }
```

---

## Reception and processing

Message reception is **decoupled** of it's coupling.

The mailbox is not affected by the actors activity. Notably: it will not be blocked by the actor.

---

# Message ordering

---

Message order is guaranteed between **two actors**

```
A sends    M1, M2, M3, M4 to B
B receives M1, M2, M3, M4 in order
```

Warning: messages coming from other actors might be interlaced in the former sequence.

---

The ordering guarantees only apply 
- between **two actors**
- if the mailbox is FIFO

---

# Actor lifecycle

---

- Actor creation `system.actorOf()`
- Actor resumes or restarts
- Actor terminates: `context.stop()` or `PoisonPill` or `Kill`

---

Several methods can be overloaded to customize the actors behavior during it's lifecycle.

- `preStart`
- `receive`
- `preRestart`
- `postRestart` 
- `postStop`

---

### preStart

- Called after the actor is spawned
- Allows the execution of initialization code
- By default, this method does nothing

---

### receive

- Represents the runtime behavior of the actor
- The only method that must be overridden

---

### preRestart

- Called before the actor is restarted
- Allows to free resources
- By default all child actors are killed, calls **postStop** 
- Two parameters
 - The exception that caused the restart
 - The eventual message that was being processed

---

### postRestart

- Called after the actor restart
- By default: calls **preStart**

---

### postStop

- Called after the actor has stopped
- Allows for resource freeing
- By default the method does nothing

---

## Closing points

- Few delivery guarantees
- Avoid concurrency at all costs
- Failures will happen, **let-it-crash** and recover
- Be aware that actor restarts will happen
