(ns i-read-slowly.core
  (:import (com.gargoylesoftware.htmlunit WebClient))
  (:use [clojure.contrib.command-line :only [with-command-line]])
  (:gen-class))

;; My Account https://catalog.nypl.org/patroninfo/0/top
;; My Checked Out Items https://catalog.nypl.org/patroninfo/0/items

;; note: using BrowserVersion/FIREFOX_3_6 causes a ScriptException.
;; Fine with default IE7.
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

(defn doit [user pass]
  (let [items-page (login user pass (WebClient.))
        result (renew-all items-page)]
    (.asXml result)))

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
