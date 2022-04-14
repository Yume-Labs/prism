(ns prism.db-test
  (:require [clojure.test :refer :all]
            [prism.db :refer :all]
            [prism.helpers :refer [with-test-node
                                   full-config
                                   full-nft
                                   second-nft
                                   partial-config-twin-dedupe
                                   partial-config-no-twin-dedupe]]
            [xtdb.api :refer :all]))

(deftest test-storing-and-retrieving-config
  (testing "We can store and retrieve config"
    (-> (with-test-node)
       (store-config "test" full-config)
       (retrieve-config "test")
       (= full-config)
       (is "we can get our config back by collection name"))))

(deftest test-state-validation
  (testing "validate-nft-state"
    (is (= true (validate-nft-state :to-do)) "testing :to-do")
    (is (= true (validate-nft-state :decisions-made)) "testing :decisions-made")
    (is (= true (validate-nft-state :outcomes-resolved)) "testing :outcomes-resolved")
    (is (= true (validate-nft-state :ready)) "testing :ready")
    (is (= true (validate-nft-state :metadata-generated)) "testing :metadata-generated")
    (is (= true (validate-nft-state :image-rendered)) "testing :image-rendered")
    (is (= false (validate-nft-state :another-value)) "testing incorrect state"))
  (testing "get-previous-state"
    (is (= nil (get-previous-state :to-do)) "testing :to-do")
    (is (= :to-do (get-previous-state :decisions-made)) "testing :decisions-made")
    (is (= :decisions-made (get-previous-state :outcomes-resolved)) "testing :outcomes-resolved")
    (is (= :outcomes-resolved (get-previous-state :ready)) "testing :ready")
    (is (= :ready (get-previous-state :metadata-generated)) "testing :metadata-generated")
    (is (= :metadata-generated (get-previous-state :image-rendered)) "testing :image-rendered")))

(defn check-states
  [res & states]
  (if (= (count res) (count states))
    (loop [cur-res (first res)
           cur-state (first states)
           next-res (next res)
           next-states (next states)
           err []]
      (if (and (nil? cur-res) (nil? cur-state))
        err
        (let [payload (second cur-res)
              chk-state (:state payload)]
          (recur (first next-res)
                 (first next-states)
                 (next next-res)
                 (next next-states)
                 (if (= cur-state chk-state)
                   err
                   (conj err "Mismatched."))))))
    ["Mismatched array sizes."]))

(deftest test-storing-and-retrieving-nft
  (testing "We can store and retrieve config"
    (-> (with-test-node)
       (store-nft "test" full-nft full-config)
       (second)
       (retrieve-nft "test" (:id full-nft))
       (= full-nft)
       (is "we can get our nft back by collection name and id")))
  (testing "get-nfts"
    (-> (with-test-node)
       (store-nft "test" full-nft full-config)
       (second)
       (store-nft "test" (assoc second-nft :state :to-do) full-config)
       (second)
       (store-nft "test" second-nft full-config)
       (second)
       (get-nfts-thawed "test" get-nfts)
       (count)
       (= 2)
       (is "we get back two NFTs as expected")))
  (testing "get-nfts returns right state"
    (-> (with-test-node)
       (store-nft "test" full-nft full-config)
       (second)
       (store-nft "test" (assoc second-nft :state :to-do) full-config)
       (second)
       (store-nft "test" second-nft full-config)
       (second)
       (get-nfts-thawed "test" get-nfts)
       (check-states :to-do :decisions-made)
       (is "we get back correct states on results"))))

(deftest test-digests
  (testing "get-digest"
    (is (= nil (get-digest full-nft partial-config-twin-dedupe)))
    (is (= "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589"
           (get-digest second-nft partial-config-twin-dedupe)))
    (is (= nil (get-digest second-nft partial-config-no-twin-dedupe)))))

(deftest test-counting-nfts-with-digests
  (testing "count-nfts-with-digest"
    (-> (with-test-node)
       (store-nft "test" second-nft partial-config-twin-dedupe true)
       (second)
       (count-nfts-with-digest "test" "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589"))))
