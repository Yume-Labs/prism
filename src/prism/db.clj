(ns prism.db
  (:gen-class)
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-commons.digest :as digest]
            [taoensso.nippy :as nippy]))

(defn get-node
  [configuration]
  (xt/start-node configuration))

(defn get-production-node
  []
  (get-node
    {:xtdb/index-store {:kv-store {:xtdb/module 'xtdb.lmdb/->kv-store
                                   :db-dir (io/file "db/index-store")
                                   :sync? true}}
     :xtdb/document-store {:kv-store {:xtdb/module 'xtdb.lmdb/->kv-store
                                      :db-dir (io/file "db/document-store")
                                      :sync? true}}
     :xtdb/tx-log {:kv-store {:xtdb/module 'xtdb.lmdb/->kv-store
                              :db-dir (io/file "db/tx-db")
                              :sync? true}}}))

(defn into-put
  [record]
  [[::xt/put record]])

(defn store-config
  [node collection config]
  (->> {:xt/id (keyword (str collection "-config"))
      :collection collection
      :document/type "config"
      :payload (nippy/freeze config)}
     (into-put)
     (xt/submit-tx node))
  (xt/sync node)
  node)

(defn thaw-if-not-nil
  [payload]
  (if (nil? payload)
    nil
    (nippy/thaw payload)))

(defn retrieve-config
  [node collection]
  (-> node
     (xt/db)
     (xt/entity (keyword (str collection "-config")))
     (:payload)
     (thaw-if-not-nil)))

(def valid-nft-states
  [:to-do
   :decisions-made
   :outcomes-resolved
   :ready
   :metadata-generated
   :image-rendered])

(defn thaw-or-nil
  [nft]
  (if (nil? nft)
    nil
    (nippy/thaw (:payload nft))))

(defn retrieve-nft
  [node collection id]
  (-> node
     (xt/db)
     (xt/entity (keyword (str collection "-nft-" id)))
     (thaw-or-nil)))

(defn validate-nft-state
  [state]
  (.contains valid-nft-states state))

(defn index-of
  [el coll]
  (first (keep-indexed #(if (= el %2) %1) coll)))

(defn get-previous-state
  [state]
  (if (= state :to-do)
    nil
    (get valid-nft-states (dec (index-of state valid-nft-states)))))

(defn existing-has-previous-state
  [node collection nft]
  (or (and (= (:state nft) :to-do)
           (nil? (retrieve-nft node collection (:id nft))))
      (= (get-previous-state (:state nft))
         (:state (retrieve-nft node collection (:id nft))))))

(defn get-digest
  [nft config]
  (if (or (nil? (:decisions nft)) (not (get-in config [:config :no-twins])))
    nil
    (digest/sha-256 (str/join "" (map #(str (name (first %))
                                            (name (second %)))
                                      (filterv #(.contains (get-in config [:config :twin-outcomes]) (first %))
                                               (:decisions nft)))))))

(defn store-nft
  ([node collection nft config] (store-nft node collection nft config false))
  ([node collection nft config override]
   (if (and (validate-nft-state (:state nft))
            (or (existing-has-previous-state node collection nft)
                override))
     (do (->> {:xt/id (keyword (str collection "-nft-" (:id nft)))
             :collection collection
             :digest (get-digest nft config)
             :document/type "nft"
             :document/id (:id nft)
             :document/state (:state nft)
             :payload (nippy/freeze nft)}
            (into-put)
            (xt/submit-tx node))
         (xt/sync node)
         [:ok node])
     [:error node])))

(defn get-nfts
  [node collection]
  (xt/q (xt/db node)
        '{:find [id payload]
          :where [[e :payload payload]
                  [e :document/type "nft"]
                  [e :collection coll]
                  [e :document/id id]]
          :in [coll]
          :order-by [[id :asc]]}
        collection))

(defn get-nfts-with-digest
  [node collection digest]
  (xt/q (xt/db node)
        '{:find [id]
          :where [[e :document/id id]
                  [e :digest hash]
                  [e :document/type "nft"]
                  [e :collection coll]]
          :in [coll hash]
          :order-by [[id :asc]]}
        collection
        digest))

(defn get-nfts-thawed
  [node collection func & args]
  (mapv (fn [v] [(first v) (nippy/thaw (second v))]) (apply func node collection args)))

(defn count-nfts-with-digest
  [node collection digest]
  (count (get-nfts-with-digest node collection digest)))
