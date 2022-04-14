(ns prism.outcomes
  (:gen-class)
  (:require [clojure.core.async :refer [>! go <! chan]]
            [prism.db :refer :all]
            [taoensso.timbre :refer :all]))

(defn send-nft
  [node collection config nft out processed]
  (go (let [new-nft (assoc nft :outcomes processed :state :outcomes-resolved)]
        (info (str "Storing and sending NFT with outcomes (ID: " (:id new-nft) ")."))
        (let [res (store-nft node collection new-nft config)]
          (if (= :ok (first res))
            (>! out new-nft)
            (do (error (str "Failed to store NFT with ID " (:id new-nft) ". Retrying"))
                (send-nft node collection config nft out processed)))))))

(defn resolve-outcomes
  [node collection config nft out]
  (go (case (:state nft)
        :decisions-made (let [decisions (vec (seq (:decisions nft)))
                              outcomes (:outcomes config)]
                          (->> decisions
                             (reduce #(concat %1 (get-in outcomes %2)) [])
                             (send-nft node collection config nft out)))
        (>! out nft))))

(defn outcome-resolver
  [node collection config in]
  (let [out (chan)]
    (go (while true (resolve-outcomes node collection config (<! in) out)))
    out))
