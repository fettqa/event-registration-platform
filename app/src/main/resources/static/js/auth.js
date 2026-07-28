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

  function renderHeaderAuth() {
    const guest = document.querySelector("[data-auth-guest]");
    const user = document.querySelector("[data-auth-user]");
    const emailEl = document.querySelector("[data-testid='auth-email']");
    const roleEl = document.querySelector("[data-testid='auth-role']");
    if (!guest || !user) {
      return;
    }

    const auth = getAuth();
    if (auth && auth.accessToken) {
      guest.hidden = true;
      user.hidden = false;
      if (emailEl) {
        emailEl.textContent = auth.email || "";
      }
      if (roleEl) {
        roleEl.textContent = auth.role || "";
      }
    } else {
      guest.hidden = false;
      user.hidden = true;
    }
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
          const message = body.error || "Login failed";
          if (errorEl) {
            errorEl.textContent = message;
            errorEl.hidden = false;
          }
          return;
        }

        setAuth({
          accessToken: body.accessToken,
          email: body.email,
          role: body.role
        });

        const params = new URLSearchParams(window.location.search);
        const redirect = params.get("redirect") || "/";
        window.location.href = redirect;
      } catch (err) {
        if (errorEl) {
          errorEl.textContent = "Network error. Is the app running?";
          errorEl.hidden = false;
        }
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

  function showCreateAlert(message) {
    const alert = document.querySelector("[data-testid='error-message']");
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
    if (!getToken()) {
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
      if (!token) {
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
        showCreateAlert("Admin role required to create events.");
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
          showCreateAlert(body.error || "Validation failed");
        }
        return;
      }

      if (!response.ok) {
        showCreateAlert(body.error || "Failed to create event");
        return;
      }

      window.location.href = "/events/" + body.id;
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    renderHeaderAuth();
    bindLogout();
    bindLoginForm();
    guardCreatePage();
    bindCreateEventForm();
  });
})();
