# `:pregenerate`

## Sample

```clojure
{:pregenerate [[[[:layers :pregenerate] {:image [:images :oneofone]}]
                [:attribute "Time" "Night"]
                [:attribute "Body" "Blue"]]]}
```

## Explanation

The `:pregenerate` list is where you put NFTs which you have already created.
Each NFT is specified as a list of outcomes. This can be used to generate 1/1s 
or 'full set' NFTs which you want to have in your set.

Note: You need to specify your NFT as a list of **resolved outcomes**, as above.
Specifying the NFT as a list of outcome names, as if you had run them 
through the `decisions`, will not work as these NFTs skip resolution.
