(ns i-read-slowly.core
  (:import (com.gargoylesoftware.htmlunit
            WebClient BrowserVersion ScriptException))
  (:use [clojure.contrib.command-line :only [with-command-line]])
  (:gen-class))

;; My Account:https://catalog.nypl.org/patroninfo/0/top
;; My Checked Out Items https://catalog.nypl.org/patroninfo/0/items

;;; utils

(defn login [user pass client]
  (let [page (.getPage client "https://catalog.nypl.org/patroninfo/0/items")
        [form] (.getForms page)
        [submit] (.getElementsByAttribute form "input" "class" "btn-submit")
        [barcode pin] (map #(.getInputByName form %) ["code" "pin"])]
    (.setValueAttribute barcode user)
    (.setValueAttribute pin pass)
    (.click submit)))

;;(defn login-robust [user pass client]
;;  (try (login user pass client)
;;       (catch ScriptException _
;;         (println "Script exception trying to login. Trying again...")
;;         (login-robust user pass (WebClient.)))))

(defn renew-all [checked-out-page]
  (let [[renew-all] (filter #(re-find #"requestRenewAll" (.getOnClickAttribute %))
                            (.getAnchors checked-out-page))
        confirm-page (.click renew-all)
        [checkout-form] (.getForms confirm-page)
        yes (.getInputByName checkout-form "renewall")]
    (.click yes)))

(defn doit [user pass]
  (let [wc (WebClient.)
        items-page (login user pass wc)
        result (renew-all items-page)]
    (.asXml result)))

(defn -main [& args]
  (with-command-line args
    "Renew all checked out items for NYPL account."
    [[barcode b "NYPL 14 digit barcode"]
     [pin p "4 digit PIN"]
     anon]
;;    (println "barcode, pin, anon:" barcode pin anon)))
    (if (some nil? [barcode pin]) (println "Need both barcode and pin!")
      (if (not (empty? anon)) (println "Didn't expect anonymous arguments:" anon)
          (do (println "got barcode and pin:" barcode "," pin)
              (doit barcode pin))))))
