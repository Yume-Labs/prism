(ns prism.outcomes-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan]]
            [prism.outcomes :refer :all]
            [prism.db :refer [retrieve-nft store-nft]]
            [prism.helpers :refer [<!!?
                                   with-test-node
                                   full-config
                                   full-nft-with-decisions
                                   full-outcomes
                                   full-nft-with-outcomes]]))

(deftest test-sends
  (testing "send-nft"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-decisions full-config true)
      (send-nft node "test" full-config full-nft-with-decisions ch full-outcomes)
      (is (= full-nft-with-outcomes (<!!? ch 3000)) "received NFT on out channel")
      (is (= full-nft-with-outcomes (retrieve-nft node "test" 33)) "received NFT from database"))))

(deftest test-resolving-outcomes
  (testing "resolve-outcomes"
    (let [ch (chan)
          node (with-test-node)]
      (store-nft node "test" full-nft-with-decisions full-config true)
      (resolve-outcomes node "test" full-config full-nft-with-decisions ch)
      (let [res (<!!? ch)]
        (is (= (:id res) 33) "get the correct ID back")
        (is (= (:state res) :outcomes-resolved) "outcomes correctly resolved")
        (is (not (empty? (:outcomes res))) "get outcomes back")
        (is (= res (retrieve-nft node "test" 33)) "get the correct NFT back from database")
        (loop [outcome (first (:outcomes res))
               rest-outcomes (next (:outcomes res))
               seen-attributes []
               seen-layers []]
          (if (nil? outcome)
            (do (is (= (count seen-attributes) 5))
                (is (.contains seen-attributes "Time"))
                (is (.contains seen-attributes "Color Scheme"))
                (is (.contains seen-attributes "Eyes"))
                (is (.contains seen-attributes "Gender"))
                (is (.contains seen-attributes "Eye Color"))
                (is (.contains seen-layers :background))
                (is (.contains seen-layers :overlay))
                (is (.contains seen-layers :highlights))
                (is (.contains seen-layers :eyes-highlights))
                (is (.contains seen-layers :shadows))
                (is (.contains seen-layers :eyes-shadows))
                (is (.contains seen-layers :body))
                (is (.contains seen-layers :secondary))
                (is (.contains seen-layers :eyes-outline))
                (is (.contains seen-layers :eyes)))
            (recur (first rest-outcomes)
                   (next rest-outcomes)
                   (if (= :attribute (first outcome))
                     (conj seen-attributes (second outcome))
                     seen-attributes)
                   (if (vector? (first outcome))
                     (conj seen-layers (second (first outcome)))
                     seen-layers))))))))
