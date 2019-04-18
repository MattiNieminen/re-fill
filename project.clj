(defproject mattinieminen/re-fill "0.1.0"
  :description "A collection of Re-frame components that most applications need."
  :url "https://github.com/MattiNieminen/re-fill"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [re-frame "0.10.6" :scope "provided"]
                 [reagent "0.8.1" :scope "provided"]
                 [bidi "2.1.6"]
                 [lifecheq/pushy "0.3.9"]
                 [cljs-uuid "0.0.4"]]
  :profiles {:dev {:source-paths ["src" "example-src"]
                   :dependencies [[com.bhauman/figwheel-main "0.2.0"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [binaryage/devtools "0.9.10"]]}}
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "build-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]})
