
function toggle(id) {
    var el = document.getElementById('content-' + id);
    if (el.style.display == 'none') {
        el.style.display = '';
    } else {
        el.style.display = 'none';
    }
}
