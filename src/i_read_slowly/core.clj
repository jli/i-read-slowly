(ns i-read-slowly.core
  (:import (com.gargoylesoftware.htmlunit
            WebClient SilentCssErrorHandler))
  (:use [clojure.contrib.command-line :only [with-command-line]])
  (:gen-class))

;; My Account https://catalog.nypl.org/patroninfo/0/top
;; My Checked Out Items https://catalog.nypl.org/patroninfo/0/items

;; doesn't work to silence output of java libraries, it seems...
(defmacro with-silence [& body]
  `(let [dev-null# (java.io.StringWriter.)]
     (binding [*out* dev-null#
               *err* dev-null#]
       (try (do ~@body)
            (catch Exception _# )))))

(defn login [user pass client]
  (let [page (.getPage client "https://catalog.nypl.org/patroninfo/0/items")
        [form] (.getForms page)
        [submit] (.getElementsByAttribute form "input" "class" "btn-submit")
        [barcode pin] (map #(.getInputByName form %) ["code" "pin"])]
    (.setValueAttribute barcode user)
    (.setValueAttribute pin pass)
    (.click submit)))

(defn renew-all [checked-out-page]
  (let [[renew-all] (filter #(re-find #"requestRenewAll" (.getOnClickAttribute %))
                            (.getAnchors checked-out-page))
        confirm-page (.click renew-all)
        [checkout-form] (.getForms confirm-page)
        yes (.getInputByName checkout-form "renewall")]
    (.click yes)))

;; these help a little, but still really noisy.
;; inc-listen (proxy [com.gargoylesoftware.htmlunit.IncorrectnessListener] []
;;              [notify [_message _origin]
;;               (println "<silenced incorrectness notification>")])
;; wc (doto (WebClient.)
;;      (.setIncorrectnessListener inc-listen)
;;      (.setCssErrorHandler (SilentCssErrorHandler.))
;;      ;(.setThrowExceptionOnFailingStatusCode false)
;;      ;(.setThrowExceptionOnScriptError false)
;;      )
(defn doit [user pass]
  (println "Doing it!")
  (let [wc (WebClient.)
        items-page (login user pass wc)
        _ (println "Logged in. Renewing...")
        result (renew-all items-page)
        _ (println "Renewed!")
        page (.asText result)]
    (.closeAllWindows wc)
    page))

(defn -main [& args]
  (with-command-line args
    "Renew all checked out items for NYPL account."
    [[barcode b "NYPL 14 digit barcode"]
     [pin p "4 digit PIN"]
     anon]
    (when (some nil? [barcode pin])
      (println "Need both barcode (-b) and pin (-p)!")
      (System/exit 1))
    (when-not (empty? anon)
      (println "Didn't expect anonymous arguments:" anon)
      (System/exit 1))
    (println (doit barcode pin))))
