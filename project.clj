(defproject io.yumelabs/prism "0.2.0-beta"
  :description "Prism Engine is a next generation asset rendering program for the Solana blockchain network."
  :url "http://github.com/Yume-Labs/prism"
  :license {:name "GNU Affero General Public License"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clj-commons/digest "1.4.100"]
                 [clojure2d "1.4.3"]
                 [cheshire "5.10.0"]
                 [com.taoensso/nippy "3.1.1"]
                 [com.taoensso/timbre "5.2.1"]
                 [com.xtdb/xtdb-core "1.20.0"]
                 [com.xtdb/xtdb-lmdb "1.20.0"]
                 [org.slf4j/slf4j-api "1.7.14"]
                 [progrock "0.1.2"]]
  :main prism.core
  :aot :all
  :repl-options {:init-ns prism.core}
  :source-paths ["src"]
  :test-paths ["t"]
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path])

