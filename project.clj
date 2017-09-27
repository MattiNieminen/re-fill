(defproject metosin/re-fill "0.1.0-SNAPSHOT"
  :description "A collection of Re-frame components that most applications need."
  :url "https://github.com/metosin/re-fill"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.854" :scope "provided"]
                 [re-frame "0.10.1" :scope "provided"]
                 [reagent "0.8.0-alpha1" :scope "provided"]
                 [bidi "2.1.2"]
                 [kibu/pushy "0.3.8"]
                 [cljs-uuid "0.0.4"]]
  :plugins [[lein-figwheel "0.5.12"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "example-src"]
                :figwheel {:open-urls ["http://localhost:3449"]}
                :compiler {:main example.core
                           :asset-path "js/out"
                           :output-to "dev-build/public/js/example.js"
                           :output-dir "dev-build/public/js/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}]}
  :figwheel {:css-dirs ["example-resources/public/css"]}
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]]
                   :clean-targets ^{:protect false} ["dev-build/public/js/"
                                                     :target-path]
                   :resource-paths ["dev-build" "example-resources"]}})
