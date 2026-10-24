package com.agiletrack.config;
import com.agiletrack.model.*;
import com.agiletrack.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration public class DataSeeder {
 @Bean CommandLineRunner seed(UserRepository users,ProjectRepository projects,SprintRepository sprints,TaskRepository tasks,
   BugRepository bugs,TestCaseRepository tests,PasswordEncoder encoder){
  return args->{
   
if(users.findByEmailIgnoreCase("admin@agiletrack.local").isEmpty()){
    addUser(users,encoder,"admin@agiletrack.local","System Admin","Admin","ADMIN");
}

if(users.findByEmailIgnoreCase("manager@agiletrack.local").isEmpty()){
    addUser(users,encoder,"manager@agiletrack.local","Project Manager","Management","PROJECT_MANAGER");
}

if(users.findByEmailIgnoreCase("developer@agiletrack.local").isEmpty()){
    addUser(users,encoder,"developer@agiletrack.local","Demo Developer","Engineering","DEVELOPER");
}

if(users.findByEmailIgnoreCase("tester@agiletrack.local").isEmpty()){
    addUser(users,encoder,"tester@agiletrack.local","Demo Tester","QA","TESTER");
}

   if(projects.count()>0)return;
   Project p=new Project();p.name="AgileTrack Platform";p.description="An integrated Agile project management, bug tracking and software quality workspace.";p.owner="Project Manager";p.status="Active";p.startDate=LocalDate.now();p.endDate=LocalDate.now().plusMonths(2);projects.save(p);
   Sprint sp=new Sprint();sp.name="Sprint 1";sp.projectId=1L;sp.startDate=LocalDate.now();sp.endDate=LocalDate.now().plusDays(14);sp.status="Active";sp.goalPoints=20;sprints.save(sp);
   Task a=new Task();a.projectId=1L;a.sprintId=1L;a.title="Build project overview";a.description="Create the main project dashboard and navigation.";a.assignee="Demo Developer";a.priority="High";a.status="In Progress";a.storyPoints=5;a.taskType="Feature";tasks.save(a);
   Task b=new Task();b.projectId=1L;b.sprintId=1L;b.title="Write issue reporting tests";b.description="Verify required fields and issue submission.";b.assignee="Demo Tester";b.priority="Medium";b.status="To Do";b.storyPoints=3;b.taskType="Testing";tasks.save(b);
   Task c=new Task();c.projectId=1L;c.sprintId=1L;c.title="Prepare initial requirements";c.description="Document the MVP functional requirements.";c.assignee="Project Manager";c.priority="Low";c.status="Done";c.storyPoints=2;c.taskType="Documentation";tasks.save(c);
   Bug bug=new Bug();bug.projectId=1L;bug.title="Sample form validation defect";bug.description="Demonstration bug: the sample form should validate required inputs.";bug.severity="Major";bug.priority="High";bug.status="Open";bug.reporter="Demo Tester";bug.assignee="Demo Developer";bugs.save(bug);
   TestCase tc=new TestCase();tc.projectId=1L;tc.title="Verify bug submission";tc.steps="Open Bug Tracker, enter a title and description, then submit.";tc.expectedResult="Bug is saved and visible in the bug list.";tc.actualResult="Pending execution";tc.status="Not Run";tc.tester="Demo Tester";tests.save(tc);
  };
 }
 private void addUser(UserRepository repo,PasswordEncoder encoder,String email,String name,String dept,String role){
  UserAccount u=new UserAccount();u.email=email;u.fullName=name;u.department=dept;u.role=role;u.passwordHash=encoder.encode("Agile123!");u.active=true;repo.save(u);
 }
}