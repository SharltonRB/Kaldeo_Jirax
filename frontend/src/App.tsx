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
Archive,
Eye,
EyeOff} from 'lucide-react';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { ToastProvider, useToast } from '@/context/ToastContext';
import { useAppProjects } from '@/hooks/useAppProjects';
import { useIssues, useCreateIssue, useUpdateIssue, useUpdateIssueStatus, useDeleteIssue } from '@/hooks/useIssues';
import { useSprints, useCreateSprint, useUpdateSprint, useDeleteSprint, useStartSprint, useCompleteSprint, useCompletedSprintIssues } from '@/hooks/useSprints';
import { useLabels } from '@/hooks/useLabels';
import { useDashboardMetrics, useRecentIssues, useActiveSprintSummary, useIssueDistribution } from '@/hooks/useDashboard';
import { useCreateComment } from '@/hooks/useComments';
import { sprintService } from '@/services/api/sprint.service';
import { IssueStatus } from '@/types';
import { FrontendIssue, handleApiError } from '@/utils/api-response';
import SprintCalendar from '@/components/ui/SprintCalendar';
import SimpleSessionWarning from '@/components/ui/SimpleSessionWarning';
import { initializeCSP } from '@/utils/csp-config';
import { sanitizeText, sanitizeHtml } from '@/utils/sanitization';

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

type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
type IssueType = 'STORY' | 'TASK' | 'BUG' | 'EPIC';

type Issue = FrontendIssue;

type Label = {
  id: string;
  name: string;
  color: string;
};

/***
 * =========================================================================================
 * MOCK DATA (REMOVED - NOW USING REAL API DATA)
 * =========================================================================================
 */

const generateId = () => Math.random().toString(36).substr(2, 9);

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
  login: (email: string) => void; // Keep for backward compatibility, but will be handled by AuthContext
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
  refetchIssues: () => Promise<any>;
  // Notification functions
  showSuccess: (title: string, message?: string) => void;
  showError: (title: string, message?: string) => void;
  showWarning: (title: string, message?: string) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Don't use useAuth here - it will be handled by AppProviderContent
  return <AppProviderContent>{children}</AppProviderContent>;
};

const AppProviderContent: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user: authUser, logout: authLogout } = useAuth();
  const { showSuccess, showError, showWarning } = useToast();
  const [theme, setTheme] = useState<'light' | 'dark'>('light'); 
  const [currentView, setCurrentView] = useState<AppState['currentView']>('dashboard');
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

  // Initialize theme from localStorage
  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | null;
    if (savedTheme) {
      setTheme(savedTheme);
    }
  }, []);
  const [selectedIssueId, setSelectedIssueId] = useState<string | null>(null);
  const [isCreateIssueModalOpen, setCreateIssueModalOpen] = useState(false);
  const [createIssueInitialData, setCreateIssueInitialData] = useState<Partial<Issue> | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [issueHistory, setIssueHistory] = useState<string[]>([]);

  // Use real API hooks instead of mock data
  const isAuthenticated = !!authUser;
  
  const { 
    projects = [], 
    createProject: createProjectAPI, 
    deleteProject: deleteProjectAPI,
    isLoading: projectsLoading 
  } = useAppProjects(isAuthenticated ? '' : undefined);
  
  const { 
    data: issues = [], 
    isLoading: issuesLoading,
    refetch: refetchIssues
  } = useIssues(isAuthenticated ? {} : undefined);
  
  const createIssueMutation = useCreateIssue();
  const updateIssueMutation = useUpdateIssue();
  const updateIssueStatusMutation = useUpdateIssueStatus();
  const deleteIssueMutation = useDeleteIssue();
  
  const { 
    data: sprints = [], 
    isLoading: sprintsLoading 
  } = useSprints(isAuthenticated);
  
  const createSprintMutation = useCreateSprint();
  const updateSprintMutation = useUpdateSprint();
  const deleteSprintMutation = useDeleteSprint();
  const startSprintMutation = useStartSprint();
  const completeSprintMutation = useCompleteSprint();
  
  const { 
    data: labels = [], 
    isLoading: labelsLoading 
  } = useLabels(isAuthenticated);

  // Add comment mutation
  const createCommentMutation = useCreateComment();

  // Convert auth user to app user format
  const user = authUser ? {
    id: authUser.id,
    name: authUser.name,
    email: authUser.email,
    avatar: authUser.avatar
  } : null;

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const login = (email: string) => {
    // This is kept for backward compatibility but actual login is handled by AuthContext
    // The AuthContext will automatically update the user state
    console.log('Login called with email:', email);
  };

  const logout = () => {
    authLogout();
    setCurrentView('auth');
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };
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

  const addIssue = async (issueData: Partial<Issue>) => {
    try {
      await createIssueMutation.mutateAsync(issueData as any);
      showSuccess('Issue Created', `Issue "${issueData.title}" has been created successfully.`);
    } catch (error) {
      console.error('Failed to create issue:', error);
      showError('Issue Creation Failed', 'Failed to create issue. Please try again.');
    }
  };

  const updateIssue = async (updatedIssue: Issue) => {
    try {
      console.log('=== UPDATE ISSUE API CALL ===');
      console.log('Issue ID:', updatedIssue.id);
      console.log('Parent ID:', updatedIssue.parentId);
      console.log('Story Points:', updatedIssue.storyPoints);
      console.log('Full Issue Data:', updatedIssue);
      
      await updateIssueMutation.mutateAsync({ 
        id: parseInt(updatedIssue.id), 
        data: updatedIssue as any 
      });
      showSuccess('Issue Updated', `Issue "${updatedIssue.title}" has been updated successfully.`);
    } catch (error) {
      console.error('Failed to update issue:', error);
      console.error('Error details:', error);
      showError('Issue Update Failed', 'Failed to update issue. Please try again.');
    }
  };

  const deleteIssue = async (issueId: string) => {
    try {
      const issue = issues.find(i => i.id === issueId);
      await deleteIssueMutation.mutateAsync(parseInt(issueId));
      showSuccess('Issue Deleted', `Issue "${issue?.title || 'Unknown'}" has been deleted successfully.`);
    } catch (error) {
      console.error('Failed to delete issue:', error);
      showError('Issue Deletion Failed', 'Failed to delete issue. Please try again.');
    }
  };

  const updateIssueStatus = async (id: string, status: IssueStatus) => {
    try {
      const issue = issues.find(i => i.id === id);
      await updateIssueStatusMutation.mutateAsync({ 
        id: parseInt(id), 
        status 
      });
      showSuccess('Status Updated', `Issue "${issue?.title || 'Unknown'}" status changed to ${status.toLowerCase().replace('_', ' ')}.`);
    } catch (error) {
      console.error('Failed to update issue status:', error);
      showError('Status Update Failed', 'Failed to update issue status. Please try again.');
    }
  };

  const createProject = async (data: Partial<Project>) => {
    try {
      await createProjectAPI(data as any);
      showSuccess('Project Created', `Project "${data.name}" has been created successfully.`);
    } catch (error) {
      console.error('Failed to create project:', error);
      showError('Project Creation Failed', 'Failed to create project. Please try again.');
    }
  };

  const deleteProject = async (projectId: string) => {
    try {
      const project = projects.find(p => p.id === projectId);
      await deleteProjectAPI(projectId);
      showSuccess('Project Deleted', `Project "${project?.name || 'Unknown'}" has been deleted successfully.`);
    } catch (error) {
      console.error('Failed to delete project:', error);
      showError('Project Deletion Failed', 'Failed to delete project. Please try again.');
    }
  };

  const createSprint = async (data: Partial<Sprint>) => {
    try {
      await createSprintMutation.mutateAsync(data as any);
      showSuccess('Sprint Created', `Sprint "${data.name}" has been created successfully.`);
    } catch (error: any) {
      console.error('Failed to create sprint:', error);
      const errorMessage = handleApiError(error);
      showError('Sprint Creation Failed', errorMessage);
    }
  };

  const updateSprint = async (updatedSprint: Sprint) => {
    try {
      await updateSprintMutation.mutateAsync({ 
        id: parseInt(updatedSprint.id), 
        data: updatedSprint as any 
      });
      showSuccess('Sprint Updated', `Sprint "${updatedSprint.name}" has been updated successfully.`);
    } catch (error: any) {
      console.error('Failed to update sprint:', error);
      const errorMessage = error.message || 'Failed to update sprint. Please try again.';
      showError('Sprint Update Failed', errorMessage);
    }
  };

  const deleteSprint = async (sprintId: string) => {
    try {
      await deleteSprintMutation.mutateAsync(parseInt(sprintId));
      showSuccess('Sprint Deleted', 'Sprint has been deleted successfully.');
    } catch (error: any) {
      console.error('Failed to delete sprint:', error);
      const errorMessage = error.message || 'Failed to delete sprint. Please try again.';
      showError('Sprint Deletion Failed', errorMessage);
    }
  };

  const startSprint = async (sprintId: string, newStartDate?: string, newEndDate?: string) => {
    try {
      console.log('🔍 DEBUG: startSprint called with:', { sprintId, newStartDate, newEndDate });
      
      // Use the dedicated start sprint mutation
      const params = { id: parseInt(sprintId) };
      if (newStartDate && newEndDate) {
        // Convert ISO strings to date-only format for backend
        const startDateOnly = newStartDate.split('T')[0];
        const endDateOnly = newEndDate.split('T')[0];
        Object.assign(params, { newStartDate: startDateOnly, newEndDate: endDateOnly });
        console.log('🔍 DEBUG: params with dates:', params);
      } else {
        console.log('🔍 DEBUG: params without dates:', params);
      }
      
      await startSprintMutation.mutateAsync(params);
      const sprintName = sprints.find(s => s.id === sprintId)?.name || 'Sprint';
      showSuccess('Sprint Started', `${sprintName} has been activated successfully.`);
    } catch (error: any) {
      console.error('Failed to start sprint:', error);
      const errorMessage = error.message || 'Failed to start sprint. Please try again.';
      showError('Sprint Activation Failed', errorMessage);
    }
  };

  const completeSprint = async (sprintId: string) => {
    try {
      // The backend handles moving incomplete issues to backlog automatically
      await completeSprintMutation.mutateAsync(parseInt(sprintId));
      
      // Refresh issues to reflect the changes
      await refetchIssues();
      
      const sprintName = sprints.find(s => s.id === sprintId)?.name || 'Sprint';
      showSuccess('Sprint Completed', `${sprintName} has been completed successfully. Incomplete issues moved to backlog.`);
      
    } catch (error: any) {
      console.error('Failed to complete sprint:', error);
      const errorMessage = error.message || 'Failed to complete sprint. Please try again.';
      showError('Sprint Completion Failed', errorMessage);
    }
  };

  const addComment = async (issueId: string, content: string) => {
    if (!user) return;

    try {
      // Use the backend comment service
      await createCommentMutation.mutateAsync({
        issueId: parseInt(issueId),
        data: { content }
      });
      
      // The mutation will handle cache invalidation and UI updates
    } catch (error) {
      console.error('Failed to add comment:', error);
    }
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
      addComment, completeSprint, startSprint, refetchIssues,
      showSuccess, showError, showWarning
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

// Modal size variants for consistent sizing across the app
const MODAL_SIZES = {
  sm: 'w-full max-w-md',           // ~448px - Small confirmations, simple forms
  md: 'w-full max-w-lg',           // ~512px - Standard forms, single-column content  
  lg: 'w-full max-w-xl',           // ~576px - Wider forms, more content
  xl: 'w-full max-w-2xl',          // ~672px - Large forms
  '2xl': 'w-full max-w-3xl',       // ~768px - Wide content, dual-column
  '3xl': 'w-full max-w-4xl',       // ~896px - Very wide content
  '4xl': 'w-full max-w-5xl',       // ~1024px - Extra wide content
  '5xl': 'w-full max-w-6xl',       // ~1152px - Maximum width
  '6xl': 'w-full max-w-7xl',       // ~1280px - Ultra wide
  responsive: 'w-full max-w-md sm:max-w-lg md:max-w-xl lg:max-w-2xl xl:max-w-3xl 2xl:max-w-4xl', // Much more aggressive responsive sizing
  form: 'w-full max-w-4xl',        // ~896px - Optimal for forms
  wide: 'w-full max-w-5xl',        // ~1024px - Wide modals
  ultrawide: 'w-full max-w-6xl'    // ~1152px - Ultra wide modals
} as const;

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

const GlassPasswordInput = ({ value, onChange, placeholder, required }: {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  required?: boolean;
}) => {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="relative">
      <input
        type={showPassword ? "text" : "password"}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
        className={`w-full px-4 py-3 pr-12 rounded-2xl outline-none
          bg-white/40 dark:bg-[#020617]/50
          border border-gray-200 dark:border-white/10
          focus:border-blue-500/50 focus:bg-white/60 dark:focus:bg-[#020617]/80
          placeholder-gray-500 dark:placeholder-gray-500
          text-gray-800 dark:text-white
          transition-all duration-200
          shadow-sm`}
      />
      <button
        type="button"
        onClick={() => setShowPassword(!showPassword)}
        className="absolute right-3 top-1/2 -translate-y-1/2 p-1 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors"
        title={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
      >
        {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
      </button>
    </div>
  );
};

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
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative">
        <GlassCard className={`${MODAL_SIZES.lg} p-6 bg-white/95 dark:bg-[#09090b]/95 border-red-200 dark:border-red-900/30 shadow-2xl animate-in zoom-in-95 duration-300`}>
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
    </div>
  );
};

// --- DELETE CONFIRMATION MODAL (ISSUES) ---
const DeleteConfirmationModal = ({ isOpen, onClose, issue, onDelete, onMoveToBacklog }: { isOpen: boolean, onClose: () => void, issue: Issue | null, onDelete: () => void, onMoveToBacklog: () => void }) => {
  if (!isOpen || !issue) return null;

  const isBacklog = issue.status === IssueStatus.BACKLOG;
  const isEpic = issue.type === 'EPIC';

  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative">
        <GlassCard className={`${MODAL_SIZES.lg} p-6 bg-white/95 dark:bg-[#09090b]/95 border-red-200 dark:border-red-900/30 shadow-2xl animate-in zoom-in-95 duration-300`}>
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
  const [selectingDateType, setSelectingDateType] = useState<'start' | 'end'>('end');

  // Handle date selection from calendar
  const handleDateSelect = (date: string, type: 'start' | 'end') => {
    if (type === 'end') {
      setEndDate(date);
    }
    // Start date is always today, so we don't need to handle it
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 overflow-y-auto">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative w-full max-w-4xl my-8">
        <GlassCard className="w-full p-6 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300">
          {/* Header */}
          <div className="flex flex-col items-center text-center mb-6">
            <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900/30 rounded-full flex items-center justify-center mb-4">
              <AlertTriangle className="w-8 h-8 text-yellow-600 dark:text-yellow-400" />
            </div>
            <h3 className="text-xl font-bold dark:text-white">Update Sprint Dates</h3>
            <p className="text-sm text-gray-500 mt-2">The planned start date does not match today. Please adjust the end date.</p>
          </div>

          {/* Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            {/* LEFT: Date Information */}
            <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-900/30 rounded-xl p-4 space-y-4 h-fit">
              <div>
                <label className="text-xs font-bold text-gray-500 uppercase block mb-1">New Start Date (Today)</label>
                <div className="font-mono text-gray-800 dark:text-white font-bold text-lg">{todayStr}</div>
              </div>
              <div>
                <label className="text-xs font-bold text-gray-500 uppercase block mb-1">Selected End Date</label>
                <div className="font-mono text-gray-800 dark:text-white font-bold text-lg">
                  {endDate ? new Date(endDate).toLocaleDateString() : 'Not selected'}
                </div>
                <p className="text-xs text-gray-400 mt-1">Select an end date from the calendar on the right.</p>
              </div>
              {endDate && (
                <div className="pt-2 border-t border-blue-200 dark:border-blue-800">
                  <label className="text-xs font-bold text-gray-500 uppercase block mb-1">Sprint Duration</label>
                  <div className="text-sm text-gray-600 dark:text-gray-300">
                    {Math.ceil((new Date(endDate).getTime() - new Date(todayStr).getTime()) / (1000 * 60 * 60 * 24))} days
                  </div>
                </div>
              )}
            </div>

            {/* RIGHT: CALENDAR */}
            <div className="flex flex-col">
              <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-4">Select End Date</label>
              <div className="flex-1 min-h-0">
                <SprintCalendar
                  selectedStartDate={todayStr}
                  selectedEndDate={endDate}
                  onDateSelect={handleDateSelect}
                  sprints={[]} // Empty array since we don't need to show other sprints in this context
                  selectingType={selectingDateType}
                  onSelectingTypeChange={setSelectingDateType}
                />
              </div>
            </div>
          </div>

          {/* Footer Buttons */}
          <div className="flex gap-3 pt-4 border-t border-gray-200 dark:border-white/10">
            <button 
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
            >
              Cancel
            </button>
            <button 
              onClick={() => onConfirm(endDate)}
              disabled={!endDate}
              className="flex-1 py-2.5 rounded-xl bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed text-white shadow-lg shadow-blue-500/20 transition-all font-medium text-sm"
            >
              Confirm & Start Sprint
            </button>
          </div>
        </GlassCard>
      </div>
    </div>
  );
};

// --- SPRINT COMPLETION CONFIRM MODAL ---
const SprintCompletionModal = ({ isOpen, onClose, onConfirm, warningDetails }: { isOpen: boolean, onClose: () => void, onConfirm: () => void, warningDetails: { unfinished: number, isEarly: boolean, endDate: string } }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative">
        <GlassCard className={`${MODAL_SIZES.xl} p-6 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300`}>
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

  const availableIssues = issues.filter(i => {
    // Exclude issues that are already DONE
    if (i.status === 'DONE') {
      console.log(`❌ Excluding ${i.id} (${i.title}): status is DONE`);
      return false;
    }
    
    // If issue is in BACKLOG status, it should be available regardless of sprintId
    if (i.status === IssueStatus.BACKLOG) {
      console.log(`✅ Including ${i.id} (${i.title}): status is BACKLOG (ignoring sprintId=${i.sprintId})`);
      return true;
    }
    
    // For non-backlog issues, exclude if they're in the current sprint
    if (i.sprintId && i.sprintId === currentSprintId) {
      console.log(`❌ Excluding ${i.id} (${i.title}): already in current sprint`);
      return false;
    }
    
    // Include all other issues
    console.log(`✅ Including ${i.id} (${i.title}): status=${i.status}, sprintId=${i.sprintId}`);
    return true;
  });
  
  console.log(`🔍 Total issues: ${issues.length}, Available issues: ${availableIssues.length}`);
  const projectList = projects;
  const projectIssues = availableIssues.filter(i => i.projectId === selectedProjectId);
  const epics = projectIssues.filter(i => i.type === 'EPIC');
  const orphanIssues = projectIssues.filter(i => i.type !== 'EPIC' && !i.parentId);
  
  console.log(`🔍 Project ${selectedProjectId}:`, {
    totalAvailable: availableIssues.length,
    projectIssues: projectIssues.length,
    epics: epics.length,
    orphanIssues: orphanIssues.length
  });
  
  if (selectedProjectId) {
    projectIssues.forEach(issue => {
      console.log(`🔍 Project issue ${issue.id}:`, {
        title: issue.title,
        type: issue.type,
        parentId: issue.parentId,
        status: issue.status,
        sprintId: issue.sprintId
      });
    });
  }

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
    // Don't close here - let the onAdd handler manage the modal state
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
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-2">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative w-full max-w-6xl">
        <GlassCard className="w-full h-[70vh] flex flex-col bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300">
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
  const { isCreateIssueModalOpen, setCreateIssueModalOpen, addIssue, projects, issues, createIssueInitialData, showError, showWarning } = useApp();
  
  const [newIssue, setNewIssue] = useState<Partial<Issue>>({
    title: '',
    type: 'TASK',
    priority: 'MEDIUM',
    status: IssueStatus.BACKLOG,
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
    if (!newIssue.title?.trim()) {
      showError('Validation Error', 'Issue title is required');
      return;
    }
    if (newIssue.type !== 'EPIC' && !newIssue.parentId) {
      showError('Validation Error', 'Parent Epic is required for this issue type');
      return;
    }
    if (isFormValid) {
      addIssue(newIssue);
      setCreateIssueModalOpen(false);
      setNewIssue({
        title: '',
        type: 'TASK',
        priority: 'MEDIUM',
        status: IssueStatus.BACKLOG,
        projectId: projects[0]?.id || '',
        parentId: '',
        description: ''
      });
    }
  };

  return (
    <div className="fixed inset-0 z-[50] flex items-center justify-center p-4">
      {/* Backdrop blur effect */}
      <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
      
      {/* Modal content */}
      <div className="relative">
        <GlassCard className="w-full max-w-5xl p-6 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300">
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
  const { selectedIssueId, issues, sprints, setSelectedIssueId, updateIssue, updateIssueStatus, deleteIssue, issueHistory, goBackIssue, navigateToIssue, showError } = useApp();
  const [formData, setFormData] = useState<Issue | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    if (selectedIssueId) {
      const issue = issues.find(i => i.id === selectedIssueId);
      if (issue) {
        // Ensure all form fields have valid values (no null values)
        setFormData({ 
          ...issue,
          title: issue.title || '',
          description: issue.description || '',
          parentId: issue.parentId || '',
          sprintId: issue.sprintId || '',
          storyPoints: issue.storyPoints || 0,
          comments: issue.comments || []
        });
      }
    } else {
      setFormData(null);
    }
  }, [selectedIssueId, issues]);

  if (!selectedIssueId || !formData) return null;

  const projectEpics = issues.filter(i => {
    const isEpic = i.type === 'EPIC';
    const sameProject = String(i.projectId) === String(formData.projectId);
    const notSelf = i.id !== formData.id;
    
    // Also include issues that have children (act like epics)
    const hasChildren = issues.some(child => child.parentId === i.id);
    const isActuallyEpic = isEpic || hasChildren;
    
    return isActuallyEpic && sameProject && notSelf;
  });
  
  const isParentRequired = formData.type !== 'EPIC';
  const parentEpic = formData.parentId ? issues.find(i => i.id === formData.parentId) : null;

  const handleSave = async () => {
    if (formData) {
      if (isParentRequired && !formData.parentId) {
        showError("Epic Required", "This type of issue must belong to an Epic.");
        return;
      }
      
      // Additional validation: ensure parent epic belongs to same project
      if (formData.parentId) {
        const selectedEpic = issues.find(i => i.id === formData.parentId);
        if (selectedEpic && String(selectedEpic.projectId) !== String(formData.projectId)) {
          showError("Invalid Epic", "The selected Epic belongs to a different project.");
          return;
        }
      }
      
      // Get original issue to compare changes
      const originalIssue = issues.find(i => i.id === formData.id);
      
      // Debug logging for Epic assignment
      console.log('=== SAVING ISSUE ===');
      console.log('Issue ID:', formData.id);
      console.log('Issue Key:', formData.key);
      console.log('Current Parent ID:', formData.parentId);
      console.log('Story Points:', formData.storyPoints);
      console.log('Original Status:', originalIssue?.status);
      console.log('New Status:', formData.status);
      console.log('Full Form Data:', formData);
      
      try {
        // Check if status changed
        const statusChanged = originalIssue && originalIssue.status !== formData.status;
        
        if (statusChanged) {
          // Update status first
          await updateIssueStatus(formData.id, formData.status);
        }
        
        // Update other fields (excluding status since it's handled separately)
        const { status, ...issueDataWithoutStatus } = formData;
        await updateIssue(issueDataWithoutStatus as Issue);
        
        setSelectedIssueId(null);
      } catch (error) {
        console.error('Failed to save issue:', error);
      }
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

  const handleMoveToBacklog = async () => {
    if (formData) {
      try {
        // Update status to BACKLOG and remove from sprint
        await updateIssueStatus(formData.id, IssueStatus.BACKLOG);
        
        // Update other fields (remove sprint assignment)
        const updatedIssue = { ...formData, sprintId: undefined };
        const { status, ...issueDataWithoutStatus } = updatedIssue;
        await updateIssue(issueDataWithoutStatus as Issue);
        
        setShowDeleteModal(false);
        setSelectedIssueId(null);
      } catch (error) {
        console.error('Failed to move issue to backlog:', error);
      }
    }
  };

  return (
    <div className="absolute inset-0 z-[100] flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm animate-in fade-in duration-200">
      <GlassCard className={`${MODAL_SIZES.ultrawide} h-[85vh] overflow-hidden flex flex-col bg-white/95 dark:bg-[#09090b]/95 shadow-2xl border-gray-200 dark:border-white/10`}>
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
                  <option value={IssueStatus.BACKLOG}>Backlog</option>
                  <option value={IssueStatus.SELECTED_FOR_DEVELOPMENT}>Selected</option>
                  <option value={IssueStatus.IN_PROGRESS}>In Progress</option>
                  <option value={IssueStatus.IN_REVIEW}>In Review</option>
                  <option value={IssueStatus.DONE}>Done</option>
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
                      {(!sprint.startDate || !sprint.endDate) ? ' (Dates not set)' : ''}
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
                <select 
                  className="bg-gray-50 dark:bg-black/20 border border-gray-200 dark:border-white/10 rounded px-2 py-0.5 text-center text-gray-900 dark:text-white text-xs font-medium"
                  value={formData.storyPoints || 0}
                  onChange={(e) => setFormData({...formData, storyPoints: parseInt(e.target.value) || 0})}
                >
                  <option value={0}>0</option>
                  <option value={1}>1</option>
                  <option value={2}>2</option>
                  <option value={3}>3</option>
                  <option value={5}>5</option>
                  <option value={8}>8</option>
                  <option value={13}>13</option>
                  <option value={21}>21</option>
                  <option value={34}>34</option>
                  <option value={55}>55</option>
                  <option value={89}>89</option>
                </select>
              </div>
            </div>
          </div>

          {/* Right Main Content */}
          <div className="flex-1 overflow-y-auto p-6 md:p-8 bg-transparent">
            <div className="max-w-3xl mx-auto space-y-6">
              <textarea 
                value={formData.title || ''} 
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
  const { theme, toggleTheme } = useApp();
  const { login, register, isLoading, error, clearError } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  
  // Recuperar el último email usado del localStorage
  const [formData, setFormData] = useState({
    name: '',
    email: localStorage.getItem('lastLoginEmail') || '',
    password: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    
    try {
      if (isRegister) {
        await register({
          name: formData.name,
          email: formData.email,
          password: formData.password
        });
      } else {
        // Guardar el email en localStorage antes del login
        localStorage.setItem('lastLoginEmail', formData.email);
        
        await login({
          email: formData.email,
          password: formData.password
        });
      }
    } catch (error) {
      // Error is handled by the auth context
      console.error('Authentication failed:', error);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
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
          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-600 dark:text-red-400 text-sm">
              {error}
            </div>
          )}
          
          {isRegister && (
            <GlassInput 
              placeholder="Full Name" 
              value={formData.name}
              onChange={(e: any) => handleInputChange('name', e.target.value)}
              required 
            />
          )}
          <GlassInput 
            type="email" 
            placeholder="email@example.com" 
            value={formData.email}
            onChange={(e: any) => handleInputChange('email', e.target.value)}
            required 
          />
          <GlassPasswordInput 
            placeholder="Password" 
            value={formData.password}
            onChange={(e: any) => handleInputChange('password', e.target.value)}
            required 
          />
          
          <GlassButton type="submit" variant="orange" className="w-full py-3 text-lg" disabled={isLoading}>
            {isLoading ? 'Processing...' : (isRegister ? 'Create Account' : 'Log In')}
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
  
  // Temporarily disable backend dashboard APIs to fix infinite re-renders
  // const { data: dashboardMetrics, isLoading: metricsLoading } = useDashboardMetrics();
  // const { data: recentIssuesData, isLoading: recentLoading } = useRecentIssues(5);
  // const { data: activeSprintData, isLoading: sprintLoading } = useActiveSprintSummary();
  // const { data: issueDistribution, isLoading: distributionLoading } = useIssueDistribution();

  // Use local data for now
  const dashboardMetrics = null;
  const recentIssuesData = null;
  const activeSprintData = null;
  const issueDistribution = null;
  const metricsLoading = false;
  const recentLoading = false;
  const sprintLoading = false;
  const distributionLoading = false;

  // Fallback to local data if backend is not available
  const activeSprint = activeSprintData?.sprint || sprints.find(s => s.status === 'ACTIVE');

  // FILTRADO GLOBAL - keep for search functionality
  const filteredIssues = issues.filter(i => 
    i.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
    i.key.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Use backend data when available, fallback to local calculations
  const myIssues = filteredIssues.filter(i => i.status !== 'DONE').slice(0, 5);
  const activeSprintIssues = filteredIssues.filter(i => i.sprintId === activeSprint?.id);

  // Use backend distribution data or calculate locally
  const priorityCounts = issueDistribution?.byPriority || {
    CRITICAL: activeSprintIssues.filter(i => i.priority === 'CRITICAL').length,
    HIGH: activeSprintIssues.filter(i => i.priority === 'HIGH').length,
    MEDIUM: activeSprintIssues.filter(i => i.priority === 'MEDIUM').length,
    LOW: activeSprintIssues.filter(i => i.priority === 'LOW').length,
  };

  const totalActiveIssues = Object.values(priorityCounts).reduce((sum, count) => sum + count, 0) || 1;

  // Use backend recent issues or fallback to local calculation
  const recentIssues = recentIssuesData || [...filteredIssues].sort((a, b) => {
    const timeA = new Date(a.updatedAt || 0).getTime();
    const timeB = new Date(b.updatedAt || 0).getTime();
    return timeB - timeA;
  }).slice(0, 5);

  // Use backend metrics or fallback to local calculations
  const stats = [
    { 
      label: 'Active Projects', 
      value: dashboardMetrics?.totalProjects || projects.length, 
      icon: FolderKanban, 
      color: 'text-blue-500', 
      bg: 'bg-blue-500/10',
      action: () => navigate('projects')
    },
    { 
      label: 'Current Sprint', 
      value: activeSprint ? `Day ${activeSprintData?.remainingDays ? Math.max(0, 14 - activeSprintData.remainingDays) : 4}/14` : 'Inactive', 
      icon: Timer, 
      color: 'text-green-500', 
      bg: 'bg-green-500/10',
      action: () => navigate('kanban')
    },
    { 
      label: 'My Tasks', 
      value: dashboardMetrics?.totalIssues || issues.filter(i => i.status !== 'DONE').length, 
      icon: CheckCircle2, 
      color: 'text-orange-500', 
      bg: 'bg-orange-500/10',
      action: () => navigate('kanban')
    },
  ];

  // Show loading state if critical data is loading
  if (metricsLoading && !dashboardMetrics) {
    return (
      <div className="max-w-7xl mx-auto space-y-6 animate-in fade-in duration-500">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <GlassCard key={i} className="p-6 flex items-center gap-4">
              <div className="p-4 rounded-xl bg-gray-200 dark:bg-gray-700 animate-pulse">
                <div className="w-8 h-8 bg-gray-300 dark:bg-gray-600 rounded" />
              </div>
              <div className="space-y-2">
                <div className="h-4 w-20 bg-gray-200 dark:bg-gray-700 rounded animate-pulse" />
                <div className="h-6 w-16 bg-gray-200 dark:bg-gray-700 rounded animate-pulse" />
              </div>
            </GlassCard>
          ))}
        </div>
      </div>
    );
  }

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
            {recentIssues.map(issue => {
              // Handle both backend Issue and frontend FrontendIssue types
              const issueId = typeof issue.id === 'string' ? issue.id : issue.id.toString();
              const issueKey = 'key' in issue ? issue.key : `ISS-${issue.id}`;
              
              return (
                <div 
                  key={issueId} 
                  onClick={(e) => { e.stopPropagation(); setSelectedIssueId(issueId); }}
                  className="group flex items-center justify-between p-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
                >
                  <div className="flex items-center gap-3 overflow-hidden">
                    <div className={`w-1.5 h-1.5 shrink-0 rounded-full ${
                      issue.priority === 'CRITICAL' ? 'bg-red-500' : 
                      issue.priority === 'HIGH' ? 'bg-orange-500' : 'bg-blue-500'
                    }`} />
                    <div className="truncate">
                      <p className="text-xs font-medium text-gray-800 dark:text-gray-200 truncate">{issue.title}</p>
                      <p className="text-[10px] text-gray-500">{issueKey} • {issue.status}</p>
                    </div>
                  </div>
                  <ChevronRight className="w-3 h-3 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
              );
            })}
            {recentIssues.length === 0 && <p className="text-gray-500 text-sm italic">No recent results.</p>}
          </div>
        </GlassCard>
      </div>
    </div>
  );
};
// --- PROJECTS VIEW (LIST & DETAIL) ---
const ProjectsList = () => {
  const { projects, issues, createProject, setSelectedIssueId, setCreateIssueModalOpen, setCreateIssueInitialData, searchQuery, deleteProject, showError, showWarning } = useApp();
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
      // Check if there are epics available for this project
      const projectEpics = issues.filter(i => i.type === 'EPIC' && i.projectId === selectedProjectId);
      if (projectEpics.length === 0) {
        showWarning('No Epics Available', 'You need to create an Epic first before creating standard issues. Please create an Epic or select "Epic" as the issue type.');
        setCreationWizardStep('NONE');
        return;
      }
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
      backlog: issuesList.filter(i => [IssueStatus.BACKLOG, IssueStatus.SELECTED_FOR_DEVELOPMENT].includes(i.status)),
      inProgress: issuesList.filter(i => [IssueStatus.IN_PROGRESS, IssueStatus.IN_REVIEW].includes(i.status)),
      completed: issuesList.filter(i => i.status === IssueStatus.DONE)
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
          <div className="fixed inset-0 z-[50] flex items-center justify-center p-4">
            {/* Backdrop blur effect */}
            <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
            
            {/* Modal content */}
            <div className="relative">
              <GlassCard className={`${MODAL_SIZES.xl} p-8 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300`}>
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
          </div>
        )}

        {creationWizardStep === 'PARENT' && (
          <div className="fixed inset-0 z-[50] flex items-center justify-center p-4">
            {/* Backdrop blur effect */}
            <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
            
            {/* Modal content */}
            <div className="relative">
              <GlassCard className={`${MODAL_SIZES.xl} p-6 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl max-h-[80vh] flex flex-col animate-in zoom-in-95 duration-300`}>
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
        <div className="fixed inset-0 z-[50] flex items-center justify-center p-4">
          {/* Backdrop blur effect */}
          <div className="absolute inset-0 backdrop-blur-md bg-white/10 dark:bg-black/10"></div>
          
          {/* Modal content */}
          <div className="relative">
            <GlassCard className="w-full max-w-4xl p-6 bg-white/95 dark:bg-[#09090b]/95 border-white/30 shadow-2xl animate-in zoom-in-95 duration-300">
            <h3 className="text-xl font-bold mb-4 dark:text-white">Create Project</h3>
            <div className="space-y-4">
              <GlassInput 
                placeholder="Project Name" 
                value={newProj.name} 
                onChange={(e:any) => setNewProj({...newProj, name: e.target.value})} 
              />
              <div>
                <GlassInput 
                  placeholder="Project Key (e.g. WEB, SHOP, BLOG)" 
                  value={newProj.key} 
                  onChange={(e:any) => {
                    const value = e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '');
                    setNewProj({...newProj, key: value});
                  }} 
                  maxLength={10}
                  className={`${
                    newProj.key && (newProj.key.length < 2 || !/^[A-Z][A-Z0-9_-]*$/.test(newProj.key))
                      ? 'border-red-300 focus:border-red-500' 
                      : newProj.key.length >= 2 && /^[A-Z][A-Z0-9_-]*$/.test(newProj.key)
                      ? 'border-green-300 focus:border-green-500'
                      : ''
                  }`}
                />
                <p className={`text-xs mt-1 px-1 ${
                  newProj.key && (newProj.key.length < 2 || !/^[A-Z][A-Z0-9_-]*$/.test(newProj.key))
                    ? 'text-red-500' 
                    : newProj.key.length >= 2 && /^[A-Z][A-Z0-9_-]*$/.test(newProj.key)
                    ? 'text-green-500'
                    : 'text-gray-500'
                }`}>
                  {newProj.key && (newProj.key.length < 2 || !/^[A-Z][A-Z0-9_-]*$/.test(newProj.key))
                    ? 'Invalid: Must be 2+ chars, start with letter, only A-Z 0-9 _ -'
                    : newProj.key.length >= 2 && /^[A-Z][A-Z0-9_-]*$/.test(newProj.key)
                    ? 'Valid project key ✓'
                    : '2-10 characters, must start with a letter, only A-Z, 0-9, _, -'
                  }
                </p>
              </div>
              <textarea 
                className="w-full px-4 py-3 rounded-xl outline-none bg-white/50 dark:bg-black/20 border border-gray-200 dark:border-white/10 text-gray-800 dark:text-white"
                placeholder="Description"
                rows={3}
                value={newProj.description} 
                onChange={(e:any) => setNewProj({...newProj, description: e.target.value})}
              />
              <div className="flex justify-end gap-2 mt-4">
                <GlassButton variant="ghost" onClick={() => { 
                  setShowModal(false); 
                  setNewProj({ name: '', key: '', description: '' }); // Reset form
                }}>Cancel</GlassButton>
                <GlassButton 
                  onClick={() => { 
                    // Validate before creating
                    if (!newProj.name.trim()) {
                      showError('Validation Error', 'Project name is required');
                      return;
                    }
                    if (!newProj.key.trim()) {
                      showError('Validation Error', 'Project key is required');
                      return;
                    }
                    if (newProj.key.length < 2) {
                      showError('Validation Error', 'Project key must be at least 2 characters long');
                      return;
                    }
                    if (!/^[A-Z][A-Z0-9_-]*$/.test(newProj.key)) {
                      showError('Validation Error', 'Project key must start with a letter and contain only uppercase letters, numbers, underscores, and hyphens');
                      return;
                    }
                    
                    createProject(newProj); 
                    setShowModal(false); 
                    setNewProj({ name: '', key: '', description: '' }); // Reset form
                  }}
                  disabled={!newProj.name.trim() || !newProj.key.trim() || newProj.key.length < 2 || !/^[A-Z][A-Z0-9_-]*$/.test(newProj.key)}
                >
                  Create
                </GlassButton>
              </div>
            </div>
          </GlassCard>
          </div>
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
// --- COMPLETED SPRINT VIEW MODAL ---
const CompletedSprintViewModal = ({ isOpen, onClose, sprint, onIssueClick, onDelete }: { 
  isOpen: boolean, 
  onClose: () => void, 
  sprint: Sprint, 
  onIssueClick: (id: string) => void,
  onDelete: (sprintId: string) => void
}) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const { data: sprintIssues = [], isLoading } = useCompletedSprintIssues(
    parseInt(sprint.id), 
    isOpen && sprint.status === 'COMPLETED'
  );

  const handleDeleteClick = () => {
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = () => {
    onDelete(sprint.id);
    setShowDeleteConfirm(false);
    onClose();
  };

  if (!isOpen) return null;

  // Solo mostrar issues que fueron completados (DONE) en este sprint
  const completedIssues = sprintIssues.filter(issue => 
    issue.status === 'DONE' && (issue.sprintId === parseInt(sprint.id) || issue.lastCompletedSprintId === parseInt(sprint.id))
  );

  return (
    <div className="fixed inset-0 z-[50] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in">
      <GlassCard className="w-full max-w-4xl max-h-[95vh] overflow-hidden flex flex-col bg-white/90 dark:bg-[#09090b]/90 shadow-2xl border-white/20 backdrop-blur-2xl">
        <div className="p-6 border-b border-gray-200 dark:border-white/10 flex justify-between items-center">
          <div>
            <h3 className="text-xl font-bold dark:text-white flex items-center gap-2">
              <CheckSquare className="w-6 h-6 text-green-500" />
              {sprint.name} - Completed Sprint
            </h3>
            <p className="text-sm text-gray-500 mt-1">
              {formatDate(sprint.startDate)} - {formatDate(sprint.endDate)}
            </p>
            {sprint.goal && (
              <p className="text-sm text-gray-600 dark:text-gray-300 italic mt-1">
                Goal: "{sprint.goal}"
              </p>
            )}
          </div>
          <div className="flex items-center gap-2">
            <GlassButton 
              variant="danger" 
              onClick={handleDeleteClick}
              className="text-sm"
            >
              <Trash2 className="w-4 h-4" /> Delete
            </GlassButton>
            <button onClick={onClose} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
              <X className="w-6 h-6" />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {isLoading ? (
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
            </div>
          ) : (
            <div className="max-w-4xl mx-auto">
              {/* Completed Issues */}
              <div>
                <h4 className="font-bold text-gray-800 dark:text-white flex items-center gap-2 mb-6">
                  <CheckCircle2 className="w-5 h-5 text-green-500" />
                  Completed Issues ({completedIssues.length})
                </h4>
                <div className="space-y-3 max-h-96 overflow-y-auto custom-scrollbar">
                  {completedIssues.map(issue => (
                    <div 
                      key={issue.id}
                      onClick={() => onIssueClick(issue.id.toString())}
                      className="bg-white/80 dark:bg-[#1e293b]/80 p-4 rounded-xl border border-green-200 dark:border-green-800/30 cursor-pointer hover:bg-green-50 dark:hover:bg-green-900/10 transition-colors group"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <span className="text-xs font-mono text-gray-500">{issue.id}</span>
                        <div className="flex items-center gap-2">
                          <CheckCircle2 className="w-4 h-4 text-green-500" />
                          <Badge color="green">DONE</Badge>
                        </div>
                      </div>
                      <p className="text-sm font-medium text-gray-800 dark:text-gray-200 mb-2">{issue.title}</p>
                      <div className="flex justify-between items-center">
                        <div className="flex gap-1">
                          <span className="text-[10px] bg-gray-200 dark:bg-gray-700 px-1.5 py-0.5 rounded text-gray-600 dark:text-gray-300">
                            {issue.status}
                          </span>
                          <span className={`text-[10px] px-1.5 py-0.5 rounded text-white ${
                            issue.priority === 'CRITICAL' ? 'bg-red-500' : 
                            issue.priority === 'HIGH' ? 'bg-orange-500' : 
                            issue.priority === 'MEDIUM' ? 'bg-yellow-500' : 'bg-blue-500'
                          }`}>
                            {issue.priority}
                          </span>
                        </div>
                        {issue.storyPoints && (
                          <div className="bg-gray-200 dark:bg-gray-700 rounded-full w-6 h-6 flex items-center justify-center text-[10px] font-bold">
                            {issue.storyPoints}
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                  {completedIssues.length === 0 && (
                    <div className="text-center text-gray-500 py-12">
                      <CheckCircle2 className="w-12 h-12 mx-auto mb-4 opacity-50" />
                      <p className="text-lg font-medium mb-2">No issues were completed in this sprint</p>
                      <p className="text-sm">This sprint was completed without any finished issues.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="p-6 border-t border-gray-200 dark:border-white/10 flex justify-between items-center">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Sprint completed with {completedIssues.length} finished issue{completedIssues.length !== 1 ? 's' : ''}
          </div>
          <GlassButton onClick={onClose}>Close</GlassButton>
        </div>

        {/* Delete Confirmation Modal */}
        {showDeleteConfirm && (
          <div className="absolute inset-0 z-10 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
            <GlassCard className="w-full max-w-sm p-6 bg-white/95 dark:bg-[#09090b]/95 border-red-200 dark:border-red-900/30 shadow-2xl">
              <div className="text-center mb-6">
                <div className="w-14 h-14 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Trash2 className="w-7 h-7 text-red-600 dark:text-red-400" />
                </div>
                <h3 className="text-lg font-bold text-gray-800 dark:text-white">Delete Completed Sprint?</h3>
                <p className="text-sm text-gray-500 mt-2">
                  This will permanently delete the sprint <span className="font-bold">"{sprint.name}"</span> and its history.
                </p>
                <p className="text-xs text-red-500 mt-2 font-semibold">
                  Issues will remain in their current locations (backlog/done).
                </p>
              </div>
              <div className="flex gap-3">
                <button 
                  onClick={() => setShowDeleteConfirm(false)}
                  className="flex-1 py-2.5 rounded-xl border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-white/5 transition-colors font-medium text-sm"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleConfirmDelete}
                  className="flex-1 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white font-medium text-sm transition-colors flex items-center justify-center gap-2"
                >
                  <Trash2 className="w-4 h-4" /> Delete
                </button>
              </div>
            </GlassCard>
          </div>
        )}
      </GlassCard>
    </div>
  );
};

// --- SPRINT MANAGEMENT LIST ---
const SprintsList = () => {
  const { sprints, createSprint, updateSprint, navigate, issues, setSelectedIssueId, updateIssue, startSprint, deleteSprint, refetchIssues, showWarning } = useApp();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingSprint, setEditingSprint] = useState<Sprint | null>(null);
  const [viewingCompletedSprint, setViewingCompletedSprint] = useState<Sprint | null>(null);
  const [newSprint, setNewSprint] = useState({ name: '', startDate: '', endDate: '', goal: '' });
  const [showBacklogPicker, setShowBacklogPicker] = useState(false);
  const [showActivationModal, setShowActivationModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [selectingDateType, setSelectingDateType] = useState<'start' | 'end'>('start');

  const active = sprints.filter(s => s.status === 'ACTIVE');
  const planned = sprints.filter(s => s.status === 'PLANNED');
  const completed = sprints.filter(s => s.status === 'COMPLETED');

  const handleSprintClick = (sprint: Sprint) => {
    if (sprint.status === 'ACTIVE') {
      navigate('kanban');
    } else if (sprint.status === 'COMPLETED') {
      setViewingCompletedSprint(sprint);
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
      status: IssueStatus.BACKLOG
    });
  };

  const handleAddIssuesFromBacklog = async (selectedIssues: Issue[]) => {
    console.log('🔍 handleAddIssuesFromBacklog called with:', selectedIssues);
    if (editingSprint) {
      try {
        console.log('🔍 editingSprint:', editingSprint);
        const issueIds = selectedIssues.map(issue => parseInt(issue.id));
        console.log('🔍 issueIds:', issueIds);
        console.log('🔍 Calling sprintService.addIssuesToSprint...');
        await sprintService.addIssuesToSprint(parseInt(editingSprint.id), issueIds);
        
        console.log('🔍 Successfully added issues, refreshing...');
        // Refresh issues to get updated data from backend
        await refetchIssues();
        
        // Close the backlog picker
        setShowBacklogPicker(false);
        
        // Show success message
        console.log(`✅ Successfully added ${selectedIssues.length} issues to sprint`);
      } catch (error) {
        console.error('❌ Failed to add issues to sprint:', error);
        // You could add a toast notification here
      }
    }
  };

  const handleActivateSprintClick = () => {
    if (!editingSprint) return;

    // Check if there's already an active sprint
    const activeSprint = sprints.find(sprint => sprint.status === 'ACTIVE');
    if (activeSprint) {
      showWarning('Sprint Already Active', `Cannot start a new sprint while "${activeSprint.name}" is still active. Please complete the current sprint first.`);
      return;
    }

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

    // Check if there's already an active sprint
    const activeSprint = sprints.find(sprint => sprint.status === 'ACTIVE');
    if (activeSprint) {
      showWarning('Sprint Already Active', `Cannot start a new sprint while "${activeSprint.name}" is still active. Please complete the current sprint first.`);
      setShowActivationModal(false);
      return;
    }

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

  const handleDateSelect = (date: string, type: 'start' | 'end') => {
    if (type === 'start') {
      setNewSprint({ ...newSprint, startDate: date });
      // Auto-switch to end date selection after selecting start date
      setSelectingDateType('end');
    } else {
      setNewSprint({ ...newSprint, endDate: date });
    }
  };

  const handleEditDateSelect = (date: string, type: 'start' | 'end') => {
    if (!editingSprint) return;
    
    if (type === 'start') {
      handleUpdateSprint({ startDate: date });
    } else {
      handleUpdateSprint({ endDate: date });
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
          <GlassCard className="w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col bg-white/80 dark:bg-[#09090b]/80 border-white/20 shadow-2xl">
            <div className="p-6 border-b border-gray-200 dark:border-white/10 flex justify-between items-center">
              <h3 className="text-xl font-bold dark:text-white">Plan New Sprint</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
                <X className="w-6 h-6" />
              </button>
            </div>
            
            <div className="flex-1 overflow-y-auto p-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* LEFT: FORM */}
                <div className="space-y-6">
                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Sprint Name</label>
                    <GlassInput 
                      placeholder="Enter sprint name" 
                      value={newSprint.name} 
                      onChange={(e:any) => setNewSprint({...newSprint, name: e.target.value})} 
                    />
                  </div>
                  
                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Sprint Goal</label>
                    <GlassInput 
                      placeholder="What do you want to achieve? (Optional)" 
                      value={newSprint.goal} 
                      onChange={(e:any) => setNewSprint({...newSprint, goal: e.target.value})} 
                    />
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Selected Dates</label>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="p-3 bg-white/30 dark:bg-white/10 rounded-xl border border-white/20">
                          <div className="text-xs text-gray-500 mb-1">Start Date</div>
                          <div className="font-medium text-gray-800 dark:text-white">
                            {newSprint.startDate ? new Date(newSprint.startDate).toLocaleDateString() : 'Not selected'}
                          </div>
                        </div>
                        <div className="p-3 bg-white/30 dark:bg-white/10 rounded-xl border border-white/20">
                          <div className="text-xs text-gray-500 mb-1">End Date</div>
                          <div className="font-medium text-gray-800 dark:text-white">
                            {newSprint.endDate ? new Date(newSprint.endDate).toLocaleDateString() : 'Not selected'}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* RIGHT: CALENDAR */}
                <div>
                  <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-4">Select Sprint Dates</label>
                  <SprintCalendar
                    selectedStartDate={newSprint.startDate}
                    selectedEndDate={newSprint.endDate}
                    onDateSelect={handleDateSelect}
                    sprints={sprints}
                    selectingType={selectingDateType}
                    onSelectingTypeChange={setSelectingDateType}
                  />
                </div>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 dark:border-white/10 flex justify-end gap-2">
              <GlassButton variant="ghost" onClick={() => setShowCreateModal(false)}>Cancel</GlassButton>
              <GlassButton 
                onClick={() => { 
                  createSprint(newSprint); 
                  setShowCreateModal(false); 
                  setNewSprint({ name: '', startDate: '', endDate: '', goal: '' });
                  setSelectingDateType('start');
                }}
                disabled={!newSprint.name || !newSprint.startDate || !newSprint.endDate}
              >
                Create Sprint
              </GlassButton>
            </div>
          </GlassCard>
        </div>
      )}

      {/* EDIT SPRINT MODAL */}
      {editingSprint && (
        <div className="absolute inset-0 z-[50] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in">
          <GlassCard className="w-full max-w-7xl max-h-[95vh] overflow-hidden flex flex-col bg-white/80 dark:bg-[#09090b]/80 shadow-2xl border-white/20 backdrop-blur-2xl">
            <div className="p-6 border-b border-gray-200 dark:border-white/10 flex justify-between items-center">
              <h3 className="text-xl font-bold dark:text-white">Sprint Details</h3>
              <button onClick={() => setEditingSprint(null)} className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-8">
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 min-h-[600px]">
                {/* LEFT: FORM */}
                <div className="lg:col-span-3 space-y-6">
                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Name</label>
                    <GlassInput 
                      value={editingSprint.name} 
                      onChange={(e:any) => handleUpdateSprint({ name: e.target.value })} 
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Status</label>
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

                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Selected Dates</label>
                    <div className="space-y-3">
                      <div className="p-4 bg-white/30 dark:bg-white/10 rounded-xl border border-white/20">
                        <div className="text-xs text-gray-500 mb-1">Start Date</div>
                        <div className="font-medium text-gray-800 dark:text-white">
                          {editingSprint.startDate 
                            ? new Date(editingSprint.startDate).toLocaleDateString()
                            : 'Not set'
                          }
                        </div>
                      </div>
                      <div className="p-4 bg-white/30 dark:bg-white/10 rounded-xl border border-white/20">
                        <div className="text-xs text-gray-500 mb-1">End Date</div>
                        <div className="font-medium text-gray-800 dark:text-white">
                          {editingSprint.endDate 
                            ? new Date(editingSprint.endDate).toLocaleDateString()
                            : 'Not set'
                          }
                        </div>
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">Goal</label>
                    <GlassTextArea 
                      rows={4} 
                      value={editingSprint.goal || ''} 
                      onChange={(e:any) => handleUpdateSprint({ goal: e.target.value })} 
                      placeholder="What do you want to achieve in this sprint?"
                    />
                  </div>
                </div>

                {/* CENTER: CALENDAR */}
                <div className="lg:col-span-4">
                  <label className="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-4">Update Sprint Dates</label>
                  <div className="h-full">
                    <SprintCalendar
                      selectedStartDate={editingSprint.startDate ? editingSprint.startDate.split('T')[0] : ''}
                      selectedEndDate={editingSprint.endDate ? editingSprint.endDate.split('T')[0] : ''}
                      onDateSelect={handleEditDateSelect}
                      sprints={sprints.filter(s => s.id !== editingSprint.id)} // Exclude current sprint from overlap check
                      selectingType={selectingDateType}
                      onSelectingTypeChange={setSelectingDateType}
                    />
                  </div>
                </div>

                {/* RIGHT: ISSUES LIST */}
                <div className="lg:col-span-5 flex flex-col h-full min-h-[500px]">
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
                {editingSprint.status === 'PLANNED' && (() => {
                  const activeSprint = sprints.find(sprint => sprint.status === 'ACTIVE');
                  const isDisabled = !!activeSprint;
                  
                  return (
                    <div className="relative group">
                      <button 
                        onClick={handleActivateSprintClick}
                        disabled={isDisabled}
                        className={`flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all active:scale-95 ${
                          isDisabled 
                            ? 'bg-gray-300 dark:bg-gray-600 text-gray-500 dark:text-gray-400 cursor-not-allowed' 
                            : 'bg-green-500 hover:bg-green-600 text-white shadow-lg shadow-green-500/20'
                        }`}
                      >
                        <Rocket className="w-4 h-4" /> Start Sprint
                      </button>
                      
                      {/* Tooltip for disabled state */}
                      {isDisabled && (
                        <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-900 dark:bg-gray-700 text-white text-xs rounded-lg shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-50">
                          Complete "{activeSprint.name}" first
                          <div className="absolute top-full left-1/2 transform -translate-x-1/2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-900 dark:border-t-gray-700"></div>
                        </div>
                      )}
                    </div>
                  );
                })()}
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

      {/* Completed Sprint View Modal */}
      {viewingCompletedSprint && (
        <CompletedSprintViewModal 
          isOpen={!!viewingCompletedSprint}
          onClose={() => setViewingCompletedSprint(null)}
          sprint={viewingCompletedSprint}
          onIssueClick={setSelectedIssueId}
          onDelete={deleteSprint}
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
    'SELECTED_FOR_DEVELOPMENT': 'border-blue-300 dark:border-blue-700',
    'IN_PROGRESS': 'border-yellow-300 dark:border-yellow-700',
    'IN_REVIEW': 'border-purple-300 dark:border-purple-700',
    'DONE': 'border-green-300 dark:border-green-700',
  };

  const statusTitles = {
    'BACKLOG': 'Backlog',
    'SELECTED_FOR_DEVELOPMENT': 'Selected',
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
  const { issues, sprints, updateIssueStatus, setSelectedIssueId, setCreateIssueModalOpen, setCreateIssueInitialData, completeSprint, updateIssue, searchQuery, navigate, refetchIssues } = useApp();
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
    setCreateIssueInitialData({ sprintId: activeSprint?.id, status: IssueStatus.SELECTED_FOR_DEVELOPMENT });
    setCreateIssueModalOpen(true);
    setShowAddMenu(false);
  };

  const handleAddFromBacklog = () => {
    setShowBacklogPicker(true);
    setShowAddMenu(false);
  };

  const handleBacklogSelect = async (selectedIssues: Issue[]) => {
    // Move issues to current sprint (Multi-Select)
    console.log('🔍 handleBacklogSelect called with:', selectedIssues);
    if (activeSprint) {
      try {
        console.log('🔍 activeSprint:', activeSprint);
        const issueIds = selectedIssues.map(issue => parseInt(issue.id));
        console.log('🔍 issueIds:', issueIds);
        console.log('🔍 Calling sprintService.addIssuesToSprint...');
        await sprintService.addIssuesToSprint(parseInt(activeSprint.id), issueIds);
        
        console.log('🔍 Successfully added issues, refreshing...');
        // Refresh issues to get updated data from backend
        await refetchIssues();
        
        // Close the backlog picker
        setShowBacklogPicker(false);
        
        // Show success message
        console.log(`✅ Successfully added ${selectedIssues.length} issues to sprint`);
      } catch (error) {
        console.error('❌ Failed to add issues to sprint:', error);
        // You could add a toast notification here
      }
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

  const columns: IssueStatus[] = [IssueStatus.SELECTED_FOR_DEVELOPMENT, IssueStatus.IN_PROGRESS, IssueStatus.IN_REVIEW, IssueStatus.DONE];

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
    <div className="flex h-screen w-full bg-[#f5f5f7] dark:bg-[#020617] text-gray-900 dark:text-gray-100 transition-colors duration-300">
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
  const { currentView } = useApp();
  const { user, isLoading } = useAuth();

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center bg-gray-50 dark:bg-[#020617]">
        <div className="text-center">
          <div className="w-16 h-16 bg-blue-500 rounded-2xl mx-auto flex items-center justify-center mb-4 shadow-lg shadow-blue-500/30 animate-pulse">
            <Zap className="text-white w-8 h-8" />
          </div>
          <p className="text-gray-500 dark:text-gray-400">Loading...</p>
        </div>
      </div>
    );
  }

  // Show auth view if not authenticated
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
  // Initialize Content Security Policy on app load (only in production)
  useEffect(() => {
    if (import.meta.env.MODE === 'production') {
      initializeCSP();
    }
  }, []);

  return (
    <AuthProvider>
      <ToastProvider>
        <AppProvider>
          <SimpleSessionWarning />
          <AppContent />
        </AppProvider>
      </ToastProvider>
    </AuthProvider>
  );
}