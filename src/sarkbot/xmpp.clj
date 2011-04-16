(ns sarkbot.xmpp)

(defonce +chat-message-type+ org.jivesoftware.smack.packet.Message$Type/chat)

(defonce +chat-filter+ (org.jivesoftware.smack.filter.MessageTypeFilter.
		    +chat-message-type+))

(defn message-to-map [message]
  "Breaks apart a message into a map of its interesting parts."
  {:to (.getTo message)
   :from (.getFrom message)
   :subject (.getSubject message)
   :body (.getBody message)})

(defn make-response [orig-message-map response-body]
  "Create a response to a message."
  (let [to ( org.jivesoftware.smack.util.StringUtils/parseBareAddress (:from orig-message-map))
	from (:to orig-message-map)
	message (org.jivesoftware.smack.packet.Message. to +chat-message-type+)]
    (.setTo message to)
    (.setFrom message from)
    (.setBody message response-body)
    message))

(defn respond [connection orig-message-map response-body]
  "Responds to a message."
  (let [response (make-response orig-message-map response-body)]
    (try
      (println "Responding to message from: " (.getTo response) " " response-body)
      (.sendPacket connection response)
      (catch Exception exception
	(println exception)))))

(defn make-packet-listener [handler]
  "Make a packet listener using the handler as the execution body."
  (proxy [org.jivesoftware.smack.PacketListener]
      []
    (processPacket [packet]
      (println "Handling message: " (message-to-map packet))
      (try
	(handler (message-to-map packet))
	(catch Exception exception
	  (println exception))))))

(defn add-message-handler [connection handler]
  "Adds a handler to the connection to process inbound messages."
  (try
    (.addPacketListener connection (make-packet-listener handler) +chat-filter+)
    (catch Exception exception
      (println exception))))

(defn make-connection-config [config]
  "Creates a configuration holder object for a connection endpoint."
  (let [host (:host config)
	port (:port config)
	domain (:domain config)]
    (org.jivesoftware.smack.ConnectionConfiguration. host port domain)))

(defn make-xmpp-connection [config]
  "Creates a XMPP connection to the endpoint described by the configuration."
  (let [connection-config (make-connection-config config)]
     (org.jivesoftware.smack.XMPPConnection. connection-config)))

(defn make-connection [config]
  "Creates an open connection the service described in the cofiguration."
  (let [connection (make-xmpp-connection config)
	username (:username config)
	password (:password config)]
    (try
      (.connect connection)
      (.login connection username password)
      connection
      (catch Exception exception
	(println exception)))))

(defn disconnect [connection]
  "Disconnects from the service."
  (.disconnect connection))

(defn stdout-handler [message]
  "Dumps the body of a message to STDOUT."
  (println (:body message)))