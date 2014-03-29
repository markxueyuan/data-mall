(ns Miscellaneous.agent-error)

;the latent error mode is :fail

(def agent-99 (agent 0))

(send agent-99 #(/ 100 %))

(agent-error agent-99)

(restart-agent agent-99 0)


;change the error mode to :continue, which ignores the exception silently
(def agent-100 (agent 0 :error-mode :continue))

(send agent-100 #(/ 100 %))

(agent-error agent-100)

@agent-100

;error handler is just a function called when the exception is thrown

(def agent-101 (agent 0 :error-handler #(prn %2)));why %2? %1 is the agent

(def agent-101 (agent 0 :error-handler #(prn %1 %2)))

(send agent-101 #(/ 100 %))

(agent-error agent-101)

@agent-101

