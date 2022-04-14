# `:base`

This documentation explains the `:base` attribute, which defines the defaults 
for each individual NFT. Typically this will be a very simple setup.

## Sample

```clojure
{:base {:layers 
        {:background {:order 0}
         :body {:order 1 :image [:images :body]}
         :mouth {:order 2 :image [:images :mouth :oh]}
         :eyes {:order 3}
         :eye-outline {:order 4}
         :secondary {:order 5 :image [:images :secondary]}
         :overlay {:order 6 :image [:images :overlay]}
         :shadows {:order 7 :image [:images :shadows] :blend :multiply}
         :eyes-shadows {:order 8 :blend :multiply}
         :highlights {:order 9 :image [:images :highlights] :blend :add}
         :eyes-highlights {:order 10 :blend :add}
         :outline {:order 11 :image [:images :highlights]}
         :pregenerate {:order 12}}}}
```

## `:layers`

The `:layers` attribute is the sole attribute for the `:base`. This may lead to
the question 'Why do we need the `:base` at all, then?' The answer is because
the `:base` forms the base for each NFT during NFT generation, and will gather
more attributes throughout the program running.

Each layer has a name, mapped to any number of the below attributes.

### `:order (int)`

This is the order in which the layer is rendered. Lower numbers are rendered
before higher numbers.

### `:image (list)`

This is the location in which the image can be found in the `:resources` section
of the config (see [this doc](./resources.md)).

### `:color (list)`

This is the location in which the color can be found in the `:resources` section
of the config (see [this doc](./resources.md)).

### `:blend (keyword)`

This is the blending mode which should be applied to the layer. The underlying
library is Clojure2d, and you can find all of its blending modes
[here](https://clojure2d.github.io/clojure2d/docs/codox/clojure2d.color.blend.html#var-blends-list).
