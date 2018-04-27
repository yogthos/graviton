(defproject graviton "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [re-frame "0.10.5"]
                 [reagent "0.8.0"]
                 [cljsjs/pixi "4.7.0-0"]
                 [cljsjs/pixi-sound "1.4.1-0"]
                 [javax.xml.bind/jaxb-api "2.3.0"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.16-SNAPSHOT"]]

  :min-lein-version "2.5.0"
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :resource-paths ["public"]
  :source-paths ["src" "env/dev/cljs"]
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :figwheel {:http-server-root "."
             :nrepl-port 7002
             ;:server-port 3450
             :nrepl-middleware ["cider.piggieback/wrap-cljs-repl"]
             :css-dirs ["public/css"]}

  :cljsbuild {:builds {:app
                       {:source-paths ["src" "env/dev/cljs"]
                        :compiler
                        {:main "graviton.dev"
                         :output-to "public/js/app.js"
                         :output-dir "public/js/out"
                         :asset-path   "js/out"
                         :source-map true
                         :optimizations :none
                         :pretty-print  true}
                        :figwheel
                        {:on-jsload "graviton.core/mount-root"}}
                       :release
                       {:source-paths ["src" "env/prod/cljs"]
                        :compiler
                        {:output-to "public/js/app.js"
                         :output-dir "public/js/release"
                         :asset-path   "js/out"
                         :optimizations :advanced
                         :pretty-print false}}}}

  :aliases {"package" ["do" "clean" ["cljsbuild" "once" "release"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.7"]
                                  [figwheel-sidecar "0.5.16-SNAPSHOT"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [cider/piggieback "0.3.1"]]}})
