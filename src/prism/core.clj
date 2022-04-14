(ns prism.core
  (:gen-class)
  (:require [clojure.core.async :refer [chan]]
            [clojure.java.io :refer [file]]
            [clojure.edn :as edn]
            [prism.db :refer [get-production-node retrieve-config]]
            [prism.preflight :refer [check-config]]
            [prism.pregenerate :refer [generate-collection]]
            [prism.decisions :refer [decision-resolver]]
            [prism.outcomes :refer [outcome-resolver]]
            [prism.structure :refer [structure-resolver]]
            [prism.metadata :refer [metadata-writer]]
            [prism.render :refer [image-renderer]]
            [prism.done :refer [done-checker]]))

(defn do-generate
  [node collection config assets-root]
  (check-config config)
  (let [in-ch (chan)]
    (.mkdir (java.io.File. (str assets-root "/" collection)))
    (->> in-ch
       (decision-resolver node collection config)
       (outcome-resolver node collection config)
       (structure-resolver node collection config)
       (metadata-writer node collection config assets-root)
       (image-renderer node collection config assets-root)
       (done-checker config))
    (generate-collection node collection config in-ch)))

(defn validate-collection-name
  [node name]
  (if (or (= "test" name) (= "sample" name))
    (do (println "'test' and 'sample' are reserved names.")
        (println "Please try something else:")
        nil)
    (if (or (.exists (file (str "assets/" name)))
            (not (nil? (retrieve-config node name))))
      (do (println (str "'" name "' is already in use."))
          (println "Please choose another collection name:")
          nil)
      name)))

(defn validate-config-file-location
  [loc]
  (if (and (.exists (file loc))
           (edn/read-string (slurp loc)))
    (edn/read-string (slurp loc))
    (do (println "There doesn't seem to be a config file there.")
        (println "Please choose another config file:")
        nil)))

(defn get-collection-name
  [node]
  (println "First, we need a collection name.")
  (println "This needs to be something you haven't used before:")
  (loop [collection-name nil]
    (if (not (nil? collection-name))
      collection-name
      (let [candidate (read-line)]
        (recur (validate-collection-name node candidate))))))

(defn get-config-file
  []
  (println "")
  (println "Great, now we need to choose a config file!")
  (println "You should type the file location relative to the Prism root,")
  (println "if you don't have a config file, try 'config/sample.edn':")
  (loop [config-file nil]
    (if (not (nil? config-file))
      config-file
      (let [candidate (read-line)]
        (recur (validate-config-file-location candidate))))))

(defn -main
  "Application main ingress function."
  [& args]
  (let [splash (slurp "splash.txt")
        node (get-production-node)]
    (println splash)
    (let [collection (get-collection-name node)
          config (get-config-file)
          assets-root "assets"]
    (do-generate node collection config assets-root))))
