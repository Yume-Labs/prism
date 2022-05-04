# `:guarantees`

The `:guarantees` section describes traits which should exist together on at
least one NFT in the collection. They are specified as a struct of keys and 
values, which must be valid outcomes.

For example, if I want at least one NFT in my collection to have a diamond cane
and a top hat, I can specify this:

```clojure
{:guarantees [{:headwear :top_hat
               :holding :diamond_cane}]}
```

Any decisions not specified in the guarantee block will be generated normally.
