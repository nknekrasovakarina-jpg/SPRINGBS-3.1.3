const usersTable = document.getElementById('usersTable').getElementsByTagName('tbody')[0];
let deleteId = null;

// ==================== Load Users from Server ====================
async function loadUsers() {
    usersTable.innerHTML = ''; // очищаем таблицу
    const response = await fetch('/admin/users');
    const users = await response.json();

    users.forEach(user => addUserRow(user));
}

// ==================== Add Row to Table ====================
function addUserRow(user) {
    const row = usersTable.insertRow();
    row.innerHTML = `
        <td>${user.id}</td>
        <td>${user.firstName}</td>
        <td>${user.lastName}</td>
        <td>${user.age}</td>
        <td>${user.email}</td>
        <td>${user.roles.map(r => r.name.replace('ROLE_', '')).join(', ')}</td>
        <td>
            <button class="btn btn-primary btn-sm editBtn">Edit</button>
            <button class="btn btn-danger btn-sm deleteBtn">Delete</button>
        </td>
    `;
}

// ==================== Add User ====================
document.getElementById('newUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        age: document.getElementById('age').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        roles: Array.from(document.getElementById('role').selectedOptions).map(o => o.value)
    };

    const response = await fetch('/admin/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    });

    const newUser = await response.json();
    addUserRow(newUser);

    bootstrap.Modal.getInstance(document.getElementById('addUserModal')).hide();
    e.target.reset();
});

// ==================== Edit User ====================
document.addEventListener('click', async (e) => {
    if (e.target.classList.contains('editBtn')) {
        const row = e.target.closest('tr');
        const userId = row.cells[0].innerText;

        // Заполняем модалку
        document.getElementById('editUserId').value = userId;
        document.getElementById('editFirstName').value = row.cells[1].innerText;
        document.getElementById('editLastName').value = row.cells[2].innerText;
        document.getElementById('editAge').value = row.cells[3].innerText;
        document.getElementById('editEmail').value = row.cells[4].innerText;
        document.getElementById('editPassword').value = '';
        document.getElementById('editRole').value = row.cells[5].innerText.split(', ').map(r => 'ROLE_' + r);

        new bootstrap.Modal(document.getElementById('editUserModal')).show();
    }
});

document.getElementById('editUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const userId = document.getElementById('editUserId').value;
    const formData = {
        id: userId,
        firstName: document.getElementById('editFirstName').value,
        lastName: document.getElementById('editLastName').value,
        age: document.getElementById('editAge').value,
        email: document.getElementById('editEmail').value,
        password: document.getElementById('editPassword').value,
        roles: Array.from(document.getElementById('editRole').selectedOptions).map(o => o.value)
    };

    const response = await fetch(`/admin/edit/${userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    });

    const updatedUser = await response.json();

    // Обновляем строку таблицы
    for (let row of usersTable.rows) {
        if (row.cells[0].innerText == updatedUser.id) {
            row.cells[1].innerText = updatedUser.firstName;
            row.cells[2].innerText = updatedUser.lastName;
            row.cells[3].innerText = updatedUser.age;
            row.cells[4].innerText = updatedUser.email;
            row.cells[5].innerText = updatedUser.roles.map(r => r.name.replace('ROLE_', '')).join(', ');
            break;
        }
    }

    bootstrap.Modal.getInstance(document.getElementById('editUserModal')).hide();
});

// ==================== Delete User ====================
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('deleteBtn')) {
        const row = e.target.closest('tr');
        deleteId = row.cells[0].innerText;

        document.getElementById('deleteId').innerText = row.cells[0].innerText;
        document.getElementById('deleteFirstName').innerText = row.cells[1].innerText;
        document.getElementById('deleteLastName').innerText = row.cells[2].innerText;
        document.getElementById('deleteAge').innerText = row.cells[3].innerText;
        document.getElementById('deleteEmail').innerText = row.cells[4].innerText;
        document.getElementById('deleteRole').innerText = row.cells[5].innerText;

        new bootstrap.Modal(document.getElementById('deleteUserModal')).show();
    }
});

document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
    await fetch(`/admin/delete/${deleteId}`, { method: 'DELETE' });

    // Удаляем строку из таблицы
    for (let i = 0; i < usersTable.rows.length; i++) {
        if (usersTable.rows[i].cells[0].innerText == deleteId) {
            usersTable.deleteRow(i);
            break;
        }
    }

    bootstrap.Modal.getInstance(document.getElementById('deleteUserModal')).hide();
});

// ==================== Initialize ====================
document.addEventListener('DOMContentLoaded', loadUsers);