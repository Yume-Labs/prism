# Workflows

Prism Engine builds NFTs in the following order:

## Pre-Flight Checks

The pre-flight checks ensure that the config is valid and capable of generating
a collection.

## Add Pregenerates

First of all, the pre-generated NFTs are added to the database.

## Make Decisions

Next, one by one, NFTs are generated as a list of decisions, each of which must
resolve to outcomes.

Twins are de-duplicated here if the option is enabled.

## Resolve Outcomes

Next, each NFT is resolved to a base config and a list of outcomes from the
results of the decisions.

## Apply Outcomes

The outcomes are resolved which changes the base NFT config.

## Add Names

Each NFT is named.

## Generate Metadata

The Candy Machine-compatible metadata is created.

## Generate Images

Images are rendered.
