
function getUrl(path) {
    return "https://littlexfish.github.io/" + path
}

var xmlhttp;
if (window.XMLHttpRequest) { // code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp = new XMLHttpRequest();
}
else { // code for IE6, IE5
    xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
}
//top bar
xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
        console.log(xmlhttp.responseText)
        const topbars = document.getElementsByClassName("out_topbar");

        for(var i = 0;i < topbars.length;i++) {
            topbars.item(i).innerHTML += xmlhttp.responseText
        }
    }
}
xmlhttp.open("GET", getUrl("script/share/top_bar_zh.html"), false);
xmlhttp.send();


//side bar
xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
        const sidebar = document.getElementsByClassName("out_sidebar");

        for(var i = 0;i < sidebar.length;i++) {
            sidebar.item(i).innerHTML += xmlhttp.responseText
        }
    }
}
xmlhttp.open("GET", getUrl("script/share/sidebar.html"), true);
xmlhttp.send();
