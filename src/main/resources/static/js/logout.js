function logout() {
    fetch("/logout", {
        method: "POST",
        headers: {
            "X-CSRF-TOKEN": document.querySelector("meta[name='_csrf']").content,
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({ "_csrf": document.querySelector("meta[name='_csrf']").content })
    })
        .then(() => window.location.href = "/") // Перенаправление после выхода
        .catch(err => console.error("Ошибка выхода:", err));
}