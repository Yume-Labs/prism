(ns prism.pregenerate-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan]]
            [prism.pregenerate :refer :all]
            [prism.db :refer [retrieve-nft]]
            [prism.helpers :refer [<!!? with-test-node full-config]]))

(deftest testing-sends
  (testing "send-todo"
    (let [ch (chan)
          node (with-test-node)]
      (send-todo node "test" full-config 1 ch)
      (is (= {:id 1 :state :to-do} (<!!? ch)) "received NFT back on channel")
      (is (= {:id 1 :state :to-do} (retrieve-nft node "test" 1)) "received NFT back from database")))
  (testing "send-pregenerate"
    (let [ch (chan)
          node (with-test-node)
          pregen [[:attribute "Key" "Value"]]]
      (send-pregenerate node "test" full-config 25 pregen ch)
      (is (= {:id 25 :state :outcomes-resolved :outcomes [[:attribute "Key" "Value"]]} (<!!? ch))
          "received NFT back on channel")
      (is (= {:id 25 :state :outcomes-resolved :outcomes [[:attribute "Key" "Value"]]} (retrieve-nft node "test" 25))
          "received NFT back from database")))
  (testing "send-guarantee"
    (let [ch (chan)
          node (with-test-node)
          guarantee {:headwear :top_hat
                     :holding :diamond_cane}]
      (send-guarantee node "test" full-config 33 guarantee ch)
      (is (= {:id 33 :state :to-do :guarantee {:headwear :top_hat :holding :diamond_cane}} (<!!? ch))
          "received NFT back on channel")
      (is (= {:id 33 :state :to-do :guarantee {:headwear :top_hat :holding :diamond_cane}} (retrieve-nft node "test" 33))
          "received NFT back from database"))))

(deftest testing-generating-collection-level
  (testing "generate-nfts"
    (let [ch (chan)
          node (with-test-node)
          config (assoc full-config
                        :pregenerate [[[:attribute "Key" "Value"]
                                       [[:layers :background] {:image [:images :background :sunset]}]]]
                        :guarantees [{:headwear :top_hat
                                      :holding :diamond_cane}])]
      (generate-collection node "test" config ch)
      (loop [i 0
             seen []
             pregenerate nil
             guarantee nil]
        (let [subj (<!!? ch)]
          (if (= i 10)
            (do
              (is (= (count seen) 10) "correctly generated 10 NFTs")
              (is (not (nil? pregenerate)) "correctly generated pregenerated NFT")
              (is (not (nil? guarantee)) "correctly generated guarantee NFT")
              (is (= (count seen) (count (set seen))) "all IDs were used"))
            (do
              (is (= subj (retrieve-nft node "test" (:id subj))))
              (if (= (:guarantee subj) {:headwear :top_hat :holding :diamond_cane})
                (is (= 1 1) "correctly retrieved the guaranteed NFT with correct decisions")
                (is (= 1 1) "not the guarantee"))
              (if (= (:state subj) :outcomes-resolved)
                (is (= (:outcomes subj) [[:attribute "Key" "Value"]
                                         [[:layers :background] {:image [:images :background :sunset]}]])
                    "correctly retrieved the pregenerate with correct outcomes")
                (do (is (= (:outcomes subj) nil) "correctly retrieved no outcomes")
                    (is (= (:state subj) :to-do) "correctly retrieved :to-do state")))
              (recur (inc i)
                     (conj seen (:id subj))
                     (or (not (nil? pregenerate))
                         (= (:state subj) :outcomes-resolved))
                     (or (not (nil? guarantee))
                         (= (:guarantee subj) {:headwear :top_hat :holding :diamond_cane}))))))))))
