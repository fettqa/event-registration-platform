(function () {
  const STORAGE_KEY = "erp.auth";

  function getAuth() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  function getToken() {
    const auth = getAuth();
    return auth && auth.accessToken ? auth.accessToken : null;
  }

  function showAlert(testId, message) {
    const el = document.querySelector("[data-testid='" + testId + "']");
    if (!el) {
      return;
    }
    el.textContent = message;
    el.hidden = false;
  }

  function clearAlerts() {
    ["admin-error", "admin-success"].forEach(function (id) {
      const el = document.querySelector("[data-testid='" + id + "']");
      if (el) {
        el.hidden = true;
        el.textContent = "";
      }
    });
  }

  function guardAdminPage() {
    const table = document.querySelector("[data-testid='admin-users-table']");
    if (!table) {
      return false;
    }
    const auth = getAuth();
    if (!auth || auth.role !== "ADMIN" || !auth.accessToken) {
      window.location.href = "/login?redirect=" + encodeURIComponent("/adminPanel");
      return false;
    }
    return true;
  }

  async function loadUsers() {
    const tbody = document.querySelector("[data-testid='admin-users-tbody']");
    if (!tbody) {
      return;
    }
    clearAlerts();

    const response = await fetch("/api/admin/users", {
      headers: { "Authorization": "Bearer " + getToken() }
    });

    if (response.status === 401 || response.status === 403) {
      window.location.href = "/login?redirect=" + encodeURIComponent("/adminPanel");
      return;
    }

    if (!response.ok) {
      const body = await response.json().catch(function () { return {}; });
      showAlert("admin-error", body.error || "Failed to load users");
      return;
    }

    const users = await response.json();
    if (!users.length) {
      tbody.innerHTML = '<tr><td colspan="4" class="empty">No users found.</td></tr>';
      return;
    }

    tbody.innerHTML = users.map(function (user) {
      const role = user.role;
      return [
        "<tr data-testid=\"admin-user-row-" + user.id + "\">",
        "<td>" + user.id + "</td>",
        "<td>" + escapeHtml(user.fullName) + "</td>",
        "<td>" + escapeHtml(user.email) + "</td>",
        "<td>",
        "<select data-testid=\"admin-role-select-" + user.id + "\" data-user-id=\"" + user.id + "\">",
        "<option value=\"USER\"" + (role === "USER" ? " selected" : "") + ">USER</option>",
        "<option value=\"SUPER_USER\"" + (role === "SUPER_USER" ? " selected" : "") + ">SUPER_USER</option>",
        "</select>",
        "</td>",
        "</tr>"
      ].join("");
    }).join("");

    tbody.querySelectorAll("select[data-user-id]").forEach(function (select) {
      select.addEventListener("change", function () {
        updateRole(select.getAttribute("data-user-id"), select.value);
      });
    });
  }

  function escapeHtml(value) {
    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
  }

  async function updateRole(userId, role) {
    clearAlerts();
    const response = await fetch("/api/admin/users/" + userId + "/role", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken()
      },
      body: JSON.stringify({ role: role })
    });

    const body = await response.json().catch(function () { return {}; });
    if (!response.ok) {
      showAlert("admin-error", body.error || "Failed to update role");
      await loadUsers();
      return;
    }

    showAlert("admin-success", "Role updated for " + (body.email || ("user #" + userId)));
  }

  document.addEventListener("DOMContentLoaded", function () {
    if (!guardAdminPage()) {
      return;
    }
    loadUsers();
  });
})();
