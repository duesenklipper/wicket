// anonymous function to scope the contained variables. JavaScript is weird.
(function() {
    var inputNode = wicketGet('${targetId}');
    Wicket.Event.add(inputNode, 'click', function() {
            // more function scoping...
            (function() {
                // executed on each click:
                ${onClick}
            })();
            if (inputNode.checked) {
                // only for onCheck
                (function() {
                    ${onCheck}
                })();
            }
            if (!(inputNode.checked)) {
                // only for onUncheck
                (function() {
                    ${onUncheck}
                })();
            }
        });
})();
