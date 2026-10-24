
const API='/api';
let projects=[],sprints=[],tasks=[],bugs=[],tests=[],team=[],activity=[],currentType='',currentRole='',currentUserEmail='';

const isAdmin=()=>currentRole==='ADMIN';
const isManager=()=>currentRole==='PROJECT_MANAGER';
const canManagePlanning=()=>isAdmin()||isManager();
const canManageTests=()=>isAdmin()||isManager()||currentRole==='TESTER';
const canDeleteRecords=()=>isAdmin()||isManager();

function isMyTask(task){
 const assignee = String(task?.assignee || '').trim().toLowerCase();
 const email = currentUserEmail.trim().toLowerCase();

 const username = email.split('@')[0];

 const displayName = email === 'developer@agiletrack.local'
     ? 'demo developer'
     : username.replace(/[._-]/g, ' ');

 return !!assignee && (
     assignee === email ||
     assignee === username ||
     assignee === displayName
 );
}

function canEditTask(task){
 return canManagePlanning()||(currentRole==='DEVELOPER'&&isMyTask(task));
}

function applyRoleUI(){
 const visibility={
  'nav-team':canManagePlanning(),
  'btn-new-project':canManagePlanning(),
  'btn-new-sprint':canManagePlanning(),
  'btn-new-task':canManagePlanning(),
  'btn-new-bug':true,
  'btn-new-test':canManageTests()
 };
 Object.entries(visibility).forEach(([id,visible])=>{
  const el=$(id);
  if(el)el.hidden=!visible;
 });
}

const $=id=>document.getElementById(id);

async function request(path,method='GET',body=null){
 const o={method,credentials:'same-origin',headers:{'Content-Type':'application/json'}};
 if(body)o.body=JSON.stringify(body);
 const r=await fetch(API+path,o);
 if(r.status===401){
  location.href='/login.html';
  throw new Error('Session expired');
 }
 if(!r.ok){
  let msg='Request failed ('+r.status+')';
  try{
   const j=await r.json();
   msg=j.detail||j.message||msg;
  }catch{}
  throw new Error(msg);
 }
 return r.status===204?null:r.json();
}

async function init(){
 try{
  const me=await request('/auth/me');
  currentRole=me.role;
  currentUserEmail=me.email||'';
  $('user-name').textContent=me.email.split('@')[0];
  $('user-role').textContent=me.role.replace('_',' ');
  $('avatar').textContent=me.email[0].toUpperCase();
  applyRoleUI();
  await loadAll();
 }catch(e){
  if(!location.pathname.includes('login'))location.href='/login.html';
 }
}

async function loadAll(){
 const [
  loadedProjects,
  loadedSprints,
  loadedTasks,
  loadedBugs,
  loadedTests,
  loadedActivity
 ]=await Promise.all([
  request('/projects'),
  request('/sprints'),
  request('/tasks'),
  request('/bugs'),
  request('/tests'),
  request('/activity')
 ]);

 projects=loadedProjects;
 sprints=loadedSprints;
 tasks=loadedTasks;
 bugs=loadedBugs;
 tests=loadedTests;
 activity=loadedActivity;

 try{
  team=await request('/team');
 }catch(error){
  if(error.message.includes('(403)')){
   team=[];
  }else{
   throw error;
  }
 }

 renderAll();
}

$('logout').addEventListener('click',async()=>{
 await request('/auth/logout','POST');
 location.href='/login.html';
});

function showPage(name){
 if(name==='team'&&!canManagePlanning()){
  toast('You do not have permission to view the team directory');
  return;
 }
 document.querySelectorAll('.page').forEach(p=>p.classList.remove('active'));
 $(name).classList.add('active');
 document.querySelectorAll('.nav').forEach(b=>
  b.classList.toggle('active',b.dataset.page===name)
 );
 $('page-title').textContent=({
  dashboard:'Project overview',
  projects:'Projects',
  tasks:'Backlog & sprints',
  bugs:'Bug tracker',
  tests:'Quality assurance',
  team:'Team directory'
 })[name];
}

document.querySelectorAll('.nav').forEach(b=>
 b.addEventListener('click',()=>showPage(b.dataset.page))
);

function esc(s=''){
 return String(s).replace(/[&<>"']/g,c=>({
  '&':'&amp;',
  '<':'&lt;',
  '>':'&gt;',
  '"':'&quot;'
 }[c]));
}

function badge(v){
 return `<span class="badge ${String(v||'').replaceAll(' ','')}">${esc(v||'—')}</span>`;
}

function table(head,rows){
 return rows.length
  ?`<table><thead><tr>${head.map(x=>`<th>${x}</th>`).join('')}</tr></thead><tbody>${rows.join('')}</tbody></table>`
  :'<div class="empty">Nothing here yet. Use the action button to add your first record.</div>';
}

function projectName(id){
 return projects.find(x=>x.id==id)?.name||'—';
}

function renderAll(){
 renderDashboard();
 renderProjects();
 renderSprints();
 renderTasks();
 renderBugs();
 renderTests();
 renderTeam();
}

async function renderDashboard(){
 const d=await request('/dashboard');

 $('metric-cards').innerHTML=[
  ['Projects',d.projects,'▣','Workspace total'],
  ['Tasks',d.tasks,'☷',`${d.doneTasks} completed`],
  ['Open defects',d.openBugs,'⚑',`${d.criticalBugs} critical`],
  ['Test cases',d.tests,'✓',`${d.passedTests} passed`]
 ].map(x=>`<div class="metric"><div class="metric-top"><span>${x[0]}</span><i class="metric-icon">${x[2]}</i></div><strong>${x[1]}</strong><div class="metric-foot">${x[3]}</div></div>`).join('');

 $('progress-area').innerHTML=`
 <div class="bar-label"><span>Overall task completion</span><b>${d.taskCompletion}%</b></div>
 <div class="progress-track"><i style="width:${d.taskCompletion}%"></i></div>
 <div class="mini-stats"><span><b>${d.doneTasks}</b> completed</span><span><b>${d.tasks-d.doneTasks}</b> remaining</span></div>
 ${Object.entries(d.taskStatuses).map(([k,v])=>`<div class="bar-label"><span>${k}</span><b>${v}</b></div>`).join('')}`;

 $('quality-area').innerHTML=`
 <div class="bar-label"><span>Test pass rate</span><b>${d.testPassRate}%</b></div>
 <div class="progress-track green"><i style="width:${d.testPassRate}%"></i></div>
 <div class="mini-stats"><span><b>${d.passedTests}</b> passed</span><span><b>${d.failedTests}</b> failed</span></div>
 <div class="bar-label"><span>Critical unresolved bugs</span><b>${d.criticalBugs}</b></div>
 <div class="progress-track"><i style="width:${Math.min(d.criticalBugs*20,100)}%;background:#d65e56"></i></div>`;

 $('recent-bugs').innerHTML=table(
  ['DEFECT','SEVERITY','STATUS','ASSIGNEE'],
  bugs.slice(-4).reverse().map(b=>
   `<tr><td><b>${esc(b.title)}</b><small>#${b.id} · ${esc(b.reporter||'Unknown reporter')}</small></td><td>${badge(b.severity)}</td><td>${badge(b.status)}</td><td>${esc(b.assignee||'Unassigned')}</td></tr>`
  )
 );

 $('activity-feed').innerHTML=activity.length
  ?activity.slice(0,6).map(a=>
   `<div class="feed-item"><span class="feed-dot"></span><div><b>${esc(a.actor||'System')}</b> ${esc(a.action)} <small>${esc(a.entityType)} #${a.entityId} · ${new Date(a.createdAt).toLocaleString()}</small></div></div>`
  ).join('')
  :'<div class="empty">Workspace activity will appear here.</div>';
}

// PROJECTS: Only Admin can delete.
function renderProjects(){
 $('projects-table').innerHTML=table(
  ['PROJECT','OWNER','STATUS','DELIVERY WINDOW','ACTIONS'],
  projects.map(p=>
   `<tr>
    <td><b>${esc(p.name)}</b><small>${esc(p.description||'')}</small></td>
    <td>${esc(p.owner||'—')}</td>
    <td>${badge(p.status)}</td>
    <td>${p.startDate||'—'} → ${p.endDate||'—'}</td>
    <td>
     ${canManagePlanning()?`<button class="small-btn" onclick="editItem('project',${p.id})">Edit</button> `:''}
     ${isAdmin()?`<button class="small-btn" onclick="removeItem('projects',${p.id})">Delete</button>`:''}
    </td>
   </tr>`
  )
 );
}

// SPRINTS: Only Admin can delete.
function renderSprints(){
 $('sprints-table').innerHTML=table(
  ['SPRINT','PROJECT','DATES','GOAL','STATUS','ACTIONS'],
  sprints.map(s=>
   `<tr>
    <td><b>${esc(s.name)}</b></td>
    <td>${esc(projectName(s.projectId))}</td>
    <td>${s.startDate||'—'} → ${s.endDate||'—'}</td>
    <td>${s.goalPoints??'—'} pts</td>
    <td>${badge(s.status)}</td>
    <td>
     ${canManagePlanning()?`<button class="small-btn" onclick="editItem('sprint',${s.id})">Edit</button> `:''}
     ${isAdmin()?`<button class="small-btn" onclick="removeItem('sprints',${s.id})">Delete</button>`:''}
    </td>
   </tr>`
  )
 );
}


function renderTasks(){
 const cols=['To Do','In Progress','Done'];

 $('tasks-board').innerHTML=cols.map(status=>{
  const list=tasks.filter(t=>t.status===status);

  return `<div class="kanban-col">
   <div class="kanban-head">${status}<span>${list.length}</span></div>

   ${list.map(t=>{
    const editable=canEditTask(t);
    const deletable=canDeleteRecords();

    return `<div class="task-card">
     <b>${esc(t.title)}</b>
     <p>${esc(t.description||'No description')}</p>

     <div class="task-meta">
      ${badge(t.priority)}
      <small>${esc(t.assignee||'Unassigned')} · ${t.storyPoints??0} pts</small>
     </div>

     <div style="margin-top:9px">
      ${editable
        ? `<select aria-label="Task status"
             onchange="changeStatus('tasks',${t.id},this.value)">
             ${opts(cols,t.status)}
           </select>
           <button class="small-btn"
             onclick="editItem('task',${t.id})">
             Edit
           </button>`
        : ''
      }

      ${deletable
        ? ` <button class="small-btn"
             onclick="removeItem('tasks',${t.id})">
             Delete
           </button>`
        : ''
      }
     </div>
    </div>`;
   }).join('') || '<div class="empty">No tasks</div>'}

  </div>`;
 }).join('');
}


function renderBugs(){
 const q=($('bug-search')?.value||'').toLowerCase();
 const f=$('bug-filter')?.value||'';

 const list=bugs.filter(b=>
  (!f||b.status===f)&&
  (`${b.title} ${b.description} ${b.assignee}`.toLowerCase().includes(q))
 );

 $('bugs-table').innerHTML=table(
  ['DEFECT','PROJECT','SEVERITY','PRIORITY','STATUS','ASSIGNEE','ACTIONS'],
  list.map(b=>
   `<tr>
    <td><b>${esc(b.title)}</b><small>#${b.id} · ${esc(b.description||'')}</small><button class="small-btn" onclick="showComments(${b.id})">Comments (${b.commentCount||0})</button></td>
    <td>${esc(projectName(b.projectId))}</td>
    <td>${badge(b.severity)}</td>
    <td>${badge(b.priority)}</td>
    <td><select onchange="changeStatus('bugs',${b.id},this.value)">${opts(['Open','In Progress','Resolved','Closed'],b.status)}</select></td>
    <td>${esc(b.assignee||'—')}</td>
    <td><button class="small-btn" onclick="editItem('bug',${b.id})">Edit</button> ${canDeleteRecords()?`<button class="small-btn" onclick="removeItem('bugs',${b.id})">Delete</button>`:''}</td>
   </tr>`
  )
 );
}

function renderTests(){
 const passed=tests.filter(t=>t.status==='Passed').length;
 const failed=tests.filter(t=>t.status==='Failed').length;
 const notRun=tests.filter(t=>t.status==='Not Run').length;

 $('quality-summary').innerHTML=[
  ['Passed',passed],
  ['Failed',failed],
  ['Not executed',notRun]
 ].map(x=>`<div class="quality-card"><span>${x[0]}</span><strong>${x[1]}</strong></div>`).join('');

 $('tests-table').innerHTML=table(
  ['TEST CASE','PROJECT','EXPECTED RESULT','ACTUAL RESULT','STATUS','TESTER','ACTIONS'],
  tests.map(t=>
   `<tr>
    <td><b>${esc(t.title)}</b><small>${esc(t.steps||'')}</small></td>
    <td>${esc(projectName(t.projectId))}</td>
    <td>${esc(t.expectedResult||'—')}</td>
    <td>${esc(t.actualResult||'—')}</td>
    <td>${canManageTests()?`<select onchange="changeStatus('tests',${t.id},this.value)">${opts(['Not Run','Passed','Failed','Blocked'],t.status)}</select>`:badge(t.status)}</td>
    <td>${esc(t.tester||'—')}</td>
    <td>${canManageTests()?`<button class="small-btn" onclick="editItem('test',${t.id})">Edit</button> `:''}${canDeleteRecords()?`<button class="small-btn" onclick="removeItem('tests',${t.id})">Delete</button>`:''}</td>
   </tr>`
  )
 );
}

function renderTeam(){
 $('team-grid').innerHTML=team.map(u=>
  `<div class="team-card">
   <div class="team-avatar">${esc((u.fullName||'U')[0].toUpperCase())}</div>
   <div><b>${esc(u.fullName)}</b><small>${esc(u.role.replaceAll('_',' '))} · ${esc(u.department||'Team')}</small><small>${esc(u.email)}</small></div>
  </div>`
 ).join('');
}

function opts(values,current){
 return values.map(v=>`<option ${v===current?'selected':''}>${v}</option>`).join('');
}

const configs={
 project:{
  title:'Project',
  path:'projects',
  fields:[
   ['name','Project name','text',true],
   ['description','Description','textarea'],
   ['owner','Project owner','text'],
   ['status','Status','select',['Active','On Hold','Completed']],
   ['startDate','Start date','date'],
   ['endDate','End date','date']
  ]
 },
 sprint:{
  title:'Sprint',
  path:'sprints',
  fields:[
   ['name','Sprint name','text',true],
   ['projectId','Project','project'],
   ['startDate','Start date','date'],
   ['endDate','End date','date'],
   ['goalPoints','Sprint goal (story points)','number'],
   ['status','Status','select',['Planned','Active','Completed']]
  ]
 },
 task:{
  title:'Task',
  path:'tasks',
  fields:[
   ['title','Task title','text',true],
   ['description','Description','textarea'],
   ['projectId','Project','project'],
   ['sprintId','Sprint','sprint'],
   ['assignee','Assignee','text'],
   ['taskType','Type','select',['Feature','Bug','Testing','Documentation','Improvement']],
   ['priority','Priority','select',['Low','Medium','High','Critical']],
   ['status','Status','select',['To Do','In Progress','Done']],
   ['storyPoints','Story points','number']
  ]
 },
 bug:{
  title:'Bug report',
  path:'bugs',
  fields:[
   ['title','Bug title','text',true],
   ['description','Steps to reproduce / details','textarea'],
   ['projectId','Project','project'],
   ['severity','Severity','select',['Minor','Major','Critical','Blocker']],
   ['priority','Priority','select',['Low','Medium','High','Critical']],
   ['status','Status','select',['Open','In Progress','Resolved','Closed']],
   ['reporter','Reported by','text'],
   ['assignee','Assign to','text']
  ]
 },
 test:{
  title:'Test case',
  path:'tests',
  fields:[
   ['title','Test case title','text',true],
   ['projectId','Project','project'],
   ['steps','Test steps','textarea'],
   ['expectedResult','Expected result','textarea'],
   ['actualResult','Actual result','textarea'],
   ['linkedBugId','Linked bug ID (optional)','number'],
   ['status','Execution status','select',['Not Run','Passed','Failed','Blocked']],
   ['tester','Tester','text']
  ]
 }
};

function openForm(type,item=null){
 const allowed=
  type==='bug'||
  (type==='project'&&canManagePlanning())||
  (type==='sprint'&&canManagePlanning())||
  (type==='task'&&(item?canEditTask(item):canManagePlanning()))||
  (type==='test'&&canManageTests());

 if(!allowed){
  toast('You do not have permission to perform this action');
  return;
 }

 currentType=type;
 const c=configs[type];

 $('modal-title').textContent=(item?'Edit ':'Create ')+c.title;

 $('item-form').innerHTML=c.fields.map(([key,label,kind,extra])=>{
  let val=item?.[key]??'';
  let input='';

  if(kind==='select'){
   input=`<select name="${key}">${extra.map(x=>`<option ${x===val?'selected':''}>${x}</option>`).join('')}</select>`;
  }else if(kind==='project'){
   input=`<select name="${key}">${projects.map(p=>`<option value="${p.id}" ${p.id==val?'selected':''}>${esc(p.name)}</option>`).join('')}</select>`;
  }else if(kind==='sprint'){
   input=`<select name="${key}"><option value="">No sprint</option>${sprints.map(s=>`<option value="${s.id}" ${s.id==val?'selected':''}>${esc(s.name)}</option>`).join('')}</select>`;
  }else if(kind==='textarea'){
   input=`<textarea name="${key}">${esc(val)}</textarea>`;
  }else{
   input=`<input name="${key}" type="${kind}" value="${esc(val)}" ${extra===true?'required':''} ${kind==='number'?'min="0"':''}>`;
  }

  return `<label class="${kind==='textarea'?'full':''}">${label}${input}</label>`;
 }).join('')+
 `<div class="form-actions">
  <button type="button" class="secondary" onclick="closeForm()">Cancel</button>
  <button class="primary">Save</button>
 </div>`;

 $('item-form').onsubmit=e=>saveForm(e,item?.id);
 $('modal').classList.add('show');
}

function editItem(type,id){
 const arr={
  project:projects,
  sprint:sprints,
  task:tasks,
  bug:bugs,
  test:tests
 }[type];

 const item=arr.find(x=>x.id===id);
 if(!item)return;

 openForm(type,item);
}

function closeForm(){
 $('modal').classList.remove('show');
}

async function saveForm(e,id=null){
 e.preventDefault();

 const data=Object.fromEntries(new FormData(e.target).entries());

 for(const k of ['projectId','sprintId','storyPoints','goalPoints','linkedBugId']){
  if(data[k]==='')data[k]=null;
  else if(data[k]!=null)data[k]=Number(data[k]);
 }

 try{
  await request('/'+configs[currentType].path+(id?'/'+id:''),id?'PUT':'POST',data);
  closeForm();
  await loadAll();
  toast('Saved successfully');
 }catch(err){
  toast(err.message);
 }
}

// DELETE PERMISSIONS:
// Projects and Sprints: Admin only.
// Bugs and Tests: Admin or Project Manager.
async function removeItem(path,id){
 if((path==='projects'||path==='sprints')&&!isAdmin()){
  toast('Only Admin can delete projects and sprints');
  return;
 }

 if(!canDeleteRecords()){
  toast('Only Admin or Project Manager can delete records');
  return;
 }

 if(!confirm('Delete this record?'))return;

 try{
  await request('/'+path+'/'+id,'DELETE');
  await loadAll();
  toast('Record deleted');
 }catch(e){
  toast(e.message);
 }
}

async function changeStatus(path,id,status){
 const arr={tasks,bugs,tests}[path];
 const existing=arr?.find(x=>x.id===id);

 if(path==='tasks'&&(!existing||!canEditTask(existing))){
  toast('You can update only tasks assigned to you');
  return;
 }

 if(path==='tests'&&!canManageTests()){
  toast('You do not have permission to execute tests');
  return;
 }

 const item=arr.find(x=>x.id===id);

 if(!item){
  toast('Item not found');
  return;
 }

 const updatedItem={...item,status};

 // Automatically update actual result for test cases.
 if(path==='tests'){
  if(status==='Passed'){
   updatedItem.actualResult='Test executed successfully';
  }else if(status==='Failed'){
   updatedItem.actualResult='Test execution failed';
  }else if(status==='Not Run'){
   updatedItem.actualResult='Pending execution';
  }else if(status==='Blocked'){
   updatedItem.actualResult='Test execution blocked';
  }
 }

 try{
  await request('/'+path+'/'+id,'PUT',updatedItem);
  await loadAll();
  toast('Status updated');
 }catch(e){
  toast(e.message);
 }
}

async function showComments(id){
 try{
  const list=await request(`/bugs/${id}/comments`);
  const msg=prompt(
   'Bug comments:\\n'+
   (list.map(c=>`${c.author}: ${c.message}`).join('\\n')||'No comments yet.')+
   '\\n\\nEnter a new comment (leave blank to cancel):'
  );

  if(msg?.trim()){
   await request(`/bugs/${id}/comments`,'POST',{message:msg});
   await loadAll();
   toast('Comment added');
  }
 }catch(e){
  toast(e.message);
 }
}

function toast(msg){
 const t=$('toast');
 t.textContent=msg;
 t.classList.add('show');
 setTimeout(()=>t.classList.remove('show'),2600);
}

init();
