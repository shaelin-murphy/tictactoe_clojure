(defproject tictactoe "0.1.0-SNAPSHOT"
  :description "A simple tic-tac-toe web game"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [compojure "1.7.0"]
                 [hiccup "1.0.5"]]
  :main ^:skip-aot tictactoe.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :uberjar-name "tictactoe-standalone.jar")