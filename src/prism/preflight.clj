(ns prism.preflight
  (:gen-class)
  (:require [clojure.java.io :as io]))

(defn validate
  ([value valid-fn message] (validate value valid-fn message true))
  ([value valid-fn message nil-allowed?]
   (if nil-allowed?
     (if (or (valid-fn value) (nil? value))
       nil
       message)
     (if (valid-fn value)
       nil
       message))))

(defn get-name
  [base-loc]
  (reduce #(str %1 "." (if (keyword? %2) (name %2) %2))
          (if (keyword? (first base-loc))
            (name (first base-loc))
            (first base-loc))
          (next base-loc)))

(defn validate-string
  [name value]
  (validate value string? (str "'" name "' must be a string.")))

(defn validate-exists
  [name value]
  (validate value #(some? %) (str "'" name "' must be present.") false))

(defn validate-length
  [name value min max]
  (validate value
            #(and (>= (count %) min) (<= (count %) max))
            (str "'" name "' must be at least " min " long and at most " max " long. Found " (count value) ".")))

(defn validate-value
  [name value min max]
  (validate value
            #(and (>= % min) (<= % max))
            (str "'" name "' must be at least " min " and at most " max ". Found " value ".")))

(defn validate-integer
  [name value]
  (validate value int? (str "'" name "' must be an integer.")))

(defn validate-map
  [name value]
  (validate value map? (str "'" name "' must be a map.")))

(defn validate-keyword
  [name value]
  (validate value keyword? (str "'" name "' must be a keyword.")))

(defn validate-vector
  [name value]
  (validate value vector? (str "'" name "' must be a vector.")))

(defn validate-in
  [name value haystack]
  (validate value #(.contains haystack %) (str "'" name "' not found in haystack.")))

(defn validate-has-key
  [name value haystack]
  (validate value #(contains? haystack %) (str "'" name "' not found in haystack.")))

(defn validate-boolean
  [name value]
  (validate value boolean? (str "'" name "' must be a boolean.")))

(defn validate-fetch
  [name value haystack]
  (validate value #(not (nil? (get-in haystack %))) (str "'" name "' not found in haystack.")))

(defn add-error
  [errors new-error]
  (if (nil? new-error)
    errors
    (vec (flatten (conj errors new-error)))))

(defn v
  [[config errors] get-loc validate-fn]
  (let [value (get-in config get-loc)
        name (get-name get-loc)
        new-error (validate-fn name value)]
    [config (add-error errors new-error)]))

(defn v-multi
  [[config errors] get-loc & validate-fns]
  (loop [err errors
         now (first validate-fns)
         later (next validate-fns)]
    (if (nil? now)
      [config err]
      (recur (second (v [config err] get-loc now)) (first later) (next later)))))

(defn v-multi-cond
  [[config errors] get-loc condition & validate-fns]
  (if condition
    (apply v-multi [config errors] get-loc validate-fns)
    [config errors]))

(defn v-multi-each
  [[config errors] get-loc extra-paths & validate-fns]
  (let [size (count (get-in config get-loc))]
    (loop [err errors
           i 0]
      (if (= i size)
        [config err]
        (recur (second (apply v-multi [config err] (apply conj get-loc i extra-paths) validate-fns))
               (inc i))))))

(defn v-multi-values
  [[config errors] get-loc extra-paths & validate-fns]
  (let [ks (keys (get-in config get-loc))]
    (loop [err errors
           now (first ks)
           later (next ks)]
      (if (nil? now)
        [config err]
        (recur (second (apply v-multi [config err] (apply conj get-loc now extra-paths) validate-fns))
               (first later)
               (next later))))))

(defn extract-shares
  [creators]
  (reduce #(+ %1 (:share %2)) 0 creators))

(defn x-validate-total-wallet-shares
  [[config errors]]
  (-> config
     (get-in [:config :creators])
     (extract-shares)
     (= 100)
     (if [config errors] [config (add-error errors "config.creators.*.share must total to 100.")])))

(defn validate-config-section
  [[config errors]]
  (-> [config errors]
     (v-multi [:config :name] validate-exists validate-string)
     (v-multi [:config :family] validate-exists validate-string)
     (v-multi [:config :symbol] validate-exists validate-string #(validate-length %1 %2 0 10))
     (v-multi [:config :description] validate-exists validate-string)
     (v-multi [:config :royalties] validate-exists validate-integer #(validate-value %1 %2 0 5000))
     (v-multi [:config :url] validate-exists validate-string)
     (v-multi [:config :creators] validate-exists validate-vector #(validate-length %1 %2 1 4))
     (v-multi-each [:config :creators] [:wallet] validate-exists validate-string)
     (v-multi-each [:config :creators] [:share] validate-exists validate-integer)
     (v-multi [:config :no-twins] validate-exists validate-boolean)
     (v-multi-cond [:config :twin-outcomes]
                   (get-in config [:config :no-twins])
                   validate-exists
                   validate-vector
                   #(validate-length %1 %2 1 10000))
     (v-multi-each [:config :twin-outcomes]
                   []
                   validate-keyword
                   #(validate-has-key %1 %2 (get-in config [:outcomes])))
     (v-multi [:config :size] validate-exists validate-integer #(validate-value %1 %2 1 500000))
     (v-multi [:config :assets-format] validate-exists validate-keyword)
     (v-multi [:config :naming :type] validate-exists validate-keyword #(validate-in %1 %2 [:autoinc]))
     (v-multi [:config :naming :base] validate-exists validate-string)
     (x-validate-total-wallet-shares)))

(defn validate-base-section
  [[config errors]]
  (-> [config errors]
     (v-multi [:base :layers] validate-exists validate-map)
     (v-multi-values [:base :layers] [:order] validate-integer)
     (v-multi-values [:base :layers] [:image] validate-vector #(validate-fetch %1 %2 (get config :resources)))
     (v-multi-values [:base :layers] [:blend] validate-keyword)
     (v-multi-values [:base :layers] [:color] validate-vector #(validate-fetch %1 %2 (get config :resources)))))

(defn validate-pregenerate-section
  [[config errors]]
  (-> [config errors]
     (v-multi [:pregenerate] validate-exists
              validate-vector
              #(validate-length %1 %2 0 (get-in config [:config :size])))))

(defn validate-decisions-section
  [[config errors]]
  (-> [config errors]
     (v-multi-each [:decisions]
                   [:name]
                   validate-exists
                   validate-keyword
                   #(validate-has-key %1 %2 (get-in config [:outcomes])))
     (v-multi-each [:decisions]
                   [:type]
                   validate-exists
                   validate-keyword
                   #(validate-in %1 %2 [:random :conditional :range :custom]))))


(defn validate-images
  ([[config errors]] [config (add-error errors (validate-images config [:resources :images]))])
  ([config base-loc]
   (let [errors []
         current (get-in config base-loc)]
     (if (map? current)
       (-> #(validate-images config (conj base-loc %))
          (mapv (keys current))
          (flatten)
          (vec))
       (if (.exists (io/file current))
         []
         [(str "File '" current "' does not exist but is specified at " (get-name base-loc) ".")])))))

(defn validate-colors
  ([[config errors]] [config (add-error errors (validate-colors config [:resources :colors]))])
  ([config base-loc]
   (let [errors []
         current (get-in config base-loc)]
     (if (map? current)
       (-> #(validate-colors config (conj base-loc %))
          (mapv (keys current))
          (flatten)
          (vec))
       (if (and (vector? current)
                (or (= 3 (count current))
                    (= 4 (count current)))
                (= (count current)
                   (count (filterv #(and (>= % 0) (<= % 255)) current))))
         []
         [(str "Color defined at " (get-name base-loc) "is incorrect.")])))))

(defn deep-validate-resources
  [[config errors]]
  (-> [config errors]
     (validate-images)
     (validate-colors)))

(defn validate-resources
  [[config errors]]
  (-> [config errors]
     (v-multi [:resources :images] validate-exists validate-map)
     (v-multi [:resources :colors] validate-exists validate-map)
     (deep-validate-resources)))

(defn print-errors
  [errors]
  (println (str "Your config had " (count errors) "issues."))
  (loop [now (first errors)
         later (next errors)]
    (if (nil? now)
      :done
      (do
        (println now)
        (recur (first later) (next later))))))

(defn handle-errors
  [[config errors]]
  (if (empty? errors)
    [config errors]
    (do (print-errors errors)
        [config errors])))

(defn return-if-ok
  [[config errors]]
  (if (empty? errors)
    config
    (do (println "Errors encountered while validating config:")
        (loop [error (first errors)
               rest-errors (next errors)]
          (if (nil? error)
            (System/exit 1)
            (println error))))))

(defn check-config
  "Accepts a config struct and checks for defects."
  [config]
  (-> [config []]
     (validate-config-section)
     (validate-base-section)
     (validate-pregenerate-section)
     (validate-decisions-section)
     (validate-resources)
     (handle-errors)
     (return-if-ok)))
