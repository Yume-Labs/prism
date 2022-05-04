(ns prism.pregenerate
  (:gen-class)
  (:require [clojure.core.async :refer [>! go]]
            [prism.db :refer :all]
            [taoensso.timbre :refer :all]))

(defn send-pregenerate
  [node collection config id pregen out]
  (go (let [nft {:id id :state :outcomes-resolved :outcomes pregen}
            res (store-nft node collection nft config true)]
        (info (str "Storing and sending pregenerated NFT (ID: " id ")."))
        (if (= :ok (first res))
          (>! out nft)
          (do (error (str "Failed to store " id ". Retrying."))
              (send-pregenerate node collection config id pregen out))))))

(defn send-guarantee
  [node collection config id guarantee out]
  (go (let [nft {:id id :state :to-do :guarantee guarantee}
            res (store-nft node collection nft config true)]
        (info (str "Storing and sending guaranteed NFT (ID: " id ")."))
        (if (= :ok (first res))
          (>! out nft)
          (do (error (str "Failed to store " id ". Retrying."))
              (send-guarantee node collection config id guarantee out))))))

(defn send-todo
  [node collection config id out]
  (go (let [nft {:id id :state :to-do}
            res (store-nft node collection nft config)]
        (info (str "Storing and sending a fresh NFT (ID: " id ")."))
        (if (= :ok (first res))
          (>! out nft)
          (do (error (str "Failed to store " id ". Retrying."))
              (send-todo node collection config id out))))))

(defn generate-nfts
  [node collection config ids pregenerate-ids guarantee-ids out]
  (go (loop [id (first ids)
             rest-ids (next ids)
             pregen (first (:pregenerate config))
             rest-pregens (next (:pregenerate config))
             guarantee (first (:guarantees config))
             rest-guarantees (next (:guarantees config))]
        (if (nil? id)
          :done
          (let [is-pregen (.contains pregenerate-ids id)
                is-guarantee (.contains guarantee-ids id)]
            (info (str "Generating NFT with ID " id "."))
            (if is-pregen
              (send-pregenerate node collection config id pregen out)
              (if is-guarantee
                (send-guarantee node collection config id guarantee out)
                (send-todo node collection config id out)))
            (recur (first rest-ids)
                   (next rest-ids)
                   (if is-pregen (first rest-pregens) pregen)
                   (if is-pregen (next rest-pregens) rest-pregens)
                   (if is-guarantee (first rest-guarantees) guarantee)
                   (if is-guarantee (next rest-guarantees) rest-guarantees)))))))

(defn generate-collection
  [node collection config out]
  (let [size (get-in config [:config :size])
        pregenerate (:pregenerate config)
        guarantees (:guarantees config)
        ids (shuffle (range 0 size))
        pregenerate-ids (take (count pregenerate) ids)
        guarantee-ids (take-last (count guarantees) ids)]
    (generate-nfts node collection config ids pregenerate-ids guarantee-ids out)))
