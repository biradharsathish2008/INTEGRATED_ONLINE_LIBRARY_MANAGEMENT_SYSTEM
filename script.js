// LOGIN FUNCTION
function login(){

  var username = document.getElementById("username").value.trim();
  var password = document.getElementById("password").value;
  var userType = document.querySelector('input[name="userType"]:checked').value;

  if(userType === "admin"){
    var admin = getAdminCredentials();
    if(username === admin.username && password === admin.password){
      window.location = "dashboard.html";
    } else {
      document.getElementById("msg").innerHTML = "Invalid Admin Username or Password";
    }

  } else if(userType === "student"){
    var students = getStudents();
    var match = students.find(function(s){
      var lower = username.toLowerCase();
      return (s.email.toLowerCase() === lower || s.name.toLowerCase() === lower) && s.password === password;
    });

    if(match){
      setCurrentStudent(match.id);
      window.location = "student-dashboard.html";
    } else {
      document.getElementById("msg").innerHTML = "Invalid Student Username or Password";
    }
  }
}

function updateLoginPlaceholder(){
  var userType = document.querySelector('input[name="userType"]:checked').value;
  var usernameInput = document.getElementById("username");
  if(userType === "admin"){
    usernameInput.placeholder = "Enter Username";
  } else {
    usernameInput.placeholder = "Enter Email or Name";
  }
}

function showRegister(){
  document.getElementById('register-box').style.display = 'block';
  document.querySelector('.login-box').style.display = 'none';
  document.getElementById('msg').innerText = '';
}

function hideRegister(){
  document.getElementById('register-box').style.display = 'none';
  document.querySelector('.login-box').style.display = 'block';
  document.getElementById('register-msg').innerText = '';
}

function registerStudent(){
  var name = document.getElementById('reg-name').value.trim();
  var email = document.getElementById('reg-email').value.trim();
  var mobile = document.getElementById('reg-mobile').value.trim();
  var password = document.getElementById('reg-password').value;
  var msgEl = document.getElementById('register-msg');

  if(!name || !email || !mobile || !password){
    msgEl.style.color = '#d32f2f';
    msgEl.innerText = 'Please fill in all fields.';
    return;
  }

  var students = getStudents();
  var exists = students.find(function(s){ return s.email.toLowerCase() === email.toLowerCase(); });
  if(exists){
    msgEl.style.color = '#d32f2f';
    msgEl.innerText = 'A student with that email already exists.';
    return;
  }

  var nextId = 1;
  if(students.length){
    var lastId = parseInt(students[students.length - 1].id, 10);
    if(!isNaN(lastId)) nextId = lastId + 1;
  }

  students.push({
    id: nextId,
    name: name,
    email: email,
    mobile: mobile,
    password: password
  });

  saveStudents(students);
  msgEl.style.color = '#2b7a0b';
  msgEl.innerText = 'Registration successful! You can now login.';
  setTimeout(function(){
    hideRegister();
    document.getElementById('username').value = email;
    document.getElementById('password').value = '';
  }, 1800);
}

window.addEventListener('DOMContentLoaded', function(){
  var radios = document.querySelectorAll('input[name="userType"]');
  radios.forEach(function(r){
    r.addEventListener('change', updateLoginPlaceholder);
  });
  updateLoginPlaceholder();
});


// STUDENT STORAGE HELPERS
function getStudents() {
  var stored = localStorage.getItem("students");
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (e) {
      return [];
    }
  }

  var defaults = [
    { id: 1, name: "Sathish", email: "sathish@gmail.com", mobile: "9876543210", password: "student123" },
    { id: 2, name: "Rahul", email: "rahul@gmail.com", mobile: "9998887776", password: "student123" }
  ];
  localStorage.setItem("students", JSON.stringify(defaults));
  return defaults;
}

// ADMIN CREDENTIALS HELPERS
function getAdminCredentials() {
  var stored = localStorage.getItem("adminCredentials");
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (e) {
      // fall through and fallback to defaults
    }
  }

  var defaults = { username: "admin", password: "admin123" };
  localStorage.setItem("adminCredentials", JSON.stringify(defaults));
  return defaults;
}

function saveAdminCredentials(credentials) {
  localStorage.setItem("adminCredentials", JSON.stringify(credentials));
}

function saveStudents(students) {
  localStorage.setItem("students", JSON.stringify(students));
}

function setCurrentStudent(id) {
  localStorage.setItem("currentStudentId", id);
}

function getCurrentStudent() {
  var id = parseInt(localStorage.getItem("currentStudentId"), 10);
  if (isNaN(id)) return null;
  var students = getStudents();
  return students.find(function(s){ return s.id === id; }) || null;
}

// ISSUED BOOKS STORAGE HELPERS
function getIssuedBooks() {
  return JSON.parse(localStorage.getItem("issuedBooks") || "[]");
}

function saveIssuedBooks(list) {
  localStorage.setItem("issuedBooks", JSON.stringify(list));
}

function updateIssuedBookStatus(studentId, bookId, status) {
  var issued = getIssuedBooks();
  var item = issued.find(function(i){ return i.studentId === studentId && i.bookId === bookId; });
  if (item) {
    item.returnStatus = status;
    saveIssuedBooks(issued);
  }
}

function updateIssuedBookStatusById(recordId, status) {
  var issued = getIssuedBooks();
  var item = issued.find(function(i){ return i.id === recordId; });
  if (item) {
    item.returnStatus = status;
    saveIssuedBooks(issued);
  }
}

// LOGOUT FUNCTION
function logout(){

window.location = "login.html";

}

// ADMIN SETTINGS (dashboard)
function loadAdminSettings(){
  var admin = getAdminCredentials();
  var usernameInput = document.getElementById('admin-username');
  var passwordInput = document.getElementById('admin-password');
  if(usernameInput && passwordInput){
    usernameInput.value = admin.username;
    passwordInput.value = admin.password;
  }
}

function saveAdminSettings(){
  var usernameInput = document.getElementById('admin-username');
  var passwordInput = document.getElementById('admin-password');
  var msgEl = document.getElementById('admin-settings-msg');
  if(!usernameInput || !passwordInput || !msgEl) return;

  var username = usernameInput.value.trim();
  var password = passwordInput.value.trim();
  if(!username || !password){
    msgEl.style.color = '#d32f2f';
    msgEl.innerText = 'Please enter both username and password.';
    return;
  }

  saveAdminCredentials({ username: username, password: password });
  msgEl.style.color = '#2b7a0b';
  msgEl.innerText = 'Admin credentials saved successfully.';
  setTimeout(function(){ msgEl.innerText = ''; }, 3200);
}



// ADD NEW BOOK FUNCTION
function addNewBook() {
  var name = document.getElementById("book-name").value.trim();
  var category = document.getElementById("book-category").value;
  var author = document.getElementById("book-author").value.trim();
  var isbn = document.getElementById("book-isbn").value.trim();
  var price = document.getElementById("book-price").value.trim();

  if (!name || !category || !author || !isbn || !price) {
    alert("Please fill in all book fields.");
    return;
  }

  var tableBody = document.querySelector("table tbody");
  var lastRow = tableBody.rows[tableBody.rows.length - 1];
  var nextId = 1;
  if (lastRow) {
    var lastId = parseInt(lastRow.cells[0].innerText, 10);
    if (!isNaN(lastId)) nextId = lastId + 1;
  }

  var newRow = tableBody.insertRow();
  newRow.insertCell().innerText = nextId;
  newRow.insertCell().innerText = name;
  newRow.insertCell().innerText = category;
  newRow.insertCell().innerText = author;
  newRow.insertCell().innerText = isbn;
  newRow.insertCell().innerText = price.startsWith("$") ? price : "$" + price;

  var actionCell = newRow.insertCell();
  actionCell.innerHTML =
    '<button class="btn btn-primary btn-xs" onclick="editRow(this)">' +
    '<i class="fa fa-edit"></i> Edit</button> ' +
    '<button class="btn btn-danger btn-xs" onclick="deleteRow(this)">' +
    '<i class="fa fa-trash"></i> Delete</button>';

  // Clear form fields
  document.getElementById("add-book-form").reset();
}


// DELETE BUTTON FUNCTION (for tables)
function deleteRow(button) {
  var row = button.parentNode.parentNode;
  var bookName = row.cells[1].innerText;
  var confirmed = confirm("Delete book \"" + bookName + "\"?");
  if (confirmed) {
    row.parentNode.removeChild(row);
  }
}


// EDIT BUTTON FUNCTION (for tables)
function editRow(button){

var row = button.parentNode.parentNode;

var col1 = row.cells[1].innerText;
var col2 = row.cells[2].innerText;

var newValue1 = prompt("Edit Value:", col1);
var newValue2 = prompt("Edit Value:", col2);

if(newValue1 != null){
row.cells[1].innerText = newValue1;
}

if(newValue2 != null){
row.cells[2].innerText = newValue2;
}

}



// EDIT RETURN STATUS FUNCTION
function editReturnStatus(button){

  var row = button.parentNode.parentNode;
  var statusCell = row.cells[5]; // Return Status column
  var recordId = parseInt(row.cells[0].innerText, 10);

  var currentStatus = statusCell.innerText;

  if(currentStatus === "Not Returned"){
    var confirmReturn = confirm("Mark this book as returned?");
    if(confirmReturn){
      statusCell.innerText = "Returned";
      updateIssuedBookStatusById(recordId, "Returned");
    }
  } else {
    var confirmNotReturn = confirm("Mark this book as not returned?");
    if(confirmNotReturn){
      statusCell.innerText = "Not Returned";
      updateIssuedBookStatusById(recordId, "Not Returned");
    }
  }

}


// ISSUE BOOK FUNCTION
function issueBook() {
  var student = document.getElementById("issue-student").value.trim();
  var book = document.getElementById("issue-book").value.trim();
  var isbn = document.getElementById("issue-isbn").value.trim();
  var date = document.getElementById("issue-date").value;

  if (!student || !book || !isbn || !date) {
    alert("Please fill in all fields to issue a book.");
    return;
  }

  var issued = JSON.parse(localStorage.getItem("issuedBooks") || "[]");
  var nextId = 1;
  if (issued.length) {
    var lastId = parseInt(issued[issued.length - 1].id, 10);
    if (!isNaN(lastId)) nextId = lastId + 1;
  }

  issued.push({
    id: nextId,
    studentName: student,
    bookName: book,
    isbn: isbn,
    issueDate: date,
    returnStatus: "Not Returned"
  });

  localStorage.setItem("issuedBooks", JSON.stringify(issued));
  window.location = "manage-issue.html";
}


// LOAD ISSUED BOOKS (used on manage-issue page)
function loadIssuedBooks() {
  var table = document.querySelector("table");
  if (!table) return;

  var tbody = table.querySelector("tbody");
  if (!tbody) return;

  var issued = JSON.parse(localStorage.getItem("issuedBooks") || "[]");
  if (!issued.length) return;

  issued.forEach(function(item) {
    var row = tbody.insertRow();
    row.insertCell().innerText = item.id;
    row.insertCell().innerText = item.studentName;
    row.insertCell().innerText = item.bookName;
    row.insertCell().innerText = item.isbn;
    row.insertCell().innerText = item.issueDate;
    row.insertCell().innerText = item.returnStatus;
    var actionCell = row.insertCell();
    actionCell.innerHTML = '<button class="btn btn-primary btn-xs" onclick="editReturnStatus(this)"><i class="fa fa-edit"></i> Edit</button>';
  });
}


// Highlight active menu item
function highlightActiveMenu() {
  var links = document.querySelectorAll('.menu-bar a');
  var current = window.location.pathname.split('/').pop();
  links.forEach(function(link){
    if (link.getAttribute('href') === current) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });
}

// Run on page load (manage-issue.html)
window.addEventListener("DOMContentLoaded", function() {
  highlightActiveMenu();
  if (window.location.href.indexOf("manage-issue.html") !== -1) {
    loadIssuedBooks();
  }
});