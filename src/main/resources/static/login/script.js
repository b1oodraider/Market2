document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();

    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;

    if (username === "admin" && password === "пароль123") {
        alert("Авторизация успешна!");
        document.getElementById('message').textContent = "";
    } else {
        document.getElementById('message').textContent = "Неправильное имя пользователя или пароль";
    }
});
