(ns prism.metadata
  (:gen-class)
  (:require [clojure.core.async :refer [>! go <! chan]]
            [prism.db :refer :all]
            [taoensso.timbre :refer :all]
            [clojure.java.io :refer [writer]]
            [cheshire.core :refer :all]))

(defn send-nft
  [node collection config nft out]
  (go (let [new-nft (assoc nft :state :metadata-generated)]
        (info (str "Wrote metadata for NFT (ID: " (:id new-nft) ")."))
        (let [res (store-nft node collection new-nft config)]
          (if (= :ok (first res))
            (>! out new-nft)
            (do (error (str "Failed to store NFT with ID " (:id new-nft) ". Retrying."))
                (send-nft node collection config nft out)))))))

(defn generate-metadata
  [id base-config base-traits]
  (-> {}
     (assoc :name (str (get-in base-config [:naming :base]) id))
     (assoc :description (get-in base-config [:description]))
     (assoc :symbol (get-in base-config [:symbol]))
     (assoc :image (str id ".png"))
     (assoc :seller_fee_basis_points (get-in base-config [:royalties]))
     (assoc :collection {:name (get-in base-config [:name])
                         :family (get-in base-config [:family])})
     (assoc :external_url (get-in base-config [:url]))
     (assoc :properties {:files [{:uri (str id ".png")
                                  :type "image/png"}]
                         :creators (->> (:creators base-config)
                                      (mapv (fn [val] {:address (:wallet val)
                                                      :share (:share val)})))})
     (assoc :attributes (->> base-traits
                           (mapv #(-> {}
                                     (assoc :trait_type (first %))
                                     (assoc :value (second %))))))))

(defn write-metadata
  [node collection config assets-root nft out]
  (go (case (:state nft)
        :ready (let [base-config (get config :config)
                     base-traits (get nft :attributes)]
                 (-> (generate-metadata (:id nft) base-config base-traits)
                    (generate-stream (writer (str assets-root "/" collection "/" (:id nft) ".json"))
                                     {:pretty true}))
                 (send-nft node collection config nft out))
        (>! out nft))))

(defn metadata-writer
  [node collection config assets-root in]
  (let [out (chan)]
    (go (while true (write-metadata node collection config assets-root (<! in) out)))
    out))
