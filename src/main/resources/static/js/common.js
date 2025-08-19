//Timeout
let logoutTimer;
const INACTIVITY_LIMIT = 5 * 60 * 1000; // 5 min

function resetTimer() {
    clearTimeout(logoutTimer);
    logoutTimer = setTimeout(() => {
        localStorage.removeItem("user");
        showErrorModal("You have been logged out due to inactivity.");
        window.location.href = "/html/signin.html";
    }, INACTIVITY_LIMIT);
}

// Reset on activity
["click", "mousemove", "keydown", "scroll"].forEach(evt =>
window.addEventListener(evt, resetTimer)
);

resetTimer(); // start timer


//COMMON
const user = JSON.parse(localStorage.getItem("user"));
const member = JSON.parse(localStorage.getItem("member"));

// Define role-based restrictions
const restrictedForNone = [
    "/html/register.html", "/html/list.html", "/html/email.html",
    "/html/admin.html", "/html/memberDetails.html", "/html/spouse.html",
    "/html/beneficiary.html", "/html/spouseview.html", "/html/beneficiarylist.html"
];
const restrictedForUser = [
    "/html/register.html", "/html/list.html", "/html/email.html",
    "/html/admin.html", "/html/signup.html", "/html/signin.html"
];
const restrictedForAdmin = [
    "/html/signup.html", "/html/signin.html"
];

const restrictedForSuperAdmin = [
    "/html/signup.html", "/html/signin.html", "/html/memberDetails.html",
    "/html/spouse.html", "/html/beneficiary.html", "/html/spouseview.html", "/html/beneficiarylist.html"
];

// Get role safely
const role = user?.roles?.[0]?.name?.toUpperCase() || null;

// Choose restriction list
function getRestrictedLinks(role) {
    if (role === "SUPER_ADMIN") return restrictedForSuperAdmin;
    if (role === "ADMIN") return restrictedForAdmin;
    if (role === "USER") return restrictedForUser;
    return restrictedForNone;
}
const restrictedList = getRestrictedLinks(role);

// Hide restricted links in the navbar
const navLinks = document.querySelectorAll(".navbar-nav a");

navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (!href) return;

    const path = new URL(href, window.location.origin).pathname;

    if (restrictedList.includes(path)) {
        const li = link.closest("li.nav-item");
        if (li) li.style.display = "none";
        else link.style.display = "none";
    }
});

// Show/Hide Logout and Refresh Record links
const logoutLink = document.getElementById("logout-link");
const refreshLink = document.getElementById("refresh-record");

if (logoutLink) {
    logoutLink.classList.toggle("d-none", !(role === "USER" || role === "ADMIN" || role === "SUPER_ADMIN"));
}

if (refreshLink) {
    refreshLink.classList.toggle("d-none", !(role === "ADMIN" || role === "SUPER_ADMIN"));
}

function logout() {
    fetch("/api/v1/members/public/logout", {
        method: "POST",
        credentials: "include"
    }).then(() => {
        localStorage.clear(); // Clear  localStorage
        window.location.href = "/index.html"; // Redirect to home
    }).catch(err => console.error("Logout failed:", err));
}

function refreshRecord(){
    fetch("/api/v1/members/admin/refresh-record", {
        method: "GET",
        credentials: "include"
    }).then(() => {
        window.location.href = "/html/list.html";
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

        fetch("/api/v1/members/admin/update-user", {
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

        fetch("/api/v1/members/public/change-password", {
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

        fetch("/api/v1/members/admin/email/send", {
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

        fetch("/api/v1/members/admin/register", {
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

        fetch("/api/v1/members/public/signin", {
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
         .then(data=>{
            const userjson = JSON.stringify(data);
            const user = JSON.parse(userjson);
            const role = user?.roles?.[0]?.name?.toUpperCase() || null;
            if(role==='SUPER_ADMIN'){
                window.location.href = "/index.html";
            }
            return data;
        })
        .then(data => {
            // Save basic user info
            localStorage.setItem("user", JSON.stringify(data));
            // Fetch full member details using memberId or username
            return fetch(`/api/v1/members/user/retrieve/${data.memberId}`, {
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
        .then(member => {
            fetch(`/api/v1/members/user/retrieve-spouse/${member.memberId}`, {
                method: "GET",
                credentials: "include"
            })
                .then(response => {
                if (!response.ok) throw new Error("Failed to fetch spouse.");
                return response.json();
            })
                .then(data => {
                localStorage.setItem("spouse", JSON.stringify(data));
            })
                .catch(error => {
                showErrorModal("Could not load spouse.");
            });
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

        fetch("/api/v1/members/public/signup", {
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

//beneficiary.html
let beneficiaryIndex = 0;
function setupBeneficiaryForm(){
    document.getElementById('beneficiaryForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const userString = localStorage.getItem("user");
        const user = JSON.parse(userString);
        const grantorId = user.memberId;
        const beneficiaryDivs = document.querySelectorAll('.beneficiary-entry');

        const beneficiaries = Array.from(beneficiaryDivs).map((div, index) => {
            return {
                fullName: div.querySelector(`#fullName_${index}`)?.value || '',
                maritalStatus: div.querySelector(`#maritalStatus_${index}`)?.value || '',
                email: div.querySelector(`#email_${index}`)?.value || ''
            };
        });

        const payload = {
            grantorId: grantorId,
            beneficiaries: beneficiaries
        };

        fetch("/api/v1/members/user/add-beneficiaries", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        })
            .then(res => {
            if (res.ok) {
                fetch(`/api/v1/members/user/retrieve-beneficiaries/${grantorId}`, {
                    method: "GET",
                    credentials: "include"
                })
                .then(response => {
                    if (!response.ok) throw new Error("Failed to fetch beneficiaries.");
                    return response.json();
                })
                .then(data => {
                    localStorage.setItem("beneficiaries", JSON.stringify(data));
                    window.location.href = "/html/beneficiarylist.html";
                })
                .catch(error => {
                    console.error(error);
                    showErrorModal("Could not load beneficiaries.");
                });
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
        .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });

    // Add initial beneficiary on page load
    addBeneficiary(beneficiaryIndex);
}

function addBeneficiary() {
    const container = document.getElementById('beneficiariesContainer');
    const div = document.createElement('div');
    div.className = 'beneficiary-entry';
    div.innerHTML = `
      <div class="form-group">
        <h4> Beneficiary ${beneficiaryIndex+1}</h4>
        <label for="fullName_${beneficiaryIndex}">Full Name:</label>
        <input type="text" id="fullName_${beneficiaryIndex}" name="beneficiaries[${beneficiaryIndex}].fullName" required>
        <small>Enter first and last name.</small>
      </div>
      <div class="form-group">
        <label for="maritalStatus_${beneficiaryIndex}">Marital Status:</label>
        <select id="maritalStatus_${beneficiaryIndex}" name="beneficiaries[${beneficiaryIndex}].maritalStatus" required>
          <option value="">-- Select --</option>
          <option value="Single">Single</option>
          <option value="Married">Married</option>
        </select>
      </div>
      <div class="form-group">
        <label for="email_${beneficiaryIndex}">Email Address:</label>
        <input type="email" id="email_${beneficiaryIndex}" name="beneficiaries[${beneficiaryIndex}].email">
        <small>Must be a valid email.</small>
      </div>
      ${beneficiaryIndex === 0 ? '':`<button type="button" class="remove-btn" onclick="removeBeneficiary(this)">Remove</button>`}
    `;
    container.appendChild(div);
    beneficiaryIndex++;
}
function removeBeneficiary(button) {
    beneficiaryIndex--;
    const entry = button.closest('.beneficiary-entry');
    entry.remove();
}

// beneficiarylist.html
function renderBeneficiaries() {
    const stored = localStorage.getItem("beneficiaries");
    const container = document.getElementById("beneficiariesList");
    container.innerHTML = ""; // clear previous

    if (!stored) {
        container.innerHTML = "<p>No beneficiaries found.</p>";
        return;
    }

    let beneficiaries;
    try {
        beneficiaries = JSON.parse(stored); //Convert JSON string to JS array
    } catch (e) {
        container.innerHTML = "<p>Error parsing saved beneficiaries.</p>";
        return;
    }

    if (!Array.isArray(beneficiaries) || beneficiaries.length === 0) {
        container.innerHTML = "<p>No beneficiaries found.</p>";
        return;
    }

    beneficiaries.forEach((b, index) => {
        const div = document.createElement("div");
        div.className = "beneficiary-card";
        div.innerHTML = `
              <h4>Beneficiary ${index + 1}</h4>
              <p><strong>Beneficiary ID:</strong> ${b.beneficiaryId}</p>
              <p><strong>Full Name:</strong> ${b.fullName}</p>
              <p><strong>Marital Status:</strong> ${b.maritalStatus}</p>
              <p><strong>Email:</strong> ${b.email || "N/A"}</p>
            `;
        container.appendChild(div);
    });
}
function setupSpouseForm(){
    document.getElementById('spouseForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const userString = localStorage.getItem("user");
        const user = JSON.parse(userString);
        const grantorId = user.memberId;

        const payload = {
            grantorId: grantorId,
            fullName: document.getElementById('fullName').value,
            email: document.getElementById('email').value,
            maritalStatus: document.getElementById('maritalStatus').value
        };

        fetch("/api/v1/members/user/add-spouse", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (res.ok) {
                fetch(`/api/v1/members/user/retrieve-spouse/${grantorId}`, {
                    method: "GET",
                    credentials: "include"
                })
                .then(response => {
                    if (!response.ok) throw new Error("Failed to fetch spouse.");
                    return response.json();
                })
                .then(data => {
                    localStorage.setItem("spouse", JSON.stringify(data));
                    window.location.href = "/html/spouseview.html";
                })
                .catch(error => {
                    showErrorModal("Could not load spouse.");
                });
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
       .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}
function renderSpouse() {
    const stored = localStorage.getItem("spouse");
    const container = document.getElementById("spouse");
    container.innerHTML = ""; // clear previous

    if (!stored) {
        container.innerHTML = "<p>No spouse found.</p>";
        return;
    }

    let spouse;
    try {
        spouse = JSON.parse(stored); //Convert JSON string to JS Object
    } catch (e) {
        container.innerHTML = "<p>Error parsing saved spouse.</p>";
        return;
    }

    if (!spouse.spouseId) {
        container.innerHTML = "<p>No spouse found.</p>";
        return;
    }

    document.getElementById("spouse").innerHTML = `<p><strong>Spouse ID:</strong> ${spouse.spouseId}</p>
              <p><strong>Full Name:</strong> ${spouse.fullName}</p>
              <p><strong>Marital Status:</strong> ${spouse.maritalStatus}</p>
              <p><strong>Email:</strong> ${spouse.email || "N/A"}</p>
            `;

}





