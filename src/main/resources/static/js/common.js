//COMMON
const user = JSON.parse(localStorage.getItem("user"));
const nav = document.getElementById("main-nav");
const links = Array.from(nav.querySelectorAll("a"));

// Define restricted links by role
const restrictedForNone = ["/html/register.html", "/html/list.html", "/html/email.html", "/html/admin.html", "/html/memberDetails.html"];
const restrictedForUser = ["/html/register.html", "/html/list.html", "/html/email.html", "/html/admin.html", "/html/signup.html", "/html/signin.html"];
const restrictedForAdmin = ["/html/signup.html", "/html/signin.html"];

// Hide links based on role
if (user === null) {
    links.forEach(link => {
        if (restrictedForNone.some(r => link.href.includes(r))) {
            link.style.display = "none";
        }
    });
} else if (user?.roles[0].name === 'USER') {
    links.forEach(link => {
        if (restrictedForUser.some(r => link.href.includes(r))) {
            link.style.display = "none";
        }
    });
} else if (user?.roles[0].name === 'ADMIN') {
    links.forEach(link => {
        if (restrictedForAdmin.some(r => link.href.includes(r))) {
            link.style.display = "none";
        }
    });
}
if (user?.roles?.[0]?.name === 'USER' || user?.roles?.[0]?.name === 'ADMIN') {
    document.getElementById("logout-link").style.display = "inline";
} else {
    document.getElementById("logout-link").style.display = "none";
}

if (user?.roles?.[0]?.name === 'ADMIN') {
    document.getElementById("refresh-record").style.display = "inline";
} else {
    document.getElementById("refresh-record").style.display = "none";
}

// Add separator (|) between visible links
const visibleLinks = links.filter(link => link.style.display !== "none");
nav.innerHTML = ''; // clear nav

visibleLinks.forEach((link, index) => {
    nav.appendChild(link);
    if (index < visibleLinks.length - 1) {
        const separator = document.createTextNode(" | ");
        nav.appendChild(separator);
    }
});

function logout() {
    fetch("/api/v1/members/logout", {
        method: "POST",
        credentials: "include"
    }).then(() => {
        localStorage.removeItem("user"); // Clear user info from localStorage
        window.location.href = "/index.html"; // Redirect to home
    }).catch(err => console.error("Logout failed:", err));
}

function refreshRecord(){
    fetch("/api/v1/members/refresh-record", {
        method: "GET",
        credentials: "include"
    }).then(() => {
        window.location.href = "/index.html"; // Redirect to home
    }).catch(err => console.error("Refresh record failed:", err));
}

// Inject footer
fetch("/html/footer.html")
    .then(res => res.text())
    .then(data => document.getElementById("footer").innerHTML = data);

//Error Modal
function showErrorModal(message) {
    const modal = document.getElementById("errorModal");
    const messageBox = document.getElementById("errorMessage");
    messageBox.textContent = message;
    modal.style.display = "block";
}

function closeErrorModal() {
    document.getElementById("errorModal").style.display = "none";
}

// Optional: Close modal on outside click
window.onclick = function(event) {
    const modal = document.getElementById("errorModal");
    if (event.target === modal) {
        modal.style.display = "none";
    }
};

function setupMakeAdminForm() {
    const form = document.getElementById("makeAdminForm");
    if (!form) return; // Skip if not on the admin page

    form.addEventListener("submit", function(e) {
        e.preventDefault();

        const user = {
            memberId: document.getElementById("memberId").value,
            username: document.getElementById("username").value
        };

        fetch("/api/v1/members/admin", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(user)
        })
            .then(res => {
            if (res.ok) {
                window.location.href = "/index.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}


//changePassword.html
function setupChangePasswordForm() {
    document.getElementById("changePasswordForm").addEventListener("submit", function(e) {
        e.preventDefault();
        const user = {
            memberId: document.getElementById("memberId").value,
            username: document.getElementById("username").value,
            password: document.getElementById("password").value,
            email: document.getElementById("email").value
        };

        fetch("/api/v1/members/change-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(user)
        })
            .then(res => {
            if (res.ok) {
                window.location.href = "/html/signin.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}
//email.html
function setupNotificationForm() {
    document.getElementById("notificationForm").addEventListener("submit", function(e) {
        e.preventDefault();
        const email = {
            subject: document.getElementById("subject").value,
            body: document.getElementById("body").value
        };

        fetch("/api/v1/members/email/send", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(email)
        })
            .then(res => {
            if (res.ok) {
                window.location.href = "/index.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}

//register.html
function setupRegistrationForm(){
    document.getElementById("registerForm").addEventListener("submit", function(e) {
        e.preventDefault();

        const rawDate = document.getElementById("joinDate").value;
        let formattedDate = null;
        if(rawDate){
            const [year, month, day] = rawDate.split("-");
            formattedDate = `${month}/${day}/${year}`;
        }

        const member = {
            fullName: document.getElementById("fullName").value,
            maritalStatus: document.getElementById("maritalStatus").value,
            joinDate: formattedDate,
            email: document.getElementById("email").value
        };

        fetch("/api/v1/members/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(member)
        })
            .then(res => {
            if (res.ok) {
                window.location.href = "/html/list.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Registration failed. Please check your input.");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}
//signin.html
function setupSigninForm() {
    document.getElementById("signinForm").addEventListener("submit", function(e) {
        e.preventDefault();
        const user = {
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        };

        fetch("/api/v1/members/signin", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(user)
        })
            .then(res => {
            if (!res.ok) {
                return res.text().then(() => {
                    throw new Error("Invalid username or password.");
                });
            }
            return res.json();
        })
            .then(data => {
            // Save basic user info
            localStorage.setItem("user", JSON.stringify(data));

            // Fetch full member details using memberId or username
            return fetch(`/api/v1/members/retrieve/${data.memberId}`, {
                method: "GET",
                credentials: "include"
            });
        })
            .then(res => {
            if (!res.ok) {
                throw new Error("Failed to retrieve member details.");
            }
            return res.json();
        })
            .then(member => {
            // Save full member info
            localStorage.setItem("member", JSON.stringify(member));
            window.location.href = "/index.html";
        })
            .catch(error => {
            console.error(error);
            showErrorModal(error.message || "An unexpected error occurred.");
        });
    });
}
//signup.html
function setupSignupForm(){
    document.getElementById("signupForm").addEventListener("submit", function(e) {
        e.preventDefault();

        const user = {
            memberId: document.getElementById("memberId").value,
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        };

        fetch("/api/v1/members/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(user)
        })
            .then(res => {
            if (res.ok) {
                window.location.href = "/html/signin.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}



