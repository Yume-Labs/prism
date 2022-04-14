(ns prism.helpers
  (:require [clojure.core.async :refer [timeout alts!!]]
            [prism.db :refer [get-node]]))

(defn <!!?
  ([chan] (<!!? chan 1000))
  ([chan ms]
   (let [timeout (timeout ms)
         [value port] (alts!! [chan timeout])]
     (if (= chan port)
       value
       :timed-out))))

(defn with-test-node
  []
  (get-node {}))

(def full-config
  {:config {:name "Sample Collection"
            :family "Yume Labs"
            :symbol "COLL"
            :url "https://yumelabs.io"
            :description "This is a sample collection by Yume Labs."
            :royalties 500
            :no-twins true
            :creators [{:wallet "Fk3mJaeqYjZucRs3UsGTDxyEm5q5dg39EjRoR4NPK9hM" 
                        :share 100}]
            :twin-outcomes [:background
                            :color-scheme
                            :eyes
                            :eye-color]
            :size 10
            :assets-format :solana
            :naming {:base "Sample #"
                     :type :autoinc}}
   :base {:layers {:background {:order 0}
                   :body {:order 1 :image [:images :body]}
                   :mouth {:order 2 :image [:images :mouth :smile]}
                   :eyes {:order 3}
                   :secondary {:order 4 :image [:images :secondary]}
                   :overlay {:order 5 :image nil :blend :overlay}
                   :shadows {:order 6 :image [:images :shadows] :blend :multiply}
                   :eyes-shadows {:order 7 :blend :multiply}
                   :highlights {:order 8 :image [:images :highlights] :blend :add}
                   :eyes-highlights {:order 9 :blend :add}
                   :eyes-outline {:order 10}
                   :outline {:order 11 :image [:images :outline]}}}
   :pregenerate []
   :decisions [{:name :background
                    :type :random
                    :outcomes [{:name :day :weight 3}
                               {:name :sunset :weight 2}
                               {:name :night :weight 1}]}
                   {:name :color-scheme
                    :type :random
                    :outcomes [:strawberry :blueberry :midnight :coder]}
                   {:name :eyes
                    :type :random
                    :outcomes [:open :closed]}
                   {:name :eye-color
                    :filter '(fn [processed] (not (= (:eyes processed) :open)))
                    :type :random
                    :outcomes [:red :blue :green]}
                   {:name :gender
                    :type :random
                    :outcomes [:male :female]}]
   :outcomes {:background {:day [[:attribute "Time" "Day"]
                                 [[:layers :background] {:image [:images :background :day]}]
                                 [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :day]}]
                                 [[:layers :highlights] {:color [:colors :highlights :day]}]
                                 [[:layers :eyes-highlights] {:color [:colors :highlights :day]}]
                                 [[:layers :shadows] {:color [:colors :shadows :day]}]
                                 [[:layers :eyes-shadows] {:color [:colors :shadows :day]}]]
                           :sunset [[:attribute "Time" "Sunset"]
                                    [[:layers :background] {:image [:images :background :sunset]}]
                                    [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :sunset]}]
                                    [[:layers :highlights] {:color [:colors :highlights :sunset]}]
                                    [[:layers :eyes-highlights] {:color [:colors :highlights :sunset]}]
                                    [[:layers :shadows] {:color [:colors :shadows :sunset]}]
                                    [[:layers :eyes-shadows] {:color [:colors :shadows :sunset]}]]
                           :night [[:attribute "Time" "Night"]
                                   [[:layers :background] {:image [:images :background :night]}]
                                   [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :night]}]
                                   [[:layers :highlights] {:color [:colors :highlights :night]}]
                                   [[:layers :eyes-highlights] {:color [:colors :highlights :night]}]
                                   [[:layers :shadows] {:color [:colors :shadows :night]}]
                                   [[:layers :eyes-shadows] {:color [:colors :shadows :night]}]]}
              :color-scheme {:strawberry [[:attribute "Color Scheme" "Strawberry"]
                                          [[:layers :body] {:color [:colors :cream]}]
                                          [[:layers :secondary] {:color [:colors :red]}]]
                             :blueberry [[:attribute "Color Scheme" "Blueberry"]
                                         [[:layers :body] {:color [:colors :cream]}]
                                         [[:layers :secondary] {:color [:colors :blue]}]]
                             :midnight [[:attribute "Color Scheme" "Midnight"]
                                        [[:layers :body] {:color [:colors :grey]}]
                                        [[:layers :secondary] {:color [:colors :blue]}]]
                             :coder [[:attribute "Color Scheme" "Coder"]
                                     [[:layers :body] {:color [:colors :grey]}]
                                     [[:layers :secondary] {:color [:colors :green]}]]}
              :eyes {:open [[:attribute "Eyes" "Open"]
                            [[:layers :eyes-outline] {:image [:images :eyes :outline :open]}]
                            [[:layers :eyes-highlight] {:image [:images :eyes :highlights :open]}]
                            [[:layers :eyes-shadows] {:image [:images :eyes :shadows :open]}]
                            [[:layers :eyes] {:image [:images :eyes :color :open]}]]
                     :closed [[:attribute "Eyes" "Closed"]
                              [:attribute "Eye Color" "None"]
                              [[:layers :eyes-outline] {:image [:images :eyes :outline :closed]}]]}
              :eye-color {:red [[:attribute "Eye Color" "Red"]
                                [[:layers :eyes] {:color [:colors :red]}]]
                          :blue [[:attribute "Eye Color" "Blue"]
                                 [[:layers :eyes] {:color [:colors :blue]}]]
                          :green [[:attribute "Eye Color" "Green"]
                                  [[:layers :eyes] {:color [:colors :green]}]]}
              :gender {:male [[:attribute "Gender" "Male"]]
                       :female [[:attribute "Gender" "Female"]]}}
   :resources {:images {:background {:day "doc/tutorial/res/background/day.png"
                                     :night "doc/tutorial/res/background/night.png"
                                     :sunset "doc/tutorial/res/background/sunset.png"}
                        :body "doc/tutorial/res/body/body.png"
                        :mouth {:smile "doc/tutorial/res/mouth/smile.png"}
                        :eyes {:outline {:open "doc/tutorial/res/eyes/open/outline.png"
                                         :closed "doc/tutorial/res/eyes/closed/outline.png"}
                               :color {:open "doc/tutorial/res/eyes/open/color.png"}
                               :highlights {:open "doc/tutorial/res/eyes/open/highlights.png"}
                               :shadows {:open "doc/tutorial/res/eyes/open/shadows.png"}}
                        :overlay "doc/tutorial/res/overlay/overlay.png"
                        :secondary "doc/tutorial/res/secondary/secondary-color.png"
                        :outline "doc/tutorial/res/outline/outline.png"
                        :shadows "doc/tutorial/res/shadows/shadows.png"
                        :highlights "doc/tutorial/res/highlights/highlights.png"}
               :colors {:cream [255 235 215]
                        :grey [60 50 50]
                        :red [255 175 175]
                        :green [175 255 175]
                        :blue [175 175 255]
                        :overlay {:day [127 127 127]
                                  :sunset [160 133 96]
                                  :night [45 55 110]}
                        :highlights {:day [235 235 175]
                                     :sunset [230 195 120]
                                     :night [115 170 200]}
                        :shadows {:day [30 30 205]
                                  :sunset [25 70 150]
                                  :night [40 20 100]}}}})

(def full-nft
  {:id 1
   :state :to-do})

(def second-nft
  {:id 2
   :state :decisions-made
   :decisions [[:a :b]
               [:c :d]]})

(def partial-config-twin-dedupe
  {:config {:no-twins true
            :twin-outcomes [:a :c]}})

(def partial-config-no-twin-dedupe
  {:config {:no-twins false
            :twin-outcomes [:a :c]}})

(def full-nft-with-decisions
  {:id 33
   :state :decisions-made
   :decisions {:background :day
               :color-scheme :blueberry
               :eyes :open
               :eye-color :red
               :gender :female}})

(def full-outcomes
  [[:attribute "Time" "Day"]
   [[:layers :background] {:image [:images :background :day]}]
   [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :day]}]
   [[:layers :highlights] {:color [:colors :highlights :day]}]
   [[:layers :eyes-highlights] {:color [:colors :highlights :day]}]
   [[:layers :shadows] {:color [:colors :shadows :day]}]
   [[:layers :eyes-shadows] {:color [:colors :shadows :day]}]
   [:attribute "Color Scheme" "Blueberry"]
   [[:layers :body] {:color [:colors :cream]}]
   [[:layers :secondary] {:color [:colors :blue]}]
   [:attribute "Eyes" "Open"]
   [[:layers :eyes-outline] {:image [:images :eyes :outline :open]}]
   [[:layers :eyes-highlights] {:image [:images :eyes :highlights :open]}]
   [[:layers :eyes-shadows] {:image [:images :eyes :shadows :open]}]
   [[:layers :eyes] {:image [:images :eyes :color :open]}]
   [:attribute "Eye Color" "Red"]
   [[:layers :eyes] {:color [:colors :red]}]
   [:attribute "Gender" "Female"]])

(def full-nft-with-outcomes
  {:id 33
   :state :outcomes-resolved
   :decisions {:background :day
               :color-scheme :blueberry
               :eyes :open
               :eye-color :red
               :gender :female}
   :outcomes [[:attribute "Time" "Day"]
              [[:layers :background] {:image [:images :background :day]}]
              [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :day]}]
              [[:layers :highlights] {:color [:colors :highlights :day]}]
              [[:layers :eyes-highlights] {:color [:colors :highlights :day]}]
              [[:layers :shadows] {:color [:colors :shadows :day]}]
              [[:layers :eyes-shadows] {:color [:colors :shadows :day]}]
              [:attribute "Color Scheme" "Blueberry"]
              [[:layers :body] {:color [:colors :cream]}]
              [[:layers :secondary] {:color [:colors :blue]}]
              [:attribute "Eyes" "Open"]
              [[:layers :eyes-outline] {:image [:images :eyes :outline :open]}]
              [[:layers :eyes-highlights] {:image [:images :eyes :highlights :open]}]
              [[:layers :eyes-shadows] {:image [:images :eyes :shadows :open]}]
              [[:layers :eyes] {:image [:images :eyes :color :open]}]
              [:attribute "Eye Color" "Red"]
              [[:layers :eyes] {:color [:colors :red]}]
              [:attribute "Gender" "Female"]]})

(def full-attributes [["Time" "Day"]
                    ["Color Scheme" "Blueberry"]
                    ["Eyes" "Open"]
                    ["Eye Color" "Red"]
                    ["Gender" "Female"]])

(def full-layers {:background {:order 0 :image [:images :background :day]}
                :body {:order 1 :image [:images :body] :color [:colors :cream]}
                :mouth {:order 2 :image [:images :mouth :smile]}
                :eyes {:order 3 :image [:images :eyes :color :open] :color [:colors :red]}
                :secondary {:order 4 :image [:images :secondary] :color [:colors :blue]}
                :overlay {:order 5
                          :blend :overlay
                          :image [:images :overlay]
                          :color [:colors :overlay :day]}
                :shadows {:order 6
                          :image [:images :shadows]
                          :blend :multiply
                          :color [:colors :shadows :day]}
                :eyes-shadows {:order 7
                               :blend :multiply
                               :image [:images :eyes :shadows :open]
                               :color [:colors :shadows :day]}
                :highlights {:order 8
                             :image [:images :highlights]
                             :blend :add
                             :color [:colors :highlights :day]}
                :eyes-highlights {:order 9
                                  :blend :add
                                  :color [:colors :highlights :day]
                                  :image [:images :eyes :highlights :open]}
                :eyes-outline {:order 10 :image [:images :eyes :outline :open]}
                :outline {:order 11 :image [:images :outline]}})

(def full-nft-with-structure
  {:id 33
   :state :ready
   :decisions {:background :day
               :color-scheme :blueberry
               :eyes :open
               :eye-color :red
               :gender :female}
   :outcomes [[:attribute "Time" "Day"]
              [[:layers :background] {:image [:images :background :day]}]
              [[:layers :overlay] {:image [:images :overlay] :color [:colors :overlay :day]}]
              [[:layers :highlights] {:color [:colors :highlights :day]}]
              [[:layers :eyes-highlights] {:color [:colors :highlights :day]}]
              [[:layers :shadows] {:color [:colors :shadows :day]}]
              [[:layers :eyes-shadows] {:color [:colors :shadows :day]}]
              [:attribute "Color Scheme" "Blueberry"]
              [[:layers :body] {:color [:colors :cream]}]
              [[:layers :secondary] {:color [:colors :blue]}]
              [:attribute "Eyes" "Open"]
              [[:layers :eyes-outline] {:image [:images :eyes :outline :open]}]
              [[:layers :eyes-highlights] {:image [:images :eyes :highlights :open]}]
              [[:layers :eyes-shadows] {:image [:images :eyes :shadows :open]}]
              [[:layers :eyes] {:image [:images :eyes :color :open]}]
              [:attribute "Eye Color" "Red"]
              [[:layers :eyes] {:color [:colors :red]}]
              [:attribute "Gender" "Female"]]
   :layers {:background {:order 0 :image [:images :background :day]}
            :body {:order 1 :image [:images :body] :color [:colors :cream]}
            :mouth {:order 2 :image [:images :mouth :smile]}
            :eyes {:order 3 :image [:images :eyes :color :open] :color [:colors :red]}
            :secondary {:order 4 :image [:images :secondary] :color [:colors :blue]}
            :overlay {:order 5
                      :blend :overlay
                      :image [:images :overlay]
                      :color [:colors :overlay :day]}
            :shadows {:order 6 :image [:images :shadows] :blend :multiply :color [:colors :shadows :day]}
            :eyes-shadows {:order 7
                           :blend :multiply
                           :image [:images :eyes :shadows :open]
                           :color [:colors :shadows :day]}
            :highlights {:order 8
                         :image [:images :highlights]
                         :blend :add
                         :color [:colors :highlights :day]}
            :eyes-highlights {:order 9
                              :blend :add
                              :color [:colors :highlights :day]
                              :image [:images :eyes :highlights :open]}
            :eyes-outline {:order 10 :image [:images :eyes :outline :open]}
            :outline {:order 11 :image [:images :outline]}}
   :attributes [["Time" "Day"]
                ["Color Scheme" "Blueberry"]
                ["Eyes" "Open"]
                ["Eye Color" "Red"]
                ["Gender" "Female"]]})

(def full-nft-with-metadata
  (assoc full-nft-with-structure :state :metadata-generated))

(def full-nft-with-image
  (assoc full-nft-with-metadata :state :image-rendered))

(def full-metadata
  {:name "Sample #33"
   :description "This is a sample collection by Yume Labs."
   :symbol "COLL"
   :image "33.png"
   :seller_fee_basis_points 500
   :collection {:name "Sample Collection"
                :family "Yume Labs"}
   :external_url "https://yumelabs.io"
   :properties {:files [{:uri "33.png" :type "image/png"}]
                :creators [{:address "Fk3mJaeqYjZucRs3UsGTDxyEm5q5dg39EjRoR4NPK9hM"
                            :share 100}]}
   :attributes [{:trait_type "Time" :value "Day"}
                {:trait_type "Color Scheme" :value "Blueberry"}
                {:trait_type "Eyes" :value "Open"}
                {:trait_type "Eye Color" :value "Red"}
                {:trait_type "Gender" :value "Female"}]})
