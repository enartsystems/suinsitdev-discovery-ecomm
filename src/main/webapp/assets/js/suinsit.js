// Función para leer el valor de una cookie por su nombre
function getCookie(name) {
    var nameEQ = name + "=";
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        while (cookie.charAt(0) == ' ') {
            cookie = cookie.substring(1, cookie.length);
        }
        if (cookie.indexOf(nameEQ) == 0) {
            return cookie.substring(nameEQ.length, cookie.length);
        }
    }
    return null; // Retorna null si la cookie no se encuentra
}

// Función para cambiar el icono basado en el valor de la cookie "compactScreen"
function cambiarIconoSegunCookie() {
    // Leer el valor de la cookie "compactScreen"
    var cookieValue = getCookie("compactScreen");
	
    // Obtener una referencia al elemento al que deseas aplicar el icono
    var btnIcon = document.getElementById("btnIcon"); // Icono del compactScreen
	var tooltip = document.getElementById("tooltipCompactScreen");
    // Verificar si el valor de la cookie termina en "_c"
    if (cookieValue && cookieValue.endsWith("_c")) {
        // Cambiar la clase del icono
        btnIcon.className = 'bx bx-expand-horizontal fs-22';
        tooltip.title = "Expandir contenido de página";
    } else {
        // Cambiar la clase del icono a otra clase si no termina en "_c"
        btnIcon.className = 'bx bx-collapse-horizontal fs-22';
        tooltip.title = "Contraer contenido de página";

    }
}

// Llamar a la función para cambiar el icono cuando se cargue la página
cambiarIconoSegunCookie();