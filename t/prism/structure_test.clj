(ns prism.structure-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan]]
            [prism.structure :refer :all]
            [prism.db :refer [retrieve-nft store-nft]]
            [prism.helpers :refer [<!!?
                                   with-test-node
                                   full-config
                                   full-nft-with-outcomes
                                   full-outcomes
                                   full-layers
                                   full-attributes
                                   full-nft-with-structure]]))

(deftest testing-applicative-functions
  (testing "apply-layers"
    (is (= {:a {:b :b :e :f} :c {:d :d}} (apply-layers {:a {:e :f} :c {:d :c}}
                                                 [[[:layers :a] {:b :b}]
                                                  [[:layers :c] {:d :d}]])))
    (is (= full-layers (apply-layers (get-in full-config [:base :layers]) full-outcomes))))
  (testing "apply-attributes"
    (is (= full-attributes (apply-attributes [] full-outcomes)))))

(deftest testing-sends
  (testing "send-nft"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-outcomes full-config true)
      (send-nft node "test" full-config full-nft-with-outcomes ch full-layers full-attributes)
      (is (= full-nft-with-structure (<!!? ch 3000)) "received NFT on out channel")
      (is (= full-nft-with-structure (retrieve-nft node "test" 33)) "received NFT from database"))))

(deftest testing-full-structure-resolution
  (testing "resolve-structure"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-outcomes full-config true)
      (resolve-structure node "test" full-config full-nft-with-outcomes ch)
      (is (= full-nft-with-structure (<!!? ch)) "received NFT on out channel")
      (is (= full-nft-with-structure (retrieve-nft node "test" 33)) "received NFT from database"))))
