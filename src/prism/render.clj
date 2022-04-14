(ns prism.render
  (:gen-class)
  (:require [clojure.core.async :refer [>! go <! chan]]
            [clojure.java.io :refer [file]]
            [prism.db :refer :all]
            [clojure2d.pixels :as px]
            [clojure2d.color.blend :as blend]
            [clojure2d.core :as c2d]
            [clojure2d.color :as color]
            [taoensso.timbre :refer :all]))

(defn- blend-layers
  [fun bottom top x y]
  (let [color-bottom (px/get-color bottom x y)
        color-top (px/get-color top x y)]
    (blend/blend-colors fun color-bottom color-top)))

(defn- compose-layers
  [blend-mode bottom top]
  (px/filter-colors-xy (partial blend-layers (blend/blends blend-mode) bottom) top))

(defn- correct-alpha
  [opacity color]
  (if (= opacity 100)
    color
    (color/set-alpha color (* opacity 2.55))))

(defn- to-hex
  [[r g b]]
  (format "#%x%x%x" r g b))

(defn send-nft
  [node collection config nft out]
  (go (let [new-nft (assoc nft :state :image-rendered)]
        (info (str "Rendered image for NFT (ID: " (:id new-nft) ")."))
        (let [res (store-nft node collection new-nft config)]
          (if (= :ok (first res))
            (>! out new-nft)
            (do (error (str "Failed to store NFT with ID " (:id new-nft) ". Retrying."))
                (send-nft node collection config nft out)))))))

(defn preload-images-from-resources
  [images]
  (loop [key (first (keys images))
         rest-keys (next (keys images))
         res {}]
    (if (nil? key)
      res
      (recur (first rest-keys)
             (next rest-keys)
             (assoc res key (if (string? (get images key))
                              (px/load-pixels (get images key))
                              (preload-images-from-resources (get images key))))))))

(defn save-image
  [assets-root collection id image]
  (c2d/save image (str assets-root "/" collection "/" id ".png")))

(defn render-layer
  [base layer colors images]
  (let [color (if (nil? (:color layer)) nil (get-in colors (:color layer)))
        image (get-in images (:image layer))
        blend-mode (:blend layer)]
    (compose-layers
     (if (nil? blend-mode) :normal blend-mode)
     base
     (if (nil? color)
       image
       (px/filter-channels (px/tint (color/to-color (to-hex color))) false image)))))

(defn render-image
  [node collection config images colors assets-root nft out]
  (go (case (:state nft)
        :metadata-generated (let [layers (get nft :layers)
                                  ks (keys layers)
                                  valid-keys (sort-by
                                              #(:order (get layers %))
                                              (filterv #(not (nil? (:image (get layers %))))
                                                       ks))]
                              (loop [key (first (next valid-keys))
                                     rest-keys (next (next valid-keys))
                                     wip (get-in images (:image (get layers (first valid-keys))))]
                                (if (nil? key)
                                  (do (save-image assets-root collection (:id nft) wip)
                                      (send-nft node collection config nft out))
                                  (recur (first rest-keys)
                                         (next rest-keys)
                                         (render-layer wip (get layers key) colors images)))))
        (>! out nft))))

(defn image-renderer
  [node collection config assets-root in]
  (let [images {:images (preload-images-from-resources (get-in config [:resources :images]))}
        colors {:colors (get-in config [:resources :colors])}
        out (chan)]
    (go (while true (render-image node collection config images colors assets-root (<! in) out)))
    out))
