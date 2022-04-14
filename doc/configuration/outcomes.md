# `:outcomes`

The `:outcomes` section is a multi-tiered object which describes how to translate
each potential outcome of the `:decisions` into changes in the NFT data
structure, usually changes to the image and/or changes to the NFT attributes.

## Sample

```clojure
{:outcomes {:background 
             {:day [[:attribute "Time" "Day"]
                    [[:layers :background] {:image [:images :background :day]}]]
              :sunset [[:attribute "Time" "Sunset"]
                       [[:layers :background] {:image [:images :background :sunset]}]]
              :night [[:attribute "Time" "Night"]
                      [[:layers :background] {:image [:images :background :night]}]]}
            :body-color
             {:cream [[:attribute "Body" "Cream"]
                      [[:layers :body] {:color [:colors :cream]}]]
              :grey [[:attribute "Body" "Grey"]
                     [[:layers :body] {:color [:colors :grey]}]]}}}
```

## Structure

At the top level, there are keywords which match the `:name` of your 
`:decisions`, and each one is mapped to a struct which has keywords which
match the `:outcomes` of that `:decisions`.

### Resolving Outcomes

Each outcome name resolves to a list of outcomes.

Each outcome itself is a list, in which the first value describes what needs to
be updated, and subsequent values describe how to update it.

Sounds complex? Let's look at the most common examples:

#### Attribute Changes (Metadata)

```clojure
[:attribute "Body" "Cream"]
```

This list has three values. The first one, `:attribute`, tells the resolver that
it is supposed to be read by the metadata compiler. The second and third are the
name and value of the attribute. `Body: Cream`

#### Layer Changes (Rendering)

```clojure
[[:layers :background] {:image [:images :background :night]}]
[[:layers :body] {:color [:colors :cream]}]
```

Layer changes are slightly more complex than attribute changes. The first value
describes where to find the value to be updated. Remember the `:base` config?
That's where we're searching.

The second value is a struct which will be merged with the struct found, so if
the `:base` contains:

```clojure
{:layers {:background {:order 1}
          :body {:order 2 :image [:images :body]}}}
```

This will merge on top, leaving:

```clojure
{:layers {:background {:order 1 :image [:images :background :night]}
          :body {:order 2 :image [:images :body] :color [:colors :cream]}}}
```

If this doesn't make sense, refresh your memory on [base](./base.md) and
[resources](./resources.md).

### `:default`

Not shown here, any process step can also have a default handler, but these are
a little more complex. Each `:default` handler needs to be a function which
takes the value and resolves it to a list of outcomes.

The most common one would be for a `:range` step, and looks something like this:

```clojure
{:outcomes {:age {:default (fn (x) [[:attribute "Age" x]])}}}
```
