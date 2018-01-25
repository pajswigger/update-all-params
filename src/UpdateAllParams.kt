package burp


class SessionHandlingAction : ISessionHandlingAction {
    companion object {
        val inputRegex = Regex("<input[^>]+>", RegexOption.IGNORE_CASE)
        val nameRegex = Regex("name\\s*=\\s*\"([^\">]+)\"", RegexOption.IGNORE_CASE)
        val valueRegex = Regex("value\\s*=\\s*\"([^\">]+)\"", RegexOption.IGNORE_CASE)
    }
    override fun getActionName(): String {
        return BurpExtender.name
    }

    override fun performAction(currentRequest: IHttpRequestResponse, macroItems: Array<out IHttpRequestResponse>?) {
        val helpers = BurpExtender.cb.helpers
        if (macroItems == null) {
            return
        }
        val derivedParams = HashMap<String, String>()
        for (macroItem in macroItems) {
            val response = helpers.bytesToString(macroItem.response)
            for (input in inputRegex.findAll(response)) {
                val inputValue = input.value
                val nameMatch = nameRegex.find(inputValue)
                val valueMatch = valueRegex.find(inputValue)
                if (nameMatch != null && valueMatch != null) {
                    derivedParams.put(nameMatch.groups.get(1)!!.value, valueMatch.groups.get(1)!!.value)
                }
            }
        }

        val requestInfo = helpers.analyzeRequest(currentRequest.request)
        val params = HashMap<String, IParameter>()
        for (param in requestInfo.parameters) {
            params.put(param.name, param)
        }

        for ((name, value) in derivedParams) {
            val requestParam = params.get(name)
            if (requestParam != null) {
                val updateParam = helpers.buildParameter(name, value, requestParam.type)
                currentRequest.request = helpers.updateParameter(currentRequest.request, updateParam)
            }
        }
    }
}


class BurpExtender : IBurpExtender {
    companion object {
        const val name = "Update all parameters"
        lateinit var cb: IBurpExtenderCallbacks
    }

    override fun registerExtenderCallbacks(callbacks: IBurpExtenderCallbacks) {
        cb = callbacks
        cb.setExtensionName(name)
        cb.registerSessionHandlingAction(SessionHandlingAction())
    }
}
