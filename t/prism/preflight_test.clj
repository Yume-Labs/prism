(ns prism.preflight-test
  (:require [clojure.test :refer :all]
            [prism.preflight :refer :all]
            [prism.helpers :refer [full-config]]))

(deftest test-validators
  (testing "validate/3"
    (is (= (validate 5 int? "fail") nil) "testing a valid value")
    (is (= (validate nil int? "fail") nil) "testing nil")
    (is (= (validate "5" int? "fail") "fail") "testing an invalid value"))
  (testing "validate/4 disallowing nil"
    (is (= (validate nil int? "fail" false) "fail") "testing nil"))
  (testing "validate-string"
    (is (= (validate-string "name" "value") nil) "testing a string")
    (is (= (validate-string "name" 5) "'name' must be a string.") "testing a number"))
  (testing "validate-exists"
    (is (= (validate-exists "name" "value") nil) "testing an existing value")
    (is (= (validate-exists "name" nil) "'name' must be present.") "testing nil"))
  (testing "validate-length"
    (is (= (validate-length "name" "value" 5 10) nil) "testing acceptable length string")
    (is (= (validate-length "name" [1 2 3 4 5] 0 5) nil) "testing acceptable length vec")
    (is (string? (validate-length "name" "value" 6 10)) "testing too short string")
    (is (string? (validate-length "name" "value" 0 4)) "testing too long string")
    (is (string? (validate-length "name" [1 2 3 4 5] 6 10)) "testing too short vec")
    (is (string? (validate-length "name" [1 2 3 4 5] 0 4)) "testing too long vec"))
  (testing "validate-value"
    (is (nil? (validate-value "name" 5 0 5)) "testing acceptable value")
    (is (string? (validate-value "name" 6 0 5)) "testing too high value")
    (is (string? (validate-value "name" -1 0 5)) "testing too low value"))
  (testing "validate-integer"
    (is (nil? (validate-integer "name" 1)) "testing integer")
    (is (string? (validate-integer "name" "1")) "testing string"))
  (testing "validate-map"
    (is (nil? (validate-map "name" {:a :b})) "testing map")
    (is (string? (validate-map "name" [:a :b])) "testing vector"))
  (testing "validate-keyword"
    (is (nil? (validate-keyword "name" :keyword)) "testing keyword")
    (is (string? (validate-keyword "name" "keyword")) "testing string"))
  (testing "validate-vector"
    (is (nil? (validate-vector "name" [1 2 3])) "testing vector")
    (is (string? (validate-vector "name" '(1 2 3))) "testing seq"))
  (testing "validate-in"
    (is (nil? (validate-in "name" 1 [1 2 3])) "testing found")
    (is (string? (validate-in "name" 1 [2 3 4])) "testing not found"))
  (testing "validate-has-key"
    (is (nil? (validate-has-key "name" :a {:a :b :c :d})) "testing found")
    (is (string? (validate-has-key "name" :a {:c :d})) "testing not found"))
  (testing "validate-boolean"
    (is (nil? (validate-boolean "name" true)) "testing true")
    (is (nil? (validate-boolean "name" false)) "testing false")
    (is (string? (validate-boolean "name" "true")) "testing string"))
  (testing "validate-fetch"
    (is (nil? (validate-fetch "name" [:a :b] {:a {:b :c}})) "testing valid fetch target")
    (is (string? (validate-fetch "name" [:a :c] {:a {:b :c}})) "testing invalid fetch target")))

(deftest v-test
  (testing "v"
    (is (= [{:a :b} []] (v [{:a :b} []] [:a] validate-exists)) "testing valid")
    (is (= [{:a :b} ["'a' must be a string."]]
           (v [{:a :b} []] [:a] validate-string)) "testing not string")
    (is (= [{:a {:b :c}} ["'a.b' must be a string."]]
           (v [{:a {:b :c}} []] [:a :b] validate-string)) "testing name concat")))

(defn test-v-convenience-function
  [[expected-config expected-errors] result message]
  (let [[config errors] result]
    (is (= expected-config config) (str message " (config)"))
    (is (= expected-errors (count errors)) (str message " (errors)"))))

(def config
  {:valid {:string "This is a string"
           :a "a"
           :int 5
           :vector ["a" "b" "c"]
           :deep-vector [{:a :b} {:a :d} {:a :e}]
           :map {:a :b :c :d}
           :deep-map {:a {:b [1 2 3]}
                      :b {:b [4 5 6]}
                      :c {:b [7 8 9]}}}
   :invalid {:string 5
             :int "This is a string"
             :vector {:a :b :c :d}
             :map ["a" "b" "c"]}})

(deftest testing-v-convenience-functions
  (testing "v-multi"
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi [:valid :string] validate-exists validate-string))
                                 "valid string exists and is string")
    (test-v-convenience-function [config 1]
                                 (-> [config []]
                                    (v-multi [:invalid :string] validate-exists validate-string))
                                 "invalid string exists and is string")
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi [:valid :a]
                                             validate-exists
                                             #(validate-in %1 %2 (get-in config [:valid :vector]))))
                                 "more complex validations"))
  (testing "v-multi-cond"
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi-cond [:valid :int] true validate-exists validate-integer))
                                 "testing valid integer")
    (test-v-convenience-function [config 1]
                                 (-> [config []]
                                    (v-multi-cond [:invalid :int] true validate-exists validate-integer))
                                 "testing invalid integer")
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi-cond [:invalid :int] false validate-exists validate-integer))
                                 "testing invalid integer but condition should make it pass anyway"))
  (testing "v-multi-each"
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi-each [:valid :deep-vector] [:a] validate-exists validate-keyword))
                                 "testing with deep vec")
    (test-v-convenience-function [config 3]
                                 (-> [config []]
                                    (v-multi-each [:valid :deep-vector] [:b] validate-exists validate-keyword))
                                 "testing for value which doesn't exist in deep vec"))
  (testing "v-multi-values"
    (test-v-convenience-function [config 0]
                                 (-> [config []]
                                    (v-multi-values [:valid :deep-map] [:b] validate-exists validate-vector))
                                 "testing with deep map")
    (test-v-convenience-function [config 3]
                                 (-> [config []]
                                    (v-multi-values [:valid :deep-map] [:c] validate-exists validate-vector))
                                 "testing for value which doesn't exist in deep map")))

(deftest testing-full-validator-sets
  (testing "validate-config-section"
    (is (= [full-config []] (validate-config-section [full-config []]))))
  (testing "validate-base-section"
    (is (= [full-config []] (validate-base-section [full-config []]))))
  (testing "validate-pregenerate-section"
    (is (= [full-config []] (validate-pregenerate-section [full-config []]))))
  (testing "validate-decisions-section"
    (is (= [full-config []] (validate-decisions-section [full-config []]))))
  (testing "validate-images"
    (is (= [full-config []] (validate-images [full-config []]))))
  (testing "validate-colors"
    (is (= [full-config []] (validate-colors [full-config []]))))
  (testing "validate-resources"
    (is (= [full-config []] (validate-resources [full-config []])))))

(deftest testing-ok-return
  (testing "return-if-ok"
    (is (= full-config (return-if-ok [full-config []])))))
