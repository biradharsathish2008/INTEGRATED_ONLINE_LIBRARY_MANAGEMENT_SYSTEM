import java.util.*;
import java.time.LocalDate;

// ============================================================
// DSA STRUCTURES USED:
//   - LinkedList<Book>         → Books catalog (dynamic insertion/deletion)
//   - HashMap<String, Student> → Student registry (O(1) lookup by username)
//   - Stack<String>            → Action log / undo history
//   - Queue<IssueRequest>      → Pending borrow requests (FIFO)
//   - ArrayList<IssuedBook>    → Issued books ledger
//   - Linear Search            → Find book by ISBN / name
//   - Binary Search            → Find book by ID (on sorted list)
// ============================================================

// ─────────────────────────────────────────────
// MODEL: Book
// ─────────────────────────────────────────────
class Book {
    int id;
    String name, category, author, isbn;
    double price;

    Book(int id, String name, String category, String author, String isbn, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-25s %-15s %-20s %-10s $%.2f",
                id, name, category, author, isbn, price);
    }
}

// ─────────────────────────────────────────────
// MODEL: Student
// ─────────────────────────────────────────────
class Student {
    int id;
    String name, email, mobile, username, password;

    Student(int id, String name, String email, String mobile, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.username = email;   // username = email (same as HTML app)
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-20s %-30s %-15s", id, name, email, mobile);
    }
}

// ─────────────────────────────────────────────
// MODEL: IssuedBook
// ─────────────────────────────────────────────
class IssuedBook {
    int id;
    int studentId;
    String studentName, bookName, author, isbn, issueDate, returnStatus;

    IssuedBook(int id, int studentId, String studentName,
               String bookName, String author, String isbn, String issueDate) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.bookName = bookName;
        this.author = author;
        this.isbn = isbn;
        this.issueDate = issueDate;
        this.returnStatus = "Not Returned";
    }

    @Override
    public String toString() {
        return String.format("%-4d %-20s %-25s %-12s %-12s %-15s",
                id, studentName, bookName, isbn, issueDate, returnStatus);
    }
}

// ─────────────────────────────────────────────
// MODEL: IssueRequest  (Queue element)
// ─────────────────────────────────────────────
class IssueRequest {
    String studentName;
    String bookName;
    String isbn;
    String requestDate;

    IssueRequest(String studentName, String bookName, String isbn, String requestDate) {
        this.studentName = studentName;
        this.bookName = bookName;
        this.isbn = isbn;
        this.requestDate = requestDate;
    }
}

// ─────────────────────────────────────────────
// LIBRARY MANAGEMENT SYSTEM — CORE CLASS
// ─────────────────────────────────────────────
public class LibraryManagementSystem {

    // ── Data Stores ──
    // DSA: LinkedList for Books → O(1) add/remove at ends, easy traversal
    private static LinkedList<Book> books = new LinkedList<>();

    // DSA: HashMap for Students → O(1) average lookup by username
    private static HashMap<String, Student> studentMap = new HashMap<>();

    // DSA: ArrayList for IssuedBooks → indexed access + iteration
    private static ArrayList<IssuedBook> issuedBooks = new ArrayList<>();

    // DSA: Stack for action log (undo simulation / history)
    private static Stack<String> actionLog = new Stack<>();

    // DSA: Queue for pending borrow requests (FIFO processing)
    private static Queue<IssueRequest> issueQueue = new LinkedList<>();

    // ── State ──
    private static int bookIdCounter = 6;
    private static int studentIdCounter = 1;
    private static int issuedIdCounter = 1;

    private static String adminUsername = "admin";
    private static String adminPassword = "admin123";

    private static String currentRole = null;   // "admin" or "student"
    private static Student currentStudent = null;

    private static Scanner sc = new Scanner(System.in);

    // ─────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        seedData();
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   📚 Online Library Management System  ║");
        System.out.println("╚══════════════════════════════════════╝");

        while (true) {
            if (currentRole == null) {
                showLoginMenu();
            } else if (currentRole.equals("admin")) {
                showAdminMenu();
            } else {
                showStudentMenu();
            }
        }
    }

    // ─────────────────────────────────────────
    // SEED DEFAULT DATA
    // ─────────────────────────────────────────
    static void seedData() {
        books.add(new Book(1, "Core Java",          "Technology", "Kumar Pandule",        "222333", 45.0));
        books.add(new Book(2, "Physics",             "Science",    "HC Verma",             "1111",   30.0));
        books.add(new Book(3, "Mathematics",         "Science",    "RD Sharma",            "333444", 28.0));
        books.add(new Book(4, "English Literature",  "Fiction",    "William Shakespeare",  "444555", 22.0));
        books.add(new Book(5, "World History",       "History",    "John Smith",           "555666", 35.0));
    }

    // ─────────────────────────────────────────
    // LOGIN MENU
    // ─────────────────────────────────────────
    static void showLoginMenu() {
        System.out.println("\n─────────────── Login ───────────────");
        System.out.println("  1. Admin Login");
        System.out.println("  2. Student Login");
        System.out.println("  3. Student Register");
        System.out.println("  0. Exit");
        System.out.print("Choice: ");
        String ch = sc.nextLine().trim();

        switch (ch) {
            case "1": adminLogin();    break;
            case "2": studentLogin();  break;
            case "3": studentRegister(); break;
            case "0": System.out.println("Goodbye!"); System.exit(0); break;
            default:  System.out.println("Invalid choice.");
        }
    }

    static void adminLogin() {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine().trim();

        if (u.equals(adminUsername) && p.equals(adminPassword)) {
            currentRole = "admin";
            System.out.println("✓ Admin login successful. Welcome, Admin!");
            actionLog.push("Admin logged in.");
        } else {
            System.out.println("✗ Invalid admin credentials.");
        }
    }

    static void studentLogin() {
        System.out.print("Email (username): ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine().trim();

        // DSA: HashMap lookup O(1)
        Student s = studentMap.get(u);
        if (s != null && s.password.equals(p)) {
            currentRole = "student";
            currentStudent = s;
            System.out.println("✓ Welcome, " + s.name + "!");
            actionLog.push("Student logged in: " + s.name);
        } else {
            System.out.println("✗ Invalid student credentials.");
        }
    }

    static void studentRegister() {
        System.out.print("Full Name:  "); String name   = sc.nextLine().trim();
        System.out.print("Email:      "); String email  = sc.nextLine().trim();
        System.out.print("Mobile:     "); String mobile = sc.nextLine().trim();
        System.out.print("Password:   "); String pwd    = sc.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || pwd.isEmpty()) {
            System.out.println("✗ All fields are required."); return;
        }
        if (studentMap.containsKey(email)) {
            System.out.println("✗ Email already registered."); return;
        }

        Student st = new Student(studentIdCounter++, name, email, mobile, pwd);
        studentMap.put(email, st);  // DSA: HashMap insert O(1)
        actionLog.push("Student registered: " + name);
        System.out.println("✓ Registration successful! Please login.");
    }

    static void logout() {
        actionLog.push((currentRole.equals("admin") ? "Admin" : currentStudent.name) + " logged out.");
        currentRole = null;
        currentStudent = null;
        System.out.println("✓ Logged out.");
    }

    // ─────────────────────────────────────────
    // ADMIN MENU
    // ─────────────────────────────────────────
    static void showAdminMenu() {
        System.out.println("\n════════════ Admin Panel ════════════");
        System.out.println("  1. Dashboard");
        System.out.println("  2. Books   (Add / Delete / List / Search)");
        System.out.println("  3. Students (List / Edit / Delete)");
        System.out.println("  4. Issue a Book");
        System.out.println("  5. Manage Issued Books");
        System.out.println("  6. Process Pending Requests (Queue)");
        System.out.println("  7. View Action Log (Stack)");
        System.out.println("  8. Update Admin Credentials");
        System.out.println("  9. Binary Search Book by ID");
        System.out.println("  0. Logout");
        System.out.print("Choice: ");
        String ch = sc.nextLine().trim();

        switch (ch) {
            case "1": adminDashboard();        break;
            case "2": booksMenu();             break;
            case "3": studentsMenu();          break;
            case "4": issueBookAdmin();        break;
            case "5": manageIssuedBooks();     break;
            case "6": processIssueQueue();     break;
            case "7": viewActionLog();         break;
            case "8": updateAdminCredentials(); break;
            case "9": binarySearchBookById();  break;
            case "0": logout();                break;
            default:  System.out.println("Invalid choice.");
        }
    }

    static void adminDashboard() {
        long returned = issuedBooks.stream().filter(i -> i.returnStatus.equals("Returned")).count();
        System.out.println("\n──────────── Dashboard ────────────");
        System.out.println("  📚 Books Listed      : " + books.size());
        System.out.println("  📋 Times Issued      : " + issuedBooks.size());
        System.out.println("  🔄 Times Returned    : " + returned);
        System.out.println("  👥 Registered Students: " + studentMap.size());
        System.out.println("  📬 Pending Requests   : " + issueQueue.size());
    }

    // ─────────────────────────────────────────
    // BOOKS MENU
    // ─────────────────────────────────────────
    static void booksMenu() {
        while (true) {
            System.out.println("\n──────── Books ────────");
            System.out.println("  1. List All Books");
            System.out.println("  2. Add Book");
            System.out.println("  3. Delete Book");
            System.out.println("  4. Linear Search (by name / ISBN)");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();
            if (ch.equals("0")) break;
            switch (ch) {
                case "1": listBooks(books);           break;
                case "2": addBook();                  break;
                case "3": deleteBook();               break;
                case "4": linearSearchBooks();        break;
                default:  System.out.println("Invalid.");
            }
        }
    }

    static void listBooks(Iterable<Book> list) {
        System.out.println("\n" + String.format("%-4s %-25s %-15s %-20s %-10s %s",
                "ID", "Name", "Category", "Author", "ISBN", "Price"));
        System.out.println("─".repeat(90));
        boolean any = false;
        for (Book b : list) {
            System.out.println(b);
            any = true;
        }
        if (!any) System.out.println("  (No books found)");
    }

    static void addBook() {
        System.out.print("Book Name:  "); String name     = sc.nextLine().trim();
        System.out.print("Category:   "); String category = sc.nextLine().trim();
        System.out.print("Author:     "); String author   = sc.nextLine().trim();
        System.out.print("ISBN:       "); String isbn     = sc.nextLine().trim();
        System.out.print("Price ($):  "); String priceStr = sc.nextLine().trim();

        if (name.isEmpty() || category.isEmpty() || author.isEmpty() || isbn.isEmpty() || priceStr.isEmpty()) {
            System.out.println("✗ All fields required."); return;
        }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { System.out.println("✗ Invalid price."); return; }

        // DSA: LinkedList addLast O(1)
        books.addLast(new Book(bookIdCounter++, name, category, author, isbn, price));
        actionLog.push("Book added: " + name);  // DSA: Stack push O(1)
        System.out.println("✓ Book added successfully.");
    }

    static void deleteBook() {
        System.out.print("Enter Book ID to delete: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            // DSA: LinkedList removeIf O(n)
            boolean removed = books.removeIf(b -> b.id == id);
            if (removed) {
                actionLog.push("Book deleted. ID=" + id);  // DSA: Stack push
                System.out.println("✓ Book deleted.");
            } else {
                System.out.println("✗ Book not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }

    // DSA: Linear Search O(n) — search by name or ISBN
    static void linearSearchBooks() {
        System.out.print("Enter name or ISBN to search: ");
        String query = sc.nextLine().trim().toLowerCase();
        System.out.println("\nSearch Results:");
        System.out.println(String.format("%-4s %-25s %-15s %-20s %-10s %s",
                "ID", "Name", "Category", "Author", "ISBN", "Price"));
        System.out.println("─".repeat(90));

        boolean found = false;
        for (Book b : books) {  // O(n) linear scan
            if (b.name.toLowerCase().contains(query) || b.isbn.toLowerCase().contains(query)) {
                System.out.println(b);
                found = true;
            }
        }
        if (!found) System.out.println("  No books matched your query.");
    }

    // DSA: Binary Search O(log n) — search by ID on sorted list
    static void binarySearchBookById() {
        System.out.print("Enter Book ID: ");
        try {
            int targetId = Integer.parseInt(sc.nextLine().trim());

            // Copy to ArrayList and sort by ID
            ArrayList<Book> sorted = new ArrayList<>(books);
            sorted.sort(Comparator.comparingInt(b -> b.id));

            // Binary Search implementation
            int lo = 0, hi = sorted.size() - 1, result = -1;
            while (lo <= hi) {
                int mid = lo + (hi - lo) / 2;
                if (sorted.get(mid).id == targetId) {
                    result = mid; break;
                } else if (sorted.get(mid).id < targetId) {
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }

            if (result != -1) {
                System.out.println("✓ Found (Binary Search):");
                System.out.println(sorted.get(result));
            } else {
                System.out.println("✗ Book with ID " + targetId + " not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }

    // ─────────────────────────────────────────
    // STUDENTS MENU
    // ─────────────────────────────────────────
    static void studentsMenu() {
        while (true) {
            System.out.println("\n──────── Students ────────");
            System.out.println("  1. List All Students");
            System.out.println("  2. Edit Student");
            System.out.println("  3. Delete Student");
            System.out.println("  0. Back");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();
            if (ch.equals("0")) break;
            switch (ch) {
                case "1": listStudents();   break;
                case "2": editStudent();    break;
                case "3": deleteStudent();  break;
                default:  System.out.println("Invalid.");
            }
        }
    }

    static void listStudents() {
        System.out.println("\n" + String.format("%-4s %-20s %-30s %-15s", "ID", "Name", "Email", "Mobile"));
        System.out.println("─".repeat(72));
        if (studentMap.isEmpty()) { System.out.println("  (No students registered)"); return; }
        for (Student s : studentMap.values()) System.out.println(s);
    }

    static void editStudent() {
        System.out.print("Enter Student ID to edit: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            Student target = null;
            for (Student s : studentMap.values()) {
                if (s.id == id) { target = s; break; }
            }
            if (target == null) { System.out.println("✗ Student not found."); return; }

            System.out.print("Name   [" + target.name   + "]: "); String n = sc.nextLine().trim();
            System.out.print("Email  [" + target.email  + "]: "); String e = sc.nextLine().trim();
            System.out.print("Mobile [" + target.mobile + "]: "); String m = sc.nextLine().trim();
            System.out.print("Password: ");                        String p = sc.nextLine().trim();

            String oldKey = target.username;
            if (!n.isEmpty())  target.name   = n;
            if (!e.isEmpty()) { target.email = e; target.username = e; }
            if (!m.isEmpty())  target.mobile = m;
            if (!p.isEmpty())  target.password = p;

            // Re-insert with new key if email changed
            if (!e.isEmpty() && !e.equals(oldKey)) {
                studentMap.remove(oldKey);
                studentMap.put(target.username, target);
            }
            actionLog.push("Student edited. ID=" + id);
            System.out.println("✓ Student updated.");
        } catch (NumberFormatException ex) {
            System.out.println("✗ Invalid ID.");
        }
    }

    static void deleteStudent() {
        System.out.print("Enter Student ID to delete: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            String keyToRemove = null;
            for (Map.Entry<String, Student> e : studentMap.entrySet()) {
                if (e.getValue().id == id) { keyToRemove = e.getKey(); break; }
            }
            if (keyToRemove != null) {
                studentMap.remove(keyToRemove);  // DSA: HashMap remove O(1)
                actionLog.push("Student deleted. ID=" + id);
                System.out.println("✓ Student deleted.");
            } else {
                System.out.println("✗ Student not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }

    // ─────────────────────────────────────────
    // ISSUE BOOK (Admin)
    // ─────────────────────────────────────────
    static void issueBookAdmin() {
        System.out.print("Student Name: "); String student = sc.nextLine().trim();
        System.out.print("Book Name:    "); String book    = sc.nextLine().trim();
        System.out.print("ISBN:         "); String isbn    = sc.nextLine().trim();
        System.out.print("Issue Date (YYYY-MM-DD) [today]: "); String date = sc.nextLine().trim();
        if (date.isEmpty()) date = LocalDate.now().toString();

        if (student.isEmpty() || book.isEmpty() || isbn.isEmpty()) {
            System.out.println("✗ Fill all fields."); return;
        }

        issuedBooks.add(new IssuedBook(issuedIdCounter++, -1, student, book, "", isbn, date));
        actionLog.push("Book issued: " + book + " → " + student);
        System.out.println("✓ Book issued successfully.");
    }

    static void manageIssuedBooks() {
        System.out.println("\n" + String.format("%-4s %-20s %-25s %-12s %-12s %s",
                "ID", "Student", "Book", "ISBN", "IssueDate", "Status"));
        System.out.println("─".repeat(95));

        if (issuedBooks.isEmpty()) { System.out.println("  (No issued books)"); }
        else for (IssuedBook ib : issuedBooks) System.out.println(ib);

        System.out.print("\nEnter Issued-Record ID to mark Returned (0 to skip): ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) return;
            for (IssuedBook ib : issuedBooks) {
                if (ib.id == id) {
                    ib.returnStatus = "Returned";
                    actionLog.push("Book returned. Record ID=" + id);
                    System.out.println("✓ Marked as Returned.");
                    return;
                }
            }
            System.out.println("✗ Record not found.");
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }

    // ─────────────────────────────────────────
    // QUEUE — Pending Issue Requests
    // ─────────────────────────────────────────
    static void processIssueQueue() {
        System.out.println("\n──── Pending Issue Requests (Queue) ────");
        System.out.println("  Queue size: " + issueQueue.size());
        System.out.println("  1. Add Request to Queue");
        System.out.println("  2. Process Next Request (Dequeue)");
        System.out.println("  3. Peek at Next Request");
        System.out.println("  0. Back");
        System.out.print("Choice: ");
        String ch = sc.nextLine().trim();

        switch (ch) {
            case "1":
                System.out.print("Student Name: "); String sn = sc.nextLine().trim();
                System.out.print("Book Name:    "); String bn = sc.nextLine().trim();
                System.out.print("ISBN:         "); String is = sc.nextLine().trim();
                // DSA: Queue offer O(1)
                issueQueue.offer(new IssueRequest(sn, bn, is, LocalDate.now().toString()));
                System.out.println("✓ Request added to queue.");
                break;
            case "2":
                // DSA: Queue poll O(1)
                IssueRequest req = issueQueue.poll();
                if (req == null) { System.out.println("Queue is empty."); break; }
                issuedBooks.add(new IssuedBook(issuedIdCounter++, -1, req.studentName,
                        req.bookName, "", req.isbn, req.requestDate));
                actionLog.push("Queue request processed: " + req.bookName + " → " + req.studentName);
                System.out.println("✓ Processed: " + req.bookName + " issued to " + req.studentName);
                break;
            case "3":
                // DSA: Queue peek O(1)
                IssueRequest top = issueQueue.peek();
                if (top == null) System.out.println("Queue is empty.");
                else System.out.println("Next: " + top.studentName + " wants \"" + top.bookName + "\" (" + top.isbn + ")");
                break;
        }
    }

    // ─────────────────────────────────────────
    // STACK — Action Log
    // ─────────────────────────────────────────
    static void viewActionLog() {
        System.out.println("\n──── Action Log (Stack — most recent first) ────");
        if (actionLog.isEmpty()) { System.out.println("  (No actions logged)"); return; }
        // DSA: iterate Stack from top to bottom
        Stack<String> temp = new Stack<>();
        temp.addAll(actionLog);
        int i = temp.size();
        while (!temp.isEmpty()) {
            System.out.printf("  [%2d] %s%n", i--, temp.pop());
        }
    }

    // ─────────────────────────────────────────
    // ADMIN CREDENTIALS
    // ─────────────────────────────────────────
    static void updateAdminCredentials() {
        System.out.println("Current username: " + adminUsername);
        System.out.print("New Username: "); String u = sc.nextLine().trim();
        System.out.print("New Password: "); String p = sc.nextLine().trim();
        if (u.isEmpty() || p.isEmpty()) { System.out.println("✗ Cannot be empty."); return; }
        adminUsername = u;
        adminPassword = p;
        actionLog.push("Admin credentials updated.");
        System.out.println("✓ Credentials updated.");
    }

    // ─────────────────────────────────────────
    // STUDENT MENU
    // ─────────────────────────────────────────
    static void showStudentMenu() {
        System.out.println("\n════════════ Student Panel ════════════");
        System.out.println("  1. Dashboard");
        System.out.println("  2. Browse / Search Books");
        System.out.println("  3. My Borrowed Books");
        System.out.println("  4. Return a Book");
        System.out.println("  0. Logout");
        System.out.print("Choice: ");
        String ch = sc.nextLine().trim();

        switch (ch) {
            case "1": studentDashboard();    break;
            case "2": studentBrowseBooks();  break;
            case "3": studentMyBooks();      break;
            case "4": studentReturnBook();   break;
            case "0": logout();              break;
            default:  System.out.println("Invalid choice.");
        }
    }

    static void studentDashboard() {
        long borrowed = issuedBooks.stream()
                .filter(i -> i.studentId == currentStudent.id && i.returnStatus.equals("Not Returned")).count();
        long returned = issuedBooks.stream()
                .filter(i -> i.studentId == currentStudent.id && i.returnStatus.equals("Returned")).count();

        System.out.println("\n──────── Student Dashboard ────────");
        System.out.println("  👤 Name              : " + currentStudent.name);
        System.out.println("  📚 Total Books       : " + books.size());
        System.out.println("  📖 Currently Borrowed: " + borrowed);
        System.out.println("  ✅ Books Returned    : " + returned);
    }

    static void studentBrowseBooks() {
        System.out.print("Search (name/ISBN, blank=all): ");
        String query = sc.nextLine().trim().toLowerCase();

        System.out.println("\n" + String.format("%-4s %-25s %-15s %-20s %-10s %s",
                "ID", "Name", "Category", "Author", "ISBN", "Status"));
        System.out.println("─".repeat(95));

        // DSA: LinkedList linear traversal O(n)
        for (Book b : books) {
            if (!query.isEmpty() && !b.name.toLowerCase().contains(query)
                    && !b.isbn.toLowerCase().contains(query)) continue;

            boolean alreadyBorrowed = issuedBooks.stream()
                    .anyMatch(i -> i.studentId == currentStudent.id
                            && i.bookName.equals(b.name)
                            && i.returnStatus.equals("Not Returned"));

            String status = alreadyBorrowed ? "Borrowed" : "Available";
            System.out.printf("%-4d %-25s %-15s %-20s %-10s %s%n",
                    b.id, b.name, b.category, b.author, b.isbn, status);
        }

        System.out.print("\nEnter Book ID to borrow (0 to cancel): ");
        try {
            int bid = Integer.parseInt(sc.nextLine().trim());
            if (bid == 0) return;
            studentBorrowBook(bid);
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }

    static void studentBorrowBook(int bookId) {
        Book book = null;
        for (Book b : books) if (b.id == bookId) { book = b; break; }
        if (book == null) { System.out.println("✗ Book not found."); return; }

        final Book foundBook = book;  // effectively final for lambda
        boolean existing = issuedBooks.stream()
                .anyMatch(i -> i.studentId == currentStudent.id
                        && i.bookName.equals(foundBook.name)
                        && i.returnStatus.equals("Not Returned"));
        if (existing) { System.out.println("✗ You already have this book borrowed."); return; }

        issuedBooks.add(new IssuedBook(issuedIdCounter++, currentStudent.id, currentStudent.name,
                book.name, book.author, book.isbn, LocalDate.now().toString()));
        actionLog.push("Student borrowed: " + book.name + " ← " + currentStudent.name);
        System.out.println("✓ Book \"" + book.name + "\" borrowed successfully!");
    }

    static void studentMyBooks() {
        System.out.println("\n" + String.format("%-4s %-25s %-20s %-12s %s",
                "ID", "Book", "Author", "IssueDate", "Status"));
        System.out.println("─".repeat(80));

        boolean any = false;
        for (IssuedBook ib : issuedBooks) {
            if (ib.studentId == currentStudent.id) {
                System.out.printf("%-4d %-25s %-20s %-12s %s%n",
                        ib.id, ib.bookName, ib.author, ib.issueDate, ib.returnStatus);
                any = true;
            }
        }
        if (!any) System.out.println("  (No borrowed books yet)");
    }

    static void studentReturnBook() {
        studentMyBooks();
        System.out.print("\nEnter Issued-Record ID to return (0 to cancel): ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) return;
            for (IssuedBook ib : issuedBooks) {
                if (ib.id == id && ib.studentId == currentStudent.id) {
                    ib.returnStatus = "Returned";
                    actionLog.push("Student returned book. Record ID=" + id);
                    System.out.println("✓ Book returned successfully.");
                    return;
                }
            }
            System.out.println("✗ Record not found.");
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid ID.");
        }
    }
}
