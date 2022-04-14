# `:decisions`

The `:decisions` are the core of how Prism Engine works. They contain all of
the logic to create NFTs. Supplied as a list, they come in a few `:type`s, each 
of which works slightly differently.

## Common Fields

All `:decisions` have some fields in common:

### `:name (keyword)`

This is the name of the outcome to resolve against.

### `:type (keyword)`

This is the type of decision step.

### `:filter (function, optional)`

This is a function. The decision will be skipped if it doesn't return `true`.

## `:random`

Random decisions do exactly what you'd expect: derive a random outcome.

### Sample

```clojure
{:name :eye-color
 :filter (fn (processed) (not (= (:eyes processed) :open)))
 :type :random
 :outcomes [:red :green :blue]}
```

### Fields

#### `:outcomes`

This is a list of outcomes. Each outcome takes the form of a struct like this:

```clojure
{:name :red :weight 1}
```

Where a lower weight is rarer. Alternatively, the shorthand can be used:

```clojure
:red
```

Which expands to a struct with a weight of 1. This is idiomatically only used
where all outcomes are equally likely.

## `:conditional`

Conditional decisions take a `condition`, and when it's true, derive a fixed
outcome. For example, you may want a condition to add a trait or another layer if
one of your NFTs has two outcomes already.

### Sample

```clojure
{:name :gender
 :type :conditional
 :filter (fn (processed) (= (:eye-color processed) :red))
 :outcome :male}
{:name :gender
 :type :conditional
 :filter (fn (processed) (not (= (:eye-color processed) :red)))
 :outcome :female}
```

### Fields

#### `:filter`

This is a common field, but on a `:conditional` decision, it is mandatory.
To create a conditional which *always* runs, i.e. an outcome which will exist on
every NFT in the collection, use this filter:

```clojure
(fn (_) true)
```

This can be used for something like `Generation`, where the entire OG collection
will be Generation `1`.

#### `:outcome`

This is the outcome.

## `:range`

Range decisions allow for an outcome in a range of values.

### Sample

```clojure
{:name :age
 :type :range
 :min 12
 :max 120}
```

### Fields

#### `:min`

The minimum value.

#### `:max`

The maximum value.

## `:custom`

Custom decisions are dangerous, they allow you to use a Clojure function to
determine an outcome. This behavior is not recommended unless you have a good
understanding of Clojure.

### Sample

```clojure
{:name :iq
 :type :custom
 :function (fn (_) (* (rand 9 14) (rand 10 12)))}
```

### Fields

#### `:function`

This is a function which returns... whatever you want. Just make sure your
`:outcomes` config can handle it.
