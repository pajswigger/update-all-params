This Burp extension provides a Session Handling Action that updates the current request with all parameters derived
from Macro responses.

By default Burp updates the current request with parameters from the final macro response only. In most cases that is
the desired behavior, and the built-in behavior handles some corner cases better than this simple extension. However,
for a few applications, this extenion will help.

After installing the extension, you must configure a Session Handling Rule with an Action to Run a Macro. In the action,
select "After running the macro, invoke a Burp extension action handler".