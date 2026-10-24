package com.agiletrack.controller;

import com.agiletrack.model.*;
import com.agiletrack.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProjectRepository projects;
    private final SprintRepository sprints;
    private final TaskRepository tasks;
    private final BugRepository bugs;
    private final TestCaseRepository tests;
    private final UserRepository users;
    private final BugCommentRepository comments;
    private final ActivityLogRepository logs;

    public ApiController(
            ProjectRepository p,
            SprintRepository s,
            TaskRepository t,
            BugRepository b,
            TestCaseRepository tc,
            UserRepository u,
            BugCommentRepository c,
            ActivityLogRepository l) {

        projects = p;
        sprints = s;
        tasks = t;
        bugs = b;
        tests = tc;
        users = u;
        comments = c;
        logs = l;
    }

    private void log(String actor, String action, String type, Long id) {
        ActivityLog a = new ActivityLog();
        a.actor = actor;
        a.action = action;
        a.entityType = type;
        a.entityId = id;
        logs.save(a);
    }

    private String actor(HttpSession s) {
        Object e = s.getAttribute("userEmail");
        return e == null ? "System" : e.toString();
    }

    private <T> T require(Optional<T> value) {
        return value.orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Record not found"
                ));
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    @GetMapping("/projects")
    public List<Project> projects() {
        return projects.findAll();
    }

    @PostMapping("/projects")
    public Project addProject(
            @RequestBody Project p,
            HttpSession s) {

        p.id = null;

        Project saved = projects.save(p);

        log(
                actor(s),
                "created project",
                "Project",
                saved.id
        );

        return saved;
    }

    @PutMapping("/projects/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestBody Project p,
            HttpSession s) {

        require(projects.findById(id));

        p.id = id;

        Project x = projects.save(p);

        log(
                actor(s),
                "updated project",
                "Project",
                id
        );

        return x;
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(
            @PathVariable Long id,
            HttpSession s) {

        require(projects.findById(id));

        projects.deleteById(id);

        log(
                actor(s),
                "deleted project",
                "Project",
                id
        );
    }

    // =========================================================
    // SPRINTS
    // =========================================================

    @GetMapping("/sprints")
    public List<Sprint> sprints() {
        return sprints.findAll();
    }

    @PostMapping("/sprints")
    public Sprint addSprint(
            @RequestBody Sprint p,
            HttpSession s) {

        p.id = null;

        Sprint x = sprints.save(p);

        log(
                actor(s),
                "created sprint",
                "Sprint",
                x.id
        );

        return x;
    }

    @PutMapping("/sprints/{id}")
    public Sprint updateSprint(
            @PathVariable Long id,
            @RequestBody Sprint p,
            HttpSession s) {

        require(sprints.findById(id));

        p.id = id;

        Sprint x = sprints.save(p);

        log(
                actor(s),
                "updated sprint",
                "Sprint",
                id
        );

        return x;
    }

    @DeleteMapping("/sprints/{id}")
    public void deleteSprint(
            @PathVariable Long id,
            HttpSession s) {

        require(sprints.findById(id));

        sprints.deleteById(id);

        log(
                actor(s),
                "deleted sprint",
                "Sprint",
                id
        );
    }

    // =========================================================
    // TASKS
    // =========================================================

    @GetMapping("/tasks")
    public List<Task> tasks() {
        return tasks.findAll();
    }

    @PostMapping("/tasks")
    public Task addTask(
            @RequestBody Task p,
            HttpSession s) {

        p.id = null;

        Task x = tasks.save(p);

        log(
                actor(s),
                "created task",
                "Task",
                x.id
        );

        return x;
    }

    /**
     * Secure task update:
     *
     * ADMIN           -> can update any task
     * PROJECT_MANAGER -> can update any task
     * DEVELOPER       -> can update only their assigned task
     * TESTER          -> blocked
     */
    @PutMapping("/tasks/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @RequestBody Task p,
            HttpSession s) {

        // Get the task currently stored in the database.
        Task existing = require(tasks.findById(id));

        // Get current logged-in user's information.
        String role = String.valueOf(
                s.getAttribute("userRole")
        );

        String email = String.valueOf(
                s.getAttribute("userEmail")
        );

        // -----------------------------------------------------
        // ADMIN / PROJECT MANAGER
        // -----------------------------------------------------

        if ("ADMIN".equalsIgnoreCase(role)
                || "PROJECT_MANAGER".equalsIgnoreCase(role)) {

            // Admin and Project Manager can update any task.
        }

        // -----------------------------------------------------
        // DEVELOPER
        // -----------------------------------------------------

        else if ("DEVELOPER".equalsIgnoreCase(role)) {

            UserAccount developer =
                    users.findByEmailIgnoreCase(email)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Developer account not found"
                                    )
                            );

            String assignee =
                    normalize(existing.assignee);

            String developerEmail =
                    normalize(developer.email);

            String developerName =
                    normalize(developer.fullName);

            String username;

            if (developerEmail.contains("@")) {
                username = developerEmail.substring(
                        0,
                        developerEmail.indexOf("@")
                );
            } else {
                username = developerEmail;
            }

            username = normalize(username);

            // Check whether this task belongs to the
            // currently logged-in Developer.
            boolean assignedToDeveloper =
                    assignee.equals(developerEmail)
                    || assignee.equals(developerName)
                    || assignee.equals(username);

            if (!assignedToDeveloper) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can update only tasks assigned to you"
                );
            }

            // Developer cannot transfer the task to another user.
            // The original assignee is preserved.
            p.assignee = existing.assignee;
        }

        // -----------------------------------------------------
        // ALL OTHER ROLES
        // -----------------------------------------------------

        else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update tasks"
            );
        }

        // Always preserve the actual database ID.
        p.id = id;

        // Save the updated task.
        Task updated = tasks.save(p);

        // Record activity.
        log(
                actor(s),
                "updated task",
                "Task",
                id
        );

        return updated;
    }

    @DeleteMapping("/tasks/{id}")
    public void deleteTask(
            @PathVariable Long id,
            HttpSession s) {

        require(tasks.findById(id));

        tasks.deleteById(id);

        log(
                actor(s),
                "deleted task",
                "Task",
                id
        );
    }

    // =========================================================
    // BUGS
    // =========================================================

    @GetMapping("/bugs")
    public List<Bug> bugs() {
        return bugs.findAll();
    }

    @PostMapping("/bugs")
    public Bug addBug(
            @RequestBody Bug p,
            HttpSession s) {

        p.id = null;

        Bug x = bugs.save(p);

        log(
                actor(s),
                "reported bug",
                "Bug",
                x.id
        );

        return x;
    }

    @PutMapping("/bugs/{id}")
    public Bug updateBug(
            @PathVariable Long id,
            @RequestBody Bug p,
            HttpSession s) {

        require(bugs.findById(id));

        p.id = id;

        Bug x = bugs.save(p);

        log(
                actor(s),
                "updated bug",
                "Bug",
                id
        );

        return x;
    }

    @DeleteMapping("/bugs/{id}")
    public void deleteBug(
            @PathVariable Long id,
            HttpSession s) {

        require(bugs.findById(id));

        bugs.deleteById(id);

        log(
                actor(s),
                "deleted bug",
                "Bug",
                id
        );
    }

    // =========================================================
    // BUG COMMENTS
    // =========================================================

    @GetMapping("/bugs/{id}/comments")
    public List<BugComment> bugComments(
            @PathVariable Long id) {

        require(bugs.findById(id));

        return comments.findByBugIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/bugs/{id}/comments")
    public BugComment addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession s) {

        require(bugs.findById(id));

        String msg =
                body.getOrDefault("message", "").trim();

        if (msg.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment cannot be empty"
            );
        }

        BugComment c = new BugComment();

        c.bugId = id;
        c.author = actor(s);
        c.message = msg;

        BugComment saved = comments.save(c);

        log(
                actor(s),
                "commented on bug",
                "Bug",
                id
        );

        return saved;
    }

    // =========================================================
    // TEST CASES
    // =========================================================

    @GetMapping("/tests")
    public List<TestCase> tests() {
        return tests.findAll();
    }

    @PostMapping("/tests")
    public TestCase addTest(
            @RequestBody TestCase p,
            HttpSession s) {

        p.id = null;

        TestCase x = tests.save(p);

        log(
                actor(s),
                "created test case",
                "TestCase",
                x.id
        );

        return x;
    }

    @PutMapping("/tests/{id}")
    public TestCase updateTest(
            @PathVariable Long id,
            @RequestBody TestCase p,
            HttpSession s) {

        require(tests.findById(id));

        p.id = id;

        TestCase x = tests.save(p);

        log(
                actor(s),
                "updated test case",
                "TestCase",
                id
        );

        return x;
    }

    @DeleteMapping("/tests/{id}")
    public void deleteTest(
            @PathVariable Long id,
            HttpSession s) {

        require(tests.findById(id));

        tests.deleteById(id);

        log(
                actor(s),
                "deleted test case",
                "TestCase",
                id
        );
    }

    // =========================================================
    // TEAM
    // =========================================================

    @GetMapping("/team")
    public List<UserAccount> team() {
        return users.findAll();
    }

    // =========================================================
    // ACTIVITY
    // =========================================================

    @GetMapping("/activity")
    public List<ActivityLog> activity() {
        return logs.findTop20ByOrderByCreatedAtDesc();
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        List<Bug> bl = bugs.findAll();
        List<Task> tl = tasks.findAll();
        List<TestCase> ql = tests.findAll();

        long open = bl.stream()
                .filter(b ->
                        !"Resolved".equalsIgnoreCase(b.status)
                        && !"Closed".equalsIgnoreCase(b.status))
                .count();

        long done = tl.stream()
                .filter(t ->
                        "Done".equalsIgnoreCase(t.status))
                .count();

        long passed = ql.stream()
                .filter(t ->
                        "Passed".equalsIgnoreCase(t.status))
                .count();

        long failed = ql.stream()
                .filter(t ->
                        "Failed".equalsIgnoreCase(t.status))
                .count();

        long critical = bl.stream()
                .filter(b ->
                        "Critical".equalsIgnoreCase(b.severity)
                        && !"Resolved".equalsIgnoreCase(b.status)
                        && !"Closed".equalsIgnoreCase(b.status))
                .count();

        Map<String, Object> m =
                new LinkedHashMap<>();

        m.put("projects", projects.count());
        m.put("tasks", tl.size());
        m.put("doneTasks", done);

        m.put("bugs", bl.size());
        m.put("openBugs", open);
        m.put("criticalBugs", critical);

        m.put("tests", ql.size());
        m.put("passedTests", passed);
        m.put("failedTests", failed);

        m.put(
                "taskCompletion",
                tl.isEmpty()
                        ? 0
                        : Math.round(
                                done * 100.0 / tl.size()
                        )
        );

        m.put(
                "testPassRate",
                ql.isEmpty()
                        ? 0
                        : Math.round(
                                passed * 100.0 / ql.size()
                        )
        );

        Map<String, Long> statuses =
                new LinkedHashMap<>();

        for (String st :
                List.of(
                        "To Do",
                        "In Progress",
                        "Done"
                )) {

            statuses.put(
                    st,
                    tl.stream()
                            .filter(t ->
                                    st.equalsIgnoreCase(t.status))
                            .count()
            );
        }

        m.put("taskStatuses", statuses);

        Map<String, Long> bugStatuses =
                new LinkedHashMap<>();

        for (String st :
                List.of(
                        "Open",
                        "In Progress",
                        "Resolved",
                        "Closed"
                )) {

            bugStatuses.put(
                    st,
                    bl.stream()
                            .filter(b ->
                                    st.equalsIgnoreCase(b.status))
                            .count()
            );
        }

        m.put("bugStatuses", bugStatuses);

        return m;
    }
}