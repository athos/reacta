(ns parabola.reactor
  (:require [clojure.core.async :as a]
            [com.stuartsierra.component :as comp]))

(defn wrap [proc]
  (fn [message]
    (when-not (= (:type message) ::stop)
      (do (proc message)
          true))))

(defrecord Reactor [event robot ch proc]
  comp/Lifecycle
  (start [this]
    (a/sub (-> robot :channels :reactors-pub) event ch)
    (a/go-loop []
      (let [msg (a/<! ch)]
        (when ((wrap proc) msg)
          (recur))))
    this)
  (stop [this]
    (a/>!! ch {:type ::stop})
    this))
