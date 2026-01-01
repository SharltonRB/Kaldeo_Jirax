import React, { useState, useEffect, createContext, useContext, useRef } from 'react';
import { LayoutDashboard, FolderKanban, ListTodo, Timer, Tags, UserCircle, Moon, Sun, Plus, Search, LogOut, ChevronRight, ChevronLeft,
CheckCircle2, AlertCircle, Clock, MoreVertical,
Calendar,
X,
ChevronDown,
ChevronUp,
ChevronsDown,
ChevronsUp,
Minus,
Filter,
ArrowRight,
Save,
Trash2,
Trello,
CalendarRange,
PlayCircle,
CheckSquare,
Layers,
AlertTriangle,
Pencil,
ArrowUpRight,
Type,
FileText,
List,
MinusCircle,
BarChart3,
MessageSquare,
Send,
Download,
Check,
Zap,
Rocket,
RotateCcw,
Archive} from 'lucide-react';

/***
 * =========================================================================================
 * TYPES & ENTITIES
 * =========================================================================================
 */

type User = {
  id: string;
  name: string;
  email: string;
  avatar?: string;
};

type Comment = {
  id: string;
  issueId: string;
  userId: string;
  userName: string;
  content: string;
  createdAt: string;
};

type Project = {
  id: string;
  key: string;
  name: string;
  description: string;
  issueCount: number;
};

type SprintStatus = 'PLANNED' | 'ACTIVE' | 'COMPLETED';

type Sprint = {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  status: SprintStatus;
  goal?: string;
};

type IssueStatus = 'BACKLOG' | 'SELECTED' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
type IssueType = 'STORY' | 'TASK' | 'BUG' | 'EPIC';

type Issue = {
  id: string;
  key: string; 
  title: string;
  description?: string;
  status: IssueStatus;
  priority: IssuePriority;
  type: IssueType;
  storyPoints?: number;
  sprintId?: string;
  projectId: string;
  assigneeId?: string;
  parentId?: string; 
  updatedAt?: string;
  comments?: Comment[];
};

type Label = {
  id: string;
  name: string;
  color: string;
};

/***
 * =========================================================================================
 * MOCK DATA
 * =========================================================================================
 */

const generateId = () => Math.random().toString(36).substr(2, 9);

const MOCK_USER: User = {
  id: 'u1',
  name: 'Alex Developer',
  email: 'alex@example.com',
};

const INITIAL_PROJECTS: Project[] = [
  { id: 'p1', key: 'PIT', name: 'Personal Issue Tracker', description: 'Personal task management system', issueCount: 20 },
  { id: 'p2', key: 'WEB', name: 'Website Redesign', description: 'Personal portfolio redesign', issueCount: 8 },
];

const INITIAL_SPRINTS: Sprint[] = [
  { id: 's1', name: 'Sprint 1: Core Features', startDate: '2023-10-01', endDate: '2023-10-14', status: 'COMPLETED' },
  { id: 's2', name: 'Sprint 2: UI/UX Polish', startDate: '2023-10-15', endDate: '2023-10-29', status: 'ACTIVE', goal: 'Implement Liquid Glass theme and refine visual components.' },
  { id: 's3', name: 'Sprint 3: Backend Integration', startDate: '2023-10-30', endDate: '2023-11-12', status: 'PLANNED', goal: 'Full connection with Spring Boot REST API.' },
];

const baseDate = new Date('2023-01-01').toISOString();

const INITIAL_ISSUES: Issue[] = [
  { id: 'e1', key: 'PIT-EPIC-1', title: 'System Foundations', status: 'IN_PROGRESS', priority: 'HIGH', type: 'EPIC', projectId: 'p1', storyPoints: 0, description: 'Epic for all base configuration and initial infrastructure.', updatedAt: baseDate, comments: [] },
  { id: 'i1', key: 'PIT-1', title: 'Setup React Environment', status: 'DONE', priority: 'HIGH', type: 'TASK', projectId: 'p1', sprintId: 's1', storyPoints: 3, description: 'Initialize Vite with TypeScript.', parentId: 'e1', updatedAt: baseDate, comments: [] },
  { id: 'i101', key: 'PIT-101', title: 'Configure Tailwind CSS', status: 'DONE', priority: 'MEDIUM', type: 'TASK', projectId: 'p1', sprintId: 's1', storyPoints: 2, description: 'Install and configure preprocessor.', parentId: 'e1', updatedAt: baseDate, comments: [] },
  { id: 'i2', key: 'PIT-2', title: 'Design Glass Components', status: 'IN_PROGRESS', priority: 'CRITICAL', type: 'STORY', projectId: 'p1', sprintId: 's2', storyPoints: 8, description: 'Create base components.', parentId: 'e1', updatedAt: baseDate, comments: [{ id: 'c1', issueId: 'i2', userId: 'u1', userName: 'Alex Developer', content: 'Need to ensure blur works on Firefox.', createdAt: new Date().toISOString() }] },
  { id: 'e2', key: 'PIT-EPIC-2', title: 'Authentication & Security', status: 'SELECTED', priority: 'HIGH', type: 'EPIC', projectId: 'p1', storyPoints: 0, description: 'JWT security and login.', updatedAt: baseDate, comments: [] },
  { id: 'i3', key: 'PIT-3', title: 'Implement Auth JWT', status: 'IN_REVIEW', priority: 'HIGH', type: 'STORY', projectId: 'p1', sprintId: 's2', storyPoints: 5, description: 'Login endpoint.', parentId: 'e2', updatedAt: baseDate, comments: [] },
  { id: 'i4', key: 'PIT-4', title: 'Drag & drop bug', status: 'SELECTED', priority: 'MEDIUM', type: 'BUG', projectId: 'p1', sprintId: 's2', storyPoints: 2, description: 'Fix kanban drop.', parentId: 'e1', updatedAt: baseDate, comments: [] },
  { id: 'i105', key: 'PIT-105', title: 'Design DB Schema', status: 'BACKLOG', priority: 'HIGH', type: 'TASK', projectId: 'p1', storyPoints: 5, description: 'Initial schema.', parentId: 'e2', updatedAt: baseDate, comments: [] },
  { id: 'i106', key: 'PIT-106', title: 'API Endpoint: Users', status: 'BACKLOG', priority: 'HIGH', type: 'STORY', projectId: 'p1', storyPoints: 5, description: 'Users CRUD.', parentId: 'e2', updatedAt: baseDate, comments: [] },
  { id: 'i107', key: 'PIT-107', title: 'API Endpoint: Projects', status: 'BACKLOG', priority: 'MEDIUM', type: 'STORY', projectId: 'p1', storyPoints: 3, description: 'Projects CRUD.', parentId: 'e2', updatedAt: baseDate, comments: [] },
];

const INITIAL_LABELS: Label[] = [
  { id: 'l1', name: 'Frontend', color: 'bg-blue-500' },
  { id: 'l2', name: 'Backend', color: 'bg-green-500' },
  { id: 'l3', name: 'Design', color: 'bg-purple-500' },
  { id: 'l4', name: 'Urgent', color: 'bg-red-500' },
];

const formatDate = (dateStr: string) => {
  if (!dateStr) return 'N/A';
  try {
    const date = new Date(dateStr);
    const userTimezoneOffset = date.getTimezoneOffset() * 60000;
    const adjustedDate = new Date(date.getTime() + userTimezoneOffset);
    return adjustedDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
};

const formatTimeAgo = (dateStr: string) => {
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    if (diffInSeconds < 60) return 'just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} h ago`;
    return formatDate(dateStr);
  } catch (e) {
    return dateStr;
  }
};

/***
 * =========================================================================================
 * CONTEXT & STATE MANAGEMENT
 * =========================================================================================
 */

interface AppState {
  user: User | null;
  theme: 'light' | 'dark';
  currentView: 'auth' | 'dashboard' | 'projects' | 'sprints' | 'kanban' | 'profile';
  projects: Project[];
  sprints: Sprint[];
  issues: Issue[];
  labels: Label[];
  isSidebarCollapsed: boolean;
  selectedIssueId: string | null;
  isCreateIssueModalOpen: boolean; 
  issueHistory: string[];
  createIssueInitialData: Partial<Issue> | null;
  searchQuery: string;
}

interface AppContextType extends AppState {
  login: (email: string) => void;
  logout: () => void;
  toggleTheme: () => void;
  navigate: (view: AppState['currentView']) => void;
  toggleSidebar: () => void;
  setSearchQuery: (query: string) => void;
  setSelectedIssueId: (id: string | null) => void;
  setCreateIssueModalOpen: (isOpen: boolean) => void;
  setCreateIssueInitialData: (data: Partial<Issue> | null) => void;
  addIssue: (issue: Partial<Issue>) => void;
  updateIssue: (issue: Issue) => void;
  deleteIssue: (issueId: string) => void; 
  updateIssueStatus: (issueId: string, status: IssueStatus) => void;
  createProject: (project: Partial<Project>) => void;
  deleteProject: (projectId: string) => void;
  createSprint: (sprint: Partial<Sprint>) => void;
  updateSprint: (sprint: Sprint) => void;
  deleteSprint: (sprintId: string) => void;
  addComment: (issueId: string, content: string) => void;
  completeSprint: (sprintId: string) => void;
  startSprint: (sprintId: string, newStartDate?: string, newEndDate?: string) => void;
  navigateToIssue: (id: string) => void;
  goBackIssue: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [theme, setTheme] = useState<'light' | 'dark'>('light'); 
  const [currentView, setCurrentView] = useState<AppState['currentView']>('auth');
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [selectedIssueId, setSelectedIssueId] = useState<string | null>(null);
  const [isCreateIssueModalOpen, setCreateIssueModalOpen] = useState(false);
  const [createIssueInitialData, setCreateIssueInitialData] = useState<Partial<Issue> | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [issueHistory, setIssueHistory] = useState<string[]>([]);

  const [projects, setProjects] = useState<Project[]>(INITIAL_PROJECTS);
  const [sprints, setSprints] = useState<Sprint[]>(INITIAL_SPRINTS);
  const [issues, setIssues] = useState<Issue[]>(INITIAL_ISSUES);
  const [labels, setLabels] = useState<Label[]>(INITIAL_LABELS);

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const login = (email: string) => {
    setTimeout(() => {
      setUser({ ...MOCK_USER, email });
      setCurrentView('dashboard');
    }, 800);
  };

  const logout = () => {
    setUser(null);
    setCurrentView('auth');
  };

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');
  const navigate = (view: AppState['currentView']) => setCurrentView(view);
  const toggleSidebar = () => setIsSidebarCollapsed(prev => !prev);

  const handleSetSelectedIssueId = (id: string | null) => {
    if (id === null) setIssueHistory([]); 
    setSelectedIssueId(id);
  };

  const navigateToIssue = (id: string) => {
    if (selectedIssueId && selectedIssueId !== id) {
      setIssueHistory(prev => [...prev, selectedIssueId]);
      setSelectedIssueId(id);
    }
  };

  const goBackIssue = () => {
    setIssueHistory(prev => {
      const newHistory = [...prev];
      const lastId = newHistory.pop();
      if (lastId) {
        setSelectedIssueId(lastId);
      }
      return newHistory;
    });
  };

  // Helper function to check if an epic should be marked as DONE
  const checkEpicAutoCompletion = (issuesList: Issue[], triggerIssueId: string) => {
    const triggerIssue = issuesList.find(i => i.id === triggerIssueId);
    if (!triggerIssue || !triggerIssue.parentId) return issuesList;

    const parentId = triggerIssue.parentId;
    const children = issuesList.filter(i => i.parentId === parentId);

    // Safety check: ensure children exist
    if (children.length === 0) return issuesList;

    const allDone = children.every(i => i.status === 'DONE');
    if (allDone) {
      // Find parent and see if it needs update
      const parent = issuesList.find(i => i.id === parentId);
      if (parent && parent.status !== 'DONE' && parent.type === 'EPIC') {
        return issuesList.map(i => 
          i.id === parentId ? { ...i, status: 'DONE' as IssueStatus, updatedAt: new Date().toISOString() } : i
        );
      }
    }

    return issuesList;
  };

  const addIssue = (issueData: Partial<Issue>) => {
    const project = projects.find(p => p.id === issueData.projectId);
    const keyNum = (project?.issueCount || 0) + 1;

    if (project) {
      setProjects(prev => prev.map(p => 
        p.id === project.id ? {...p, issueCount: keyNum} : p
      ));
    }

    const newIssue: Issue = {
      id: generateId(),
      key: `${project?.key || 'UNK'}-${keyNum}`,
      title: issueData.title || 'Untitled',
      description: issueData.description || '',
      status: issueData.status || 'BACKLOG',
      priority: issueData.priority || 'MEDIUM',
      type: issueData.type || 'TASK',
      projectId: issueData.projectId || 'p1',
      sprintId: issueData.sprintId,
      storyPoints: issueData.storyPoints || 0,
      parentId: issueData.parentId,
      updatedAt: new Date().toISOString(),
      comments: []
    };

    setIssues(prev => [newIssue, ...prev]);
  };

  const updateIssue = (updatedIssue: Issue) => {
    setIssues(prev => {
      const issueWithTimestamp = { ...updatedIssue, updatedAt: new Date().toISOString() };
      const updatedList = prev.map(i => i.id === updatedIssue.id ? issueWithTimestamp : i);
      return checkEpicAutoCompletion(updatedList, updatedIssue.id);
    });
  };

  const deleteIssue = (issueId: string) => {
    setIssues(prev => {
      const issueToDelete = prev.find(i => i.id === issueId);
      const parentId = issueToDelete?.parentId;
      const nextIssues = prev.filter(i => i.id !== issueId && i.parentId !== issueId);

      // If we deleted a child, check if the parent Epic is now complete (remaining siblings all DONE)
      if (parentId) {
        const siblings = nextIssues.filter(i => i.parentId === parentId);
        if (siblings.length > 0 && siblings.every(i => i.status === 'DONE')) {
          return nextIssues.map(i => 
            i.id === parentId && i.type === 'EPIC' && i.status !== 'DONE'
              ? { ...i, status: 'DONE' as IssueStatus, updatedAt: new Date().toISOString() } 
              : i
          );
        }
      }

      return nextIssues;
    });
  };

  const updateIssueStatus = (id: string, status: IssueStatus) => {
    setIssues(prev => {
      const updatedList = prev.map(i => 
        i.id === id ? { ...i, status, updatedAt: new Date().toISOString() } : i
      );
      return checkEpicAutoCompletion(updatedList, id);
    });
  };

  const createProject = (data: Partial<Project>) => {
    const newProject: Project = {
      id: generateId(),
      key: data.key || 'NEW',
      name: data.name || 'New Project',
      description: data.description || '',
      issueCount: 0
    };
    setProjects(prev => [...prev, newProject]);
  }

  const deleteProject = (projectId: string) => {
    setProjects(prev => prev.filter(p => p.id !== projectId));
    setIssues(prev => prev.filter(i => i.projectId !== projectId));
  };

  const createSprint = (data: Partial<Sprint>) => {
    const newSprint: Sprint = {
      id: generateId(),
      name: data.name || 'New Sprint',
      startDate: data.startDate || new Date().toISOString(),
      endDate: data.endDate || new Date().toISOString(),
      status: 'PLANNED',
      goal: data.goal || ''
    };
    setSprints(prev => [...prev, newSprint]);
  }

  const updateSprint = (updatedSprint: Sprint) => {
    setSprints(prev => {
      if (updatedSprint.status === 'ACTIVE') {
        return prev.map(s => {
          if (s.id === updatedSprint.id) return updatedSprint; 
          if (s.status === 'ACTIVE') return { ...s, status: 'PLANNED' };
          return s;
        });
      }
      return prev.map(s => s.id === updatedSprint.id ? updatedSprint : s);
    });
  };

  const deleteSprint = (sprintId: string) => {
    setSprints(prev => prev.filter(s => s.id !== sprintId));
    setIssues(prev => prev.map(issue => {
      if (issue.sprintId === sprintId) {
        const newStatus: IssueStatus = issue.status === 'DONE' ? 'DONE' : 'BACKLOG';
        return { 
          ...issue, 
          sprintId: undefined, 
          status: newStatus,
          updatedAt: new Date().toISOString() 
        };
      }
      return issue;
    }));
  };

  const startSprint = (sprintId: string, newStartDate?: string, newEndDate?: string) => {
    setSprints(prev => prev.map(s => {
      if (s.status === 'ACTIVE') return { ...s, status: 'PLANNED' };
      if (s.id === sprintId) {
        return { 
          ...s, 
          status: 'ACTIVE',
          startDate: newStartDate || s.startDate,
          endDate: newEndDate || s.endDate
        };
      }
      return s;
    }));

    setIssues(prev => prev.map(i => {
      if (i.sprintId === sprintId) {
        return { ...i, status: 'SELECTED', updatedAt: new Date().toISOString() };
      }
      return i;
    }));
  };

  const completeSprint = (sprintId: string) => {
    setIssues(prev => prev.map(issue => {
      if (issue.sprintId === sprintId && issue.status !== 'DONE') {
        return { 
          ...issue, 
          sprintId: undefined, 
          status: 'BACKLOG' as IssueStatus, 
          updatedAt: new Date().toISOString() 
        };
      }
      return issue;
    }));

    setSprints(prev => prev.map(s => 
      s.id === sprintId ? { ...s, status: 'COMPLETED' } : s
    ));
  };

  const addComment = (issueId: string, content: string) => {
    if (!user) return;

    const newComment: Comment = {
      id: generateId(),
      issueId,
      userId: user.id,
      userName: user.name,
      content,
      createdAt: new Date().toISOString()
    };

    setIssues(prev => prev.map(i => {
      if (i.id === issueId) {
        const currentComments = i.comments || [];
        return { 
          ...i, 
          comments: [...currentComments, newComment], 
          updatedAt: new Date().toISOString() 
        };
      }
      return i;
    }));
  };

  return (
    <AppContext.Provider value={{ 
      user, theme, currentView, projects, sprints, issues, labels, 
      isSidebarCollapsed, selectedIssueId, isCreateIssueModalOpen, 
      issueHistory, createIssueInitialData, searchQuery,
      login, logout, toggleTheme, navigate, toggleSidebar, setSearchQuery,
      setSelectedIssueId: handleSetSelectedIssueId, setCreateIssueModalOpen, setCreateIssueInitialData,
      navigateToIssue, goBackIssue,
      addIssue, updateIssue, deleteIssue, updateIssueStatus, 
      createProject, deleteProject, createSprint, updateSprint, deleteSprint, 
      addComment, completeSprint, startSprint
    }}>
      {children}
    </AppContext.Provider>
  );
};

const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used within AppProvider");
  return context;
};

/***
 * =========================================================================================
 * UI COMPONENTS
 * =========================================================================================
 */

const GlassCard = ({ children, className = '', onClick }: { children: React.ReactNode, className?: string, onClick?: () => void }) => (
  <div 
    onClick={onClick} 
    className={`relative overflow-hidden
      bg-gradient-to-br from-white/80 via-white/50 to-white/30 
      dark:from-[#1e293b]/90 dark:via-[#0f172a]/80 dark:to-[#020617]/70
      backdrop-blur-2xl border border-white/60 dark:border-white/10
      shadow-xl shadow-black/10 dark:shadow-black/20
      rounded-3xl
      transition-all duration-300
      ${onClick ? 'cursor-pointer hover:shadow-2xl hover:scale-[1.01] hover:brightness-105 active:scale-[0.99]' : ''}
      ${className}`}
  >
    {/* Subtle Shine Effect Overlay */}
    <div className="absolute inset-0 bg-gradient-to-tr from-white/0 via-white/10 to-white/0 pointer-events-none" />
    {children}
  </div>
);

const GlassButton = ({ children, variant = 'primary', onClick, className = '', type = 'button', disabled = false }: any) => {
  const base = "px-4 py-2 rounded-2xl font-medium transition-all duration-300 flex items-center justify-center gap-2 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100";
  const variants = {
    primary: "bg-blue-500/90 hover:bg-blue-600 text-white backdrop-blur-md shadow-lg shadow-blue-500/20",
    secondary: "bg-white/50 dark:bg-white/10 hover:bg-white/70 dark:hover:bg-white/20 text-gray-800 dark:text-white border border-white/20",
    ghost: "hover:bg-black/5 dark:hover:bg-white/5 text-gray-600 dark:text-gray-300",
    danger: "bg-red-500/80 text-white hover:bg-red-600",
    orange: "bg-orange-500 hover:bg-orange-600 text-white shadow-lg shadow-orange-500/30",
    green: "bg-green-500 hover:bg-green-600 text-white shadow-lg shadow-green-500/30"
  };

  return (
    <button 
      type={type} 
      onClick={onClick} 
      disabled={disabled} 
      className={`${base} ${variants[variant as keyof typeof variants]} ${className}`}
    >
      {children}
    </button>
  );
};

const GlassInput = (props: any) => (
  <input 
    {...props}
    className={`w-full px-4 py-3 rounded-2xl outline-none
      bg-white/40 dark:bg-[#020617]/50
      border border-gray-200 dark:border-white/10
      focus:border-blue-500/50 focus:bg-white/60 dark:focus:bg-[#020617]/80
      placeholder-gray-500 dark:placeholder-gray-500
      text-gray-800 dark:text-white
      transition-all duration-200
      shadow-sm
      ${props.className}`}
  />
);

const GlassTextArea = React.forwardRef<HTMLTextAreaElement, any>((props, ref) => (
  <textarea 
    ref={ref}
    {...props}
    className={`w-full px-4 py-3 rounded-2xl outline-none
      bg-white/40 dark:bg-[#020617]/50 
      border border-gray-200 dark:border-white/10
      focus:border-blue-500/50 focus:bg-white/60 dark:focus:bg-[#020617]/80
      placeholder-gray-500 dark:placeholder-gray-500
      text-gray-800 dark:text-white
      transition-all duration-200
      resize-none
      shadow-sm
      ${props.className}`}
  />
));

// --- SIMPLE MARKDOWN RENDERER ---
const parseInline = (text: string): React.ReactNode[] => {
  const parts = text.split(/(\*\*.*?\*\*|\*.*?\*|`.*?`|\[.*?\]\(.*?\))/g);
  
  return parts.map((part, index) => {
    if (part.startsWith('**') && part.endsWith('**')) 
      return <strong key={index} className="text-gray-900 dark:text-white font-bold">{part.slice(2, -2)}</strong>;
    if (part.startsWith('*') && part.endsWith('*')) 
      return <em key={index} className="italic">{part.slice(1, -1)}</em>;
    if (part.startsWith('`') && part.endsWith('`')) 
      return <code key={index} className="bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded px-1.5 py-0.5 text-sm font-mono text-pink-600 dark:text-pink-400">{part.slice(1, -1)}</code>;
    if (part.startsWith('[') && part.includes('](') && part.endsWith(')')) {
      const match = part.match(/\[(.*?)\]\((.*?)\)/);
      if (match) return <a key={index} href={match[2]} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline">{match[1]}</a>;
    }
    return part;
  });
};

const MarkdownRenderer = ({ content }: { content: string }) => {
  if (!content) return null;
  
  const lines = content.split('\n');
  
  return (
    <div className="prose prose-sm dark:prose-invert max-w-none text-gray-800 dark:text-gray-300">
      {lines.map((line, i) => {
        if (line.startsWith('# ')) 
          return <h1 key={i} className="text-2xl font-bold mb-3 mt-5 text-gray-900 dark:text-white">{parseInline(line.slice(2))}</h1>;
        if (line.startsWith('## ')) 
          return <h2 key={i} className="text-xl font-bold mb-2 mt-4 text-gray-900 dark:text-white">{parseInline(line.slice(3))}</h2>;
        if (line.startsWith('### ')) 
          return <h3 key={i} className="text-lg font-bold mb-2 mt-3 text-gray-900 dark:text-white">{parseInline(line.slice(4))}</h3>;
        if (line.startsWith('> ')) 
          return <blockquote key={i} className="border-l-4 border-gray-300 dark:border-gray-700 pl-4 italic my-2 text-gray-600 dark:text-gray-400">{parseInline(line.slice(2))}</blockquote>;
        if (line.match(/^(\*|-)\s/)) 
          return (
            <div key={i} className="flex gap-2 ml-2 mb-1">
              <span className="text-gray-400 mt-1.5">•</span>
              <span className="flex-1">{parseInline(line.replace(/^(\*|-)\s/, ''))}</span>
            </div>
          );
        if (line.startsWith('- [ ] ')) 
          return <div key={i} className="flex gap-2 ml-1 mb-1 items-start">
            <div className="w-4 h-4 border border-gray-300 rounded mt-1"></div>
            <span className="flex-1">{parseInline(line.slice(6))}</span>
          </div>;
        if (line.startsWith('- [x] ')) 
          return <div key={i} className="flex gap-2 ml-1 mb-1 items-start">
            <div className="w-4 h-4 bg-blue-500 border border-blue-500 rounded mt-1 flex items-center justify-center">
              <CheckSquare className="w-3 h-3 text-white" />
            </div>
            <span className="flex-1 line-through text-gray-500">{parseInline(line.slice(6))}</span>
          </div>;
        if (!line.trim()) return <div key={i} className="h-4" />;
        return <p key={i} className="mb-2 leading-relaxed">{parseInline(line)}</p>;
      })}
    </div>
  );
};

// --- MARKDOWN EDITOR COMPONENT ---
const MarkdownEditor = ({ value, onChange, placeholder, className }: { value: string, onChange: (val: string) => void, placeholder?: string, className?: string }) => {
  const [isEditing, setIsEditing] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (isEditing && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [isEditing]);

  if (isEditing) {
    return (
      <div className="relative group">
        <GlassTextArea 
          ref={textareaRef}
          value={value}
          onChange={(e: any) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`font-mono text-sm ${className}`}
          onBlur={() => setIsEditing(false)}
          autoFocus
        />
        <div className="absolute bottom-2 right-2 text-xs text-gray-400 pointer-events-none bg-white/50 dark:bg-black/50 px-2 py-1 rounded-xl backdrop-blur-sm">
          Supported Markdown: **Bold**, *Italic*, # Headers, - Lists, `Code`
        </div>
      </div>
    );
  }

  return (
    <div 
      onClick={() => setIsEditing(true)}
      className={`cursor-text min-h-[150px] whitespace-pre-wrap ${className} border border-transparent hover:border-blue-500/30 hover:bg-white/50 dark:hover:bg-[#0f172a]/50
        transition-all duration-200 rounded-2xl relative`}
    >
      {value ? (
        <MarkdownRenderer content={value} />
      ) : (
        <span className="text-gray-400 italic flex items-center gap-2">
          <Type className="w-4 h-4" /> {placeholder || 'Add description...'}
        </span>
      )}
    </div>
  );
};

const Badge = ({ children, color = 'blue' }: { children: React.ReactNode, color?: string }) => {
  const colorClasses: Record<string, string> = {
    blue: 'bg-blue-500/10 text-blue-700 dark:text-blue-300 border-blue-500/20',
    green: 'bg-green-500/10 text-green-700 dark:text-green-300 border-green-500/20',
    red: 'bg-red-500/10 text-red-700 dark:text-red-300 border-red-500/20',
    yellow: 'bg-yellow-500/10 text-yellow-700 dark:text-yellow-300 border-yellow-500/20',
    gray: 'bg-gray-500/10 text-gray-700 dark:text-gray-300 border-gray-500/20',
    purple: 'bg-purple-500/10 text-purple-700 dark:text-purple-300 border-purple-500/20',
  };

  return (
    <span className={`px-2 py-0.5 text-xs font-semibold rounded-lg border ${colorClasses[color] || colorClasses.gray}`}>
      {children}
    </span>
  );
};
/***
 * =========================================================================================
 * FEATURE COMPONENTS (DEFINED BEFORE USE)
 * =========================================================================================
 */

// --- DELETE PROJECT CONFIRMATION MODAL ---
const DeleteProjectConfirmationModal = ({ isOpen, onClose, onConfirm, project }: { isOpen: boolean, onClose: () => void, onConfirm: () => void, project: Project | null }) => {
  if (!isOpen || !project) return null;

  return (
    <div className="absolute inset-0 z-[200] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in">
      <GlassCard className="w-full max-w-sm p-6 bg-white/95 dark:bg-[#09090b]/95 border-red-200 dark:border-red-900/30 shadow-2xl">
        <div className="text-center mb-6">
          <div className="w-14 h-14 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center mx-auto mb-4">
            <Trash2 className="w-7 h-7 text-red-600 dark:text-red-400" />
          </div>
          <h3 className="text-lg font-bold text-gray-800 dark:text-white">Delete Project?</h3>
          <p className="text-sm text-gray-500 mt-2">
            This will delete the project <span className="font-bold">"{project.name}"</span> and <strong className="text-red-500">ALL associated issues</strong>.
          </p>
          <p className="text-xs text-red-500 mt-2 font-semibold uppercase tracking-wider">This action is irreversible.</p>
        </div>
        <div className="space-y-3">
          <button 
            onClick={onConfirm}
            className="w-full py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-medium text-sm transition-colors flex items-center justify-center gap-2"
          >
            <Trash2 className="w-4 h-4" /> Yes, delete everything
          </button>
          <button 
            onClick={onClose}
            className="w-full py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
          >
            Cancel
          </button>
        </div>
      </GlassCard>
    </div>
  );
};

// --- DELETE CONFIRMATION MODAL (ISSUES) ---
const DeleteConfirmationModal = ({ isOpen, onClose, issue, onDelete, onMoveToBacklog }: { isOpen: boolean, onClose: () => void, issue: Issue | null, onDelete: () => void, onMoveToBacklog: () => void }) => {
  if (!isOpen || !issue) return null;

  const isBacklog = issue.status === 'BACKLOG';
  const isEpic = issue.type === 'EPIC';

  return (
    <div className="absolute inset-0 z-[200] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in">
      <GlassCard className="w-full max-w-sm p-6 bg-white/95 dark:bg-[#09090b]/95 border-red-200 dark:border-red-900/30 shadow-2xl">
        <div className="text-center mb-6">
          <div className="w-14 h-14 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center mx-auto mb-4">
            <Trash2 className="w-7 h-7 text-red-600 dark:text-red-400" />
          </div>
          <h3 className="text-lg font-bold text-gray-800 dark:text-white">
            {isBacklog ? 'Delete Issue' : 'Manage Deletion'}
          </h3>
          <p className="text-sm text-gray-500 mt-2">
            {isBacklog ? (
              isEpic ? "This will delete the Epic and ALL child issues. This action is irreversible." 
                     : "Are you sure you want to permanently delete this issue?"
            ) : (
              `The issue "${issue.key}" is not in the Backlog. What would you like to do?`
            )}
          </p>
        </div>
        <div className="space-y-3">
          {!isBacklog && (
            <button 
              onClick={onMoveToBacklog}
              className="w-full py-2.5 rounded-xl bg-blue-500 hover:bg-blue-600 text-white font-medium text-sm transition-colors flex items-center justify-center gap-2"
            >
              <RotateCcw className="w-4 h-4" /> Move to Backlog
            </button>
          )}
          <button 
            onClick={onDelete}
            className="w-full py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-medium text-sm transition-colors flex items-center justify-center gap-2"
          >
            <Trash2 className="w-4 h-4" /> Delete Permanently
          </button>
          <button 
            onClick={onClose}
            className="w-full py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
          >
            Cancel
          </button>
        </div>
      </GlassCard>
    </div>
  );
}

// --- SPRINT ACTIVATION MODAL ---
const SprintActivationModal = ({ isOpen, onClose, onConfirm, sprint }: { isOpen: boolean, onClose: () => void, onConfirm: (newEndDate: string) => void, sprint: Sprint }) => {
  if (!isOpen) return null;

  const today = new Date();
  const todayStr = today.toISOString().split('T')[0];
  const twoWeeksLater = new Date(today);
  twoWeeksLater.setDate(today.getDate() + 14);
  const twoWeeksLaterStr = twoWeeksLater.toISOString().split('T')[0];

  const [endDate, setEndDate] = useState(twoWeeksLaterStr);

  return (
    <div className="absolute inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in">
      <GlassCard className="w-full max-w-md p-6 bg-white/90 dark:bg-[#09090b]/90 border-white/20 shadow-2xl">
        <div className="flex flex-col items-center text-center mb-6">
          <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900/30 rounded-full flex items-center justify-center mb-4">
            <AlertTriangle className="w-8 h-8 text-yellow-600 dark:text-yellow-400" />
          </div>
          <h3 className="text-xl font-bold dark:text-white">Update Sprint Dates</h3>
          <p className="text-sm text-gray-500 mt-2">The planned start date does not match today.</p>
        </div>

        <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-900/30 rounded-xl p-4 mb-6 text-left space-y-4">
          <div>
            <label className="text-xs font-bold text-gray-500 uppercase block mb-1">New Start Date (Today)</label>
            <div className="font-mono text-gray-800 dark:text-white font-bold">{todayStr}</div>
          </div>
          <div>
            <label className="text-xs font-bold text-gray-500 uppercase block mb-1">End Date (Estimated)</label>
            <GlassInput 
              type="date" 
              value={endDate} 
              onChange={(e: any) => setEndDate(e.target.value)} 
              className="bg-white dark:bg-black/20"
            />
            <p className="text-[10px] text-gray-400 mt-1">Automatically calculated to 2 weeks, but you can modify it.</p>
          </div>
        </div>

        <div className="flex gap-3">
          <button 
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
          >
            Cancel
          </button>
          <button 
            onClick={() => onConfirm(endDate)}
            className="flex-1 py-2.5 rounded-xl bg-blue-500 hover:bg-blue-600 text-white shadow-lg shadow-blue-500/20 transition-all font-medium text-sm"
          >
            Confirm & Start
          </button>
        </div>
      </GlassCard>
    </div>
  );
};

// --- SPRINT COMPLETION CONFIRM MODAL ---
const SprintCompletionModal = ({ isOpen, onClose, onConfirm, warningDetails }: { isOpen: boolean, onClose: () => void, onConfirm: () => void, warningDetails: { unfinished: number, isEarly: boolean, endDate: string } }) => {
  if (!isOpen) return null;

  return (
    <div className="absolute inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in">
      <GlassCard className="w-full max-w-md p-6 bg-white/90 dark:bg-[#09090b]/90 border-white/20 shadow-2xl">
        <div className="flex flex-col items-center text-center mb-6">
          <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center mb-4">
            <CheckCircle2 className="w-8 h-8 text-blue-600 dark:text-blue-400" />
          </div>
          <h3 className="text-xl font-bold dark:text-white">Complete Sprint</h3>
          <p className="text-sm text-gray-500 mt-2">This action will finish the current sprint.</p>
        </div>

        {(warningDetails.isEarly || warningDetails.unfinished > 0) && (
          <div className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-900/30 rounded-xl p-4 mb-6 text-left">
            <h4 className="text-sm font-bold text-yellow-700 dark:text-yellow-500 flex items-center gap-2 mb-2">
              <AlertTriangle className="w-4 h-4" /> Warnings
            </h4>
            <ul className="text-xs text-yellow-800 dark:text-yellow-200 space-y-1 list-disc pl-4">
              {warningDetails.isEarly && (
                <li>Sprint ends on {formatDate(warningDetails.endDate)}, it's still early.</li>
              )}
              {warningDetails.unfinished > 0 && (
                <li>There are <strong>{warningDetails.unfinished} unfinished tickets</strong>. They will be moved to Backlog.</li>
              )}
            </ul>
          </div>
        )}

        <div className="flex gap-3">
          <button 
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
          >
            Cancel
          </button>
          <button 
            onClick={onConfirm}
            className="flex-1 py-2.5 rounded-xl bg-blue-500 hover:bg-blue-600 text-white shadow-lg shadow-blue-500/20 transition-all font-medium text-sm"
          >
            Confirm Completion
          </button>
        </div>
      </GlassCard>
    </div>
  );
};
// --- BACKLOG PICKER MODAL (MULTI-SELECT) ---
const BacklogPickerModal = ({ isOpen, onClose, onAdd, currentSprintId }: { isOpen: boolean, onClose: () => void, onAdd: (issues: Issue[]) => void, currentSprintId?: string }) => {
  const { projects, issues } = useApp();
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [expandedEpics, setExpandedEpics] = useState<string[]>([]);

  // --- MULTI-SELECT STATE ---
  const [selectedIssueIds, setSelectedIssueIds] = useState<Set<string>>(new Set());

  useEffect(() => {
    // Reset selections when modal opens/closes
    if (isOpen) {
      setSelectedIssueIds(new Set());
      setSelectedProjectId(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const availableIssues = issues.filter(i => i.sprintId !== currentSprintId && i.status !== 'DONE');
  const projectList = projects;
  const projectIssues = availableIssues.filter(i => i.projectId === selectedProjectId);
  const epics = projectIssues.filter(i => i.type === 'EPIC');
  const orphanIssues = projectIssues.filter(i => i.type !== 'EPIC' && !i.parentId);

  const toggleEpic = (id: string) => {
    setExpandedEpics(prev => prev.includes(id) ? prev.filter(e => e !== id) : [...prev, id]);
  };

  const toggleIssueSelection = (id: string) => {
    const newSet = new Set(selectedIssueIds);
    if (newSet.has(id)) newSet.delete(id);
    else newSet.add(id);
    setSelectedIssueIds(newSet);
  };

  const handleConfirmSelection = () => {
    const selectedIssuesList = issues.filter(i => selectedIssueIds.has(i.id));
    onAdd(selectedIssuesList);
    onClose();
  };

  const SelectionCheckbox = ({ id }: { id: string }) => {
    const isSelected = selectedIssueIds.has(id);
    return (
      <div className={`w-5 h-5 rounded border flex items-center justify-center transition-all ${
        isSelected ? 'bg-blue-500 border-blue-500' : 'border-gray-300 dark:border-gray-600 bg-white dark:bg-white/5'
      }`}>
        {isSelected && <Check className="w-3.5 h-3.5 text-white" />}
      </div>
    );
  };

  return (
    <div className="absolute inset-0 z-[60] flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in">
      <GlassCard className="w-full max-w-2xl h-[70vh] flex flex-col bg-white/95 dark:bg-[#09090b]/95 border-white/20 shadow-2xl">
        <div className="p-6 border-b border-gray-200 dark:border-white/10 flex justify-between items-center">
          <div>
            <h3 className="text-xl font-bold dark:text-white">Add from Backlog</h3>
            <p className="text-xs text-gray-500">Select one or more issues to add.</p>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 relative">
          {!selectedProjectId ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {projectList.map(p => (
                <button 
                  key={p.id}
                  onClick={() => setSelectedProjectId(p.id)}
                  className="p-4 rounded-xl border border-gray-200 dark:border-white/10 hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all text-left group"
                >
                  <h4 className="font-bold text-gray-800 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400">{p.name}</h4>
                  <p className="text-xs text-gray-500 mt-1">{p.key}</p>
                </button>
              ))}
            </div>
          ) : (
            <div className="space-y-4 pb-20">
              <button 
                onClick={() => setSelectedProjectId(null)}
                className="flex items-center gap-1 text-sm text-blue-500 hover:underline mb-4"
              >
                <ChevronLeft className="w-4 h-4" /> Switch Project
              </button>

              {epics.length === 0 && orphanIssues.length === 0 && (
                <p className="text-center text-gray-500 italic py-8">No available issues to add in this project.</p>
              )}

              {/* Epics List */}
              {epics.map(epic => {
                const children = projectIssues.filter(i => i.parentId === epic.id);
                const isExpanded = expandedEpics.includes(epic.id);

                return (
                  <div key={epic.id} className="border border-gray-200 dark:border-white/10 rounded-xl overflow-hidden">
                    <div 
                      onClick={() => toggleEpic(epic.id)}
                      className="p-3 bg-gray-50/50 dark:bg-white/5 flex items-center justify-between cursor-pointer hover:bg-gray-100 dark:hover:bg-white/10"
                    >
                      <div className="flex items-center gap-2">
                        <Layers className="w-4 h-4 text-purple-500" />
                        <span className="font-semibold text-gray-800 dark:text-gray-200">{epic.title}</span>
                        <span className="text-xs text-gray-500">({children.length})</span>
                      </div>
                      {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                    </div>

                    {isExpanded && (
                      <div className="divide-y divide-gray-100 dark:divide-white/5">
                        {children.length > 0 ? children.map(child => (
                          <div 
                            key={child.id}
                            onClick={() => toggleIssueSelection(child.id)}
                            className={`p-3 pl-10 cursor-pointer flex justify-between items-center group transition-colors
                              ${selectedIssueIds.has(child.id) ? 'bg-blue-50 dark:bg-blue-900/20' : 'hover:bg-gray-50 dark:hover:bg-white/5'}`}
                          >
                            <div className="flex items-center gap-3">
                              <SelectionCheckbox id={child.id} />
                              <div>
                                <div className="flex items-center gap-2">
                                  <Badge color="gray">{child.type}</Badge>
                                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{child.title}</span>
                                </div>
                                <div className="flex items-center gap-2 mt-1">
                                  <span className="text-xs text-gray-500 font-mono">{child.key}</span>
                                  {child.sprintId && <span className="text-[10px] bg-yellow-100 text-yellow-800 px-1 rounded border border-yellow-200">In another Sprint</span>}
                                </div>
                              </div>
                            </div>
                          </div>
                        )) : (
                          <div className="p-3 pl-10 text-xs text-gray-400 italic">No child issues available.</div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}

              {/* Orphan Issues */}
              {orphanIssues.length > 0 && (
                <div className="mt-6">
                  <h5 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Loose Issues</h5>
                  <div className="border border-gray-200 dark:border-white/10 rounded-xl overflow-hidden divide-y divide-gray-100 dark:divide-white/5">
                    {orphanIssues.map(issue => (
                      <div 
                        key={issue.id}
                        onClick={() => toggleIssueSelection(issue.id)}
                        className={`p-3 cursor-pointer flex justify-between items-center group transition-colors
                          ${selectedIssueIds.has(issue.id) ? 'bg-blue-50 dark:bg-blue-900/20' : 'hover:bg-gray-50 dark:hover:bg-white/5'}`}
                      >
                        <div className="flex items-center gap-3">
                          <SelectionCheckbox id={issue.id} />
                          <div>
                            <div className="flex items-center gap-2">
                              <Badge color="gray">{issue.type}</Badge>
                              <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{issue.title}</span>
                            </div>
                            <div className="flex items-center gap-2 mt-1">
                              <span className="text-xs text-gray-500 font-mono">{issue.key}</span>
                              {issue.sprintId && <span className="text-[10px] bg-yellow-100 text-yellow-800 px-1 rounded border border-yellow-200">In another Sprint</span>}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Floating Action Button for Adding */}
        <div className="p-4 border-t border-gray-200 dark:border-white/10 bg-white/50 dark:bg-[#09090b]/50 backdrop-blur-md flex justify-between items-center">
          <span className="text-sm text-gray-500">{selectedIssueIds.size} issues selected</span>
          <div className="flex gap-2">
            <GlassButton variant="ghost" onClick={onClose} className="text-xs">Cancel</GlassButton>
            <GlassButton 
              onClick={handleConfirmSelection} 
              disabled={selectedIssueIds.size === 0}
              className="px-6"
            >
              Add Selection
            </GlassButton>
          </div>
        </div>
      </GlassCard>
    </div>
  );
};

// --- COMMENT SECTION ---
const CommentsSection = ({ issueId, comments = [] }: { issueId: string, comments?: Comment[] }) => {
  const { addComment } = useApp();
  const [newComment, setNewComment] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (newComment.trim()) {
      addComment(issueId, newComment);
      setNewComment('');
    }
  };

  return (
    <div className="mt-8 pt-6 border-t border-gray-200 dark:border-white/10">
      <h4 className="text-sm font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
        <MessageSquare className="w-4 h-4" /> Comments ({comments.length})
      </h4>

      <div className="space-y-4 mb-6">
        {comments.map(comment => (
          <div key={comment.id} className="flex gap-3 animate-in fade-in slide-in-from-bottom-2">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold shrink-0">
              {comment.userName.substring(0, 2).toUpperCase()}
            </div>
            <div className="flex-1 bg-white/50 dark:bg-white/5 p-3 rounded-xl rounded-tl-none border border-gray-100 dark:border-white/5">
              <div className="flex justify-between items-center mb-1">
                <span className="font-semibold text-sm text-gray-800 dark:text-gray-200">{comment.userName}</span>
                <span className="text-[10px] text-gray-500">{formatTimeAgo(comment.createdAt)}</span>
              </div>
              <p className="text-sm text-gray-700 dark:text-gray-300">{comment.content}</p>
            </div>
          </div>
        ))}
        {comments.length === 0 && (
          <p className="text-sm text-gray-400 italic text-center py-4">No comments yet. Be the first.</p>
        )}
      </div>

      <form onSubmit={handleSubmit} className="flex gap-2 items-start">
        <GlassInput 
          value={newComment}
          onChange={(e: any) => setNewComment(e.target.value)}
          placeholder="Write a comment..."
          className="py-2.5 text-sm"
        />
        <GlassButton type="submit" disabled={!newComment.trim()} className="h-[42px] px-3">
          <Send className="w-4 h-4" />
        </GlassButton>
      </form>
    </div>
  );
};
// --- CREATE ISSUE MODAL ---
const CreateIssueModal = () => {
  const { isCreateIssueModalOpen, setCreateIssueModalOpen, addIssue, projects, issues, createIssueInitialData } = useApp();
  
  const [newIssue, setNewIssue] = useState<Partial<Issue>>({
    title: '',
    type: 'TASK',
    priority: 'MEDIUM',
    status: 'BACKLOG',
    projectId: projects[0]?.id || '',
    parentId: '',
    description: ''
  });

  useEffect(() => {
    if (isCreateIssueModalOpen) {
      if (createIssueInitialData) {
        setNewIssue(prev => ({
          ...prev,
          ...createIssueInitialData
        }));
      } else if (projects.length > 0 && !newIssue.projectId) {
        setNewIssue(prev => ({ ...prev, projectId: projects[0].id }));
      }
    }
  }, [isCreateIssueModalOpen, createIssueInitialData, projects]);

  if (!isCreateIssueModalOpen) return null;

  const projectEpics = issues.filter(i => i.type === 'EPIC' && i.projectId === newIssue.projectId);
  const isParentRequired = newIssue.type !== 'EPIC';
  const isFormValid = newIssue.title && (newIssue.type === 'EPIC' || newIssue.parentId);

  // Determine if we should restrict to Standard types (Task, Story, Bug)
  // We restrict if the modal was opened with a pre-defined parentId (Standard Issue flow)
  const isRestrictedToStandard = !!createIssueInitialData?.parentId;

  const handleSave = () => {
    if (isFormValid) {
      addIssue(newIssue);
      setCreateIssueModalOpen(false);
      setNewIssue({
        title: '',
        type: 'TASK',
        priority: 'MEDIUM',
        status: 'BACKLOG',
        projectId: projects[0]?.id || '',
        parentId: '',
        description: ''
      });
    }
  };

  return (
    <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm animate-in fade-in duration-200">
      <GlassCard className="w-full max-w-3xl p-6 bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl">
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-bold dark:text-white">Create New Issue</h3>
          <button onClick={() => setCreateIssueModalOpen(false)} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-xs text-gray-500 mb-1 block uppercase font-bold">Project</label>
              <select 
                value={newIssue.projectId}
                onChange={(e) => setNewIssue({...newIssue, projectId: e.target.value, parentId: ''})} 
                className="w-full appearance-none bg-white/30 dark:bg-[#1e293b]/50 border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500"
              >
                {projects.map(p => (
                  <option key={p.id} value={p.id}>{p.name} ({p.key})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-xs text-gray-500 mb-1 block uppercase font-bold">Type</label>
              <select 
                value={newIssue.type}
                onChange={(e) => setNewIssue({...newIssue, type: e.target.value as IssueType})}
                className="w-full appearance-none bg-white/30 dark:bg-[#1e293b]/50 border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500"
              >
                <option value="TASK">Task</option>
                <option value="STORY">Story</option>
                <option value="BUG">Bug</option>
                {!isRestrictedToStandard && <option value="EPIC">Epic</option>}
              </select>
            </div>
          </div>

          <GlassInput 
            placeholder="Issue Title" 
            value={newIssue.title} 
            onChange={(e: any) => setNewIssue({...newIssue, title: e.target.value})} 
            className="font-semibold text-lg"
          />

          <div>
            <label className="text-xs text-gray-500 mb-1 block uppercase font-bold">Description</label>
            <MarkdownEditor 
              value={newIssue.description || ''}
              onChange={(val) => setNewIssue({...newIssue, description: val})}
              placeholder="Describe the issue..."
              className="w-full min-h-[150px] bg-white/30 dark:bg-[#1e293b]/30 border border-gray-200 dark:border-white/10 p-4"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-xs text-gray-500 mb-1 block uppercase font-bold">Priority</label>
              <select 
                value={newIssue.priority}
                onChange={(e) => setNewIssue({...newIssue, priority: e.target.value as IssuePriority})}
                className="w-full appearance-none bg-white/30 dark:bg-[#1e293b]/50 border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            {isParentRequired && (
              <div className="animate-in fade-in slide-in-from-top-2 duration-200">
                <label className="text-xs text-gray-500 mb-1 block uppercase font-bold flex items-center gap-1">
                  Parent Epic <span className="text-red-500">*</span>
                </label>
                {projectEpics.length > 0 ? (
                  <div className="relative">
                    <select 
                      value={newIssue.parentId}
                      onChange={(e) => setNewIssue({...newIssue, parentId: e.target.value})}
                      className={`w-full appearance-none bg-white/30 dark:bg-[#1e293b]/50 border rounded-xl px-4 py-2.5 text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500 ${
                        !newIssue.parentId ? 'border-red-300 dark:border-red-900/50' : 'border-gray-200 dark:border-white/10'
                      }`}
                    >
                      <option value="">-- Select Epic --</option>
                      {projectEpics.map(epic => (
                        <option key={epic.id} value={epic.id}>{epic.key}: {epic.title}</option>
                      ))}
                    </select>
                    <ChevronDown className="absolute right-3 top-3 w-4 h-4 text-gray-500 pointer-events-none" />
                  </div>
                ) : (
                  <div className="p-3 bg-red-50/50 dark:bg-red-900/20 border border-red-100 dark:border-red-900/30 rounded-xl text-xs text-red-600 dark:text-red-400 flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4" />
                    No epics available.
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="flex justify-end gap-2 mt-6 pt-4 border-t border-gray-100/30 dark:border-white/5">
            <GlassButton variant="ghost" onClick={() => setCreateIssueModalOpen(false)}>
              Cancel
            </GlassButton>
            <GlassButton onClick={handleSave} disabled={!isFormValid}>
              Create Issue
            </GlassButton>
          </div>
        </div>
      </GlassCard>
    </div>
  );
};

// --- PRIORITY SELECTOR COMPONENT ---
const PrioritySelector = ({ value, onChange }: { value: IssuePriority, onChange: (val: IssuePriority) => void }) => {
  const priorities = [
    { id: 'LOW', label: 'Low', color: 'bg-blue-500', icon: ChevronsDown },
    { id: 'MEDIUM', label: 'Medium', color: 'bg-yellow-500', icon: Minus },
    { id: 'HIGH', label: 'High', color: 'bg-orange-500', icon: ChevronsUp },
    { id: 'CRITICAL', label: 'Critical', color: 'bg-red-600', icon: AlertTriangle },
  ];

  const selectedIndex = priorities.findIndex(p => p.id === value);

  return (
    <div className="relative flex w-full p-1 bg-gray-100/50 dark:bg-black/40 rounded-xl border border-gray-200/50 dark:border-white/10 isolate">
      <div 
        className="absolute top-1 bottom-1 left-1 bg-white dark:bg-[#1e293b] rounded-lg shadow-sm border border-gray-200/50 dark:border-white/10 transition-transform duration-300 ease-[cubic-bezier(0.23,1,0.32,1)] z-0"
        style={{
          width: `calc((100% - 0.5rem) / 4)`, 
          transform: `translateX(${selectedIndex * 100}%)`
        }}
      />
      {priorities.map((p) => {
        const isSelected = value === p.id;
        const Icon = p.icon;
        const activeColorClass = p.color.replace('bg-', 'text-');

        return (
          <button
            key={p.id}
            onClick={() => onChange(p.id as IssuePriority)}
            className={`relative z-10 flex-1 flex flex-col items-center justify-center gap-1 py-2 rounded-lg transition-all duration-300
              ${isSelected ? 'text-gray-800 dark:text-white' : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300'}`}
          >
            <Icon className={`w-5 h-5 transition-all duration-300 ${
              isSelected ? `${activeColorClass} scale-110` : 'scale-100'
            }`} />
            <span className={`text-[9px] sm:text-[10px] font-bold uppercase tracking-wide transition-opacity duration-300 ${
              isSelected ? 'opacity-100 font-extrabold' : 'opacity-70 font-medium'
            }`}>
              {p.label}
            </span>
          </button>
        );
      })}
    </div>
  );
};
// --- ISSUE DETAIL MODAL (EDITING) ---
const IssueDetailModal = () => {
  const { selectedIssueId, issues, sprints, setSelectedIssueId, updateIssue, deleteIssue, issueHistory, goBackIssue, navigateToIssue } = useApp();
  const [formData, setFormData] = useState<Issue | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    if (selectedIssueId) {
      const issue = issues.find(i => i.id === selectedIssueId);
      if (issue) setFormData({ ...issue });
    } else {
      setFormData(null);
    }
  }, [selectedIssueId, issues]);

  if (!selectedIssueId || !formData) return null;

  const projectEpics = issues.filter(i => i.type === 'EPIC' && i.projectId === formData.projectId && i.id !== formData.id);
  const isParentRequired = formData.type !== 'EPIC';
  const parentEpic = formData.parentId ? issues.find(i => i.id === formData.parentId) : null;

  const handleSave = () => {
    if (formData) {
      if (isParentRequired && !formData.parentId) {
        alert("Este tipo de issue debe pertenecer a una Épica.");
        return;
      }
      updateIssue(formData);
      setSelectedIssueId(null);
    }
  };

  const handleDeleteClick = () => {
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = () => {
    if (formData) {
      deleteIssue(formData.id);
      setShowDeleteModal(false);
      setSelectedIssueId(null);
    }
  };

  const handleMoveToBacklog = () => {
    if (formData) {
      updateIssue({ ...formData, status: 'BACKLOG', sprintId: undefined });
      setShowDeleteModal(false);
      setSelectedIssueId(null);
    }
  };

  return (
    <div className="absolute inset-0 z-[100] flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm animate-in fade-in duration-200">
      <GlassCard className="w-full max-w-5xl h-[85vh] overflow-hidden flex flex-col bg-white/95 dark:bg-[#09090b]/95 shadow-2xl border-gray-200 dark:border-white/10">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-white/10 flex justify-between items-center bg-white/50 dark:bg-[#09090b]/50 z-10 shrink-0">
          <div className="flex items-center gap-4 flex-1">
            {issueHistory.length > 0 && (
              <button 
                onClick={goBackIssue}
                className="flex items-center gap-1 text-xs text-blue-600 dark:text-blue-400 hover:underline bg-gray-100 dark:bg-white/10 px-3 py-1.5 rounded-lg transition-colors"
              >
                <ChevronLeft className="w-3 h-3" /> Back
              </button>
            )}
            <Badge color={formData.type === 'BUG' ? 'red' : formData.type === 'EPIC' ? 'purple' : 'blue'}>
              {formData.type}
            </Badge>
            <span className="text-sm font-mono text-gray-600 dark:text-gray-300">{formData.key}</span>
            {parentEpic && (
              <div 
                onClick={() => navigateToIssue(parentEpic.id)}
                className="hidden sm:flex items-center gap-2 px-3 py-1 bg-purple-50 dark:bg-purple-900/20 border border-purple-100 dark:border-purple-800/30 rounded-full cursor-pointer hover:bg-purple-100 dark:hover:bg-purple-900/40 transition-all"
              >
                <Layers className="w-3 h-3 text-purple-600 dark:text-purple-400" />
                <span className="text-xs text-purple-700 dark:text-purple-300 font-medium truncate max-w-[200px]">
                  {parentEpic.key}: {parentEpic.title}
                </span>
                <ArrowUpRight className="w-3 h-3 text-purple-500" />
              </div>
            )}
          </div>
          <div className="flex items-center gap-3">
            <GlassButton variant="danger" onClick={handleDeleteClick} className="text-xs px-3 py-1.5 h-8 opacity-90 hover:opacity-100">
              <Trash2 className="w-3 h-3" />
            </GlassButton>
            <GlassButton onClick={handleSave} className="text-xs px-3 py-1.5 h-8 opacity-90 hover:opacity-100">
              <Save className="w-3 h-3" /> Save
            </GlassButton>
            <button onClick={() => setSelectedIssueId(null)} className="p-2 hover:bg-gray-100 dark:hover:bg-white/10 rounded-full transition-colors ml-2">
              <X className="w-5 h-5 text-gray-600 dark:text-gray-300" />
            </button>
          </div>
        </div>

        <div className="flex flex-col md:flex-row h-full overflow-hidden">
          {/* Left Sidebar: Metadata */}
          <div className="w-full md:w-[320px] shrink-0 border-b md:border-b-0 md:border-r border-gray-200 dark:border-white/10 overflow-y-auto p-6 space-y-6 bg-gray-50/50 dark:bg-[#020617]/50">
            {/* Status */}
            <div>
              <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-2">Status</label>
              <div className="relative">
                <select 
                  value={formData.status}
                  onChange={(e) => setFormData({...formData, status: e.target.value as IssueStatus})}
                  className="w-full appearance-none bg-white dark:bg-[#1e293b] border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-sm text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500 transition-colors font-medium shadow-sm"
                >
                  <option value="BACKLOG">Backlog</option>
                  <option value="SELECTED">Selected</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="IN_REVIEW">In Review</option>
                  <option value="DONE">Done</option>
                </select>
                <ChevronDown className="absolute right-3 top-3 w-4 h-4 text-gray-500 pointer-events-none" />
              </div>
            </div>

            {/* Priority */}
            <div>
              <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-2">Priority</label>
              <PrioritySelector 
                value={formData.priority} 
                onChange={(val) => setFormData({...formData, priority: val})} 
              />
            </div>

            {/* Sprint */}
            <div>
              <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-2">Sprint</label>
              <div className="relative">
                <select 
                  value={formData.sprintId || ''}
                  onChange={(e) => setFormData({...formData, sprintId: e.target.value || undefined})}
                  className="w-full appearance-none bg-white dark:bg-[#1e293b] border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-sm text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500 transition-colors font-medium shadow-sm"
                >
                  <option value="">-- Backlog (No Sprint) --</option>
                  {sprints.filter(s => s.status !== 'COMPLETED').map(sprint => (
                    <option key={sprint.id} value={sprint.id}>
                      {sprint.status === 'ACTIVE' ? '🟢 ' : '📅 '} {sprint.name}
                    </option>
                  ))}
                </select>
                <ChevronDown className="absolute right-3 top-3 w-4 h-4 text-gray-500 pointer-events-none" />
              </div>
            </div>

            <div className="h-px bg-gray-200 dark:bg-white/10 w-full" />

            {isParentRequired && (
              <div>
                <label className="block text-[10px] font-bold text-gray-600 dark:text-gray-400 uppercase tracking-wider mb-2">Move to another Epic</label>
                <div className="relative">
                  <select 
                    value={formData.parentId || ''}
                    onChange={(e) => setFormData({...formData, parentId: e.target.value})}
                    className="w-full appearance-none bg-white dark:bg-[#1e293b] border border-gray-200 dark:border-white/10 rounded-xl px-4 py-2.5 text-sm text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500 transition-colors font-medium shadow-sm"
                  >
                    <option value="" disabled>-- Select --</option>
                    {projectEpics.map(epic => (
                      <option key={epic.id} value={epic.id}>{epic.key}: {epic.title}</option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-3 top-3 w-4 h-4 text-gray-500 pointer-events-none" />
                </div>
              </div>
            )}

            <div className="bg-white dark:bg-[#1e293b] p-4 rounded-xl space-y-3 border border-gray-200 dark:border-white/5 shadow-sm">
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                  <UserCircle className="w-4 h-4" />
                  <span>Assignee</span>
                </div>
                <span className="text-gray-900 dark:text-white font-medium text-xs">Alex D.</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                  <Tags className="w-4 h-4" />
                  <span>Points</span>
                </div>
                <input 
                  type="number" 
                  className="w-12 bg-gray-50 dark:bg-black/20 border border-gray-200 dark:border-white/10 rounded px-1 py-0.5 text-center text-gray-900 dark:text-white text-xs font-medium"
                  value={formData.storyPoints}
                  onChange={(e) => setFormData({...formData, storyPoints: parseInt(e.target.value) || 0})}
                />
              </div>
            </div>
          </div>

          {/* Right Main Content */}
          <div className="flex-1 overflow-y-auto p-6 md:p-8 bg-transparent">
            <div className="max-w-3xl mx-auto space-y-6">
              <textarea 
                value={formData.title} 
                onChange={(e) => setFormData({...formData, title: e.target.value})}
                rows={1}
                className="text-3xl font-bold bg-transparent outline-none text-gray-900 dark:text-white w-full placeholder-gray-400 resize-none overflow-hidden h-auto leading-tight p-2 -ml-2 rounded-lg hover:bg-gray-100 dark:hover:bg-white/5 transition-colors focus:bg-gray-100 dark:focus:bg-white/5"
                placeholder="Issue Title"
                onInput={(e: any) => {
                  e.target.style.height = 'auto';
                  e.target.style.height = e.target.scrollHeight + 'px';
                }}
              />

              <div className="relative group">
                <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2 ml-1">Description</label>
                <MarkdownEditor 
                  value={formData.description || ''}
                  onChange={(val) => setFormData({...formData, description: val})}
                  placeholder="Add a detailed description (Markdown supported)..."
                  className="w-full min-h-[200px] bg-gray-50 dark:bg-[#0f172a] border-transparent p-6 shadow-inner text-lg leading-relaxed text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/5"
                />
              </div>

              {/* COMMENTS SECTION */}
              <CommentsSection issueId={formData.id} comments={formData.comments} />
            </div>
          </div>
        </div>
      </GlassCard>

      <DeleteConfirmationModal 
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        issue={formData}
        onDelete={handleConfirmDelete}
        onMoveToBacklog={handleMoveToBacklog}
      />
    </div>
  );
};
// --- AUTH VIEW ---
const AuthView = () => {
  const { login, theme, toggleTheme } = useApp();
  const [isRegister, setIsRegister] = useState(false);
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setTimeout(() => {
      login(email);
      setLoading(false);
    }, 1500);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-gray-50 dark:bg-[#020617]">
      {/* Theme Toggle Button - Absolute Positioned */}
      <div className="absolute top-6 right-6 z-20">
        <button
          onClick={toggleTheme}
          className="p-3 rounded-full bg-white/50 dark:bg-black/50 backdrop-blur-md border border-white/20 shadow-lg text-gray-600 dark:text-gray-300 hover:scale-110 transition-all active:scale-95"
          title="Toggle Theme"
        >
          {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
        </button>
      </div>

      {/* Background blobs con tonos naranjas para el login neon */}
      <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-orange-500/20 rounded-full blur-[100px] animate-pulse" />
      <div className="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-amber-500/20 rounded-full blur-[100px] animate-pulse delay-1000" />

      <GlassCard className="w-full max-w-md p-8 z-10 m-4">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-orange-500 rounded-2xl mx-auto flex items-center justify-center mb-4 shadow-lg shadow-orange-500/30">
            {/* Nuevo icono Zap (Rayo) para Kirax */}
            <Zap className="text-white w-8 h-8" />
          </div>
          <h1 className="text-3xl font-bold text-gray-800 dark:text-white mb-2">Kirax</h1>
          <p className="text-gray-500 dark:text-gray-400">Manage your projects with clarity.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {isRegister && (
            <GlassInput placeholder="Full Name" required />
          )}
          <GlassInput 
            type="email" 
            placeholder="email@example.com" 
            value={email}
            onChange={(e: any) => setEmail(e.target.value)}
            required 
          />
          <GlassInput type="password" placeholder="Password" required />
          
          <GlassButton type="submit" variant="orange" className="w-full py-3 text-lg" disabled={loading}>
            {loading ? 'Processing...' : (isRegister ? 'Create Account' : 'Log In')}
          </GlassButton>
        </form>

        <div className="mt-6 text-center">
          <button
            onClick={() => setIsRegister(!isRegister)}
            className="text-sm text-gray-500 dark:text-gray-400 hover:text-orange-500 dark:hover:text-orange-400 transition-colors"
          >
            {isRegister ? 'Already have an account? Log In' : "Don't have an account? Sign Up"}
          </button>
        </div>
      </GlassCard>
    </div>
  );
};

// --- DASHBOARD ---
const Dashboard = () => {
  const { issues, sprints, projects, navigate, setSelectedIssueId, searchQuery } = useApp();
  const activeSprint = sprints.find(s => s.status === 'ACTIVE');

  // FILTRADO GLOBAL
  const filteredIssues = issues.filter(i => 
    i.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
    i.key.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const myIssues = filteredIssues.filter(i => i.status !== 'DONE').slice(0, 5);
  const activeSprintIssues = filteredIssues.filter(i => i.sprintId === activeSprint?.id);

  const priorityCounts = {
    CRITICAL: activeSprintIssues.filter(i => i.priority === 'CRITICAL').length,
    HIGH: activeSprintIssues.filter(i => i.priority === 'HIGH').length,
    MEDIUM: activeSprintIssues.filter(i => i.priority === 'MEDIUM').length,
    LOW: activeSprintIssues.filter(i => i.priority === 'LOW').length,
  };

  const totalActiveIssues = activeSprintIssues.length || 1;

  const recentIssues = [...filteredIssues].sort((a, b) => {
    const timeA = new Date(a.updatedAt || 0).getTime();
    const timeB = new Date(b.updatedAt || 0).getTime();
    return timeB - timeA;
  }).slice(0, 5);

  const stats = [
    { 
      label: 'Active Projects', 
      value: projects.length, 
      icon: FolderKanban, 
      color: 'text-blue-500', 
      bg: 'bg-blue-500/10',
      action: () => navigate('projects')
    },
    { 
      label: 'Current Sprint', 
      value: activeSprint ? 'Day 4/14' : 'Inactive', 
      icon: Timer, 
      color: 'text-green-500', 
      bg: 'bg-green-500/10',
      action: () => navigate('kanban')
    },
    { 
      label: 'My Tasks', 
      value: issues.filter(i => i.status !== 'DONE').length, 
      icon: CheckCircle2, 
      color: 'text-orange-500', 
      bg: 'bg-orange-500/10',
      action: () => navigate('kanban')
    },
  ];

  return (
    <div className="max-w-7xl mx-auto space-y-6 animate-in fade-in duration-500">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {stats.map((stat, idx) => (
          <GlassCard key={idx} onClick={stat.action} className="p-6 flex items-center gap-4 group">
            <div className={`p-4 rounded-xl ${stat.bg} group-hover:scale-110 transition-transform`}>
              <stat.icon className={`w-8 h-8 ${stat.color}`} />
            </div>
            <div>
              <p className="text-gray-500 dark:text-gray-400 text-sm">{stat.label}</p>
              <h3 className="text-2xl font-bold text-gray-800 dark:text-white flex items-center gap-2">
                {stat.value}
                <ArrowRight className="w-4 h-4 opacity-0 group-hover:opacity-100 transition-opacity -ml-2 group-hover:ml-0 text-gray-400" />
              </h3>
            </div>
          </GlassCard>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <GlassCard className="p-6 flex flex-col h-full lg:col-span-1" onClick={() => navigate('kanban')}>
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-bold text-gray-800 dark:text-white flex items-center gap-2">
              <Trello className="w-5 h-5 text-blue-500" />
              Current Sprint
            </h3>
            {activeSprint && <Badge color="green">Active</Badge>}
          </div>

          {activeSprint ? (
            <div className="space-y-4">
              <div className="flex justify-between text-sm text-gray-500">
                <span className="font-medium text-gray-700 dark:text-gray-300">{activeSprint.name}</span>
                <span>{formatDate(activeSprint.endDate)}</span>
              </div>
              <div className="h-3 w-full bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div className="h-full bg-blue-500 w-[45%]" />
              </div>
              <p className="text-xs text-gray-500 italic line-clamp-2">"{activeSprint.goal}"</p>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-gray-400 py-8">
              <Calendar className="w-12 h-12 mb-2 opacity-50" />
              <p>No active sprint</p>
            </div>
          )}
        </GlassCard>

        <GlassCard className="p-6 flex flex-col h-full lg:col-span-1">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-bold text-gray-800 dark:text-white flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-purple-500" />
              Priorities
            </h3>
          </div>

          {activeSprint ? (
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-600 dark:text-gray-400 font-medium">Critical</span>
                  <span className="text-gray-800 dark:text-white">{priorityCounts.CRITICAL}</span>
                </div>
                <div className="h-2 w-full bg-gray-100 dark:bg-white/5 rounded-full overflow-hidden">
                  <div className="h-full bg-red-500" style={{ width: `${(priorityCounts.CRITICAL / totalActiveIssues) * 100}%` }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-600 dark:text-gray-400 font-medium">High</span>
                  <span className="text-gray-800 dark:text-white">{priorityCounts.HIGH}</span>
                </div>
                <div className="h-2 w-full bg-gray-100 dark:bg-white/5 rounded-full overflow-hidden">
                  <div className="h-full bg-orange-500" style={{ width: `${(priorityCounts.HIGH / totalActiveIssues) * 100}%` }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-600 dark:text-gray-400 font-medium">Medium</span>
                  <span className="text-gray-800 dark:text-white">{priorityCounts.MEDIUM}</span>
                </div>
                <div className="h-2 w-full bg-gray-100 dark:bg-white/5 rounded-full overflow-hidden">
                  <div className="h-full bg-yellow-500" style={{ width: `${(priorityCounts.MEDIUM / totalActiveIssues) * 100}%` }} />
                </div>
              </div>
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-600 dark:text-gray-400 font-medium">Low</span>
                  <span className="text-gray-800 dark:text-white">{priorityCounts.LOW}</span>
                </div>
                <div className="h-2 w-full bg-gray-100 dark:bg-white/5 rounded-full overflow-hidden">
                  <div className="h-full bg-blue-500" style={{ width: `${(priorityCounts.LOW / totalActiveIssues) * 100}%` }} />
                </div>
              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-gray-400 py-8">
              <p className="text-xs">Require active sprint</p>
            </div>
          )}
        </GlassCard>

        <GlassCard className="p-6 lg:col-span-1">
          <div className="flex justify-between items-center mb-6">
            <h3 className="text-lg font-bold text-gray-800 dark:text-white">Recents</h3>
          </div>
          <div className="space-y-3">
            {recentIssues.map(issue => (
              <div 
                key={issue.id} 
                onClick={(e) => { e.stopPropagation(); setSelectedIssueId(issue.id); }}
                className="group flex items-center justify-between p-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
              >
                <div className="flex items-center gap-3 overflow-hidden">
                  <div className={`w-1.5 h-1.5 shrink-0 rounded-full ${
                    issue.priority === 'CRITICAL' ? 'bg-red-500' : 
                    issue.priority === 'HIGH' ? 'bg-orange-500' : 'bg-blue-500'
                  }`} />
                  <div className="truncate">
                    <p className="text-xs font-medium text-gray-800 dark:text-gray-200 truncate">{issue.title}</p>
                    <p className="text-[10px] text-gray-500">{issue.key} • {issue.status}</p>
                  </div>
                </div>
                <ChevronRight className="w-3 h-3 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity" />
              </div>
            ))}
            {recentIssues.length === 0 && <p className="text-gray-500 text-sm italic">No recent results.</p>}
          </div>
        </GlassCard>
      </div>
    </div>
  );
};
// --- PROJECTS VIEW (LIST & DETAIL) ---
const ProjectsList = () => {
  const { projects, issues, createProject, setSelectedIssueId, setCreateIssueModalOpen, setCreateIssueInitialData, searchQuery, deleteProject } = useApp();
  const [showModal, setShowModal] = useState(false);
  const [newProj, setNewProj] = useState({ name: '', key: '', description: '' });
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [expandedEpics, setExpandedEpics] = useState<string[]>([]);
  const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);
  const [creationWizardStep, setCreationWizardStep] = useState<'NONE' | 'TYPE' | 'PARENT'>('NONE');

  // Logic to separate projects
  const isProjectCompleted = (projectId: string) => {
    const projectIssues = issues.filter(i => i.projectId === projectId);
    // If no issues, consider it active (new project)
    if (projectIssues.length === 0) return false;
    // Completed if ALL issues are DONE
    return projectIssues.every(i => i.status === 'DONE');
  };

  const activeProjects = projects.filter(p => !isProjectCompleted(p.id));
  const completedProjects = projects.filter(p => isProjectCompleted(p.id));

  const toggleEpic = (epicId: string) => {
    setExpandedEpics(prev => prev.includes(epicId) ? prev.filter(id => id !== epicId) : [...prev, epicId]);
  };

  const startIssueCreation = () => {
    setCreationWizardStep('TYPE');
  };

  const handleTypeSelection = (isEpic: boolean) => {
    if (isEpic) {
      setCreateIssueInitialData({ type: 'EPIC', projectId: selectedProjectId || undefined });
      setCreateIssueModalOpen(true);
      setCreationWizardStep('NONE');
    } else {
      setCreationWizardStep('PARENT');
    }
  };

  const handleParentSelection = (epicId: string) => {
    setCreateIssueInitialData({ 
      type: 'TASK', 
      projectId: selectedProjectId || undefined,
      parentId: epicId 
    });
    setCreateIssueModalOpen(true);
    setCreationWizardStep('NONE');
  };

  const handleDeleteProjectConfirm = () => {
    if(projectToDelete) {
      deleteProject(projectToDelete.id);
      setProjectToDelete(null);
    }
  };

  const groupIssuesByStatus = (issuesList: Issue[]) => {
    return {
      backlog: issuesList.filter(i => ['BACKLOG', 'SELECTED'].includes(i.status)),
      inProgress: issuesList.filter(i => ['IN_PROGRESS', 'IN_REVIEW'].includes(i.status)),
      completed: issuesList.filter(i => i.status === 'DONE')
    };
  };

  // --- VISTA DE DETALLE ---
  if (selectedProjectId) {
    const project = projects.find(p => p.id === selectedProjectId);

    // Filter issues by Search Query
    const projectIssues = issues.filter(i => 
      i.projectId === selectedProjectId && 
      (i.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
       i.key.toLowerCase().includes(searchQuery.toLowerCase()))
    );

    const epics = projectIssues.filter(i => i.type === 'EPIC');
    const orphanIssues = projectIssues.filter(i => i.type !== 'EPIC' && !i.parentId);
    const groupedEpics = groupIssuesByStatus(epics);

    const renderEpicGroup = (title: string, epicList: Issue[], colorClass: string) => {
      if (epicList.length === 0) return null;

      return (
        <div className="mb-8">
          <h3 className={`text-sm font-bold uppercase tracking-wider mb-4 flex items-center gap-2 ${colorClass}`}>
            {title} <span className="bg-gray-200 dark:bg-white/10 px-2 py-0.5 rounded-full text-xs text-gray-600 dark:text-gray-400">{epicList.length}</span>
          </h3>
          <div className="space-y-4">
            {epicList.map(epic => {
              const childIssues = projectIssues.filter(i => i.parentId === epic.id);
              const isExpanded = expandedEpics.includes(epic.id);
              const groupedChildren = groupIssuesByStatus(childIssues);

              return (
                <GlassCard key={epic.id} className="overflow-hidden transition-all">
                  {/* Epic Header */}
                  <div 
                    onClick={() => toggleEpic(epic.id)}
                    className="p-4 flex items-center justify-between cursor-pointer hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className={`p-1.5 rounded bg-purple-500/20 text-purple-600 dark:text-purple-300`}>
                        <Layers className="w-4 h-4" />
                      </div>
                      <div>
                        <h4 className="font-bold text-gray-800 dark:text-white flex items-center gap-2">
                          {epic.title}
                          <div className="flex gap-1 ml-2">
                            <button 
                              onClick={(e) => { e.stopPropagation(); setSelectedIssueId(epic.id); }}
                              className="p-1 hover:bg-gray-200 dark:hover:bg-white/20 rounded-lg text-gray-500 transition-colors"
                              title="Editar Épica"
                            >
                              <Pencil className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </h4>
                        <div className="flex items-center gap-2 text-xs text-gray-500">
                          <span>{epic.key}</span>
                          <span>•</span>
                          <span>{childIssues.length} child issues</span>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge color={epic.status === 'DONE' ? 'green' : 'blue'}>
                        {epic.status}
                      </Badge>
                      {isExpanded ? <ChevronUp className="w-5 h-5 text-gray-400" /> : <ChevronDown className="w-5 h-5 text-gray-400" />}
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="bg-gray-50/50 dark:bg-black/20 border-t border-gray-100 dark:border-white/5 p-4 space-y-4">
                      {groupedChildren.backlog.length > 0 && (
                        <div>
                          <h5 className="text-[10px] uppercase font-bold text-gray-400 mb-2 pl-2">Backlog / Selected</h5>
                          <div className="space-y-1">
                            {groupedChildren.backlog.map(child => renderChildIssue(child))}
                          </div>
                        </div>
                      )}

                      {groupedChildren.inProgress.length > 0 && (
                        <div>
                          <h5 className="text-[10px] uppercase font-bold text-blue-500 mb-2 pl-2">In Progress</h5>
                          <div className="space-y-1">
                            {groupedChildren.inProgress.map(child => renderChildIssue(child))}
                          </div>
                        </div>
                      )}

                      {groupedChildren.completed.length > 0 && (
                        <div>
                          <h5 className="text-[10px] uppercase font-bold text-green-500 mb-2 pl-2">Completed</h5>
                          <div className="space-y-1">
                            {groupedChildren.completed.map(child => renderChildIssue(child))}
                          </div>
                        </div>
                      )}

                      {childIssues.length === 0 && (
                        <p className="text-center py-2 text-sm text-gray-400 italic">No issues in this epic.</p>
                      )}
                    </div>
                  )}
                </GlassCard>
              );
            })}
          </div>
        </div>
      );
    };

    const renderChildIssue = (child: Issue) => (
      <div 
        key={child.id}
        onClick={() => setSelectedIssueId(child.id)}
        className="flex items-center justify-between p-3 rounded-lg hover:bg-white dark:hover:bg-white/5 cursor-pointer ml-4 border-l-2 border-gray-200 dark:border-gray-700 hover:border-blue-500 transition-all bg-white/40 dark:bg-white/5"
      >
        <div className="flex items-center gap-3">
          <Badge color="gray">{child.type}</Badge>
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{child.title}</span>
        </div>
        <div className="flex items-center gap-4 text-xs text-gray-500">
          <span>{child.key}</span>
          <Badge color={child.status === 'DONE' ? 'green' : 'gray'}>
            {child.status}
          </Badge>
        </div>
      </div>
    );

    return (
      <div className="max-w-6xl mx-auto space-y-6 animate-in slide-in-from-right duration-300 pb-20">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <GlassButton variant="secondary" onClick={() => setSelectedProjectId(null)}>
              <ChevronLeft className="w-4 h-4" /> Back
            </GlassButton>
            <div>
              <h2 className="text-2xl font-bold text-gray-800 dark:text-white flex items-center gap-2">
                {project?.name} 
                <span className="text-sm font-normal text-gray-500 bg-gray-200 dark:bg-white/10 px-2 py-0.5 rounded-md">{project?.key}</span>
              </h2>
              <p className="text-gray-500">{project?.description}</p>
            </div>
          </div>
          <GlassButton onClick={startIssueCreation}>
            <Plus className="w-4 h-4" /> Create Issue
          </GlassButton>
        </div>

        {creationWizardStep === 'TYPE' && (
          <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
            <GlassCard className="w-full max-w-md p-8 bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl">
              <h3 className="text-xl font-bold text-center mb-6 dark:text-white">What kind of Issue is it?</h3>
              <div className="grid grid-cols-2 gap-4">
                <button 
                  onClick={() => handleTypeSelection(true)}
                  className="flex flex-col items-center justify-center gap-3 p-6 rounded-2xl border-2 border-purple-100 dark:border-purple-900/30 bg-purple-50/50 dark:bg-purple-900/10 hover:border-purple-500 hover:bg-purple-100 dark:hover:bg-purple-900/20 transition-all group"
                >
                  <div className="p-3 bg-purple-500 rounded-xl text-white shadow-lg shadow-purple-500/30 group-hover:scale-110 transition-transform">
                    <Layers className="w-8 h-8" />
                  </div>
                  <span className="font-bold text-purple-700 dark:text-purple-300">Epic</span>
                </button>
                <button 
                  onClick={() => handleTypeSelection(false)}
                  className="flex flex-col items-center justify-center gap-3 p-6 rounded-2xl border-2 border-blue-100 dark:border-blue-900/30 bg-blue-50/50 dark:bg-blue-900/10 hover:border-blue-500 hover:bg-blue-100 dark:hover:bg-blue-900/20 transition-all group"
                >
                  <div className="p-3 bg-blue-500 rounded-xl text-white shadow-lg shadow-blue-500/30 group-hover:scale-110 transition-transform">
                    <FileText className="w-8 h-8" />
                  </div>
                  <span className="font-bold text-blue-700 dark:text-blue-300">Standard Issue</span>
                </button>
              </div>
              <button 
                onClick={() => setCreationWizardStep('NONE')}
                className="mt-6 w-full py-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
              >
                Cancel
              </button>
            </GlassCard>
          </div>
        )}

        {creationWizardStep === 'PARENT' && (
          <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
            <GlassCard className="w-full max-w-md p-6 bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl max-h-[80vh] flex flex-col">
              <h3 className="text-xl font-bold text-center mb-2 dark:text-white">Select Parent Epic</h3>
              <p className="text-center text-sm text-gray-500 mb-6">This issue must belong to an Epic.</p>
              
              <div className="flex-1 overflow-y-auto space-y-2 pr-2">
                {epics.length > 0 ? epics.map(epic => (
                  <button 
                    key={epic.id}
                    onClick={() => handleParentSelection(epic.id)}
                    className="w-full text-left p-4 rounded-xl border border-gray-200 dark:border-white/10 hover:border-purple-500 hover:bg-purple-50 dark:hover:bg-purple-900/20 transition-all flex items-center justify-between group"
                  >
                    <div>
                      <div className="font-bold text-gray-800 dark:text-gray-200 group-hover:text-purple-700 dark:group-hover:text-purple-300">{epic.title}</div>
                      <div className="text-xs text-gray-500">{epic.key}</div>
                    </div>
                    <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-purple-500" />
                  </button>
                )) : (
                  <div className="text-center py-8 text-gray-400 border border-dashed border-gray-300 rounded-xl">
                    No epics available.<br/>Create an epic first.
                  </div>
                )}
              </div>
              
              <button 
                onClick={() => setCreationWizardStep('NONE')}
                className="mt-4 w-full py-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 border-t border-gray-100 dark:border-white/5 pt-4"
              >
                Cancel
              </button>
            </GlassCard>
          </div>
        )}

        <div className="space-y-4">
          {renderEpicGroup("Backlog", groupedEpics.backlog, "text-gray-500")}
          {renderEpicGroup("In Progress", groupedEpics.inProgress, "text-blue-500")}
          {renderEpicGroup("Completed", groupedEpics.completed, "text-green-500")}

          {epics.length === 0 && (
            <div className="text-center py-10 text-gray-400">No epics in this project yet.</div>
          )}

          {orphanIssues.length > 0 && (
            <div className="mt-8 pt-8 border-t border-red-200 dark:border-red-900/30">
              <div className="flex items-center gap-2 mb-3 text-red-500">
                <AlertTriangle className="w-5 h-5" />
                <h3 className="text-sm font-bold uppercase tracking-wider">Orphan Issues (Action Required)</h3>
              </div>
              <GlassCard className="divide-y divide-gray-100 dark:divide-white/5 border-red-200 dark:border-red-900/50">
                {orphanIssues.map(issue => (
                  <div 
                    key={issue.id}
                    onClick={() => setSelectedIssueId(issue.id)}
                    className="flex items-center justify-between p-4 cursor-pointer hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <Badge color="red">NO PARENT</Badge>
                      <span className="font-medium text-gray-800 dark:text-white">{issue.title}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-xs text-gray-500 font-mono">{issue.key}</span>
                      <ChevronRight className="w-4 h-4 text-gray-400" />
                    </div>
                  </div>
                ))}
              </GlassCard>
            </div>
          )}
        </div>
      </div>
    );
  }

  // --- VISTA DE LISTA (PRINCIPAL) ---
  const ProjectCard = ({ project }: { project: Project }) => (
    <div 
      onClick={() => setSelectedProjectId(project.id)}
      className="group flex items-center justify-between p-6 cursor-pointer hover:bg-blue-50/50 dark:hover:bg-white/5 transition-colors relative"
    >
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-bold text-lg shadow-lg shadow-blue-500/20 group-hover:scale-105 transition-transform">
          {project.key.substring(0, 2)}
        </div>
        <div>
          <h3 className="text-lg font-bold text-gray-800 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{project.name}</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md line-clamp-1">{project.description}</p>
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="text-right hidden sm:block">
          <span className="block text-2xl font-bold text-gray-800 dark:text-white">{project.issueCount}</span>
          <span className="text-xs text-gray-500 uppercase tracking-wide">Issues</span>
        </div>
        {/* Delete Button */}
        <button 
          onClick={(e) => { e.stopPropagation(); setProjectToDelete(project); }}
          className="w-8 h-8 rounded-full flex items-center justify-center text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all z-10"
          title="Delete Project"
        >
          <Trash2 className="w-4 h-4" />
        </button>
        <div className="w-8 h-8 rounded-full bg-gray-100 dark:bg-white/10 flex items-center justify-center group-hover:bg-blue-500 group-hover:text-white transition-all">
          <ChevronRight className="w-5 h-5" />
        </div>
      </div>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800 dark:text-white">Projects</h2>
        <GlassButton onClick={() => setShowModal(true)}>
          <Plus className="w-4 h-4" /> New Project
        </GlassButton>
      </div>

      {/* Active Projects Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 flex items-center gap-2">
          <FolderKanban className="w-5 h-5 text-blue-500" /> In Progress ({activeProjects.length})
        </h3>
        <GlassCard className="overflow-hidden">
          <div className="divide-y divide-gray-200 dark:divide-white/10">
            {activeProjects.length > 0 ? (
              activeProjects.map(project => <ProjectCard key={project.id} project={project} />)
            ) : (
              <div className="p-8 text-center text-gray-400">No active projects.</div>
            )}
          </div>
        </GlassCard>
      </div>

      {/* Completed Projects Section */}
      {completedProjects.length > 0 && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 flex items-center gap-2">
            <Archive className="w-5 h-5 text-green-500" /> Completed ({completedProjects.length})
          </h3>
          <GlassCard className="overflow-hidden opacity-70 hover:opacity-100 transition-opacity">
            <div className="divide-y divide-gray-200 dark:divide-white/10">
              {completedProjects.map(project => <ProjectCard key={project.id} project={project} />)}
            </div>
          </GlassCard>
        </div>
      )}

      {/* CREATE PROJECT MODAL */}
      {showModal && (
        <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <GlassCard className="w-full max-w-lg p-6 bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl">
            <h3 className="text-xl font-bold mb-4 dark:text-white">Create Project</h3>
            <div className="space-y-4">
              <GlassInput 
                placeholder="Project Name" 
                value={newProj.name} 
                onChange={(e:any) => setNewProj({...newProj, name: e.target.value})} 
              />
              <GlassInput 
                placeholder="KEY (e.g. WEB)" 
                value={newProj.key} 
                onChange={(e:any) => setNewProj({...newProj, key: e.target.value.toUpperCase()})} 
                maxLength={4} 
              />
              <textarea 
                className="w-full px-4 py-3 rounded-xl outline-none bg-white/50 dark:bg-black/20 border border-gray-200 dark:border-white/10 text-gray-800 dark:text-white"
                placeholder="Description"
                rows={3}
                value={newProj.description} 
                onChange={(e:any) => setNewProj({...newProj, description: e.target.value})}
              />
              <div className="flex justify-end gap-2 mt-4">
                <GlassButton variant="ghost" onClick={() => setShowModal(false)}>Cancel</GlassButton>
                <GlassButton onClick={() => { createProject(newProj); setShowModal(false); }}>Create</GlassButton>
              </div>
            </div>
          </GlassCard>
        </div>
      )}

      {/* DELETE PROJECT CONFIRMATION */}
      <DeleteProjectConfirmationModal 
        isOpen={!!projectToDelete} 
        onClose={() => setProjectToDelete(null)}
        onConfirm={handleDeleteProjectConfirm}
        project={projectToDelete}
      />
    </div>
  );
};
// --- SPRINT MANAGEMENT LIST ---
const SprintsList = () => {
  const { sprints, createSprint, updateSprint, navigate, issues, setSelectedIssueId, updateIssue, startSprint, deleteSprint } = useApp();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingSprint, setEditingSprint] = useState<Sprint | null>(null);
  const [newSprint, setNewSprint] = useState({ name: '', startDate: '', endDate: '', goal: '' });
  const [showBacklogPicker, setShowBacklogPicker] = useState(false);
  const [showActivationModal, setShowActivationModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const active = sprints.filter(s => s.status === 'ACTIVE');
  const planned = sprints.filter(s => s.status === 'PLANNED');
  const completed = sprints.filter(s => s.status === 'COMPLETED');

  const handleSprintClick = (sprint: Sprint) => {
    if (sprint.status === 'ACTIVE') {
      navigate('kanban');
    } else {
      setEditingSprint(sprint);
    }
  };

  const handleUpdateSprint = (updates: Partial<Sprint>) => {
    if (editingSprint) {
      updateSprint({ ...editingSprint, ...updates });
      setEditingSprint(prev => prev ? { ...prev, ...updates } : null);
    }
  };

  const handleRemoveIssueFromSprint = (issue: Issue) => {
    updateIssue({
      ...issue,
      sprintId: undefined,
      status: 'BACKLOG'
    });
  };

  const handleAddIssuesFromBacklog = (selectedIssues: Issue[]) => {
    if (editingSprint) {
      selectedIssues.forEach(issue => {
        updateIssue({
          ...issue,
          sprintId: editingSprint.id,
          status: 'SELECTED' 
        });
      });
    }
  };

  const handleActivateSprintClick = () => {
    if (!editingSprint) return;

    const today = new Date().toISOString().split('T')[0];
    const sprintStart = editingSprint.startDate.split('T')[0];

    if (today !== sprintStart) {
      setShowActivationModal(true);
    } else {
      // Fechas coinciden, iniciar directamente
      startSprint(editingSprint.id);
      setEditingSprint(null); // Cerrar modal
      navigate('kanban'); // Ir al tablero
    }
  };

  const handleConfirmActivation = (newEndDate: string) => {
    if (!editingSprint) return;

    const today = new Date().toISOString(); // Full ISO for DB
    // Fix endDate format just in case it is simple date
    let finalEndDate = newEndDate;
    if(newEndDate.length === 10) finalEndDate = new Date(newEndDate).toISOString();

    startSprint(editingSprint.id, today, finalEndDate);
    setShowActivationModal(false);
    setEditingSprint(null);
    navigate('kanban');
  };

  const handleDeleteSprint = () => {
    if (editingSprint) {
      deleteSprint(editingSprint.id);
      setEditingSprint(null);
      setShowDeleteConfirm(false);
    }
  };

  const SprintItem = ({ sprint }: { sprint: Sprint }) => (
    <GlassCard 
      onClick={() => handleSprintClick(sprint)}
      className="p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-l-4 border-l-blue-500 cursor-pointer hover:bg-white/80 dark:hover:bg-[#334155]/80"
    >
      <div>
        <div className="flex items-center gap-2 mb-1">
          <h4 className="font-bold text-gray-800 dark:text-white">{sprint.name}</h4>
          <Badge color={sprint.status === 'ACTIVE' ? 'green' : sprint.status === 'PLANNED' ? 'blue' : 'gray'}>
            {sprint.status}
          </Badge>
        </div>
        <p className="text-xs text-gray-500 mb-2">{formatDate(sprint.startDate)} - {formatDate(sprint.endDate)}</p>
        {sprint.goal && <p className="text-sm text-gray-600 dark:text-gray-300 italic">"{sprint.goal}"</p>}
      </div>
      <div className="flex gap-2">
        <GlassButton variant="ghost" className="text-xs">
          {sprint.status === 'ACTIVE' ? <ArrowRight className="w-4 h-4" /> : <Pencil className="w-4 h-4" />}
        </GlassButton>
      </div>
    </GlassCard>
  );

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800 dark:text-white">Sprint Management</h2>
        <GlassButton onClick={() => setShowCreateModal(true)}>
          <Plus className="w-4 h-4" /> Plan Sprint
        </GlassButton>
      </div>

      {/* Active Sprint Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 flex items-center gap-2">
          <PlayCircle className="w-5 h-5 text-green-500" /> Active Sprint
        </h3>
        {active.length > 0 ? active.map(s => <SprintItem key={s.id} sprint={s} />) : (
          <div className="p-4 border border-dashed border-gray-300 dark:border-gray-700 rounded-xl text-center text-gray-500">
            No active sprint currently.
          </div>
        )}
      </div>

      {/* Planned Sprints Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-blue-500" /> Planned
        </h3>
        {planned.length > 0 ? planned.map(s => <SprintItem key={s.id} sprint={s} />) : (
          <p className="text-sm text-gray-500">No future sprints planned.</p>
        )}
      </div>

      {/* Completed Sprints Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold text-gray-700 dark:text-gray-200 flex items-center gap-2">
          <CheckSquare className="w-5 h-5 text-gray-500" /> Completed
        </h3>
        <div className="opacity-70 hover:opacity-100 transition-opacity space-y-4">
          {completed.map(s => <SprintItem key={s.id} sprint={s} />)}
        </div>
      </div>

      {/* CREATE SPRINT MODAL */}
      {showCreateModal && (
        <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
          <GlassCard className="w-full max-w-lg p-6 bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl">
            <h3 className="text-xl font-bold mb-4 dark:text-white">Plan New Sprint</h3>
            <div className="space-y-4">
              <GlassInput 
                placeholder="Sprint Name" 
                value={newSprint.name} 
                onChange={(e:any) => setNewSprint({...newSprint, name: e.target.value})} 
              />
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs text-gray-500 mb-1 block">Start Date</label>
                  <GlassInput 
                    type="date" 
                    value={newSprint.startDate} 
                    onChange={(e:any) => setNewSprint({...newSprint, startDate: e.target.value})} 
                  />
                </div>
                <div>
                  <label className="text-xs text-gray-500 mb-1 block">End Date</label>
                  <GlassInput 
                    type="date" 
                    value={newSprint.endDate} 
                    onChange={(e:any) => setNewSprint({...newSprint, endDate: e.target.value})} 
                  />
                </div>
              </div>
              <GlassInput 
                placeholder="Sprint Goal (Optional)" 
                value={newSprint.goal} 
                onChange={(e:any) => setNewSprint({...newSprint, goal: e.target.value})} 
              />
              <div className="flex justify-end gap-2 mt-4">
                <GlassButton variant="ghost" onClick={() => setShowCreateModal(false)}>Cancel</GlassButton>
                <GlassButton onClick={() => { createSprint(newSprint); setShowCreateModal(false); }}>Create</GlassButton>
              </div>
            </div>
          </GlassCard>
        </div>
      )}

      {/* EDIT SPRINT MODAL */}
      {editingSprint && (
        <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in">
          <GlassCard className="w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col bg-white/80 dark:bg-[#09090b]/80 shadow-2xl border-white/20 backdrop-blur-2xl">
            <div className="p-6 border-b border-gray-200 dark:border-white/10 flex justify-between items-center">
              <h3 className="text-xl font-bold dark:text-white">Sprint Details</h3>
              <button onClick={() => setEditingSprint(null)} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* LEFT: FORM */}
                <div className="lg:col-span-1 space-y-6">
                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Name</label>
                    <GlassInput 
                      value={editingSprint.name} 
                      onChange={(e:any) => handleUpdateSprint({ name: e.target.value })} 
                    />
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Status</label>
                      <select 
                        value={editingSprint.status}
                        onChange={(e) => handleUpdateSprint({ status: e.target.value as SprintStatus })}
                        className="w-full appearance-none bg-white/50 dark:bg-[#1e293b] border border-gray-200 dark:border-white/10 rounded-xl px-4 py-3 text-gray-800 dark:text-gray-200 outline-none focus:border-blue-500"
                      >
                        <option value="PLANNED">Planned</option>
                        <option value="ACTIVE">Active</option>
                        <option value="COMPLETED">Completed</option>
                      </select>
                    </div>

                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="text-xs text-gray-500 mb-1 block">Start Date</label>
                        <GlassInput 
                          type="date" 
                          value={editingSprint.startDate.split('T')[0]} 
                          onChange={(e:any) => handleUpdateSprint({ startDate: e.target.value })} 
                        />
                      </div>
                      <div>
                        <label className="text-xs text-gray-500 mb-1 block">End Date</label>
                        <GlassInput 
                          type="date" 
                          value={editingSprint.endDate.split('T')[0]} 
                          onChange={(e:any) => handleUpdateSprint({ endDate: e.target.value })} 
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Goal</label>
                    <GlassTextArea 
                      rows={3} 
                      value={editingSprint.goal || ''} 
                      onChange={(e:any) => handleUpdateSprint({ goal: e.target.value })} 
                    />
                  </div>
                </div>

                {/* RIGHT: ISSUES LIST */}
                <div className="lg:col-span-2 flex flex-col h-full">
                  <div className="flex justify-between items-center mb-4">
                    <h4 className="font-bold text-gray-800 dark:text-white flex items-center gap-2">
                      <ListTodo className="w-5 h-5 text-blue-500" /> Issues in this Sprint
                    </h4>
                    <GlassButton onClick={() => setShowBacklogPicker(true)} className="text-xs px-3 h-8">
                      <Plus className="w-3 h-3" /> Add from Backlog
                    </GlassButton>
                  </div>

                  <div className="flex-1 space-y-2 bg-gray-50 dark:bg-[#020617] p-4 rounded-2xl border border-gray-200 dark:border-white/5 min-h-[300px] max-h-[400px] overflow-y-auto custom-scrollbar">
                    {issues.filter(i => i.sprintId === editingSprint.id).length > 0 ? (
                      issues.filter(i => i.sprintId === editingSprint.id).map(issue => (
                        <div 
                          key={issue.id} 
                          onClick={(e) => { e.stopPropagation(); setSelectedIssueId(issue.id); }}
                          className="flex items-center justify-between p-3 bg-white dark:bg-[#1e293b] rounded-xl border border-gray-100 dark:border-white/5 shadow-sm cursor-pointer hover:bg-blue-50 dark:hover:bg-blue-900/10 transition-colors group"
                        >
                          <div className="flex items-center gap-3">
                            <div className={`w-2 h-2 rounded-full ${issue.status === 'DONE' ? 'bg-green-500' : 'bg-blue-500'}`} />
                            <span className="font-medium text-gray-700 dark:text-gray-200 text-sm">{issue.title}</span>
                          </div>
                          <div className="flex items-center gap-3">
                            <Badge color="gray">{issue.status}</Badge>
                            <span className="text-xs text-gray-500 font-mono">{issue.key}</span>
                            <button 
                              onClick={(e) => {
                                e.stopPropagation();
                                handleRemoveIssueFromSprint(issue);
                              }}
                              className="p-1 hover:bg-red-100 dark:hover:bg-red-900/30 text-gray-400 hover:text-red-500 rounded transition-colors opacity-0 group-hover:opacity-100"
                              title="Remove from Sprint (Move to Backlog)"
                            >
                              <MinusCircle className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="h-full flex flex-col items-center justify-center text-gray-400 text-sm py-10">
                        <FolderKanban className="w-8 h-8 mb-2 opacity-50" />
                        No issues assigned to this sprint.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 dark:border-white/10 flex justify-between items-center">
              <div className="flex gap-2">
                {editingSprint.status === 'PLANNED' && (
                  <button 
                    onClick={handleActivateSprintClick}
                    className="flex items-center gap-2 px-4 py-2 bg-green-500 hover:bg-green-600 text-white rounded-xl shadow-lg shadow-green-500/20 font-medium transition-all active:scale-95"
                  >
                    <Rocket className="w-4 h-4" /> Start Sprint
                  </button>
                )}
                <button 
                  onClick={() => setShowDeleteConfirm(true)}
                  className="flex items-center gap-2 px-4 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-600 dark:text-red-400 rounded-xl font-medium transition-all active:scale-95"
                >
                  <Trash2 className="w-4 h-4" /> Delete
                </button>
              </div>
              <GlassButton onClick={() => setEditingSprint(null)}>Close</GlassButton>
            </div>
          </GlassCard>
        </div>
      )}

      {/* Delete Sprint Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="absolute inset-0 z-[100] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
          <GlassCard className="w-full max-w-sm p-6 bg-white/90 dark:bg-[#09090b]/90 border-red-200 dark:border-red-900/30 shadow-2xl">
            <div className="text-center mb-6">
              <div className="w-14 h-14 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center mx-auto mb-4">
                <Trash2 className="w-7 h-7 text-red-600 dark:text-red-400" />
              </div>
              <h3 className="text-lg font-bold text-gray-800 dark:text-white">Delete Sprint?</h3>
              <p className="text-sm text-gray-500 mt-2">This action will delete the sprint. Incomplete issues will return to the Backlog.</p>
            </div>
            <div className="flex gap-3">
              <button 
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
              >
                Cancel
              </button>
              <button 
                onClick={handleDeleteSprint}
                className="flex-1 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-medium text-sm transition-colors"
              >
                Delete
              </button>
            </div>
          </GlassCard>
        </div>
      )}

      {/* Backlog Picker for Planned Sprint */}
      <BacklogPickerModal 
        isOpen={showBacklogPicker} 
        onClose={() => setShowBacklogPicker(false)} 
        onAdd={handleAddIssuesFromBacklog}
        currentSprintId={editingSprint?.id}
      />

      {/* Activation Confirmation Modal */}
      {editingSprint && (
        <SprintActivationModal 
          isOpen={showActivationModal}
          onClose={() => setShowActivationModal(false)}
          onConfirm={handleConfirmActivation}
          sprint={editingSprint}
        />
      )}
    </div>
  );
};
// --- KANBAN BOARD (ACTIVE SPRINT ONLY) ---
const KanbanColumn = ({ status, issues, onDrop, onEdit }: { status: IssueStatus, issues: Issue[], onDrop: (id: string, status: IssueStatus) => void, onEdit: (id: string) => void }) => {
  const handleDragOver = (e: React.DragEvent) => e.preventDefault();
  const handleDrop = (e: React.DragEvent) => {
    const id = e.dataTransfer.getData('issueId');
    if(id) onDrop(id, status);
  };

  const statusColors = {
    'BACKLOG': 'border-gray-300 dark:border-gray-700',
    'SELECTED': 'border-blue-300 dark:border-blue-700',
    'IN_PROGRESS': 'border-yellow-300 dark:border-yellow-700',
    'IN_REVIEW': 'border-purple-300 dark:border-purple-700',
    'DONE': 'border-green-300 dark:border-green-700',
  };

  const statusTitles = {
    'BACKLOG': 'Backlog',
    'SELECTED': 'Selected',
    'IN_PROGRESS': 'In Progress',
    'IN_REVIEW': 'Review',
    'DONE': 'Done'
  };

  return (
    <div 
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      className={`flex-1 min-w-[250px] bg-white/40 dark:bg-[#1e293b]/40 rounded-xl p-3 border-t-4 ${statusColors[status]}`}
    >
      <div className="flex justify-between items-center mb-4 px-1">
        <h4 className="font-semibold text-gray-700 dark:text-gray-300 text-sm">{statusTitles[status]}</h4>
        <span className="text-xs bg-black/5 dark:bg-white/10 px-2 py-0.5 rounded-full">{issues.length}</span>
      </div>

      <div className="space-y-3 h-[calc(100%-2rem)] overflow-y-auto custom-scrollbar pb-10">
        {issues.map(issue => (
          <div 
            key={issue.id}
            draggable
            onDragStart={(e) => e.dataTransfer.setData('issueId', issue.id)}
            onClick={() => onEdit(issue.id)}
            className="bg-white/80 dark:bg-[#0f172a]/90 backdrop-blur-sm p-4 rounded-lg shadow-sm border border-white/40 dark:border-white/10 cursor-move hover:shadow-md transition-all active:cursor-grabbing group hover:bg-white dark:hover:bg-[#1e293b]"
          >
            <div className="flex justify-between items-start mb-2">
              <span className="text-xs font-mono text-gray-500">{issue.key}</span>
              <div className={`w-2 h-2 rounded-full ${
                issue.priority === 'CRITICAL' ? 'bg-red-500' : 
                issue.priority === 'HIGH' ? 'bg-orange-500' : 'bg-blue-400'
              }`} />
            </div>
            <p className="text-sm font-medium text-gray-800 dark:text-gray-200 mb-3 line-clamp-2">{issue.title}</p>
            <div className="flex justify-between items-center">
              <div className="flex gap-1">
                <span className="text-[10px] bg-gray-200 dark:bg-gray-700 px-1.5 py-0.5 rounded text-gray-600 dark:text-gray-300">{issue.type}</span>
              </div>
              {issue.storyPoints && (
                <span className="text-xs flex items-center gap-1 text-gray-500">
                  <div className="bg-gray-200 dark:bg-gray-700 rounded-full w-5 h-5 flex items-center justify-center text-[10px] font-bold">
                    {issue.storyPoints}
                  </div>
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const SprintBoard = () => {
  const { issues, sprints, updateIssueStatus, setSelectedIssueId, setCreateIssueModalOpen, setCreateIssueInitialData, completeSprint, updateIssue, searchQuery, navigate } = useApp();
  const activeSprint = sprints.find(s => s.status === 'ACTIVE');
  const [showAddMenu, setShowAddMenu] = useState(false);
  const [showBacklogPicker, setShowBacklogPicker] = useState(false);
  const [showConfirmComplete, setShowConfirmComplete] = useState(false);

  const getCompletionWarnings = () => {
    if (!activeSprint) return { unfinished: 0, isEarly: false, endDate: '' };

    const endDate = new Date(activeSprint.endDate);
    const now = new Date();
    const unfinishedCount = issues.filter(i => i.sprintId === activeSprint.id && i.status !== 'DONE').length;

    return {
      unfinished: unfinishedCount,
      isEarly: now < endDate,
      endDate: activeSprint.endDate
    };
  };

  const handleConfirmCompletion = () => {
    if (activeSprint) {
      completeSprint(activeSprint.id);
      setShowConfirmComplete(false);
    }
  };

  const handleAddIssueClick = () => {
    setShowAddMenu(!showAddMenu);
  };

  const handleCreateNew = () => {
    setCreateIssueInitialData({ sprintId: activeSprint?.id, status: 'SELECTED' });
    setCreateIssueModalOpen(true);
    setShowAddMenu(false);
  };

  const handleAddFromBacklog = () => {
    setShowBacklogPicker(true);
    setShowAddMenu(false);
  };

  const handleBacklogSelect = (selectedIssues: Issue[]) => {
    // Move issues to current sprint (Multi-Select)
    if (activeSprint) {
      selectedIssues.forEach(issue => {
        updateIssue({
          ...issue,
          sprintId: activeSprint.id,
          status: 'SELECTED'
        });
      });
      setShowBacklogPicker(false);
    }
  };

  // RENDER: NO ACTIVE SPRINT (OPAQUE OVERLAY)
  if (!activeSprint) return (
    <div className="relative h-[calc(100vh-140px)] flex flex-col items-center justify-center overflow-hidden rounded-2xl">
      {/* Fake Board Background to simulate opacity/blur */}
      <div className="absolute inset-0 grid grid-cols-4 gap-4 p-4 opacity-30 pointer-events-none filter blur-sm">
        {[1,2,3,4].map(i => (
          <div key={i} className="bg-gray-200 dark:bg-gray-800 rounded-xl h-full border-t-4 border-gray-300"></div>
        ))}
      </div>

      <div className="z-10 bg-white/80 dark:bg-[#0f172a]/90 backdrop-blur-md p-8 rounded-2xl shadow-2xl border border-white/20 text-center max-w-md animate-in zoom-in-95 duration-300">
        <div className="w-16 h-16 bg-blue-500/10 rounded-full flex items-center justify-center mx-auto mb-4">
          <FolderKanban className="w-8 h-8 text-blue-500" />
        </div>
        <h2 className="text-2xl font-bold text-gray-800 dark:text-white mb-2">Inactive Board</h2>
        <p className="text-gray-500 dark:text-gray-400 mb-6 leading-relaxed">
          There is no active sprint. You need to start a new one from the sprint planning to view the task board.
        </p>
        <GlassButton onClick={() => navigate('sprints')} className="w-full justify-center">
          Go to Sprints <ArrowRight className="w-4 h-4" />
        </GlassButton>
      </div>
    </div>
  );

  const sprintIssues = issues.filter(i => 
    i.sprintId === activeSprint.id &&
    (i.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
     i.key.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  const columns: IssueStatus[] = ['SELECTED', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

  return (
    <div className="h-[calc(100vh-140px)] flex flex-col relative">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 dark:text-white flex items-center gap-2">
            <Trello className="w-6 h-6 text-blue-500" /> {activeSprint.name}
          </h2>
          <p className="text-sm text-gray-500">Goal: {activeSprint.goal}</p>
        </div>
        <div className="flex gap-2 relative">
          <GlassButton variant="secondary" className="text-sm" onClick={() => setShowConfirmComplete(true)}>
            <CheckCircle2 className="w-4 h-4 text-green-500" /> Complete Sprint
          </GlassButton>
          <div className="relative">
            <GlassButton className="text-sm" onClick={handleAddIssueClick}>
              <Plus className="w-4 h-4" /> Add Issue <ChevronDown className="w-3 h-3 ml-1" />
            </GlassButton>
            {/* Add Menu Dropdown */}
            {showAddMenu && (
              <>
                <div className="fixed inset-0 z-10" onClick={() => setShowAddMenu(false)} />
                <div className="absolute right-0 top-full mt-2 w-48 bg-white dark:bg-[#1e293b] rounded-xl shadow-xl border border-gray-100 dark:border-white/10 z-20 overflow-hidden animate-in fade-in slide-in-from-top-2">
                  <button 
                    onClick={handleCreateNew} 
                    className="w-full text-left px-4 py-3 hover:bg-gray-50 dark:hover:bg-white/5 text-sm text-gray-700 dark:text-gray-200 flex items-center gap-2"
                  >
                    <Plus className="w-4 h-4" /> Create from scratch
                  </button>
                  <button 
                    onClick={handleAddFromBacklog} 
                    className="w-full text-left px-4 py-3 hover:bg-gray-50 dark:hover:bg-white/5 text-sm text-gray-700 dark:text-gray-200 flex items-center gap-2 border-t border-gray-100 dark:border-white/5"
                  >
                    <Download className="w-4 h-4" /> Bring from Backlog
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-x-auto flex gap-4 pb-4">
        {columns.map(status => (
          <KanbanColumn 
            key={status} 
            status={status} 
            issues={sprintIssues.filter(i => i.status === status)} 
            onDrop={updateIssueStatus}
            onEdit={setSelectedIssueId}
          />
        ))}
      </div>

      {/* Backlog Picker Modal (Multi-Select Supported) */}
      <BacklogPickerModal 
        isOpen={showBacklogPicker} 
        onClose={() => setShowBacklogPicker(false)} 
        onAdd={handleBacklogSelect}
        currentSprintId={activeSprint.id}
      />

      {/* Confirmation Modal */}
      <SprintCompletionModal 
        isOpen={showConfirmComplete}
        onClose={() => setShowConfirmComplete(false)}
        onConfirm={handleConfirmCompletion}
        warningDetails={getCompletionWarnings()}
      />
    </div>
  );
};
/***
 * =========================================================================================
 * LAYOUT PRINCIPAL
 * =========================================================================================
 */

const Sidebar = () => {
  const { navigate, currentView, logout, isSidebarCollapsed, toggleSidebar } = useApp();

  const menuItems = [
    { id: 'dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { id: 'projects', icon: FolderKanban, label: 'Projects' },
    { id: 'sprints', icon: CalendarRange, label: 'Sprints' }, 
    { id: 'kanban', icon: Trello, label: 'Active Board' },  
  ];

  return (
    <aside className={`h-full flex-shrink-0 flex flex-col bg-white/60 dark:bg-[#09090b]/80 backdrop-blur-xl border-r border-gray-200 dark:border-white/10 z-50
      transition-all duration-300 ease-in-out
      ${isSidebarCollapsed ? 'w-20' : 'w-64'}`}
    >
      <div className={`p-6 flex items-center gap-3 ${isSidebarCollapsed ? 'justify-center px-2' : ''}`}>
        <div className="w-8 h-8 min-w-[32px] bg-blue-500 rounded-lg flex items-center justify-center shadow-lg shadow-blue-500/20">
          <ListTodo className="text-white w-5 h-5" />
        </div>
        {!isSidebarCollapsed && (
          <h1 className="font-bold text-xl text-gray-800 dark:text-white tracking-tight whitespace-nowrap overflow-hidden animate-in fade-in duration-300">
            Kirax
          </h1>
        )}
      </div>

      <nav className="flex-1 px-3 space-y-2 mt-4">
        {menuItems.map(item => (
          <button
            key={item.id}
            onClick={() => navigate(item.id as AppState['currentView'])}
            title={isSidebarCollapsed ? item.label : ''}
            className={`w-full flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200
              ${currentView === item.id 
                ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400 font-medium' 
                : 'text-gray-600 dark:text-gray-400 hover:bg-black/5 dark:hover:bg-white/5'}
              ${isSidebarCollapsed ? 'justify-center' : ''}`}
          >
            <item.icon className="w-5 h-5 min-w-[20px]" />
            {!isSidebarCollapsed && <span className="whitespace-nowrap overflow-hidden animate-in fade-in">{item.label}</span>}
          </button>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-200 dark:border-white/10 flex flex-col gap-2">
        <button 
          onClick={toggleSidebar}
          className={`w-full flex items-center gap-3 px-3 py-2 rounded-xl text-gray-500 hover:bg-black/5 dark:hover:bg-white/5 transition-colors
            ${isSidebarCollapsed ? 'justify-center' : ''}`}
        >
          {isSidebarCollapsed ? <ChevronRight className="w-5 h-5" /> : <ChevronLeft className="w-5 h-5" />}
          {!isSidebarCollapsed && <span className="text-sm">Collapse</span>}
        </button>
        <button 
          onClick={logout}
          className={`w-full flex items-center gap-3 px-3 py-3 rounded-xl text-red-500 hover:bg-red-500/10 transition-colors
            ${isSidebarCollapsed ? 'justify-center' : ''}`}
          title="Logout"
        >
          <LogOut className="w-5 h-5 min-w-[20px]" />
          {!isSidebarCollapsed && <span className="whitespace-nowrap">Logout</span>}
        </button>
      </div>
    </aside>
  );
};

const TopBar = () => {
  const { user, theme, toggleTheme, searchQuery, setSearchQuery } = useApp();

  return (
    <header className="h-16 mb-8 flex items-center justify-between gap-4">
      <h2 className="text-sm text-gray-500 hidden md:block">
        Workspace / <span className="text-gray-800 dark:text-white font-medium">Personal</span>
      </h2>

      {/* Search Bar */}
      <div className="flex-1 max-w-md relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input 
          type="text" 
          placeholder="Search issues..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full bg-white dark:bg-[#1e293b] border border-gray-200 dark:border-white/10 rounded-xl py-2 pl-10 pr-4 text-sm outline-none focus:border-blue-500 text-gray-800 dark:text-white shadow-sm"
        />
      </div>

      <div className="flex items-center gap-4">
        <button 
          onClick={toggleTheme}
          className="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/10 text-gray-600 dark:text-gray-300 transition-colors"
        >
          {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
        </button>

        <div className="flex items-center gap-3 pl-4 border-l border-gray-200 dark:border-white/10">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium text-gray-800 dark:text-white">{user?.name}</p>
            <p className="text-xs text-gray-500">Developer</p>
          </div>
          <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 flex items-center justify-center text-white font-medium shadow-md shadow-blue-500/20">
            {user?.name?.substring(0,2).toUpperCase()}
          </div>
        </div>
      </div>
    </header>
  );
};

const MainLayout = ({ children }: { children: React.ReactNode }) => {
  const { isSidebarCollapsed } = useApp();

  return (
    <div className="flex h-screen w-full bg-[#f5f5f7] dark:bg-[#020617] text-gray-900 dark:text-gray-100 transition-colors duration-300 overflow-hidden">
      {/* Ambient Background Lights - Positioned absolutely within the container but behind content */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div className="absolute top-[-10%] left-[20%] w-[600px] h-[600px] bg-blue-500/5 dark:bg-blue-500/5 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[10%] w-[500px] h-[500px] bg-purple-500/5 dark:bg-purple-500/5 rounded-full blur-[120px]" />
      </div>

      {/* Sidebar is now a flex child */}
      <Sidebar />

      {/* Main content takes remaining space */}
      <main className="flex-1 relative flex flex-col h-full overflow-hidden z-10">
        {/* Scrollable Container */}
        <div className="flex-1 overflow-y-auto overflow-x-hidden p-6">
          <TopBar />
          {children}
        </div>

        {/* Modals live here, absolute to main */}
        <IssueDetailModal />
        <CreateIssueModal />
      </main>
    </div>
  );
};

/***
 * =========================================================================================
 * APP ROOT
 * =========================================================================================
 */

const AppContent = () => {
  const { currentView, user } = useApp();

  if (!user) return <AuthView />;

  const renderView = () => {
    switch (currentView) {
      case 'dashboard': return <Dashboard />;
      case 'projects': return <ProjectsList />;
      case 'sprints': return <SprintsList />;
      case 'kanban': return <SprintBoard />;
      default: return <Dashboard />;
    }
  };

  return (
    <MainLayout>
      {renderView()}
    </MainLayout>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}