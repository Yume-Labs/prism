(ns prism.structure
  (:gen-class)
  (:require [clojure.core.async :refer [>! go <! chan]]
            [prism.db :refer :all]
            [taoensso.timbre :refer :all]))

(defn send-nft
  [node collection config nft out layers attributes]
  (go (let [new-nft (assoc nft :layers layers :attributes attributes :state :ready)]
        (info (str "Storing and sending NFT with structure (ID: " (:id new-nft) ")."))
        (let [res (store-nft node collection new-nft config)]
          (if (= :ok (first res))
            (>! out new-nft)
            (do (error (str "Failed to store NFT with ID " (:id new-nft) ". Retrying."))
                (send-nft node collection config nft out layers attributes)))))))

(defn apply-layers
  [base-layers outcomes]
  (loop [outcome (first outcomes)
         rest-outcomes (next outcomes)
         layers base-layers]
    (if (nil? outcome)
      layers
      (recur (first rest-outcomes)
             (next rest-outcomes)
             (if (and (vector? (first outcome))
                      (= :layers (first (first outcome))))
               (assoc layers
                      (second (first outcome))
                      (merge (get layers (second (first outcome)))
                             (second outcome)))
               layers)))))

(defn apply-attributes
  [base-attrs outcomes]
  (loop [outcome (first outcomes)
         rest-outcomes (next outcomes)
         attributes base-attrs]
    (if (nil? outcome)
      attributes
      (recur (first rest-outcomes)
             (next rest-outcomes)
             (if (= :attribute (first outcome))
               (conj attributes (vec (next outcome)))
               attributes)))))

(defn resolve-structure
  [node collection config nft out]
  (go (case (:state nft)
        :outcomes-resolved (let [base-layers (get-in config [:base :layers])
                                 base-attrs (if (nil? (get-in config [:base :attributes]))
                                              []
                                              (get-in config [:base :attributes]))
                                 outcomes (:outcomes nft)
                                 layers (apply-layers base-layers outcomes)
                                 attributes (apply-attributes base-attrs outcomes)]
                             (send-nft node collection config nft out layers attributes))
        (>! out nft))))

(defn structure-resolver
  [node collection config in]
  (let [out (chan)]
    (go (while true (resolve-structure node collection config (<! in) out)))
    out))
