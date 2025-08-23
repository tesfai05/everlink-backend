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

document.addEventListener("DOMContentLoaded", function () {
    // Inject footer
    fetch("/html/footer.html")
        .then(res => res.text())
        .then(data => document.getElementById("footer").innerHTML = data);

    // Inject nav-bar
    loadNavbar();
});
function loadNavbar() {
    fetch("/html/nav-bar.html")
        .then(res => res.text())
        .then(data => {
            document.getElementById("nav-bar").innerHTML = data;
            // Get role safely
            const role = user?.roles?.[0]?.name?.toUpperCase() || null;
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
        })
        .catch(err => console.error("Failed to load navbar:", err));
}

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


// Choose restriction list
function getRestrictedLinks(role) {
    if (role === "SUPER_ADMIN") return restrictedForSuperAdmin;
    if (role === "ADMIN") return restrictedForAdmin;
    if (role === "USER") return restrictedForUser;
    return restrictedForNone;
}


function logout() {
    fetch("/api/v1/public/logout", {
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

        fetch("/api/v1/users/admin/update", {
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

        fetch("/api/v1/public/change-password", {
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

        fetch("/api/v1/members/admin/send-email", {
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

        fetch("/api/v1/public/signin", {
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
            if(member.maritalStatus==='Single'){
                window.location.href = "/index.html";
            }
            return member;
        })
        .then(member => {
            fetch(`/api/v1/spouse/user/retrieve/${member.memberId}`, {
                method: "GET",
                credentials: "include"
            })
            .then(response => {
                if (!response.ok) throw new Error("Failed to fetch spouse.");
                return response.json();
            })
            .then(data => {
                localStorage.setItem("spouse", JSON.stringify(data));
                window.location.href = "/index.html";
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

        fetch("/api/v1/public/signup", {
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
        fetch("/api/v1/beneficiaries/user/add", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (res.ok) {
                fetch(`/api/v1/beneficiaries/user/retrieve-member/${grantorId}`, {
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

function retrieveBeneficiaries(grantorId){
    fetch(`/api/v1/beneficiaries/user/retrieve-member/${grantorId}`)
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
}

function addBeneficiary() {
    const container = document.getElementById('beneficiariesContainer');
    const div = document.createElement('div');
    div.className = 'beneficiary-entry';
    div.innerHTML = `
      <div class="form-group">
        <h4 style="text-align:center; color:#5694d7; margin-bottom:10px;"> Beneficiary ${beneficiaryIndex+1}</h4>
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
      ${beneficiaryIndex === 0 ? '':`<button type="button" class="btn btn-outline-danger" onclick="removeBeneficiary(this)">Remove</button>`}
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
              <h4 style="text-align:center; color:#5694d7; margin-bottom:10px;">Beneficiary ${index + 1}</h4>
              <p><strong>Beneficiary ID:</strong> ${b.beneficiaryId}</p>
              <p><strong>Full Name:</strong> ${b.fullName}</p>
              <p><strong>Marital Status:</strong> ${b.maritalStatus}</p>
              <p><strong>Email:</strong> ${b.email || "N/A"}</p>
              <button id="edit-btn" class="btn btn-outline-success" onclick="editBeneficiary('${b.beneficiaryId}')">Edit</button>
              <button id="remove-btn" class="btn btn-outline-danger" onclick="deleteBeneficiary('${b.beneficiaryId}')">Remove</button>
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

        fetch("/api/v1/spouse/user/add", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (res.ok) {
                fetch(`/api/v1/spouse/user/retrieve/${grantorId}`, {
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
                    const e = JSON.parse(errorMessage);
                    throw new Error(e.message || "Unknown error occurred");
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
    const role = user?.roles?.[0]?.name?.toUpperCase() || null;
    document.getElementById("spouse").innerHTML = `<p><strong>Spouse ID:</strong> ${spouse.spouseId}</p>
              <p><strong>Full Name:</strong> ${spouse.fullName}</p>
              <p><strong>Marital Status:</strong> ${spouse.maritalStatus}</p>
              <p><strong>Email:</strong> ${spouse.email || "N/A"}</p>
              <button id="edit-btn" class="btn btn-outline-success" onclick="window.location.href = '/html/spouseedit.html'">Edit</button>
               ${role==='USER' ? '':`<button type="button" class="btn btn-outline-danger" onclick="deleteSpouse()">Delete</button>`}
            `;

}

function editSpouseDetails(){
    const stored = localStorage.getItem("spouse");
    let spouse;
    if (stored) {
        try {
            spouse = JSON.parse(stored);
            if (spouse && spouse.spouseId) {
                document.getElementById("fullName").value = spouse.fullName || "";
                document.getElementById("maritalStatus").value = spouse.maritalStatus || "";
                document.getElementById("email").value = spouse.email || "";
            }
        } catch (e) {
            console.error("Error parsing saved spouse:", e);
        }
    }

    // Handle form submission for update
    const form = document.getElementById("editSpouseForm");
    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const updated = {
                fullName: document.getElementById("fullName").value,
                maritalStatus: document.getElementById("maritalStatus").value,
                email: document.getElementById("email").value
            };
            try {
                const res = await fetch(`/api/v1/spouse/user/update/${spouse.spouseId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(updated)
                });
                if (res.ok) {
                    localStorage.setItem("spouse", JSON.stringify({ ...spouse, ...updated }));
                    window.location.href = "/html/spouseview.html";
                } else {
                    document.getElementById("messageBox").textContent = "Failed to update spouse.";
                }
            } catch (err) {
                console.error("Update error:", err);
            }
        });
    }
}

function deleteSpouse(){
    const stored = localStorage.getItem("spouse");
    let spouse;
    if (stored) {
        try {
            spouse = JSON.parse(stored);
        }catch (e) {
            console.error("Error parsing saved spouse:", e);
        }
    }
    showConfirmation("Are you sure you want to delete this spouse?", () => {
        fetch(`/api/v1/spouse/admin/delete/${spouse.spouseId}`, {
            method: 'DELETE',
            credentials: "include"
        })
        .then(res => {
            if (res.ok) {
                localStorage.removeItem("spouse");
                window.location.href = "/html/spouseview.html";
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
       .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}

function showConfirmation(message, onConfirm) {
    document.getElementById("confirmMessage").textContent = message;
    const confirmModal = document.getElementById("confirmModal");
    confirmModal.style.display = "block";

    const confirmYesBtn = document.getElementById("confirmYes");

    // Remove previous event listeners
    const newBtn = confirmYesBtn.cloneNode(true);
    confirmYesBtn.parentNode.replaceChild(newBtn, confirmYesBtn);

    newBtn.onclick = () => {
        closeModal();
        onConfirm();
    };
}

function closeModal() {
    document.getElementById("confirmModal").style.display = "none";
}

// Populate edit form with localStorage beneficiary data
function editBeneficiary(id){
    try {
        fetch(`/api/v1/beneficiaries/user/retrieve/${id}`, {
            method: "GET",
            credentials: "include",
        })
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch beneficiary.");
            return response.json();
        })
        .then(data => {
            localStorage.setItem("beneficiary", JSON.stringify(data));
            window.location.href = "/html/beneficiaryview.html";
        })
        .catch(error => {
            console.error(error);
            showErrorModal("Could not load beneficiary.");
        });
    } catch (err) {
        showErrorModal("Edit error:"+err);
    }
}

function deleteBeneficiary(id){
    const beneficiaries = JSON.parse(localStorage.getItem("beneficiaries"));
    showConfirmation("Are you sure you want to delete this beneficiary?", () => {
        fetch(`/api/v1/beneficiaries/user/delete/${id}`, {
            method: 'DELETE',
            credentials: "include"
        })
        .then(res => {
            if (res.ok) {
                const updated = beneficiaries.filter(b => b.beneficiaryId !== id);
                localStorage.setItem("beneficiaries", JSON.stringify(updated));
                renderBeneficiaries();
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
         .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}


function renderBeneficiary(){
    const stored = localStorage.getItem("beneficiary");
    if (stored) {
        try {
            const beneficiary = JSON.parse(stored);
            if (beneficiary && beneficiary.beneficiaryId) {
                document.getElementById("fullName").value = beneficiary.fullName || "";
                document.getElementById("maritalStatus").value = beneficiary.maritalStatus || "";
                document.getElementById("email").value = beneficiary.email || "";
            }
        } catch (e) {
            console.error("Error parsing saved beneficiary:", e);
        }
    }

    // Handle form submission for update
    const form = document.getElementById("beneficiaryviewForm");
    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            const updated = {
                fullName: document.getElementById("fullName").value,
                maritalStatus: document.getElementById("maritalStatus").value,
                email: document.getElementById("email").value
            };

            const stored = localStorage.getItem("beneficiary");
            if (!stored) return;

            const beneficiary = JSON.parse(stored);

            try {
                const res = await fetch(`/api/v1/beneficiaries/user/update/${beneficiary.beneficiaryId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(updated)
                });
                if (res.ok) {
                    localStorage.setItem("beneficiary", JSON.stringify({ ...beneficiary, ...updated }));
                    retrieveBeneficiaries(beneficiary.grantorId);
                } else {
                    document.getElementById("messageBox").textContent = "Failed to update beneficiary.";
                }
            } catch (err) {
                console.error("Update error:", err);
            }
        });
    }
}

function setupEditProfileForm(){
    const memberData = localStorage.getItem("member");
    let member ;
    if (memberData) {
        member = JSON.parse(memberData);
        document.getElementById("fullName").value = member.fullName;
        document.getElementById("email").value = member.email;
    }

    document.getElementById("profileEditForm").addEventListener("submit", function (e) {
        e.preventDefault();
        const memberId = member.memberId;
        const updatedMember = {
            memberId: memberId,
            fullName: document.getElementById("fullName").value,
            maritalStatus: member.maritalStatus,
            email: document.getElementById("email").value,
            joinDate: member.joinDate,
            leaveDate: member.leaveDate,
            statusChangeDate: member.statusChangeDate
        };

        fetch(`/api/v1/members/user/update/${memberId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(updatedMember)
        })
        .then(res => {
            const messageBox = document.getElementById("messageBox");
            if (res.ok) {
                localStorage.setItem("member", JSON.stringify({ ...member, ...updatedMember }));
                window.location.href = "/html/memberDetails.html";
            } else {
                messageBox.textContent = "Update failed. Please try again.";
                messageBox.className = "message error";
            }
        })
        .catch(() => {
            const messageBox = document.getElementById("messageBox");
            messageBox.textContent = "Error occurred during update.";
            messageBox.className = "message error";
        });
    });
}

function setupIndexPage(){
    const user1 = localStorage.getItem("user");
    if(!user1){
        document.getElementById("signup-btn").innerHTML = `<button onclick="window.location.href='/html/signup.html'">Signup</button>`;
    }
}

function setupMemberUpdateForm(){
    const memberData = localStorage.getItem("editMember");
    let memberId = null;

    function toInputDateFormat(mmddyyyy) {
        if (!mmddyyyy || !mmddyyyy.includes("/")) return "";
        const [mm, dd, yyyy] = mmddyyyy.split("/");
        return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
    }

    if (memberData) {
        const member = JSON.parse(memberData);
        memberId = member.memberId;

        document.getElementById("fullName").value = member.fullName || "";
        document.getElementById("maritalStatus").value = member.maritalStatus || "";
        document.getElementById("email").value = member.email || "";

        document.getElementById("joinDate").value = toInputDateFormat(member.joinDate);
        document.getElementById("leaveDate").value = toInputDateFormat(member.leaveDate);
        document.getElementById("statusChangeDate").value = toInputDateFormat(member.statusChangeDate);
    }

    document.getElementById("memberForm").addEventListener("submit", function (e) {
        e.preventDefault();
        const rawJoinDate = document.getElementById("joinDate").value;
        const rawLeaveDate = document.getElementById("leaveDate").value;
        const rawScDate = document.getElementById("statusChangeDate").value;

        const formatDate = (dateStr) => {
            if (!dateStr) return null;
            const [year, month, day] = dateStr.split("-");
            return `${month}/${day}/${year}`;
        };

        const updatedMember = {
            memberId: memberId,
            fullName: document.getElementById("fullName").value,
            maritalStatus: document.getElementById("maritalStatus").value,
            email: document.getElementById("email").value,
            joinDate: formatDate(rawJoinDate),
            leaveDate: formatDate(rawLeaveDate),
            statusChangeDate: formatDate(rawScDate)
        };

        fetch(`/api/v1/members/user/update/${memberId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(updatedMember)
        })
            .then(res => {
            const messageBox = document.getElementById("messageBox");
            if (res.ok) {
                window.location.href = "/html/list.html";
                localStorage.removeItem("editMember");
            } else {
                messageBox.textContent = "Update failed. Please try again.";
                messageBox.className = "message error";
            }
        })
            .catch(() => {
            const messageBox = document.getElementById("messageBox");
            messageBox.textContent = "Error occurred during update.";
            messageBox.className = "message error";
        });
    });
}

function setupProfilePreFill(){
    const memberData = localStorage.getItem("member");

    function toInputDateFormat(mmddyyyy) {
        if (!mmddyyyy || !mmddyyyy.includes("/")) return "";
        const [mm, dd, yyyy] = mmddyyyy.split("/");
        return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
    }

    if (memberData) {
        const member = JSON.parse(memberData);
        document.getElementById("memberName").textContent = member.fullName;
        document.getElementById("fullName").textContent = member.fullName;
        document.getElementById("maritalStatus").textContent = member.maritalStatus;
        document.getElementById("email").textContent = member.email;
        document.getElementById("joinDate").textContent = toInputDateFormat(member.joinDate);
        document.getElementById("membershipStatus").textContent = member.membershipStatus;
        document.getElementById("statusChangeDate").textContent = toInputDateFormat(member.statusChangeDate) || "N/A";
        document.getElementById("currentMonthlyContribution").textContent = "$"+member.currentMonthlyContribution;
        let pmc = member.previousMonthlyContribution;
        if(pmc){
            pmc = "$"+member.previousMonthlyContribution;
        }
        document.getElementById("previousMonthlyContribution").textContent = pmc || "N/A";
        let tplp = member.totalPreviousLegacyPool;
        if(tplp){
            tplp = "$"+member.totalPreviousLegacyPool;
        }
        document.getElementById("totalPreviousLegacyPool").textContent = tplp || "0.0";
        let tc = member.totalContribution;
        if(tc){
            tc = "$"+member.totalContribution;
        }
        document.getElementById("totalContribution").textContent = tc || "0.0";
        let per = member.percentageOfOwnership;
        if(per){
            per = member.percentageOfOwnership+"%";
        }
        document.getElementById("percentageOfOwnership").textContent = per || "0.0";

        const memberName = document.getElementById("memberName").textContent.trim();
        if (!memberName) {
            document.getElementById("memberDetailsSection").style.display = "none";
            document.getElementById("superAdminDetails").textContent = "This is a super admin role, no individual details to display here.";
        }
        document.getElementById("btn-edit").innerHTML = `<a class="btn btn-outline-success" href="/html/profile.html">Edit</a>`;
        if(member.maritalStatus==='Single'){
            document.getElementById("spouse-div").style.display = "none";
        }
    }
}

function loadBeneficiaries(grantorId) {
    if(!grantorId){
        grantorId = member.memberId;
    }
    if (!grantorId) {
        showErrorModal("Please enter a grantor ID.");
        return;
    }

    fetch(`/api/v1/beneficiaries/user/retrieve-member/${grantorId}`)
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
}

function loadSpouse(grantorId) {
    if(!grantorId){
        grantorId = member.memberId;
    }
    if (!grantorId) {
        showErrorModal("Please enter a grantor ID.");
        return;
    }

    fetch(`/api/v1/spouse/user/retrieve/${grantorId}`)
        .then(response => {
        if (!response.ok) throw new Error("Failed to fetch spouse.");
        return response.json();
    })
        .then(data => {
        localStorage.setItem("spouse", JSON.stringify(data));
        window.location.href = "/html/spouseview.html";
    })
        .catch(error => {
        console.error(error);
        showErrorModal("Could not load spouse.");
    });
}

function diplayMembers(){
    fetch("/api/v1/members/admin",{
        method: "GET",
        credentials: "include"
    })
        .then(res =>res.json())
        .then(data => {
        const tbody = document.querySelector("#memberTable tbody");
        data.forEach(member => {
            const row = `<tr>
                            <td>
                                <a href="#" class="member-link" data-id="${member.memberId}">${member.memberId}<span class="arrow">&#9660;</span> </a>
                                <div class="dropdown tbl-dd" id="dropdown-${member.memberId}" style="display: none; margin-top: 5px;">
                                  ${member.maritalStatus === 'Married' ? `<a href="#" onclick="loadSpouse('${member.memberId}')">Spouse</a><br/>` : ''}
                                  <a href="#" onclick="loadBeneficiaries('${member.memberId}')">Beneficiaries</a></br>
                                  <a href="#" onclick="editMember('${member.memberId}')">✏️ Edit</a></br>
                                  <a href="#" onclick="deleteMember('${member.memberId}')">🗑️ Delete</a>
                                </div>
                            </td>
                            <td>${member.fullName}</td>
                            <td>${member.maritalStatus}</td>
                            <td>${member.email || "-"}</td>
                            <td>${member.joinDate}</td>
                            <td>${member.leaveDate || "-"}</td>
                            <td>${member.membershipStatus}</td>
                            <td>${member.totalContribution}</td>
                            <td>${member.totalPreviousLegacyPool || "0.0"}</td>
                            <td>${member.percentageOfOwnership}</td>
                            <td>${member.statusChangeDate || "-"}</td>
                            <td>${member.statusChanged ? "Yes" : "No"}</td>
                        </tr>`;
            tbody.innerHTML += row;
        });

        document.querySelector("#memberTable tbody").addEventListener("click", function (e) {
            if (e.target.classList.contains("member-link") || e.target.closest(".member-link")) {
                e.preventDefault();

                const link = e.target.closest(".member-link");
                const memberId = link.dataset.id;
                const dropdown = document.getElementById("dropdown-" + memberId);
                const arrow = link.querySelector(".arrow");

                // Hide all dropdowns and reset arrows
                document.querySelectorAll(".dropdown").forEach(el => {
                    if (el !== dropdown) el.style.display = "none";
                });
                document.querySelectorAll(".arrow").forEach(a => a.classList.remove("expanded"));

                // Toggle this one
                const isVisible = dropdown.style.display === "block";
                dropdown.style.display = isVisible ? "none" : "block";

                document.addEventListener("click", function (e) {
                    if (!e.target.classList.contains("member-link")) {
                        document.querySelectorAll(".dropdown").forEach(el => el.style.display = "none");
                    }
                });
            }
        });
    });
}

function editMember(memberId) {
    fetch(`/api/v1/members/user/retrieve/${memberId}`,{
        method:"GET",
        credentials: "include"
    })
        .then(res => {
        if (!res.ok) throw new Error("Failed to fetch member data");
        return res.json();
    })
        .then(member => {
        localStorage.setItem("editMember", JSON.stringify(member));
        window.location.href = "/html/update.html";
    })
        .catch(error => {
        error => showErrorModal(error.message || "An unexpected error occurred.")
    });
}

function deleteMember(memberId) {
    showConfirmation("Are you sure you want to delete this member?", () => {
        fetch(`/api/v1/members/admin/delete/${memberId}`, {
            method: 'DELETE',
            credentials: "include"
        })
            .then(res => {
            if (res.ok) {
                showErrorModal("Member deleted.");
                location.reload();
            } else {
                return res.text().then(errorMessage => {
                    throw new Error(errorMessage || "Unknown error occurred");
                });
            }
        })
            .catch(error => showErrorModal(error.message || "An unexpected error occurred."));
    });
}
