(ns prism.decisions
  (:gen-class)
  (:require [clojure.core.async :refer [>! go <! chan]]
            [prism.db :refer :all]
            [taoensso.timbre :refer :all]))

(defn send-nft
  [node collection config nft processed out in]
  (go (let [new-nft (assoc nft :decisions processed :state :decisions-made)
            digest (get-digest new-nft config)]
        (if (or (nil? digest) (= 0 (count-nfts-with-digest node collection digest)))
          (do (info (str "Storing and sending NFT with decisions (ID: " (:id nft) ")."))
              (let [res (store-nft node collection new-nft config)]
                (if (= :ok (first res))
                  (>! out new-nft)
                  (do (error (str "Failed to store NFT with ID " (:id new-nft) ". Retrying."))
                      (send-nft node collection config nft processed out in)))))
          (do (info (str "De-duping twin with existing digest " digest "."))
              (>! in nft))))))

(defn make-custom-decision
  [processed decision]
  (assoc processed (:name decision) ((eval (:function decision)) processed)))

(defn make-range-decision
  [processed decision]
  (assoc processed (:name decision) (+ (:min decision)
                                       (rand-int (+ 1 (- (:max decision) (:min decision)))))))

(defn make-conditional-decision
  [processed decision]
  (assoc processed (:name decision) (:outcome decision)))

(defn weighted-random-list
  [list]
  (->> list
     (mapv #(if (keyword? %) {:name % :weight 1} %))
     (mapv #(take (:weight %) (repeat (:name %))))
     (flatten)
     (vec)))

(defn make-random-decision
  [processed decision]
  (assoc processed (:name decision) (rand-nth (weighted-random-list (:outcomes decision)))))

(defn make-decision
  [processed decision]
  (if (or (nil? (:filter decision))
          ((eval (:filter decision)) processed))
    (case (:type decision)
      :random (make-random-decision processed decision)
      :conditional (make-conditional-decision processed decision)
      :range (make-range-decision processed decision)
      :custom (make-custom-decision processed decision))
    processed))

(defn make-decisions
  [node collection config nft out in]
  (go (case (:state nft)
        :to-do (let [decisions (:decisions config)]
                 (loop [processed {}
                        decision (first decisions)
                        rest-decisions (next decisions)]
                   (if (nil? decision)
                     (send-nft node collection config nft processed out in)
                     (recur (make-decision processed decision)
                            (first rest-decisions)
                            (next rest-decisions)))))
        (>! out nft))))

(defn decision-resolver
  [node collection config in]
  (let [out (chan)]
    (go (while true (make-decisions node collection config (<! in) out in)))
    out))
