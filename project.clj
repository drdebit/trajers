(defproject trajers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [metasoarous/oz "1.6.0-alpha5"]
                 [incanter/incanter-core "1.9.3"]
                 [uncomplicate/neanderthal "0.26.1"]
                 ]
  :java-source-paths ["src/java"]
  :plugins [[lein-with-env-vars "0.2.0"]]
  :env-vars {:LD_LIBRARY_PATH "/home/matt/intel/mkl/lib/:/home/matt/intel/lib/:/home/matt/intel/mkl/lib/intel64/"}
  :main ^:skip-aot trajers.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
