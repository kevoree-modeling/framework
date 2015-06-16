
function fieldFilter(src, attName) {
    var metaAtt = src.metaClass().attribute(attName);
    //todo improve
    var id = 'el_' + Math.floor(Math.random() * 10000);
    var id_fct = id + '_fct';
    var copyObj = src;
    window[id_fct] = function(newVal){
        copyObj.set(metaAtt,newVal);
        copyObj.view().universe().model().save();
    }
    var currentVal = src.get(metaAtt);
    var ret = new nunjucks.runtime.SafeString('<input type="text" value="' + currentVal + '" id="' + id + '" onchange="javascript:window.'+id_fct+'(value);">');
    return ret;
}
