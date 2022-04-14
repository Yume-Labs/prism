# `:config`

This documentation explains the `:config` section of Prism config files. This
section is primarily used to determine collection-level config.

## Sample

```clojure
{:config {:name "Sample Collection"
          :family "Yume Labs"
          :symbol "COLL"
          :url "https://yumelabs.io"
          :description "This is a sample collection by Yume Labs."
          :royalties 500
          :no-twins true
          :creators [{:wallet "Fk3mJaeqYjZucRs3UsGTDxyEm5q5dg39EjRoR4NPK9hM" 
                      :share 100}]
          :twin-outcomes [:background 
                          :body-color 
                          :secondary-color 
                          :eyes 
                          :eye-color]
          :size 10
          :assets-format :solana
          :naming {:base "Sample #"
                   :type :autoinc}}}
```

## `:name (string)`

This is the name of the collection.

## `:family (string)`

This is the family of the collection.

## `:symbol (string)`

This is the symbol for the collection.

## `:url (string)`

This is the external URL for the collection.

## `:description (string)`

This is the description for the collection.

## `:royalties (int)`

This is the royalties for the collection (in basis points). For 5% royalties,
this should be set to 500.

## `:creators (list of structs)`

**Note: limitations here are set by the Metaplex token standard and candy 
machine. They are not limitations imposed by Yume Labs or Prism Engine.**

This is a list of (4 or less) creators, each with the following:

### `:wallet (string)`

The wallet address for the creator.

### `:share (int)`

The share of royalties to receive. Note: shares cannot be decimal and must add
up to 100.

## `:no-twins (bool)`

Set to `true` if you want to prevent duplicate NFTs.

## `:twin-outcomes (list of keywords)`

List of OUTCOMES (not attributes) which will determine a twin.

## `:size (int)`

Number of NFTs in the collection.

## `:assets-format (keyword)`

The format to write assets in (only `:solana` is supported for now).

## `:naming (object)`

How to name NFTs.

### `:base (string)`

The base string for the name.

### `:type (keyword)`

What to append to the `:base`.

If the `:base` is `"Sample #"` and the `:type` is `:autoinc`, this will generate
names like `"Sample #1"`, `"Sample #2"`, etc.

Only `:autoinc` is supported for now.
