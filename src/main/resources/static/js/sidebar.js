// sidebar.js
document.addEventListener("DOMContentLoaded", function () {
    let sidebar = document.querySelector(".sidebar");
    // let toggleBtn = document.querySelector(".toggle-btn");
    let toggleIcon = document.querySelector(".toggle-icon");

    // Проверяем состояние из localStorage
    if (localStorage.getItem("sidebarCollapsed") === "true") {
        sidebar.classList.add("collapsed");
    } else {
        sidebar.classList.remove("collapsed");
    }

    toggleIcon.addEventListener("click", function () {
        sidebar.classList.toggle("collapsed");
        localStorage.setItem("sidebarCollapsed", sidebar.classList.contains("collapsed"));
    });
});

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


