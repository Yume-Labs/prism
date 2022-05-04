(ns prism.decisions-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan]]
            [prism.decisions :refer :all]
            [prism.db :refer [retrieve-nft store-nft]]
            [prism.helpers :refer [<!!? with-test-node full-config partial-config-twin-dedupe]]))

(deftest testing-decision-making
  (testing "make-decision"
    (is (= {:a :b}
           (make-custom-decision {} {:name :a :type :custom :function #(if (= {} %) :b :c)}))
        "can make a decision based on a function"))
  (testing "make-range-decision"
    (dotimes [n 100]
      (let [res (make-decision {} {:name :a :type :range :min 0 :max 10})]
        (is (<= (:a res) 10) "number falls within range")
        (is (>= (:a res) 0) "number falls within range"))))
  (testing "make-conditional-decision"
    (is (= {:a :b} (make-decision {} {:name :a :type :conditional :outcome :b :filter #(nil? (:a %))}))
        "successfully resolves outcome when condition met")
    (is (= {} (make-decision {} {:name :a :type :conditional :outcome :b :filter #(not (nil? (:a %)))}))
        "doesn't resolve outcome when condition not met"))
  (testing "make-random-decision"
    (dotimes [n 100]
      (let [res (make-random-decision {} {:name :a :type :random :outcomes [:b :c {:name :d :weight 2}]})]
        (is (.contains [:b :c :d] (:a res)) "result falls within expected outcomes")))))

(deftest testing-weighted-random-list
  (testing "weighted-random-lists"
    (= [:a :b :c] (weighted-random-list [:a :b :c]))
    (= [:a :a :b :c] (weighted-random-list [{:name :a :weight 2} :b :c]))))

(deftest testing-sends
  (testing "send-nft"
    (let [out-ch (chan)
          in-ch (chan)
          node (with-test-node)]
      (send-nft node "test" partial-config-twin-dedupe {:id 1 :state :to-do} {:a :b :c :d} out-ch in-ch)
      (store-nft node "test" {:id 1 :state :to-do} partial-config-twin-dedupe)
      (store-nft node "test" {:id 2 :state :to-do} partial-config-twin-dedupe)
      (is (= {:id 1 :state :decisions-made :decisions {:a :b :c :d}} (<!!? out-ch 3000))
          "received NFT on out channel")
      (is (= {:id 1 :state :decisions-made :decisions {:a :b :c :d}} (retrieve-nft node "test" 1))
          "received NFT back from database")
      (send-nft node "test" partial-config-twin-dedupe {:id 2 :state :to-do} {:a :b :c :d} out-ch in-ch)
      (is (= {:id 2 :state :to-do} (<!!? in-ch 3000))
          "correctly de-duped twin")
      (is (= {:id 2 :state :to-do} (retrieve-nft node "test" 2))
          "correctly refused to write twin to database"))))

(deftest testing-full-generation
  (testing "make-decisions"
    (let [out-ch (chan)
          in-ch (chan)
          node (with-test-node)]
      (store-nft node "test" {:id 1 :state :to-do} full-config)
      (make-decisions node "test" full-config {:id 1 :state :to-do} out-ch in-ch)
      (let [res (<!!? out-ch)]
        (is (= res (retrieve-nft node "test" 1)) "NFT from out-channel matches database")
        (is (= :decisions-made (:state res)) "NFT was saved with the correct state")
        (is (not (nil? (get-in res [:decisions :background]))) "NFT has a background")
        (is (not (nil? (get-in res [:decisions :color-scheme]))) "NFT has a color scheme")
        (is (not (nil? (get-in res [:decisions :eyes]))) "NFT has eyes")
        (is (not (nil? (get-in res [:decisions :gender]))) "NFT has a gender"))))
  (testing "make-decisions with guarantee"
    (dotimes [n 10]
      (let [out-ch (chan)
            in-ch (chan)
            node (with-test-node)]
        (store-nft node "test" {:id 2 :state :to-do :guarantee {:color-scheme :blueberry :eyes :open :eye-color :blue}} full-config)
        (make-decisions node
                        "test"
                        full-config
                        {:id 2 :state :to-do :guarantee {:color-scheme :blueberry :eyes :open :eye-color :blue}}
                        out-ch
                        in-ch)
        (let [res (<!!? out-ch)]
          (is (= res (retrieve-nft node "test" 2)) "NFT from out channel matches database")
          (is (= :decisions-made (:state res)) "NFT was saved with the correct state")
          (is (= :blueberry (get-in res [:decisions :color-scheme])) "NFT has the correct color scheme from the guarantee")
          (is (= :open (get-in res [:decisions :eyes])) "NFT has open eyes from the guarantee")
          (is (= :blue (get-in res [:decisions :eye-color])) "NFT has the correct eye color from the guarantee")
          (is (not (nil? (get-in res [:decisions :gender]))) "NFT has a gender")
          (is (not (nil? (get-in res [:decisions :background]))) "NFT has a background color"))))))
