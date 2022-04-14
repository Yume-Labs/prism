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
  [node collection config ids pregenerate-ids out]
  (go (loop [id (first ids)
             rest-ids (next ids)
             pregen (first (:pregenerate config))
             rest-pregens (next (:pregenerate config))]
        (if (nil? id)
          :done
          (let [is-pregen (.contains pregenerate-ids id)]
            (info (str "Generating NFT with ID " id "."))
            (if is-pregen
              (send-pregenerate node collection config id pregen out)
              (send-todo node collection config id out))
            (recur (first rest-ids)
                   (next rest-ids)
                   (if is-pregen (first rest-pregens) pregen)
                   (if is-pregen (next rest-pregens) rest-pregens)))))))

(defn generate-collection
  [node collection config out]
  (let [size (get-in config [:config :size])
        pregenerate (:pregenerate config)
        ids (shuffle (range 0 size))
        pregenerate-ids (take (count pregenerate) ids)]
    (generate-nfts node collection config ids pregenerate-ids out)))
