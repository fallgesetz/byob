(ns byob.handlers
  (:require [byob.xmpp :as xmpp])
  (:require [byob.commands :as cmd])
  (:require [clojure.contrib.string :as string-util]))

(defn stdout-handler
  "Dumps the body of a message to STDOUT."
  [connection message]
  (println (:body message)))

(defn command-handler
  "Interrogates a message for an instruction from the sender and attempts to execute the instruction if it is recognized."
  [connection message]
  (if (not (nil? (:body message)))
    (let [message-body (:body message)
	  [command & args] (string-util/split #"\s+" message-body)
	  command-function (cmd/commands (symbol command))]
      (if (nil? command-function)
	(xmpp/send-chat connection message (str "I don't know how to interpret the command: " (symbol command)))
	(xmpp/send-chat connection message (str (apply command-function args)))))))