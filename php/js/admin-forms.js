
function clearForm() {
    acdata = {
        opts : {},
        init : {},
        objs : {},
        vals : {}
    };
}

function validateForm() {
    var canSubmit = true;
    $("input[required=true]").each(function(i,e) {
        var val = $(e).val();
        var valued = val.toString().length > 0;
        canSubmit &= valued;
        $(e).closest("div").find("label").css('background',valued ? '' : 'yellow');
    });
    if (!canSubmit) {
        alert("Ensure all the required fields are filled in");
    }
    return canSubmit;
}
            
function registerForm() {
    $("form").submit(function () {
        $("input:text[acurl]").each(function(i,e) {
            var el = $(e);
            el.val(el.attr('acval'));
        });
    });
    $("input:text[acurl]").each(function(i,e) {
        var el = $(e);
        $.getScript(el.attr('acurl'),function(d) {
            var name = el.attr('name');
            var opts = acdata.opts[name] || {};
            opts = $.extend({
                formatItem: function(item) {
                    return item.name;
                },
                minChars: 0
            },opts);
            var data = acdata.vals[name] = eval(d);
            if (el.val()) {
                el.attr('acval',el.val());
                for (var i in data) {
                    if (data[i].id == el.val()) {
                        el.val(data[i].name);
                        break;
                    }
                }
            }
            var ac = el.autocomplete(data,opts);
            if (name in acdata.init) {
                acdata.init[name](ac);
            }
            acdata.objs[name] = ac;
        });
    });

};

