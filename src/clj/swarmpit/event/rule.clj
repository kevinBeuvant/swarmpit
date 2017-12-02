(ns swarmpit.event.rule
  (:refer-clojure :exclude [list])
  (:require [swarmpit.api :as api]))

(defprotocol Rule
  (match? [this event])
  (subscription [this event])
  (subscribed-data [this event]))

(defn- service-container-event?
  [event]
  (let [service-id (get-in event [:Actor :Attributes :com.docker.swarm.service.id])
        supported-actions #{"start" "die"}]
    (and (= "container" (:Type event))
         (some? service-id)
         (contains? supported-actions (:Action event)))))

(def service-container-event
  (reify Rule
    (match? [_ event]
      (service-container-event? event))
    (subscription [_ event]
      {:handler :service-list})
    (subscribed-data [_ event]
      (api/services-memo))))

(def task-container-event
  (reify Rule
    (match? [_ event]
      (service-container-event? event))
    (subscription [_ event]
      {:handler :task-list})
    (subscribed-data [_ event]
      (api/tasks-memo))))

(def service-event
  (reify Rule
    (match? [_ event]
      (= "service" (:Type event)))
    (subscription [_ event]
      {:handler :service-list})
    (subscribed-data [_ event]
      (api/services))))

(def node-event
  (reify Rule
    (match? [_ event]
      (= "node" (:Type event)))
    (subscription [_ event]
      {:handler :node-list})
    (subscribed-data [_ event]
      (api/nodes))))

(def list [service-container-event
           task-container-event
           service-event
           node-event])