$(function() {
    var startContent;
    $("#logout").click(function() {
        // IE method - document.execCommand("ClearAuthenticationCache");
        try {
            $.ajax({
                async: false,
                url:$(this).attr('href'),
                username:"logout",
                password:"logout",
                error: function(x,s,e) {
                    alert("Unable to logout, please close browser to complete logout.");
                }
            });
            window.location.href = window.location.href.replace(/admin.*/,'');
        } finally {
            return false;
        }
    });
    function linkHandler() {
        var href = $(this).attr('href');
        window.location.hash = href;
        return false;
    }
    function showImage(href) {
        $("#contentArea").html("<img src='" + href + "'/>");
    }
    function postResultsReceived() {
        var result;
        try {
            result = frames['postResults'].document.getElementsByTagName("body")[0].innerHTML;
        } catch (ex) {
            result = $('#postResults').get(0).contentDocument.getElementsByTagName("body")[0].innerHTML;
        }
        $("#formArea").html(result);
        if ($(".msgwarn").length) {
            $("#formArea").prepend($("<span>[Show All]</span>").click(function() {
                $("#formArea .msginfo").show();
            }));
            $("#formArea").prepend($("<span>[Only Warnings]</span>").click(function() {
                $("#formArea .msginfo").hide();
            }));
        }
        window.setTimeout(function() {
            // cannot remove iframe on it's own window event thread :-<
            $("#postResults").remove();
        }, 100);
    }
    function postComplete(d) {
        $("#progress").hide();
        alert(d);
        window.location.hash = "";
    }
    function addPostResults() {
        //adding iframe dynamically does not result in history event (at least in firefox)
        var pr = $("<iframe id='postResults' name='postResults'></iframe>");
        pr.appendTo("body");
        var iframe = pr.get(0);
        if (iframe.addEventListener) {
            iframe.addEventListener("load", postResultsReceived, false); // firefox
        } else if (iframe.attachEvent) {
            iframe.attachEvent("onload", postResultsReceived); // IE
        }
    }
    function submitForm() {
        var valid = validateForm();
        if (!valid) return false;
        var url = window.location.hash.substr(1);
        $("input[type=submit]").parent().append(makeLoadingElement("Submitting Form ..."));
        if ( $("#formArea input[type=file]").length ) {
            addPostResults();
            $("#formArea form").attr("action",url).attr("target","postResults");
        } else {
            var data = $("#formArea form").find('input:not([name^=__]),textarea,select,hidden').serialize();
            $.post(url, data, postComplete);
            return false; // don't let form submit itself
        }
    }
    function showHelp($el) {
        $.get(pscpHome + "/help/" + $el.attr("href"),function(d) {
            var offset = $el.offset();
            $("#helpViewer div").eq(0).html(d);
            $("#helpViewer").css({
                top : offset.top,
                left: offset.left
            }).fadeIn(500);
        });
    }
    function makeLoadingElement(what) {
        var img = pscpHome + "/images/loading.gif";
        return "<span id='progress'><img id='progress' src='" + img + "'/>" + what + "</span>";
    }
    function loadPage(href,display) {
        display = display || "";
        if (!startContent) {
            startContent = $("#startContent").remove();
        }
        $("#formArea").html(makeLoadingElement("Loading " + display + "..."));
        if (href) {
            contentLoadRequest = $.get(href,function(d) {
                clearForm();
                $("#leftNav").hide();
                var okbutton = $("#formArea").html(d).find("input[type=submit]").click(submitForm).val("OK");
                var cancelbutton = $("<input type='button' id='cancel' value='Cancel'/>");
                cancelbutton.appendTo(okbutton.closest("div")).click(
                        function() {
                            window.location.hash = "";
                        }
                    );
                registerForm();
            });
        } else {
            $("#leftNav").show();
            $("#formArea").html("");
            startContent.appendTo($("#formArea"));
        }
    }
    $("#leftNavElements a").click(linkHandler);
    $("#formArea").click(function(e) {
        var target = $(e.target);
        var handle = true;
        if (target.is('a') ) { 
            if (target.hasClass( 'helplink')) {
                showHelp(target);            
            } else {
                window.location.hash = target.attr('href');
            }
            handle = false;
        }
        return handle;
    });
    $("#helpViewer").click(function() {
       $("#helpViewer").hide();
    });
    $(window).history(function( e, ui ) {
        if (ui) {
            if (ui.value.lastIndexOf(".jpg") > 0) {
                showImage(ui.value);
            } else {
                loadPage(ui.value);
            }
        }
    });
    try {
        if (console == null) {
    // do nothing
    }
    } catch (e) {
        console = {
            log:function() {
                if (arguments.length == 0) return;
                for (var i = 0; i < arguments.length; i++) {
                    $("body").append("'" + arguments[i] + "'");
                }
                $("body").append("<br/>");
            }
        }
    }
    if (window.location.hash != "") {
        loadPage(window.location.hash.replace("#",""));
    }
    if (typeof window.pscpHome == "undefined") {
        alert("Must define pscpHome variable!");
    }
    if (typeof window.pscpAPI == "undefined") {
        alert("Must define pscpAPI variable!");
    }
});
