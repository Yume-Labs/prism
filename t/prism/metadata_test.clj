(ns prism.metadata-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan]]
            [clojure.java.io :refer [reader delete-file]]
            [prism.metadata :refer :all]
            [prism.db :refer [retrieve-nft store-nft]]
            [prism.helpers :refer [<!!?
                                   with-test-node
                                   full-config
                                   full-nft-with-structure
                                   full-nft-with-metadata
                                   full-metadata]]
            [cheshire.core :refer :all]))

(deftest testing-generating-metadata
  (testing "generate-metadata"
    (is (= (generate-metadata (:id full-nft-with-structure)
                              (get-in full-config [:config])
                              (get-in full-nft-with-structure [:attributes]))
           full-metadata))))

(deftest testing-sends
  (testing "send-nft"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-structure full-config true)
      (send-nft node "test" full-config full-nft-with-structure ch)
      (is (= full-nft-with-metadata (<!!? ch)) "received NFT on out channel")
      (is (= full-nft-with-metadata (retrieve-nft node "test" 33)) "received NFT from database"))))

(deftest testing-full-metadata-generation
  (testing "write-metadata"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-structure full-config true)
      (.mkdir (java.io.File. "assets/test"))
      (write-metadata node "test" full-config "assets" full-nft-with-structure ch)
      (is (= full-nft-with-metadata (<!!? ch)) "received NFT on out channel")
      (is (= full-nft-with-metadata (retrieve-nft node "test" 33)) "received NFT from database")
      (is (= full-metadata (parse-stream (reader "assets/test/33.json") true)))
      (delete-file "assets/test/33.json"))))
