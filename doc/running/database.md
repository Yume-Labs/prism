# The Database

Prism uses [XTDB](https://docs.xtdb.com). This runs in-memory when running the
tests, and on an [LMDB](https://docs.xtdb.com/storage/lmdb/) base when running 
in production.

## Entities

### Config

The config is stored in the database as `:config`. This is to ensure that we
do not try to resume a paused generation with a different config.

### NFTs

Each NFT is stored in the database as `:nft/sha`.

## Performance

XTDB on RocksDB is highly performant, and highly capable. Using the database
allows us to support pausing and resuming, as well as keep RAM usage manageable.
