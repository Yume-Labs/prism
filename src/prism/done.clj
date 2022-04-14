(ns prism.done
  (:gen-class)
  (:require [clojure.core.async :refer [<! go]]
            [taoensso.timbre :refer :all]))

(defn done-checker
  [config in]
  (go (loop [count (get-in config [:config :size])]
        (if (> count 0)
          (if (nil? (<! in))
            (recur count)
            (recur (dec count)))
          (do (info "Finished processing.")
              (System/exit 0))))))
