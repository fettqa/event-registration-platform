(function () {
  const STORAGE_KEY = "erp.auth";
  const CREATE_ROLES = { ADMIN: true, SUPER_USER: true };

  function getAuth() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  function setAuth(auth) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  }

  function clearAuth() {
    localStorage.removeItem(STORAGE_KEY);
  }

  function getToken() {
    const auth = getAuth();
    return auth && auth.accessToken ? auth.accessToken : null;
  }

  function canCreateEvents(auth) {
    return !!(auth && auth.role && CREATE_ROLES[auth.role]);
  }

  function renderHeaderAuth() {
    const guest = document.querySelector("[data-auth-guest]");
    const user = document.querySelector("[data-auth-user]");
    const fullNameEl = document.querySelector("[data-testid='auth-full-name']");
    const emailEl = document.querySelector("[data-testid='auth-email']");
    const roleEl = document.querySelector("[data-testid='auth-role']");
    const adminLink = document.querySelector("[data-testid='admin-panel-link']");
    if (!guest || !user) {
      return;
    }

    const auth = getAuth();
    if (auth && auth.accessToken) {
      guest.hidden = true;
      user.hidden = false;
      if (fullNameEl) {
        fullNameEl.textContent = auth.fullName || "";
      }
      if (emailEl) {
        emailEl.textContent = auth.email || "";
      }
      if (roleEl) {
        roleEl.textContent = auth.role || "";
      }
      if (adminLink) {
        adminLink.hidden = auth.role !== "ADMIN";
      }
    } else {
      guest.hidden = false;
      user.hidden = true;
      if (adminLink) {
        adminLink.hidden = true;
      }
    }
  }

  function renderCreateEventLink() {
    const link = document.querySelector("[data-testid='create-event-link']");
    if (!link) {
      return;
    }
    link.hidden = !canCreateEvents(getAuth());
  }

  function bindLogout() {
    const logoutBtn = document.querySelector("[data-testid='logout-button']");
    if (!logoutBtn) {
      return;
    }
    logoutBtn.addEventListener("click", function () {
      clearAuth();
      window.location.href = "/";
    });
  }

  function redirectToLogin(redirectPath) {
    const target = redirectPath || window.location.pathname;
    window.location.href = "/login?redirect=" + encodeURIComponent(target);
  }

  function redirectAfterAuth() {
    const params = new URLSearchParams(window.location.search);
    window.location.href = params.get("redirect") || "/";
  }

  function showFormError(errorEl, message) {
    if (!errorEl) {
      return;
    }
    errorEl.textContent = message;
    errorEl.hidden = false;
  }

  function saveAuthFromResponse(body) {
    setAuth({
      accessToken: body.accessToken,
      fullName: body.fullName,
      email: body.email,
      role: body.role
    });
  }

  function bindLoginForm() {
    const form = document.querySelector("[data-testid='login-form']");
    if (!form) {
      return;
    }

    const errorEl = document.querySelector("[data-testid='login-error']");

    form.addEventListener("submit", async function (event) {
      event.preventDefault();
      if (errorEl) {
        errorEl.hidden = true;
        errorEl.textContent = "";
      }

      const email = form.querySelector("[data-testid='login-email-input']").value.trim();
      const password = form.querySelector("[data-testid='login-password-input']").value;

      try {
        const response = await fetch("/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: email, password: password })
        });

        const body = await response.json().catch(function () {
          return {};
        });

        if (!response.ok) {
          showFormError(errorEl, body.error || "Login failed");
          return;
        }

        saveAuthFromResponse(body);
        redirectAfterAuth();
      } catch (err) {
        showFormError(errorEl, "Network error. Is the app running?");
      }
    });
  }

  function bindRegisterForm() {
    const form = document.querySelector("[data-testid='register-form']");
    if (!form) {
      return;
    }

    const errorEl = document.querySelector("[data-testid='register-error']");

    form.addEventListener("submit", async function (event) {
      event.preventDefault();
      if (errorEl) {
        errorEl.hidden = true;
        errorEl.textContent = "";
      }

      const fullName = form.querySelector("[data-testid='register-fullname-input']").value.trim();
      const email = form.querySelector("[data-testid='register-email-input']").value.trim();
      const password = form.querySelector("[data-testid='register-password-input']").value;
      const confirm = form.querySelector("[data-testid='register-password-confirm-input']").value;

      if (!fullName) {
        showFormError(errorEl, "full name must not be blank");
        return;
      }

      if (password !== confirm) {
        showFormError(errorEl, "Passwords do not match");
        return;
      }

      if (password.length < 6) {
        showFormError(errorEl, "password must be at least 6 characters");
        return;
      }

      try {
        const response = await fetch("/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ fullName: fullName, email: email, password: password })
        });

        const body = await response.json().catch(function () {
          return {};
        });

        if (!response.ok) {
          let message = body.error || "Registration failed";
          if (body.fields) {
            const fieldMessages = Object.values(body.fields).filter(Boolean);
            if (fieldMessages.length > 0) {
              message = fieldMessages.join("; ");
            }
          }
          showFormError(errorEl, message);
          return;
        }

        saveAuthFromResponse(body);
        redirectAfterAuth();
      } catch (err) {
        showFormError(errorEl, "Network error. Is the app running?");
      }
    });
  }

  function clearCreateErrors(form) {
    const alert = document.querySelector("[data-testid='error-message']");
    if (alert) {
      alert.hidden = true;
      alert.textContent = "";
    }
    const formErrors = form.querySelector("[data-testid='form-errors']");
    if (formErrors) {
      formErrors.hidden = true;
      formErrors.innerHTML = "";
    }
    ["name-error", "seats-error"].forEach(function (testId) {
      const el = form.querySelector("[data-testid='" + testId + "']");
      if (el) {
        el.hidden = true;
        el.textContent = "";
      }
    });
  }

  function showCreateFieldError(form, testId, message) {
    const el = form.querySelector("[data-testid='" + testId + "']");
    if (!el) {
      return;
    }
    el.textContent = message;
    el.hidden = false;
  }

  function showCreateFormErrors(form, messages) {
    const formErrors = form.querySelector("[data-testid='form-errors']");
    if (!formErrors) {
      return;
    }
    formErrors.innerHTML = messages.map(function (m) {
      return "<p>" + m + "</p>";
    }).join("");
    formErrors.hidden = false;
  }

  function showAlert(testId, message) {
    const alert = document.querySelector("[data-testid='" + testId + "']");
    if (!alert) {
      return;
    }
    alert.textContent = message;
    alert.hidden = false;
  }

  function guardCreatePage() {
    const form = document.querySelector("[data-testid='create-event-form']");
    if (!form) {
      return;
    }
    const auth = getAuth();
    if (!getToken() || !canCreateEvents(auth)) {
      redirectToLogin("/events/new");
    }
  }

  function bindCreateEventForm() {
    const form = document.querySelector("[data-testid='create-event-form']");
    if (!form) {
      return;
    }

    form.addEventListener("submit", async function (event) {
      event.preventDefault();
      clearCreateErrors(form);

      const token = getToken();
      const auth = getAuth();
      if (!token || !canCreateEvents(auth)) {
        redirectToLogin("/events/new");
        return;
      }

      const nameInput = form.querySelector("[data-testid='event-name-input']");
      const seatsInput = form.querySelector("[data-testid='event-seats-input']");
      const name = nameInput ? nameInput.value.trim() : "";
      const maxSeatsRaw = seatsInput ? seatsInput.value : "";
      const maxSeats = maxSeatsRaw === "" ? null : Number(maxSeatsRaw);

      const response = await fetch("/api/events", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ name: name, maxSeats: maxSeats })
      });

      if (response.status === 401) {
        clearAuth();
        redirectToLogin("/events/new");
        return;
      }

      if (response.status === 403) {
        showAlert("error-message", "Admin or Super User role required to create events.");
        return;
      }

      const body = await response.json().catch(function () {
        return {};
      });

      if (response.status === 400) {
        const fields = body.fields || {};
        const messages = [];
        if (fields.name) {
          showCreateFieldError(form, "name-error", fields.name);
          messages.push(fields.name);
        }
        if (fields.maxSeats) {
          showCreateFieldError(form, "seats-error", fields.maxSeats);
          messages.push(fields.maxSeats);
        }
        if (messages.length > 0) {
          showCreateFormErrors(form, messages);
        } else {
          showAlert("error-message", body.error || "Validation failed");
        }
        return;
      }

      if (!response.ok) {
        showAlert("error-message", body.error || "Failed to create event");
        return;
      }

      window.location.href = "/events/" + body.id;
    });
  }

  function bindEventRegistration() {
    const guestBox = document.querySelector("[data-testid='register-guest']");
    const userBox = document.querySelector("[data-testid='register-user']");
    const submitBtn = document.querySelector("[data-testid='submit-registration']");
    if (!guestBox || !userBox || !submitBtn) {
      return;
    }

    const auth = getAuth();
    if (auth && auth.accessToken) {
      guestBox.hidden = true;
      userBox.hidden = false;
      const nameEl = document.querySelector("[data-testid='register-as-name']");
      const emailEl = document.querySelector("[data-testid='register-as-email']");
      if (nameEl) {
        nameEl.textContent = auth.fullName || "";
      }
      if (emailEl) {
        emailEl.textContent = auth.email || "";
      }
    } else {
      guestBox.hidden = false;
      userBox.hidden = true;
    }

    submitBtn.addEventListener("click", async function () {
      const success = document.querySelector("[data-testid='success-message']");
      const error = document.querySelector("[data-testid='error-message']");
      if (success) {
        success.hidden = true;
        success.textContent = "";
      }
      if (error) {
        error.hidden = true;
        error.textContent = "";
      }

      const token = getToken();
      if (!token) {
        redirectToLogin(window.location.pathname);
        return;
      }

      const eventId = submitBtn.getAttribute("data-event-id");
      const response = await fetch("/api/events/" + eventId + "/registrations", {
        method: "POST",
        headers: { "Authorization": "Bearer " + token }
      });

      if (response.status === 401) {
        clearAuth();
        redirectToLogin(window.location.pathname);
        return;
      }

      const body = await response.json().catch(function () {
        return {};
      });

      if (!response.ok) {
        showAlert("error-message", body.error || "Registration failed");
        return;
      }

      window.location.href = "/events/" + eventId + "?registered=1";
    });
  }

  function canDeleteEvent(auth, createdByEmail) {
    if (!auth) {
      return false;
    }
    if (auth.role === "ADMIN") {
      return true;
    }
    return auth.role === "SUPER_USER"
        && createdByEmail
        && auth.email
        && createdByEmail.toLowerCase() === auth.email.toLowerCase();
  }

  function canDeleteRegistration(auth, registrationEmail, createdByEmail) {
    if (!auth) {
      return false;
    }
    if (auth.role === "ADMIN") {
      return true;
    }
    if (auth.role === "SUPER_USER"
        && createdByEmail
        && auth.email
        && createdByEmail.toLowerCase() === auth.email.toLowerCase()) {
      return true;
    }
    return !!(registrationEmail
        && auth.email
        && registrationEmail.toLowerCase() === auth.email.toLowerCase());
  }

  function bindDeleteActions() {
    const auth = getAuth();
    const deleteEventBtn = document.querySelector("[data-testid='delete-event-button']");
    if (deleteEventBtn) {
      const createdByEmail = deleteEventBtn.getAttribute("data-created-by-email");
      if (canDeleteEvent(auth, createdByEmail)) {
        deleteEventBtn.hidden = false;
        deleteEventBtn.addEventListener("click", async function () {
          if (!window.confirm("Delete this event and all its registrations?")) {
            return;
          }
          const eventId = deleteEventBtn.getAttribute("data-event-id");
          const response = await fetch("/api/events/" + eventId, {
            method: "DELETE",
            headers: { "Authorization": "Bearer " + getToken() }
          });
          if (response.status === 401) {
            clearAuth();
            redirectToLogin(window.location.pathname);
            return;
          }
          if (!response.ok) {
            const body = await response.json().catch(function () { return {}; });
            showAlert("error-message", body.error || "Failed to delete event");
            return;
          }
          window.location.href = "/";
        });
      }
    }

    document.querySelectorAll("[data-testid='delete-registration-button']").forEach(function (btn) {
      const registrationEmail = btn.getAttribute("data-registration-email");
      const createdByEmail = btn.getAttribute("data-created-by-email");
      if (!canDeleteRegistration(auth, registrationEmail, createdByEmail)) {
        return;
      }
      btn.hidden = false;
      btn.addEventListener("click", async function () {
        if (!window.confirm("Delete this registration?")) {
          return;
        }
        const eventId = btn.getAttribute("data-event-id");
        const registrationId = btn.getAttribute("data-registration-id");
        const response = await fetch(
            "/api/events/" + eventId + "/registrations/" + registrationId,
            {
              method: "DELETE",
              headers: { "Authorization": "Bearer " + getToken() }
            });
        if (response.status === 401) {
          clearAuth();
          redirectToLogin(window.location.pathname);
          return;
        }
        if (!response.ok) {
          const body = await response.json().catch(function () { return {}; });
          showAlert("error-message", body.error || "Failed to delete registration");
          return;
        }
        window.location.reload();
      });
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    renderHeaderAuth();
    renderCreateEventLink();
    bindLogout();
    bindLoginForm();
    bindRegisterForm();
    guardCreatePage();
    bindCreateEventForm();
    bindEventRegistration();
    bindDeleteActions();
  });
})();