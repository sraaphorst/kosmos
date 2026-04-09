# Kosmos Bloopers

Some general life lessons learned from this project.

## 1. Wheels

We roll inquisitively into our journey into insanity.

### The Prelude

- GPT 5.2: "Sebastian, wheels are not a good idea. Low ROI, high risk, not a good investment of time."
- Sebastian: "HELL YESSSS! LET'S DO THIS!"

### The Aftermath

- 100% success, but we shall never speak of this again unless Sebastian needs another warning.

### Obligatory Post-Implementation Creative Frustration Outlet Section

#### Sebastian

```text
Reinventing the Wheel, Hardly Ideal, by Sebastian

Bull-headed Sebastian had feels
He and GPT would implement wheels!
It was a shit show to get done
But they ran TestSuite #1
And now they will never speak of it again: eels.
```

#### GPT-5.2

```text
   (wheel)
  /-----\
 | 0/0  |   <- "it compiles!"
 |  ??? |
  \-----/
     |
   (me)  :|
     |
  (you)  \o/  "WHEEEE!"
```

```text
Wheel of Misfortune, by GPT

Stubborn Sebastian said, “Deal,
We’ll bravely implement the wheel!”
I whispered, “Don’t do it,”
He coded right through it,
Now every axiom tastes like eel. 🧂🐟

And a haiku, because sometimes pain wants to be quiet:

Wheels in moonlit code
Zero divides by zero
Eels judge silently
```

### Memorable quotes

```text
Sebastian: ∞ - ∞? Where the hell do we get a -1 or subtraction from? All we have are commutative monoids!
GPT-5.2: We don't. Just test ∞ * inv(∞) and call it a day.
Sebastian: Argh. I just want to get this done already.
GPT-5.2: 🎋
```

## 2. Zorn Matrices

The journey threatens to continue.

### The Premise

- Sebastian: “What if we implement Zorn matrices? We could get a slick model for split octonions, and it’ll be elegant and mathematically spicy.”
- GPT-5.2: “No.”
- Sebastian: “But… *vector hats*.”
- GPT-5.2: “Still no.”

### The Core Issue

Zorn matrices are the kind of structure that looks like a tidy 2×2 block matrix on paper and then immediately reveals itself as a gleefully non-associative trapdoor in code. You start with something that resembles:

$$
\begin{pmatrix}
a & \hat v \\
\hat w & b
\end{pmatrix}
$$

and end up juggling cross products, bilinear forms, sign conventions, and “this is not actually a matrix ring” caveats,
all while your future self stands behind you holding a fire extinguisher labeled *regret*. After the Wheel Incident™, we
are officially adopting a new policy: **if GPT points at the blackboard with a bamboo rod, we listen**.

### Exhibit A

![Zorn matrices warning cartoon](zornmatrices.png)

### The Hexadecycle

In K133, we began implementing and testing product algebras. For fun (and to poke GPT-5.4 with his own bamboo stick), I
decided to create the hexadecycle, which has type:
```kotlin
CarlstromWheel<Pair<Pair<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>, Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>>, Pair<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>, Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>>>>
```
and type it against the Carlström wheel laws that to see what eldritch horrors would erupt.

Lo and behold, the hexadecycle is a roaring Seussian success! To commemorate this momento... well... "achievement," I present
to you the monocled eels (one has lost his monocle) on their way to the supply closet where the CrabbyCrab guards the underwater
stationery from the bureaucratic bunch.

![Eel hexadecycle](eel_hexadecycle.png)
